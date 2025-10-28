package com.example.learnverse.ui.screen.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Import viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.data.model.CommunityPost
import com.example.learnverse.data.model.FollowStats
// Import CommunityPostCard and other needed composables
import com.example.learnverse.ui.screen.community.CommunityPostCard
// Import ViewModels and Factory
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.CommunityViewModel
import com.example.learnverse.viewmodel.TutorProfileUiState
import com.example.learnverse.viewmodel.TutorProfileViewModel
import com.example.learnverse.viewmodel.TutorProfileViewModelFactory
// Import repository instances (or get them via dependency injection)
import com.example.learnverse.data.remote.ApiClient
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.CommunityRepository


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorProfileScreen(
    tutorId: String, // Passed via navigation
    navController: NavController,
    authViewModel: AuthViewModel // To get current user ID
) {
    // --- Instantiate TutorProfileViewModel ---
    // This assumes you have access to repositories here.
    // In a real app, use Hilt or another DI framework.
    val context = LocalContext.current.applicationContext
    val authRepository = remember { AuthRepository(ApiClient.getInstance(context).retrofit.create(com.example.learnverse.data.remote.ApiService::class.java), context) }
    val communityRepository = remember { CommunityRepository(ApiClient.getInstance(context).retrofit.create(com.example.learnverse.data.remote.ApiService::class.java)) }

    val tutorProfileViewModel: TutorProfileViewModel = viewModel(
        factory = TutorProfileViewModelFactory(communityRepository, authRepository)
    )
    // --- End ViewModel Instantiation ---

    val uiState by tutorProfileViewModel.uiState.collectAsStateWithLifecycle()
    val currentUserId by authViewModel.currentUserId.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // State for delete confirmation
    var postToDelete by remember { mutableStateOf<CommunityPost?>(null) }

    // Fetch profile data when the screen is shown or tutorId changes
    LaunchedEffect(tutorId) {
        tutorProfileViewModel.loadTutorProfile(tutorId)
    }

    // Effect for infinite scrolling tutor posts
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: -1
                val totalItemCount = listState.layoutInfo.totalItemsCount
                if (lastVisibleItemIndex >= totalItemCount - 2 && totalItemCount > 0 && uiState is TutorProfileUiState.Success) {
                    // Check if not already loading more? Add flag if needed
                    tutorProfileViewModel.loadMoreTutorPosts(tutorId)
                }
            }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutor Profile") }, // Placeholder title
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is TutorProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TutorProfileUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is TutorProfileUiState.Success -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- Tutor Info Header ---
                        item {
                            TutorInfoHeader(
                                tutorName = "Tutor Name",
                                tutorId = tutorId,
                                currentUserId = currentUserId,
                                followStats = state.followStats,
                                isFollowing = state.isCurrentUserFollowing,
                                onFollowClick = { tutorProfileViewModel.followThisTutor() },
                                onUnfollowClick = { tutorProfileViewModel.unfollowThisTutor() }
                            )
                        }

                        // --- Tutor's Posts ---
                        if (state.posts.isNotEmpty()) {
                            item {
                                Text(
                                    "Posts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                            items(state.posts, key = { it.id }) { post ->
                                val isLiked = currentUserId != null && post.likedBy.contains(currentUserId)
                                // Note: isFollowed is not relevant for the card here, we use the header button
                                CommunityPostCard(
                                    post = post,
                                    currentUserId = currentUserId,
                                    isLiked = isLiked,
                                    isFollowed = state.isCurrentUserFollowing,
                                    onLikeClick = {
                                        tutorProfileViewModel.likeOrUnlikePost(post.id)
                                    },
                                    onCommentClick = { navController.navigate("postDetail/${post.id}") },
                                    onFollowClick = { /* Handled by header button */ },
                                    onUnfollowClick = { /* Handled by header button */ },
                                    onAuthorClick = { /* Already on author's profile */ },
                                    onPostClick = { /* TODO */ },
                                    onEditClick = { navController.navigate("createPost?postId=${post.id}") },
                                    onDeleteClick = { postToDelete = post }
                                )
                            }
                        } else {
                            item {
                                Text("This tutor hasn't posted anything yet.")
                            }
                        }
                    }
                }
            }

        }
    }
}


@Composable
fun TutorInfoHeader(
    tutorName: String, // TODO: Pass actual tutor data
    tutorId: String,
    currentUserId: String?,
    followStats: FollowStats?,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        // Placeholder for Profile Picture
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(tutorName, style = MaterialTheme.typography.headlineSmall)
        // TODO: Add tutor bio/details if available

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("${followStats?.followersCount ?: 0} Followers")
            Text("${followStats?.followingCount ?: 0} Following")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show Follow/Unfollow button only if not viewing own profile
        if (currentUserId != null && tutorId != currentUserId) {
            Button(onClick = if (isFollowing) onUnfollowClick else onFollowClick) {
                Text(if (isFollowing) "Unfollow" else "Follow")
            }
        }
        Divider(modifier = Modifier.padding(top = 16.dp))
    }
}