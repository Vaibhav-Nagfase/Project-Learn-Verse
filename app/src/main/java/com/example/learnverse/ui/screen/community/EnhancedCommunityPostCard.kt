package com.example.learnverse.ui.screen.community

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.learnverse.R
import com.example.learnverse.data.model.CommunityPost
import com.example.learnverse.utils.VideoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * FIXED Modern Post Card
 * - Images now display correctly
 * - Text expandable (8-9 lines with "more")
 * - Time shown at bottom right
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EnhancedCommunityPostCard(
    post: CommunityPost,
    currentUserId: String?,
    isLiked: Boolean,
    isFollowed: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onPostClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    videoVisibilityTracker: VideoVisibilityTracker? = null,
    modifier: Modifier = Modifier
) {
    var showLikeAnimation by remember { mutableStateOf(false) }
    var likeScale by remember { mutableStateOf(1f) }
    var isTextExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp)
    ) {
        // Header: Profile Picture + Name + Follow/Menu
        PostHeader(
            post = post,
            currentUserId = currentUserId,
            isFollowed = isFollowed,
            onAuthorClick = onAuthorClick,
            onFollowClick = onFollowClick,
            onUnfollowClick = onUnfollowClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )

        // Content Text with Read More (FIXED: 8-9 lines)
        if (!post.content.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            ExpandableText(
                text = post.content,
                isExpanded = isTextExpanded,
                onToggle = { isTextExpanded = !isTextExpanded },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Media: Image or Video (FIXED: Image now displays)
        if (!post.mediaUrl.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (!isLiked) {
                                    onLikeClick()
                                    showLikeAnimation = true
                                }
                            }
                        )
                    }
            ) {
                when (post.mediaType) {
                    // For images, add logging:
                    "image" -> {
                        Log.d("PostImage", "Loading image: ${post.mediaUrl}")
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(post.mediaUrl)
                                .listener(
                                    onSuccess = { _, _ -> Log.d("PostImage", "Image loaded successfully") },
                                    onError = { _, result -> Log.e("PostImage", "Error: ${result.throwable}") }
                                )
                                .crossfade(true)
                                .build(),
                            contentDescription = "Post image",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    "video" -> {
                        val shouldPlay by remember {
                            derivedStateOf {
                                videoVisibilityTracker?.shouldPlayVideo(post.id) ?: true
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .then(
                                    if (videoVisibilityTracker != null) {
                                        Modifier.trackVideoVisibility(
                                            videoId = post.id,
                                            visibilityTracker = videoVisibilityTracker,
                                            onVisibilityChanged = { /* Optional: log visibility */ }
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                AutoPlayVideoPlayer(
                                    videoUrl = post.mediaUrl,
                                    shouldPlay = shouldPlay,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Heart animation overlay
                AnimatedHeartOverlay(
                    show = showLikeAnimation,
                    onAnimationEnd = { showLikeAnimation = false }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Action Buttons + Time (FIXED: Time now visible at bottom right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Action buttons (left)
            ActionButtonsRow(
                isLiked = isLiked,
                likeScale = likeScale,
                likesCount = post.likedBy.size,
                commentsCount = post.commentsCount,
                onLikeClick = {
                    onLikeClick()
                    likeScale = 1.3f
                },
                onCommentClick = onCommentClick
            )

            // FIXED: Time now properly displayed
            Text(
                text = formatTimeAgo(post.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // Bottom divider
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    // Reset like animation
    LaunchedEffect(likeScale) {
        if (likeScale > 1f) {
            delay(150)
            likeScale = 1f
        }
    }
}

/**
 * Post Header with Profile Picture
 */
@Composable
private fun PostHeader(
    post: CommunityPost,
    currentUserId: String?,
    isFollowed: Boolean,
    onAuthorClick: () -> Unit,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Profile Picture + Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onAuthorClick)
        ) {
            // Profile Picture
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.authorProfilePicture ?: R.drawable.baseline_account_circle_24)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                placeholder = rememberAsyncImagePainter(R.drawable.baseline_account_circle_24),
                error = rememberAsyncImagePainter(R.drawable.baseline_account_circle_24)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Name + Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = post.authorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )

                if (post.authorType == "TUTOR") {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (currentUserId != null && post.authorId != currentUserId) {
            TextButton(
                onClick = if (isFollowed) onUnfollowClick else onFollowClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isFollowed)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isFollowed) "Following" else "Follow",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        } else if (currentUserId == post.authorId) {
            PostOptionsMenu(
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

/**
 * Options Menu (Edit/Delete)
 */
@Composable
private fun PostOptionsMenu(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options"
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showMenu = false
                    onEditClick()
                },
                leadingIcon = { Icon(Icons.Outlined.Edit, null) }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    onDeleteClick()
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Delete,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

/**
 * FIXED: Expandable Text - Shows 8-9 lines, then "more"
 */
@Composable
private fun ExpandableText(
    text: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // DEBUG: Log text info
    val lineCount = text.lines().size
    val charCount = text.length
    Log.d("ExpandableText", "Lines: $lineCount, Chars: $charCount, Text: ${text.take(50)}...")

    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (isExpanded) Int.MAX_VALUE else 8,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 20.sp,
            onTextLayout = { textLayoutResult ->
                // DEBUG: Log actual rendered lines
                Log.d("ExpandableText", "Rendered lines: ${textLayoutResult.lineCount}")
            }
        )

        // FIXED: Better detection - check rendered lines
        if (!isExpanded && (lineCount > 8 || charCount > 300)) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "...more",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onToggle)
            )
            Log.d("ExpandableText", "Showing 'more' button")
        }
    }
}

/**
 * Heart Animation on Double Tap
 */
@Composable
private fun AnimatedHeartOverlay(
    show: Boolean,
    onAnimationEnd: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(show) {
        if (show) {
            launch {
                scale.animateTo(
                    targetValue = 1.5f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(300)
                )
                onAnimationEnd()
                scale.snapTo(0f)
                alpha.snapTo(1f)
            }
        }
    }

    if (show) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Like",
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale.value)
                    .graphicsLayer { this.alpha = alpha.value },
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Action Buttons Row
 */
@Composable
private fun ActionButtonsRow(
    isLiked: Boolean,
    likeScale: Float,
    likesCount: Int,
    commentsCount: Int,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = likeScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onLikeClick)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                modifier = Modifier
                    .size(24.dp)
                    .scale(animatedScale),
                tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (likesCount > 0) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formatCount(likesCount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }

        // Comment button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onCommentClick)
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Comment",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (commentsCount > 0) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formatCount(commentsCount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }

        // Share button
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Helper Functions
 */
private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M"
        count >= 1_000 -> "${count / 1_000}K"
        else -> count.toString()
    }
}

/**
 * FIXED: Format time ago properly
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun formatTimeAgo(isoTimestamp: String): String {
    return try {
        val instant = Instant.parse(isoTimestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val now = LocalDateTime.now()

        val minutes = ChronoUnit.MINUTES.between(dateTime, now)
        val hours = ChronoUnit.HOURS.between(dateTime, now)
        val days = ChronoUnit.DAYS.between(dateTime, now)
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        when {
            minutes < 1 -> "now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            weeks < 4 -> "${weeks}w"
            months < 12 -> "${months}mo"
            else -> "${years}y"
        }
    } catch (e: Exception) {
        // Fallback if parsing fails
        "recently"
    }
}