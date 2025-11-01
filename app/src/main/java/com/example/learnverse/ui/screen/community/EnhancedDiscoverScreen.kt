package com.example.learnverse.ui.screen.community

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.data.model.CommunityPost
import com.example.learnverse.ui.screen.home.BottomNavigationBar
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.CommunityUiState
import com.example.learnverse.viewmodel.CommunityViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Modern Discover Screen with Video Visibility Tracking
 * Videos auto-play only when visible (Instagram-style)
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun EnhancedDiscoverScreen(
    navController: NavController,
    communityViewModel: CommunityViewModel,
    authViewModel: AuthViewModel
) {
    val feedPosts by communityViewModel.feedPosts.collectAsStateWithLifecycle()
    val feedUiState by communityViewModel.feedUiState.collectAsStateWithLifecycle()
    val userRole by authViewModel.currentUserRole.collectAsStateWithLifecycle()
    val followingIds by communityViewModel.followingIds.collectAsStateWithLifecycle()
    val currentUserId by authViewModel.currentUserId.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // VIDEO VISIBILITY TRACKER - Instagram-style auto-play
    val videoVisibilityTracker = rememberVideoVisibilityTracker()

    // Pull to refresh
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                communityViewModel.fetchInitialFeed()
                delay(500)
                isRefreshing = false
            }
        }
    )

    // State for comments sheet
    var selectedPost by remember { mutableStateOf<CommunityPost?>(null) }

    // Fetch data
    LaunchedEffect(Unit) {
        communityViewModel.fetchInitialFeed()
        communityViewModel.fetchFollowingList()
    }

    // Infinite scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisible = visibleItems.lastOrNull()?.index ?: -1
                val total = listState.layoutInfo.totalItemsCount
                if (lastVisible >= total - 2 && total > 0) {
                    communityViewModel.loadMoreFeedPosts()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Discover",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (userRole == "USER") {
                BottomNavigationBar(navController = navController)
            }
        },
        floatingActionButton = {
            if (userRole == "TUTOR") {
                FloatingActionButton(
                    onClick = { navController.navigate("createPost") },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Create Post")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                feedUiState == CommunityUiState.Loading && feedPosts.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                feedPosts.isEmpty() -> {
                    EmptyFeedState(
                        onCreateClick = { navController.navigate("createPost") },
                        showCreateButton = userRole == "TUTOR"
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = feedPosts,
                            key = { it.id }
                        ) { post ->
                            // Pass video visibility tracker to each post
                            EnhancedCommunityPostCard(
                                post = post,
                                currentUserId = currentUserId,
                                isLiked = currentUserId != null && post.likedBy.contains(currentUserId),
                                isFollowed = followingIds.contains(post.authorId),
                                onLikeClick = { communityViewModel.likePost(post.id) },
                                onCommentClick = { selectedPost = post },
                                onFollowClick = { communityViewModel.followTutor(post.authorId) },
                                onUnfollowClick = { communityViewModel.unfollowTutor(post.authorId) },
                                onAuthorClick = { navController.navigate("tutorProfile/${post.authorId}") },
                                onPostClick = { navController.navigate("postDetail/${post.id}") },
                                onEditClick = { navController.navigate("createPost?postId=${post.id}") },
                                onDeleteClick = { /* TODO: Delete dialog */ },
                                videoVisibilityTracker = videoVisibilityTracker,
                                modifier = Modifier.animateItemPlacement()
                            )
                        }

                        // Loading more indicator
                        if (communityViewModel.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Pull refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Comments Bottom Sheet
    selectedPost?.let { post ->
        CommentsBottomSheet(
            post = post,
            currentUserId = currentUserId,
            onDismiss = { selectedPost = null },
            onAddComment = { commentText ->
                communityViewModel.addComment(post.id, commentText)
            },
            onLikeComment = {
            },
            onDeleteComment = {
            }
        )
    }
}

@Composable
private fun EmptyFeedState(
    onCreateClick: () -> Unit,
    showCreateButton: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "No posts yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            if (showCreateButton)
                "Be the first to share something!"
            else
                "Start following tutors to see their posts",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (showCreateButton) {
            Spacer(Modifier.height(24.dp))

            Button(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create Post")
            }
        }
    }
}