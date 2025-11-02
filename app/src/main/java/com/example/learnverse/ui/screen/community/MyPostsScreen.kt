package com.example.learnverse.ui.screen.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.data.model.CommunityPost
import com.example.learnverse.ui.screen.tutor.TutorBottomNavigationBar // ✅ Import
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.CommunityUiState
import com.example.learnverse.viewmodel.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class) // ✅ Add
@Composable
fun MyPostsScreen(
    navController: NavController,
    communityViewModel: CommunityViewModel,
    authViewModel: AuthViewModel
) {
    val currentUserId by authViewModel.currentUserId.collectAsStateWithLifecycle()
    val myPosts by communityViewModel.myPosts.collectAsStateWithLifecycle()
    val myPostsUiState by communityViewModel.myPostsUiState.collectAsStateWithLifecycle()
    val isLoadingMoreMyPosts = communityViewModel.isLoadingMoreMyPosts

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    var postToDelete by remember { mutableStateOf<CommunityPost?>(null) }

    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrBlank()) {
            communityViewModel.fetchMyPosts(currentUserId!!)
        }
    }

    LaunchedEffect(listState, currentUserId) {
        if (!currentUserId.isNullOrBlank()) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                .collect { visibleItems ->
                    val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: -1
                    val totalItemCount = listState.layoutInfo.totalItemsCount
                    if (lastVisibleItemIndex >= totalItemCount - 2 && totalItemCount > 0 && myPostsUiState !is CommunityUiState.Loading) {
                        communityViewModel.loadMoreMyPosts(currentUserId!!)
                    }
                }
        }
    }

    LaunchedEffect(myPostsUiState) {
        if (myPostsUiState is CommunityUiState.Error) {
            snackbarHostState.showSnackbar((myPostsUiState as CommunityUiState.Error).message)
        }
    }

    // ✅ WRAP EVERYTHING IN A SCAFFOLD
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Posts") })
        },
        bottomBar = {
            TutorBottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("createPost") }) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues) // ✅ Use paddingValues
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
                            val isLiked = currentUserId != null && post.likedBy.contains(currentUserId)

                            EnhancedCommunityPostCard(
                                post = post,
                                currentUserId = currentUserId,
                                isLiked = isLiked,
                                isFollowed = false, // Not applicable here
                                onLikeClick = { communityViewModel.likePost(post.id) },
                                onCommentClick = { navController.navigate("postDetail/${post.id}") },
                                onFollowClick = { /* No action */ },
                                onUnfollowClick = { /* No action */ },
                                onAuthorClick = { /* No action */ },
                                onPostClick = { /* No action */ },
                                onEditClick = { navController.navigate("createPost?postId=${post.id}") },
                                onDeleteClick = { postToDelete = post }
                            )
                        }

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

            if (postToDelete != null) {
                DeleteConfirmationDialog(
                    postName = postToDelete!!.content?.take(30) ?: "this post",
                    onConfirm = {
                        communityViewModel.deletePost(postToDelete!!.id)
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