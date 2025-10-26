package com.example.learnverse.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Import JWT library if you decode here
import com.auth0.android.jwt.JWT
import com.example.learnverse.data.model.CommunityPost
import com.example.learnverse.data.model.FollowStats
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.CommunityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

// Define a sealed class for UI states, similar to your other ViewModels
sealed class CommunityUiState {
    data object Idle : CommunityUiState()
    data object Loading : CommunityUiState()
    data class Success(val message: String? = null) : CommunityUiState()
    data class Error(val message: String) : CommunityUiState()
}

class CommunityViewModel(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository // Needed to get current user ID for optimistic updates
) : ViewModel() {

    // --- State for the Discover Feed ---
    private val _feedPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val feedPosts: StateFlow<List<CommunityPost>> = _feedPosts.asStateFlow()

    private val _feedUiState = MutableStateFlow<CommunityUiState>(CommunityUiState.Idle)
    val feedUiState: StateFlow<CommunityUiState> = _feedUiState.asStateFlow()

    // Pagination state
    private var currentPage = 0
    private var isLastPage = false
    var isLoadingMore by mutableStateOf(false)

    // --- State for Post Creation/Editing ---
    var postContent by mutableStateOf("")
    var postMediaUri by mutableStateOf<Uri?>(null)
    var postMediaType by mutableStateOf<String?>(null)

    private val _postCreationUiState = MutableStateFlow<CommunityUiState>(CommunityUiState.Idle)
    val postCreationUiState: StateFlow<CommunityUiState> = _postCreationUiState.asStateFlow()

    // --- State for Following ---
    private val _followingIds = MutableStateFlow<Set<String>>(emptySet())
    val followingIds: StateFlow<Set<String>> = _followingIds.asStateFlow()

    // --- State for Individual Post/Tutor Profile ---
    private val _selectedPost = MutableStateFlow<CommunityPost?>(null)
    val selectedPost: StateFlow<CommunityPost?> = _selectedPost.asStateFlow()

    private val _selectedTutorPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val selectedTutorPosts: StateFlow<List<CommunityPost>> = _selectedTutorPosts.asStateFlow()

    private val _selectedTutorFollowStats = MutableStateFlow<FollowStats?>(null)
    val selectedTutorFollowStats: StateFlow<FollowStats?> = _selectedTutorFollowStats.asStateFlow()

    // --- REMOVED init block ---

    // --- Feed Functions ---
    // Called from DiscoverScreen LaunchedEffect
    fun fetchInitialFeed() {
        if (_feedUiState.value == CommunityUiState.Loading) return
        viewModelScope.launch {
            _feedUiState.value = CommunityUiState.Loading
            try {
                currentPage = 0
                // No token needed here - Interceptor handles it
                val response = communityRepository.getFeedPosts(page = currentPage, size = 10)
                _feedPosts.value = response.content
                isLastPage = response.last
                _feedUiState.value = CommunityUiState.Success()
            } catch (e: Exception) {
                _feedUiState.value = CommunityUiState.Error("Failed to load feed: ${e.message}")
            }
        }
    }

    fun loadMoreFeedPosts() {
        if (isLoadingMore || isLastPage || _feedUiState.value is CommunityUiState.Loading) return
        viewModelScope.launch {
            isLoadingMore = true
            try {
                currentPage++
                // No token needed here - Interceptor handles it
                val response = communityRepository.getFeedPosts(page = currentPage, size = 10)
                _feedPosts.value = _feedPosts.value + response.content
                isLastPage = response.last
            } catch (e: Exception) {
                currentPage--
                println("Failed to load more posts: ${e.message}")
            } finally {
                isLoadingMore = false
            }
        }
    }

    // --- Post Interaction Functions ---
    fun likePost(postId: String) {
        viewModelScope.launch {
            // --- Still need token HERE for User ID decoding ---
            val token = authRepository.getToken().firstOrNull()
            val currentUserId = token?.let { decodeUserId(it) }
            if (currentUserId == null) {
                println("Cannot like post: User ID unknown.")
                return@launch
            }
            // --- End User ID Fetch ---

            // Find the current post first to check its state
            val currentPost = _feedPosts.value.find { it.id == postId } ?: return@launch
            val currentlyLiked = currentPost.likedBy.contains(currentUserId)

            // Perform optimistic update (like OR unlike)
            _feedPosts.update { posts ->
                posts.map { post ->
                    if (post.id == postId) {
                        post.copy(likedBy = if (currentlyLiked) post.likedBy - currentUserId else post.likedBy + currentUserId)
                    } else post
                }
            }

            try {
                // No token needed here - Interceptor handles it
                val updatedPostFromServer = communityRepository.likePost(postId)
                // Update UI with confirmed state from server
                _feedPosts.update { posts ->
                    posts.map { post ->
                        if (post.id == postId) updatedPostFromServer else post
                    }
                }
            } catch (e: Exception) {
                println("Failed to like/unlike post $postId: ${e.message}")
                // Revert Optimistic Update on Error
                _feedPosts.update { posts ->
                    posts.map { post -> if (post.id == postId) currentPost else post }
                }
                _feedUiState.value = CommunityUiState.Error("Like/Unlike failed: ${e.message}") // Show error
            }
        }
    }

    // --- Add or Verify Function to Load/Select Post ---
    fun findPostById(postId: String) {
        // Finds the post in the current feed list and updates _selectedPost
        val postFromFeed = _feedPosts.value.find { it.id == postId }
        _selectedPost.value = postFromFeed
        // If postFromFeed is null, you might want to fetch it specifically here
        // as discussed previously (Option 2), but let's stick with Option 1 for now.
    }

    // --- Verify addComment Function ---
    fun addComment(postId: String, commentText: String) {
        if (commentText.isBlank()) return
        viewModelScope.launch {
            _feedUiState.value = CommunityUiState.Loading // Use appropriate state

            val token = authRepository.getToken().firstOrNull()
            if (token.isNullOrBlank()) {
                _feedUiState.value = CommunityUiState.Error("Authentication token is missing.")
                return@launch
            }

            try {
                // Interceptor handles token
                val updatedPost = communityRepository.addComment(postId, commentText)
                // Update the post in the main feed list
                _feedPosts.update { posts ->
                    posts.map { if (it.id == postId) updatedPost else it }
                }
                // Update the selected post state if it's the one being viewed
                if (_selectedPost.value?.id == postId) {
                    _selectedPost.value = updatedPost
                }
                _feedUiState.value = CommunityUiState.Success("Comment added")
            } catch (e: Exception) {
                _feedUiState.value = CommunityUiState.Error("Failed to add comment: ${e.message}")
            }
        }
    }

    // --- Post Creation/Management (Tutor Only) ---
    fun createPost(content: String?, file: File?, mediaType: String?) {
        viewModelScope.launch {
            _postCreationUiState.value = CommunityUiState.Loading
            try {
                val finalMediaType = if (file != null) mediaType ?: "application/octet-stream" else "none"
                // No token needed here - Interceptor handles it
                communityRepository.createPost(content, file, finalMediaType)
                _postCreationUiState.value = CommunityUiState.Success("Post created!")
                resetPostCreationState()
                fetchInitialFeed() // Refresh feed
            } catch (e: Exception) {
                _postCreationUiState.value = CommunityUiState.Error("Failed to create post: ${e.message}")
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                // No token needed here - Interceptor handles it
                communityRepository.deletePost(postId)
                _feedPosts.update { posts -> posts.filterNot { it.id == postId } }
                // Show success message if needed
            } catch (e: Exception) {
                println("Failed to delete post $postId: ${e.message}")
                // Show error message if needed
            }
        }
    }

    // --- Following Functions ---
    fun followTutor(tutorId: String) {
        viewModelScope.launch {
            // --- Store original state ---
            val originalFollowingIds = _followingIds.value

            // --- Optimistic update ---
            _followingIds.update { it + tutorId }

            try {
                communityRepository.followUser(tutorId)
                // Success - optimistic update was correct
            } catch (e: Exception) {
                println("Failed to follow tutor $tutorId: ${e.message}")
                // --- Revert optimistic update on ANY error ---
                _followingIds.value = originalFollowingIds
                // Optionally show error message via UI state
                _feedUiState.value = CommunityUiState.Error("Failed to follow: ${e.message}")
            }
        }
    }

    fun unfollowTutor(tutorId: String) {
        viewModelScope.launch {
            // --- Store original state ---
            val originalFollowingIds = _followingIds.value

            // --- Optimistic update ---
            _followingIds.update { it - tutorId }

            try {
                communityRepository.unfollowUser(tutorId)
                // Success - optimistic update was correct
            } catch (e: Exception) {
                println("Failed to unfollow tutor $tutorId: ${e.message}")
                // --- Revert optimistic update on ANY error ---
                _followingIds.value = originalFollowingIds
                // Optionally show error message via UI state
                _feedUiState.value = CommunityUiState.Error("Failed to unfollow: ${e.message}")
            }
        }
    }

    // Called from DiscoverScreen LaunchedEffect
    fun fetchFollowingList() {
        viewModelScope.launch {
            try {
                // No token needed here - Interceptor handles it
                val following = communityRepository.getFollowingList()
                _followingIds.value = following.toSet()
            } catch (e: Exception) {
                // Handle 401 specifically? Or just log?
                println("Failed to fetch following list: ${e.message}")
            }
        }
    }

    // --- Helper Functions ---
    fun resetPostCreationState() {
        postContent = ""
        postMediaUri = null
        postMediaType = null
        _postCreationUiState.value = CommunityUiState.Idle
    }

    // --- Still need this helper to get User ID for UI logic ---
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

    // ... (Add other functions as needed) ...
}