package com.example.learnverse.ui.screen.community

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment // Or outlined.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert // For potential delete/edit menu
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // For placeholder image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter // Use Coil for image loading
import coil.request.ImageRequest
import com.example.learnverse.R // Assuming you have a placeholder drawable
import com.example.learnverse.data.model.CommunityPost
import com.example.learnverse.utils.VideoPlayer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommunityPostCard(
    post: CommunityPost,
    currentUserId: String?, // Make nullable, handle cases where ID isn't available yet
    isLiked: Boolean, // Pass the calculated like state
    isFollowed: Boolean, // Pass the calculated follow state
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onPostClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
    // Add onDelete, onUpdate callbacks if needed
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onPostClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) { // Adjust padding
            // --- Author Row ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp) // Add horizontal padding
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onAuthorClick() }
                ) {
                    // Author Profile Picture (Using Coil)
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(data = null) // TODO: Add actual author profile picture URL later
                                .placeholder(R.drawable.baseline_account_circle_24) // Add a placeholder drawable
                                .error(R.drawable.baseline_error_outline_24)       // Add an error drawable
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Author profile picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(post.authorName, fontWeight = FontWeight.SemiBold)
                        Text(
                            formatTimestamp(post.createdAt), // Use formatting utility
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // --- Follow/Unfollow Button OR Edit/Delete Menu ---
                Box(contentAlignment = Alignment.CenterEnd) { // Use Box for alignment
                    // Follow/Unfollow Button (Show only if author is not the current user)
                    if (currentUserId != null && post.authorId != currentUserId) {
                        OutlinedButton(
                            onClick = if (isFollowed) onUnfollowClick else onFollowClick,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text(if (isFollowed) "Following" else "Follow", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // --- ADD Edit/Delete Menu (Show only if author IS the current user) ---
                    var showMenu by remember { mutableStateOf(false) }
                    if (currentUserId != null && post.authorId == currentUserId) {
                        Box { // Box to anchor the DropdownMenu to the IconButton
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        showMenu = false
                                        onEditClick() // Call the passed lambda
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        onDeleteClick() // Call the passed lambda
                                    }
                                )
                            }
                        }
                    }
                    // --- END ADD ---
                }
                // --- End Follow/Unfollow OR Edit/Delete ---
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Post Content (if any) ---
            if (!post.content.isNullOrBlank()) {
                Text(
                    post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp) // Add padding
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // --- Media Display (Takes full width if present) ---
            when (post.mediaType) {
                "image" -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(data = post.mediaUrl)
                                .placeholder(R.drawable.baseline_account_circle_24) // Add a placeholder drawable
                                .error(R.drawable.baseline_error_outline_24)       // Add an error drawable
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Post image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp) // Adjust max height as needed
                            .background(MaterialTheme.colorScheme.surfaceVariant), // Background for loading/error
                        contentScale = ContentScale.Crop // Crop might look better for feed
                    )
                }
                "video" -> {

                    if (!post.mediaUrl.isNullOrBlank()) {
                        VideoPlayer(
                            videoUrl = post.mediaUrl,
                            modifier = Modifier.clip(MaterialTheme.shapes.medium) // Apply clipping if desired
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp) // Adjust height for video
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            // Simple placeholder, replace with player controls/thumbnail
                            Icon(
                                Icons.Filled.PlayCircle,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                            // Text("Video Placeholder", color = Color.White)
                        }
                    }
                }
                // "none" or other types don't need specific handling here
            }

            // Add spacer only if there was media displayed
            if (post.mediaType == "image" || post.mediaType == "video") {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // --- Action Row (Like, Comment) ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), // Add padding
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp) // Increased spacing
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onLikeClick) // Make clickable
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
                    )
                    Spacer(modifier = Modifier.width(6.dp)) // Adjust spacing
                    Text(
                        "${post.likedBy.size}",
                        style = MaterialTheme.typography.bodyMedium, // Slightly larger
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
                    )
                }

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onCommentClick) // Make clickable
                ) {
                    Icon(
                        imageVector = Icons.Filled.Comment, // Consider outlined.ChatBubbleOutline
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
                    )
                    Spacer(modifier = Modifier.width(6.dp)) // Adjust spacing
                    Text(
                        "${post.commentsCount}",
                        style = MaterialTheme.typography.bodyMedium, // Slightly larger
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
                    )
                }
                // Optional: Add Share button here
            }
        }
    }
}


// Simple timestamp formatting utility (Add this at the bottom of the file or in a utils file)
// Consider using a more robust library for complex time formatting if needed
@RequiresApi(Build.VERSION_CODES.O)
fun formatTimestamp(isoTimestamp: String): String {
    return try {
        val instant = Instant.parse(isoTimestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val now = LocalDateTime.now()

        val hoursAgo = ChronoUnit.HOURS.between(localDateTime, now)
        val daysAgo = ChronoUnit.DAYS.between(localDateTime, now)

        when {
            hoursAgo < 1 -> "Just now"
            hoursAgo < 24 -> "$hoursAgo${if (hoursAgo == 1L) "hr" else "hrs"} ago"
            daysAgo < 7 -> "$daysAgo${if (daysAgo == 1L) "d" else "d"} ago"
            else -> localDateTime.format(DateTimeFormatter.ofPattern("MMM d")) // e.g., Oct 25
        }
    } catch (e: Exception) {
        // Fallback if parsing fails
        isoTimestamp.take(10) // Show YYYY-MM-DD
    }
}