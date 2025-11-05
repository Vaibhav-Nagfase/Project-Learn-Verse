package com.example.learnverse.utils

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.learnverse.utils.course.formatDuration
import com.example.learnverse.viewmodel.VideoPlayerUiState
import com.example.learnverse.viewmodel.VideoPlayerViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    activityId: String,
    videoId: String,
    navController: NavController,
    viewModel: VideoPlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val isCompleted by viewModel.isCompleted.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    viewModel.onPlaybackStateChanged(playing)
                }
            })
        }
    }

    // Load video on first composition
    LaunchedEffect(activityId, videoId) {
        viewModel.loadVideo(activityId, videoId)
    }

    // Track position
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            kotlinx.coroutines.delay(1000)
            viewModel.onPositionChanged(exoPlayer.currentPosition / 1000)
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is VideoPlayerUiState.Ready -> Text(state.video.title)
                        else -> Text("Loading...")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            when (val state = uiState) {
                is VideoPlayerUiState.Loading -> {
                    LoadingState()
                }
                is VideoPlayerUiState.Ready -> {
                    // Initialize player with video
                    LaunchedEffect(state.video.videoUrl) {
                        val mediaItem = MediaItem.fromUri(state.video.videoUrl)
                        exoPlayer.setMediaItem(mediaItem)
                        exoPlayer.prepare()

                        // Resume from saved position
                        if (state.video.watchedSeconds > 0) {
                            exoPlayer.seekTo(state.video.watchedSeconds * 1000)
                        }

                        exoPlayer.playWhenReady = true
                    }

                    // Video Player
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                player = exoPlayer
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                useController = true
                                controllerShowTimeoutMs = 3000
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controls and Info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Mark Complete Button
                        if (!isCompleted) {
                            Button(
                                onClick = { viewModel.markAsComplete() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mark as Complete")
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        null,
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Completed",
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Next Video
                        if (state.nextVideo != null) {
                            NextVideoCard(
                                nextVideo = state.nextVideo,
                                onClick = {
                                    navController.navigate("video_player/$activityId/${state.nextVideo.videoId}") {
                                        popUpTo("video_player/$activityId/$videoId") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
                is VideoPlayerUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadVideo(activityId, videoId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NextVideoCard(
    nextVideo: com.example.learnverse.data.model.VideoWithProgress,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Next Video",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    nextVideo.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatDuration(nextVideo.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play next",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )
            Text(
                "Failed to load video",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}