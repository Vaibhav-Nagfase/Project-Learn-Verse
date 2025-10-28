package com.example.learnverse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.jwt.JWT
import com.example.learnverse.data.model.CommunityPost
import com.example.learnverse.data.model.FollowStats
// Import user model if you have one for basic tutor info
// import com.example.learnverse.data.model.profile.UserProfile
import com.example.learnverse.data.repository.AuthRepository // To get token
import com.example.learnverse.data.repository.CommunityRepository
// Import user repository if needed
// import com.example.learnverse.data.repository.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Simple state for this screen
sealed class TutorProfileUiState {
    data object Loading : TutorProfileUiState()
    data class Success(
        // val tutorInfo: UserProfile?, // Add if fetching tutor details
        val followStats: FollowStats?,
        val posts: List<CommunityPost>,
        val isCurrentUserFollowing: Boolean
    ) : TutorProfileUiState()
    data class Error(val message: String) : TutorProfileUiState()
}


class TutorProfileViewModel(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TutorProfileUiState>(TutorProfileUiState.Loading)
    val uiState: StateFlow<TutorProfileUiState> = _uiState.asStateFlow()

    private var currentTutorId: String? = null

    // Pagination for tutor's posts
    private var currentPage = 0
    private var isLastPage = false
    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())

    fun loadTutorProfile(tutorId: String) {
        currentTutorId = tutorId // Store the ID
        viewModelScope.launch {
            _uiState.value = TutorProfileUiState.Loading

            try {
                // Reset pagination for new tutor
                currentPage = 0
                isLastPage = false
                _posts.value = emptyList()

                val statsResult = communityRepository.getFollowStats(tutorId)
                val postsResult = communityRepository.getUserPosts(tutorId, page = currentPage, size = 10)

                val followingList = communityRepository.getFollowingList() // Interceptor handles token
                val isFollowing = followingList.contains(tutorId)

                _posts.value = postsResult.content
                isLastPage = postsResult.last

                // Combine results into Success state
                _uiState.value = TutorProfileUiState.Success(
                    followStats = statsResult,
                    posts = _posts.value, // Use the flow's current value
                    isCurrentUserFollowing = isFollowing
                )

            } catch (e: Exception) {
                _uiState.value = TutorProfileUiState.Error("Failed to load tutor profile: ${e.message}")
            }
        }
    }

    fun loadMoreTutorPosts(tutorId: String) {
        if (isLastPage || _uiState.value !is TutorProfileUiState.Success) return // Only load more if currently successful and not last page

        viewModelScope.launch {

            try {
                currentPage++
                val postsResult = communityRepository.getUserPosts(tutorId, page = currentPage, size = 10)
                _posts.value = _posts.value + postsResult.content // Append
                isLastPage = postsResult.last

                // Update the Success state with the combined posts list
                val currentState = _uiState.value
                if (currentState is TutorProfileUiState.Success) {
                    _uiState.value = currentState.copy(posts = _posts.value)
                }


            } catch (e: Exception) {
                currentPage-- // Revert page on error
                println("Failed to load more tutor posts: ${e.message}")
                // Optionally update uiState to show error
            }
        }
    }

    // --- Action: Like/Unlike Post on this profile ---
    fun likeOrUnlikePost(postId: String) {
        val tutorId = currentTutorId ?: return // Ensure we know which profile we're on
        viewModelScope.launch {
            val token = authRepository.getToken().firstOrNull()
            if (token.isNullOrBlank()) { /* Handle error */ return@launch }

            // Store current state for potential revert
            val originalUiState = _uiState.value

            // --- Optional: Optimistic UI Update ---
            val currentUserId = token?.let { decodeUserId(it) }
            if (currentUserId == null) {
                println("Cannot like post: User ID unknown.")
                return@launch
            }
            _uiState.update { currentState ->
                if (currentState is TutorProfileUiState.Success && currentUserId != null) {
                    currentState.copy(posts = currentState.posts.map { post ->
                        if (post.id == postId) {
                            val liked = post.likedBy.contains(currentUserId)
                            post.copy(likedBy = if (liked) post.likedBy - currentUserId else post.likedBy + currentUserId)
                        } else post
                    })
                } else {
                    currentState // No change if not in Success state or no user ID
                }
            }
            // --- End Optimistic ---

            try {
                // Call repository directly (Interceptor handles token)
                communityRepository.likePost(postId)
                // --- Refresh posts AFTER success ---
                refreshTutorPostsInternal(tutorId) // Call internal refresh
            } catch (e: Exception) {
                println("Failed to like/unlike post $postId: ${e.message}")
                // --- Revert Optimistic on Error ---
                _uiState.value = originalUiState
                // Show error
                _uiState.value = TutorProfileUiState.Error("Like/Unlike failed: ${e.message}")
            }
        }
    }

    // --- Action: Follow Tutor shown on this profile ---
    fun followThisTutor() {
        val tutorId = currentTutorId ?: return
        viewModelScope.launch {
            val token = authRepository.getToken().firstOrNull()
            if (token.isNullOrBlank()) { /* Handle error */ return@launch }

            // Store current state for potential revert
            val originalUiState = _uiState.value

            // --- Optional: Optimistic UI Update for stats ---
            _uiState.update { currentState ->
                if (currentState is TutorProfileUiState.Success && currentState.followStats != null) {
                    currentState.copy(
                        followStats = currentState.followStats.copy(
                            followersCount = currentState.followStats.followersCount + 1 // Increment optimistically
                        ),
                        isCurrentUserFollowing = true
                    )
                } else currentState
            }
            // --- End Optimistic ---

            try {
                communityRepository.followUser(tutorId) // Interceptor handles token
                // --- Refresh stats AFTER success ---
                refreshFollowStatsInternal(tutorId) // Call internal refresh
            } catch (e: Exception) {
                println("Failed to follow tutor $tutorId: ${e.message}")
                // --- Revert Optimistic on Error ---
                _uiState.value = originalUiState
                // Show error
                _uiState.value = TutorProfileUiState.Error("Follow failed: ${e.message}")
            }
        }
    }

    // --- Action: Unfollow Tutor shown on this profile ---
    fun unfollowThisTutor() {
        val tutorId = currentTutorId ?: return
        viewModelScope.launch {
            val token = authRepository.getToken().firstOrNull()
            if (token.isNullOrBlank()) { /* Handle error */ return@launch }

            // Store current state for potential revert
            val originalUiState = _uiState.value

            // --- Optional: Optimistic UI Update for stats ---
            _uiState.update { currentState ->
                if (currentState is TutorProfileUiState.Success && currentState.followStats != null) {
                    currentState.copy(
                        followStats = currentState.followStats.copy(
                            // Decrement optimistically, ensuring not negative
                            followersCount = maxOf(0, currentState.followStats.followersCount - 1)
                        ),
                        isCurrentUserFollowing = false
                    )
                } else currentState
            }
            // --- End Optimistic ---

            try {
                communityRepository.unfollowUser(tutorId) // Interceptor handles token
                // --- Refresh stats AFTER success ---
                refreshFollowStatsInternal(tutorId) // Call internal refresh
            } catch (e: Exception) {
                println("Failed to unfollow tutor $tutorId: ${e.message}")
                // --- Revert Optimistic on Error ---
                _uiState.value = originalUiState
                // Show error
                _uiState.value = TutorProfileUiState.Error("Unfollow failed: ${e.message}")
            }
        }
    }


    // --- Internal refresh functions (called after actions succeed) ---
    private suspend fun refreshTutorPostsInternal(tutorId: String) {
        try {
            currentPage = 0
            isLastPage = false
            val postsResult = communityRepository.getUserPosts(tutorId, page = currentPage, size = 10) // Interceptor handles auth
            _posts.value = postsResult.content
            isLastPage = postsResult.last
            // Update the main UI state
            _uiState.update { currentState ->
                if (currentState is TutorProfileUiState.Success) {
                    currentState.copy(posts = _posts.value)
                } else currentState
            }
        } catch (e: Exception) {
            println("Internal refresh posts failed: ${e.message}")
            // Don't necessarily change main state to Error here, maybe just log
        }
    }

    private suspend fun refreshFollowStatsInternal(tutorId: String) {
        try {
            val statsResult = communityRepository.getFollowStats(tutorId) // Interceptor handles auth
            _uiState.update { currentState ->
                if (currentState is TutorProfileUiState.Success) {
                    currentState.copy(followStats = statsResult)
                } else currentState
            }
        } catch (e: Exception) {
            println("Internal refresh stats failed: ${e.message}")
        }
    }

    private fun decodeUserId(token: String): String? {
        return try {
            JWT(token).subject // 'sub' claim usually holds the user ID
        } catch (e: Exception) {
            println("Error decoding JWT subject: ${e.message}")
            null
        }
        // Alternatively, get ID from AuthViewModel if it exposes it
        // return authViewModel.currentUserId.value
    }

}

