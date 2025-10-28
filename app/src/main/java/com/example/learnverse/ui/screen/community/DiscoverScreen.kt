package com.example.learnverse.ui.screen.community

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add // Icon for Create Post FAB
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.data.model.CommunityPost
import com.example.learnverse.ui.screen.home.BottomNavigationBar // Reuse your existing BottomNavBar
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.CommunityUiState
import com.example.learnverse.viewmodel.CommunityViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    navController: NavController,
    communityViewModel: CommunityViewModel,
    authViewModel: AuthViewModel // Needed to check user role for FAB
) {
    val feedPosts by communityViewModel.feedPosts.collectAsStateWithLifecycle()
    val feedUiState by communityViewModel.feedUiState.collectAsStateWithLifecycle()
    val isLoadingMore = communityViewModel.isLoadingMore
    val userRole by authViewModel.currentUserRole.collectAsStateWithLifecycle() // Get user role
    val followingIds by communityViewModel.followingIds.collectAsStateWithLifecycle()
    val currentUserId by authViewModel.currentUserId.collectAsStateWithLifecycle() // Get the actual user ID

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // State for delete confirmation
    var postToDelete by remember { mutableStateOf<CommunityPost?>(null) }

    // Fetch data when the screen is first composed (i.e., when user is authenticated)
    LaunchedEffect(Unit) {
        communityViewModel.fetchInitialFeed()
        communityViewModel.fetchFollowingList() // Fetch following list here too
    }

    // Effect to show error messages
    LaunchedEffect(feedUiState) {
        if (feedUiState is CommunityUiState.Error) {
            snackbarHostState.showSnackbar((feedUiState as CommunityUiState.Error).message)
            // Optionally reset state: communityViewModel.resetFeedUiState()
        }
    }

    // Effect for infinite scrolling
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: -1
                val totalItemCount = listState.layoutInfo.totalItemsCount
                // Trigger load more when the second-to-last item is visible
                if (lastVisibleItemIndex >= totalItemCount - 2 && totalItemCount > 0) {
                    communityViewModel.loadMoreFeedPosts()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Discover") })
        },
        bottomBar = {
            // Use the same BottomNavigationBar as HomeScreen
            BottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            // Show FAB only for Tutors
            if (userRole == "TUTOR") {
                FloatingActionButton(onClick = { navController.navigate("createPost") }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Post")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Handle initial loading state
            if (feedUiState == CommunityUiState.Loading && feedPosts.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (feedPosts.isEmpty() && feedUiState !is CommunityUiState.Loading) {
                Text(
                    "No posts available yet.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(feedPosts, key = { it.id }) { post ->
                        CommunityPostCard(
                            post = post,
                            // Pass the actual currentUserId
                            currentUserId = currentUserId,
                            // Calculate isLiked based on the post's likedBy list and current user ID
                            isLiked = currentUserId != null && post.likedBy.contains(currentUserId),
                            // Calculate isFollowed based on the collected followingIds set
                            isFollowed = followingIds.contains(post.authorId),
                            // Keep the rest of the parameters as they were
                            onLikeClick = { communityViewModel.likePost(post.id) },
                            onCommentClick = { navController.navigate("postDetail/${post.id}") },
                            onFollowClick = { communityViewModel.followTutor(post.authorId) },
                            onUnfollowClick = { communityViewModel.unfollowTutor(post.authorId) },
                            onAuthorClick = { navController.navigate("tutorProfile/${post.authorId}") },
                            onPostClick = { /* TODO */ },
                            onEditClick = { navController.navigate("createPost?postId=${post.id}") },
                            onDeleteClick = { postToDelete = post }
                        )
                    }

                    // Show loading indicator at the bottom when loading more
                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

            }

            // --- Delete Confirmation Dialog ---
            if (postToDelete != null) {
                DeleteConfirmationDialog( // Reuse dialog from TutorDashboardScreen?
                    postName = postToDelete!!.content?.take(30) ?: "this post", // Show snippet
                    onConfirm = {
                        communityViewModel.deletePost(postToDelete!!.id) // Use specific delete function?
                        postToDelete = null
                    },
                    onDismiss = {
                        postToDelete = null
                    }
                )
            }

        }
    }
}