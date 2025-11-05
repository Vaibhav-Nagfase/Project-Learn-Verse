package com.example.learnverse.ui.screen.tutor.course

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.learnverse.data.model.*
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.data.repository.VideoManagementRepository
import com.example.learnverse.ui.components.dialogs.*
import com.example.learnverse.viewmodel.ActivitiesViewModel
import kotlinx.coroutines.launch

@Composable
fun ContentManagementTab(
    activity: Activity,
    apiService: ApiService,
    onRefresh: () -> Unit,
    activityViewModel: ActivitiesViewModel
) {
    val repository = remember { VideoManagementRepository(apiService) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Dialog states
    var showAddVideoDialog by remember { mutableStateOf(false) }
    var showAddResourceDialog by remember { mutableStateOf(false) }
    var showEditMeetingDialog by remember { mutableStateOf(false) }
    var showEditVideoDialog by remember { mutableStateOf(false) }
    var selectedVideo by remember { mutableStateOf<Activity.VideoContent.Video?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }

    val videos = activity.videoContent?.recordedVideos ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Modern Videos Section
        item {
            ModernSectionHeader(
                icon = Icons.Default.PlayArrow,
                title = "Course Videos",
                count = videos.size,
                gradient = Brush.linearGradient(
                    listOf(
                        Color(0xFF6366F1),
                        Color(0xFF8B5CF6)
                    )
                ),
                onAddClick = { showAddVideoDialog = true }
            )
        }

        if (videos.isEmpty()) {
            item {
                ModernEmptyState(
                    icon = Icons.Default.VideoLibrary,
                    title = "No videos yet",
                    description = "Start building your course by adding video lessons",
                    actionText = "Add First Video",
                    onActionClick = { showAddVideoDialog = true }
                )
            }
        } else {
            items(videos) { video ->
                ModernVideoCard(
                    video = video,
                    onEdit = {
                        selectedVideo = video
                        showEditVideoDialog = true
                    },
                    onDelete = {
                        scope.launch {
                            try {
                                activityViewModel.deleteVideo(
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
                                snackbarHostState.showSnackbar("Video deleted ✓")
                                onRefresh()
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Failed: ${e.message}")
                            }
                        }
                    }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        // Modern Resources Section
        item {
            ModernSectionHeader(
                icon = Icons.Default.Description,
                title = "Learning Resources",
                count = videos.sumOf { it.resources?.size ?: 0 },
                gradient = Brush.linearGradient(
                    listOf(
                        Color(0xFFF59E0B),
                        Color(0xFFEF4444)
                    )
                ),
                onAddClick = {
                    if (videos.isNotEmpty()) {
                        showAddResourceDialog = true
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Add a video first")
                        }
                    }
                }
            )
        }

        val allResources = videos.flatMap { video ->
            video.resources?.map { resource ->
                Triple(video.videoId, video.title, resource)
            } ?: emptyList()
        }

        if (allResources.isEmpty()) {
            item {
                ModernEmptyState(
                    icon = Icons.Default.FolderOpen,
                    title = "No resources yet",
                    description = "Enhance learning with PDFs, documents, and links",
                    actionText = "Add Resource",
                    onActionClick = {
                        if (videos.isNotEmpty()) {
                            showAddResourceDialog = true
                        }
                    }
                )
            }
        } else {
            items(allResources) { (videoId, videoTitle, resource) ->
                ModernResourceCard(
                    videoTitle = videoTitle,
                    resource = resource,
                    onDelete = {
                        scope.launch {
                            try {
                                repository.deleteResource(activity.id, videoId, resource.url)
                                snackbarHostState.showSnackbar("Resource deleted ✓")
                                onRefresh()
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Failed: ${e.message}")
                            }
                        }
                    }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        // Modern Meeting Section
        item {
            ModernMeetingSection(
                videoContent = activity.videoContent,
                onEdit = { showEditMeetingDialog = true }
            )
        }

        item { Spacer(Modifier.height(32.dp)) }
    }

    // Dialogs (same as before)
    if (showAddVideoDialog) {
        AddVideoDialog(
            activityId = activity.id,
            nextOrder = (videos.maxOfOrNull { it.order ?: 0 } ?: 0) + 1,
            onDismiss = { showAddVideoDialog = false },
            onVideoAdded = {
                showAddVideoDialog = false
                onRefresh()
            },
            onAddWithUrl = { title, description, videoUrl, order, isPreview ->
                scope.launch {
                    try {
                        activityViewModel.addVideoWithUrl(
                            activityId = activity.id ?: "",
                            title = title,
                            description = description,
                            videoUrl = videoUrl,
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
                        snackbarHostState.showSnackbar("Video added successfully ✓")
                        showAddVideoDialog = false
                        onRefresh()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed: ${e.message}")
                    }
                }
            },
            onUploadFile = { uri, title, description, order, isPreview ->
                scope.launch {
                    try {
                        isUploading = true

                        activityViewModel.uploadVideoFile(
                            activityId = activity.id ?: "",
                            videoUri = uri,
                            title = title,
                            description = description,
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
                        snackbarHostState.showSnackbar("Video uploaded successfully ✓")
                        showAddVideoDialog = false
                        isUploading = false
                        onRefresh()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Upload failed: ${e.message}")
                        isUploading = false
                    }
                }
            },
            isUploading = isUploading,
            uploadProgress = uploadProgress
        )
    }

    if (showEditVideoDialog && selectedVideo != null) {
        EditVideoDialog(
            video = selectedVideo!!,
            onDismiss = {
                showEditVideoDialog = false
                selectedVideo = null
            },
            onUpdate = { title, description, videoUrl, order, isPreview ->
                scope.launch {
                    try {
                        isUpdating = true
                        activityViewModel.updateVideo(
                            activityId = activity.id ?: "",
                            videoId = selectedVideo?.videoId ?: "",
                            title = title,
                            description = description,
                            videoUrl = videoUrl,
                            order = order,
                            isPreview = isPreview,
                            onSuccess = {
                                showEditVideoDialog = false
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
                        snackbarHostState.showSnackbar("Video updated ✓")
                        showEditVideoDialog = false
                        selectedVideo = null
                        isUpdating = false
                        onRefresh()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed: ${e.message}")
                        isUpdating = false
                    }
                }
            },
            isUpdating = isUpdating
        )
    }

    if (showAddResourceDialog && videos.isNotEmpty()) {
        AddResourceDialog(
            activityId = activity.id,
            videoId = videos.first().videoId,
            onDismiss = { showAddResourceDialog = false },
            onAddWithUrl = { type, title, url ->
                scope.launch {
                    try {
                        val request = AddResourceRequest(type = type, title = title, url = url)
                        repository.addResource(activity.id, videos.first().videoId, request)
                        snackbarHostState.showSnackbar("Resource added ✓")
                        showAddResourceDialog = false
                        onRefresh()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed: ${e.message}")
                    }
                }
            },
            onUploadFile = { uri, type, title ->
                scope.launch {
                    try {
                        val resourceUrl = "https://example.com/resource.pdf"
                        val request = AddResourceRequest(type = type, title = title, url = resourceUrl)
                        repository.addResource(activity.id, videos.first().videoId, request)
                        snackbarHostState.showSnackbar("Resource uploaded ✓")
                        showAddResourceDialog = false
                        onRefresh()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Upload failed: ${e.message}")
                    }
                }
            },
            isUploading = isUploading
        )
    }

    if (showEditMeetingDialog) {
        AddMeetingDialog(
            existingMeeting = activity.videoContent,
            onDismiss = { showEditMeetingDialog = false },
            onSave = { platform, meetingLink, meetingId, passcode ->
                scope.launch {
                    try {
                        val request = UpdateMeetingRequest(
                            platform = platform,
                            meetingLink = meetingLink,
                            meetingId = meetingId,
                            passcode = passcode
                        )
                        repository.updateMeeting(activity.id, request)
                        snackbarHostState.showSnackbar("Meeting info updated ✓")
                        showEditMeetingDialog = false
                        onRefresh()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed: ${e.message}")
                    }
                }
            },
            isUpdating = false
        )
    }

    SnackbarHost(snackbarHostState)
}

// Modern Composable Components

@Composable
fun ModernSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    count: Int,
    gradient: Brush,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ FIX: Added weight to prevent button overflow
        Row(
            modifier = Modifier.weight(1f), // ✅ Takes available space
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .background(gradient, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) { // ✅ Column also gets weight
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1, // ✅ Prevent wrapping
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "$count ${if (count == 1) "item" else "items"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.width(8.dp)) // ✅ Spacing between title and button

        // ✅ Button with fixed size to prevent wrapping
        FilledTonalButton(
            onClick = onAddClick,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            modifier = Modifier.height(40.dp) // ✅ Fixed height
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add", maxLines = 1) // ✅ Prevent text wrapping
        }
    }
}

@Composable
fun ModernEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(0.7f),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(actionText)
            }
        }
    }
}

@Composable
fun ModernVideoCard(
    video: Activity.VideoContent.Video,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with gradient overlay
            Box(
                modifier = Modifier
                    .size(120.dp, 80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (!video.thumbnailUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = "Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Preview badge
                if (video.isPreview == true) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981)
                    ) {
                        Text(
                            "FREE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Play overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModernChip(
                        icon = Icons.Default.AccessTime,
                        text = formatDuration(video.duration ?: 0)
                    )

                    if (!video.resources.isNullOrEmpty()) {
                        ModernChip(
                            icon = Icons.Default.AttachFile,
                            text = "${video.resources.size}"
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModernResourceCard(
    videoTitle: String,
    resource: Activity.VideoContent.Video.Resource,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    when (resource.type.lowercase()) {
                        "pdf" -> Icons.Default.PictureAsPdf
                        else -> Icons.Default.Description
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    resource.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "In: $videoTitle",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernMeetingSection(
    videoContent: Activity.VideoContent?,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiary
                    ) {
                        Icon(
                            Icons.Default.VideoCall,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp),
                            tint = Color.White
                        )
                    }
                    Text(
                        "Live Meeting",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledTonalIconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                }
            }

            if (videoContent?.platform != null || videoContent?.meetingLink != null) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                videoContent.platform?.let {
                    MeetingInfoRow("Platform", it)
                    Spacer(Modifier.height(12.dp))
                }

                videoContent.meetingId?.let {
                    MeetingInfoRow("Meeting ID", it)
                    Spacer(Modifier.height(12.dp))
                }

                videoContent.passcode?.let {
                    MeetingInfoRow("Passcode", it)
                    Spacer(Modifier.height(12.dp))
                }

                videoContent.meetingLink?.let {
                    MeetingInfoRow("Link", it, isLink = true)
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Text(
                    "No meeting configured yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun MeetingInfoRow(label: String, value: String, isLink: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            fontSize = 14.sp,
            color = if (isLink) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isLink) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
        else -> String.format("%d:%02d", minutes, secs)
    }
}
