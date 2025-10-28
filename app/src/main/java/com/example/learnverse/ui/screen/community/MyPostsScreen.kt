package com.example.learnverse.ui.screen.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.data.model.CommunityPost // Import CommunityPost
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.CommunityUiState
import com.example.learnverse.viewmodel.CommunityViewModel

@Composable
fun MyPostsScreen(
    navController: NavController,
    communityViewModel: CommunityViewModel,
    authViewModel: AuthViewModel
) {
    // We need the current user's ID to fetch their posts
    val currentUserId by authViewModel.currentUserId.collectAsStateWithLifecycle()

    // State for this screen's posts and loading/error
    // We can reuse parts of CommunityViewModel or add specific state/functions
    // Let's add a new StateFlow in CommunityViewModel for this
    val myPosts by communityViewModel.myPosts.collectAsStateWithLifecycle()
    val myPostsUiState by communityViewModel.myPostsUiState.collectAsStateWithLifecycle()
    val isLoadingMoreMyPosts = communityViewModel.isLoadingMoreMyPosts // Add this flag to ViewModel

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // State for delete confirmation
    var postToDelete by remember { mutableStateOf<CommunityPost?>(null) }

    // Fetch posts when the screen is composed or the user ID becomes available
    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrBlank()) {
            communityViewModel.fetchMyPosts(currentUserId!!) // Need this function in ViewModel
        }
    }

    // Effect for infinite scrolling 'My Posts'
    LaunchedEffect(listState, currentUserId) {
        if (!currentUserId.isNullOrBlank()) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                .collect { visibleItems ->
                    val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: -1
                    val totalItemCount = listState.layoutInfo.totalItemsCount
                    if (lastVisibleItemIndex >= totalItemCount - 2 && totalItemCount > 0 && myPostsUiState !is CommunityUiState.Loading) {
                        communityViewModel.loadMoreMyPosts(currentUserId!!) // Need this function
                    }
                }
        }
    }

    // Effect to show error messages
    LaunchedEffect(myPostsUiState) {
        if (myPostsUiState is CommunityUiState.Error) {
            snackbarHostState.showSnackbar((myPostsUiState as CommunityUiState.Error).message)
        }
    }


    Box(
        modifier = Modifier.fillMaxSize() // Scaffold padding handled by NavHost caller
    ) {
        when {
            myPostsUiState == CommunityUiState.Loading && myPosts.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            myPosts.isEmpty() && myPostsUiState !is CommunityUiState.Loading -> {
                Text(
                    "You haven't created any posts yet.\nTap the '+' to add one!",
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp)
                )
            }
            else -> {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(myPosts, key = { it.id }) { post ->
                        // isFollowed is irrelevant here, isLiked state comes from post data
                        val isLiked = currentUserId != null && post.likedBy.contains(currentUserId)

                        CommunityPostCard(
                            post = post,
                            currentUserId = currentUserId,
                            isLiked = isLiked,
                            isFollowed = false, // Not applicable here
                            onLikeClick = { communityViewModel.likePost(post.id) }, // Use specific like function?
                            onCommentClick = { navController.navigate("postDetail/${post.id}") },
                            onFollowClick = { /* No action */ },
                            onUnfollowClick = { /* No action */ },
                            onAuthorClick = { /* No action, it's the current user */ },
                            onPostClick = { /* No action */ },
                            // --- Pass Edit and Delete actions ---
                            onEditClick = { navController.navigate("createPost?postId=${post.id}") },
                            onDeleteClick = { postToDelete = post } // Show confirmation dialog
                            // --- End Pass Actions ---
                        )
                    }

                    // Loading indicator for pagination
                    if (isLoadingMoreMyPosts) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
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


// --- Add Delete Confirmation Dialog (similar to TutorDashboardScreen) ---
@Composable
fun DeleteConfirmationDialog(
    postName: String, // Changed parameter name
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Post") },
        text = { Text("Are you sure you want to delete \"$postName\"? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}