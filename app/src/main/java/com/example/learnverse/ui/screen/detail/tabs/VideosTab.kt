package com.example.learnverse.ui.screen.detail.tabs

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.learnverse.data.model.Activity
import com.example.learnverse.ui.components.dialogs.AddVideoDialog
import com.example.learnverse.ui.components.dialogs.EditVideoDialog
import com.example.learnverse.ui.components.dialogs.AddResourceDialog
import com.example.learnverse.viewmodel.ActivitiesViewModel

@Composable
fun VideosTab(
    activity: Activity,
    isTutor: Boolean,
    isEnrolled: Boolean,
    navController: NavController,
    viewModel: ActivitiesViewModel
) {
    val context = LocalContext.current
    val videos = activity.videoContent?.recordedVideos
    val canViewVideos = isTutor || isEnrolled

    // Dialog states
    var showAddVideoDialog by remember { mutableStateOf(false) }
    var showEditVideoDialog by remember { mutableStateOf<Activity.VideoContent.Video?>(null) }
    var showAddResourceDialog by remember { mutableStateOf<Activity.VideoContent.Video?>(null) }

    // Loading states from ViewModel
    val isUploadingVideo by viewModel.isUploadingVideo.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val isUploadingResource by viewModel.isUploadingResource.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Locked state
            if (!canViewVideos) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Enroll to Access Videos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Video content is only available to enrolled students",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                return@LazyColumn
            }

            // Empty state
            if (videos.isNullOrEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (isTutor) "No videos uploaded yet" else "No videos available yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isTutor) {
                            Text(
                                "Tap the + button to add videos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                return@LazyColumn
            }

            // Video list
            items(videos.sortedBy { it.order }) { video ->
                VideoItem(
                    video = video,
                    isTutor = isTutor,
                    onClick = {
                        navController.navigate("video_player/${activity.id}/${video.videoId}")
                    },
                    onEdit = { showEditVideoDialog = video },
                    onAddResource = { showAddResourceDialog = video },
                    onDelete = {
                        viewModel.deleteVideo(
                            activityId = activity.id ?: "",
                            videoId = video.videoId ?: "",
                            onSuccess = {
                                android.widget.Toast.makeText(
                                    context,
                                    "Video deleted successfully!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { error ->
                                android.widget.Toast.makeText(
                                    context,
                                    "Delete failed: $error",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                )
            }

            // Bottom padding for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB for tutors only
        if (isTutor) {
            FloatingActionButton(
                onClick = { showAddVideoDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Video")
            }
        }
    }

    // Add Video Dialog
    if (showAddVideoDialog) {
        AddVideoDialog(
            activityId = activity.id ?: "",
            nextOrder = (videos?.maxOfOrNull { it.order ?: 0 } ?: 0) + 1,
            onDismiss = { showAddVideoDialog = false },
            onVideoAdded = { showAddVideoDialog = false },
            onAddWithUrl = { title, desc, url, order, isPreview ->
                viewModel.addVideoWithUrl(
                    activityId = activity.id ?: "",
                    title = title,
                    description = desc,
                    videoUrl = url,
                    order = order,
                    isPreview = isPreview,
                    onSuccess = {
                        showAddVideoDialog = false
                        android.widget.Toast.makeText(
                            context,
                            "Video added successfully!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = { error ->
                        android.widget.Toast.makeText(
                            context,
                            "Failed: $error",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onUploadFile = { uri, title, desc, order, isPreview ->
                viewModel.uploadVideoFile(
                    activityId = activity.id ?: "",
                    videoUri = uri,
                    title = title,
                    description = desc,
                    order = order,
                    isPreview = isPreview,
                    context = context,
                    onSuccess = {
                        showAddVideoDialog = false
                        android.widget.Toast.makeText(
                            context,
                            "Video uploaded successfully!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = { error ->
                        android.widget.Toast.makeText(
                            context,
                            "Upload failed: $error",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            isUploading = isUploadingVideo,
            uploadProgress = uploadProgress
        )
    }

    // Edit Video Dialog
    showEditVideoDialog?.let { video ->
        EditVideoDialog(
            video = video,
            onDismiss = { showEditVideoDialog = null },
            onUpdate = { title, desc, url, order, isPreview ->
                viewModel.updateVideo(
                    activityId = activity.id ?: "",
                    videoId = video.videoId ?: "",
                    title = title,
                    description = desc,
                    videoUrl = url,
                    order = order,
                    isPreview = isPreview,
                    onSuccess = {
                        showEditVideoDialog = null
                        android.widget.Toast.makeText(
                            context,
                            "Video updated!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = { error ->
                        android.widget.Toast.makeText(
                            context,
                            "Update failed: $error",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            isUpdating = false
        )
    }

    // Add Resource Dialog
    showAddResourceDialog?.let { video ->
        AddResourceDialog(
            activityId = activity.id ?: "",
            videoId = video.videoId ?: "",
            onDismiss = { showAddResourceDialog = null },
            onAddWithUrl = { type, title, url ->
                viewModel.addResourceWithUrl(
                    activityId = activity.id ?: "",
                    videoId = video.videoId ?: "",
                    type = type,
                    title = title,
                    url = url,
                    onSuccess = {
                        showAddResourceDialog = null
                        android.widget.Toast.makeText(
                            context,
                            "Resource added!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = { error ->
                        android.widget.Toast.makeText(
                            context,
                            "Failed: $error",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onUploadFile = { uri, type, title ->
                viewModel.uploadResourceFile(
                    activityId = activity.id ?: "",
                    videoId = video.videoId ?: "",
                    resourceUri = uri,
                    type = type,
                    title = title,
                    context = context,
                    onSuccess = {
                        showAddResourceDialog = null
                        android.widget.Toast.makeText(
                            context,
                            "Resource uploaded!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = { error ->
                        android.widget.Toast.makeText(
                            context,
                            "Upload failed: $error",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            isUploading = isUploadingResource
        )
    }
}

@Composable
private fun VideoItem(
    video: Activity.VideoContent.Video,
    isTutor: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onAddResource: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandedResources by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick  // ✅ Make entire card clickable
    ) {
        Column {
            // ✅ YouTube-style video row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Thumbnail with play overlay
                Box(
                    modifier = Modifier
                        .width(140.dp)  // ✅ Wider thumbnail
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = video.thumbnailUrl ?: "https://via.placeholder.com/320x180?text=Video",
                        contentDescription = "Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Play overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Duration badge (bottom-right)
                    video.duration?.let { duration ->
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp),
                            color = Color.Black.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = formatDuration(duration),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Preview badge (top-left)
                    if (video.isPreview == true) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "FREE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Video info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title
                    Text(
                        text = video.title ?: "Untitled Video",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Description
                    video.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Metadata row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Resources indicator
                        video.resources?.let { resources ->
                            if (resources.isNotEmpty()) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = "Resources",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${resources.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Order number
                        Text(
                            text = "Lesson ${video.order ?: 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tutor menu button
                if (isTutor) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Menu")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Video") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Resource") },
                                onClick = {
                                    showMenu = false
                                    onAddResource()
                                },
                                leadingIcon = { Icon(Icons.Default.AttachFile, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Resources section (expandable)
            video.resources?.let { resources ->
                if (resources.isNotEmpty()) {
                    Divider()

                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedResources = !expandedResources },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Resources (${resources.size})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                if (expandedResources) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }

                        if (expandedResources) {
                            Spacer(Modifier.height(8.dp))
                            resources.forEach { resource ->
                                ResourceItem(resource)
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Video") },
            text = { Text("Are you sure you want to delete \"${video.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDuration(seconds: Int): String {
    return when {
        seconds < 60 -> "0:${String.format("%02d", seconds)}"
        seconds < 3600 -> { // Less than 1 hour
            val minutes = seconds / 60
            val secs = seconds % 60
            "$minutes:${String.format("%02d", secs)}"
        }
        else -> { // 1 hour or more
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            "$hours:${String.format("%02d", minutes)}:${String.format("%02d", secs)}"
        }
    }
}


@Composable
private fun ResourceItem(resource: Activity.VideoContent.Video.Resource) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle resource download */ }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.PictureAsPdf,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = resource.title ?: "Resource",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}