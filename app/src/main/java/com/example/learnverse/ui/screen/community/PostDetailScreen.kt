package com.example.learnverse.ui.screen.community

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.data.model.Comment
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.CommunityUiState
import com.example.learnverse.viewmodel.CommunityViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    navController: NavController,
    communityViewModel: CommunityViewModel,
    authViewModel: AuthViewModel
) {
    var commentText by remember { mutableStateOf("") }
    val selectedPost by communityViewModel.selectedPost.collectAsStateWithLifecycle()
    val uiState by communityViewModel.feedUiState.collectAsStateWithLifecycle()

    LaunchedEffect(postId) {
        communityViewModel.findPostById(postId)
    }

    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier
            .imePadding()                // ✅ Moves entire layout up when keyboard opens
            .navigationBarsPadding(),

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedPost?.authorName ?: "Post Details",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },

        bottomBar = {
            CommentInputBar(
                commentText = commentText,
                onCommentChange = { commentText = it },
                onSendComment = {
                    if (commentText.isNotBlank()) {
                        communityViewModel.addComment(postId, commentText)
                        commentText = ""
                    }
                },
                enabled = uiState !is CommunityUiState.Loading
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState == CommunityUiState.Loading && selectedPost == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                selectedPost == null -> {
                    Text(
                        "Post not found.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Post Card
                        item {
                            val currentUserId by authViewModel.currentUserId.collectAsStateWithLifecycle()
                            val followingIds by communityViewModel.followingIds.collectAsStateWithLifecycle()

                            val isLiked = currentUserId != null && selectedPost!!.likedBy.contains(currentUserId)
                            val isFollowed = followingIds.contains(selectedPost!!.authorId)

                            CommunityPostCard(
                                post = selectedPost!!,
                                currentUserId = currentUserId,
                                isLiked = isLiked,
                                isFollowed = isFollowed,
                                onLikeClick = { communityViewModel.likePost(selectedPost!!.id) },
                                onCommentClick = { /* Already here */ },
                                onFollowClick = { communityViewModel.followTutor(selectedPost!!.authorId) },
                                onUnfollowClick = { communityViewModel.unfollowTutor(selectedPost!!.authorId) },
                                onAuthorClick = { navController.navigate("tutorProfile/${selectedPost!!.authorId}") },
                                onPostClick = { /* Already here */ }
                            )
                        }

                        // Comment Header
                        item {
                            Column {
                                Divider()
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Comments (${selectedPost?.commentsCount ?: 0})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        }

                        // Comments
                        items(selectedPost?.comments ?: emptyList(), key = { it.id }) { comment ->
                            AnimatedVisibility(visible = true) {
                                CommentItem(comment = comment)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentInputBar(
    commentText: String,
    onCommentChange: (String) -> Unit,
    onSendComment: () -> Unit,
    enabled: Boolean
) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = commentText,
                onValueChange = onCommentChange,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp)),
                placeholder = { Text("Write a comment…") },
                shape = RoundedCornerShape(20.dp),
                maxLines = 3,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                enabled = enabled
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onSendComment,
                enabled = enabled && commentText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (enabled && commentText.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}


@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar (initial letter)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.authorName.firstOrNull()?.uppercase() ?: "U",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatTimestamp(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

