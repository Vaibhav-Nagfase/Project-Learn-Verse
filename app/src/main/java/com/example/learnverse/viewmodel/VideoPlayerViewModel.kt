package com.example.learnverse.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.UpdateVideoProgressRequest
import com.example.learnverse.data.model.VideoWithProgress
import com.example.learnverse.data.repository.StudentCourseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class VideoPlayerUiState {
    object Loading : VideoPlayerUiState()
    data class Ready(
        val video: VideoWithProgress,
        val nextVideo: VideoWithProgress?
    ) : VideoPlayerUiState()
    data class Error(val message: String) : VideoPlayerUiState()
}

class VideoPlayerViewModel(
    private val repository: StudentCourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VideoPlayerUiState>(VideoPlayerUiState.Loading)
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private var progressSyncJob: Job? = null
    private var activityId: String? = null
    private var currentVideo: VideoWithProgress? = null

    companion object {
        private const val PROGRESS_SYNC_INTERVAL = 5000L // 5 seconds
        private const val AUTO_COMPLETE_THRESHOLD = 0.9f // 90%
    }

    fun loadVideo(activityId: String, videoId: String) {
        this.activityId = activityId

        viewModelScope.launch {
            try {
                _uiState.value = VideoPlayerUiState.Loading

                // Fetch course data to get video details
                val courseView = repository.getCourseView(activityId)
                val video = courseView.videos.find { it.videoId == videoId }

                if (video == null) {
                    _uiState.value = VideoPlayerUiState.Error("Video not found")
                    return@launch
                }

                currentVideo = video

                // Find next video
                val currentIndex = courseView.videos.indexOf(video)
                val nextVideo = if (currentIndex < courseView.videos.size - 1) {
                    courseView.videos[currentIndex + 1]
                } else null

                _uiState.value = VideoPlayerUiState.Ready(video, nextVideo)
                _currentPosition.value = video.watchedSeconds
                _isCompleted.value = video.completed

                // Start progress sync
                startProgressSync()

            } catch (e: Exception) {
                Log.e("VideoPlayer", "Failed to load video", e)
                _uiState.value = VideoPlayerUiState.Error(e.message ?: "Failed to load video")
            }
        }
    }

    fun onPlaybackStateChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying

        if (isPlaying) {
            startProgressSync()
        } else {
            stopProgressSync()
            syncProgressNow() // Sync immediately when paused
        }
    }

    fun onPositionChanged(position: Long) {
        _currentPosition.value = position

        // Auto-complete check
        val video = currentVideo ?: return
        val progress = position.toFloat() / video.duration.toFloat()

        if (progress >= AUTO_COMPLETE_THRESHOLD && !_isCompleted.value) {
            markAsComplete()
        }
    }

    fun markAsComplete() {
        val video = currentVideo ?: return
        val actId = activityId ?: return

        viewModelScope.launch {
            try {
                val success = repository.markVideoComplete(
                    activityId = actId,
                    videoId = video.videoId,
                    videoTitle = video.title,
                    totalSeconds = video.duration.toLong()
                )

                if (success) {
                    _isCompleted.value = true
                    Log.d("VideoPlayer", "Video marked as complete")
                }
            } catch (e: Exception) {
                Log.e("VideoPlayer", "Failed to mark complete", e)
            }
        }
    }

    private fun startProgressSync() {
        if (progressSyncJob?.isActive == true) return

        progressSyncJob = viewModelScope.launch {
            while (true) {
                delay(PROGRESS_SYNC_INTERVAL)
                if (_isPlaying.value) {
                    syncProgressNow()
                }
            }
        }
    }

    private fun stopProgressSync() {
        progressSyncJob?.cancel()
        progressSyncJob = null
    }

    private fun syncProgressNow() {
        val video = currentVideo ?: return
        val actId = activityId ?: return

        viewModelScope.launch {
            try {
                val request = UpdateVideoProgressRequest(
                    videoId = video.videoId,
                    videoTitle = video.title,
                    watchedSeconds = _currentPosition.value,
                    totalSeconds = video.duration.toLong(),
                    completed = _isCompleted.value
                )

                repository.updateVideoProgress(actId, request)
                Log.d("VideoPlayer", "Progress synced: ${_currentPosition.value}s")

            } catch (e: Exception) {
                Log.e("VideoPlayer", "Failed to sync progress", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressSync()
        syncProgressNow() // Final sync before exit
    }
}

class VideoPlayerViewModelFactory(
    private val repository: StudentCourseRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoPlayerViewModel::class.java)) {
            return VideoPlayerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
