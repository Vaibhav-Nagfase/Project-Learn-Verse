package com.example.learnverse.ui.screen.my_course

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import com.example.learnverse.data.model.*
import com.example.learnverse.viewmodel.StudentCourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCourseDetailScreen(
    activityId: String,
    navController: NavController,
    viewModel: StudentCourseViewModel
) {
    val courseData by viewModel.courseData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Videos", "Resources", "Meeting")

    LaunchedEffect(activityId) {
        viewModel.fetchCourseDetails(activityId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        courseData?.activityTitle ?: "Course",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                courseData != null -> {
                    val course = courseData!!

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header with banner and stats
                        item {
                            CourseHeader(
                                course = course,
                                onContinueClick = {
                                    course.continueFromVideoId?.let { videoId ->
                                        navController.navigate("video_player/${activityId}/${videoId}")
                                    }
                                }
                            )
                        }

                        // Tab Row
                        item {
                            TabRow(
                                selectedTabIndex = selectedTab,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        text = {
                                            Text(
                                                title,
                                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // ✅ FIXED: Content based on selected tab
                        when (selectedTab) {
                            0 -> {
                                // Videos Tab
                                item {
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Course Videos (${course.completedVideos}/${course.totalVideos})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }

                                if (course.videos.isEmpty()) {
                                    item {
                                        EmptyContentMessage("No videos available yet")
                                    }
                                } else {
                                    items(course.videos) { video ->
                                        VideoItemCard(
                                            video = video,
                                            onClick = {
                                                navController.navigate("video_player/${activityId}/${video.videoId}")
                                            }
                                        )
                                    }
                                }
                            }

                            1 -> {
                                // ✅ FIXED Resources Tab
                                item {
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Course Resources (${course.completedResourcesCount}/${course.totalResources})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }

                                if (course.resources.isEmpty()) {
                                    item {
                                        EmptyContentMessage("No resources available yet")
                                    }
                                } else {
                                    items(
                                        items = course.resources,
                                        key = { it.resourceId }
                                    ) { resource ->
                                        ResourceItemCard(
                                            resource = resource,
                                            activityId = activityId,
                                            viewModel = viewModel
                                        )
                                    }
                                }
                            }

                            2 -> {
                                // Meeting Tab
                                item {
                                    Spacer(Modifier.height(16.dp))
                                    course.meetingInfo?.let { meeting ->
                                        MeetingInfoCard(meetingInfo = meeting)
                                    } ?: run {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    Icons.Default.VideoCall,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(64.dp),
                                                    tint = Color.Gray
                                                )
                                                Spacer(Modifier.height(16.dp))
                                                Text(
                                                    "No meeting information available",
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Bottom spacing
                        item {
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseHeader(
    course: StudentCourseViewResponse,
    onContinueClick: () -> Unit
) {
    Column {
        // Banner Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (!course.bannerImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = course.bannerImageUrl,
                    contentDescription = "Course Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    )
                }
            }
        }

        // Course Info
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = course.activityTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "by ${course.tutorName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(8.dp))

            // Description
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(16.dp))

            // Progress Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Overall Progress",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${course.completionPercentage.toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (course.completionPercentage / 100).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )

                    Spacer(Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            icon = Icons.Default.PlayArrow,
                            value = "${course.completedVideos}/${course.totalVideos}",
                            label = "Videos"
                        )
                        StatItem(
                            icon = Icons.Default.Description,
                            value = "${course.completedResourcesCount}/${course.totalResources}",
                            label = "Resources"
                        )
                    }
                }
            }

            // Continue Watching Card
            if (course.continueFromVideoId != null && course.continueFromVideoTitle != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onContinueClick),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Continue Watching",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                course.continueFromVideoTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun VideoItemCard(
    video: VideoWithProgress,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(100.dp, 70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!video.thumbnailUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = "Video Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Play icon overlay
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = Color.White
                    )
                }

                // Duration badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.8f)
                ) {
                    Text(
                        formatDuration(video.duration),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Video Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    if (video.completed) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Description
                if (video.description.isNotEmpty()) {
                    Text(
                        text = video.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                }

                // Progress indicator
                if (!video.completed && video.progressPercentage > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { (video.progressPercentage / 100).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${video.progressPercentage.toInt()}%",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (!video.completed) {
                    Text(
                        "Not started",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Resources count
                if (video.resources.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${video.resources.size} resource${if (video.resources.size > 1) "s" else ""}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Arrow icon
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Play video",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ResourceItemCard(
    resource: ResourceWithProgress,
    activityId: String,
    viewModel: StudentCourseViewModel
) {
    val context = LocalContext.current
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Dismiss error after 3 seconds
    LaunchedEffect(showError) {
        if (showError) {
            kotlinx.coroutines.delay(3000)
            showError = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable {
                try {
                    // Validate URL
                    if (resource.url.isBlank()) {
                        errorMessage = "Resource URL is empty"
                        showError = true
                        return@clickable
                    }

                    // Create intent to open URL
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(resource.url)
                        // Add flags to ensure it opens properly
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    // Check if there's an app to handle this intent
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)

                        // Mark as downloaded/accessed
                        viewModel.markResourceDownloaded(activityId, resource.resourceId)
                    } else {
                        errorMessage = "No app found to open this resource"
                        showError = true
                    }
                } catch (e: Exception) {
                    errorMessage = "Failed to open: ${e.message}"
                    showError = true
                    android.util.Log.e("ResourceCard", "Error opening resource", e)
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (resource.completed)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Resource type icon
                val icon = when (resource.type.lowercase()) {
                    "pdf" -> Icons.Default.PictureAsPdf
                    "link", "url" -> Icons.Default.Link
                    "document", "doc", "docs" -> Icons.Default.Description
                    "code" -> Icons.Default.Code
                    "image", "img" -> Icons.Default.Image
                    else -> Icons.Default.AttachFile
                }

                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Resource info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = resource.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))

                    // URL preview (truncated)
                    Text(
                        text = resource.url.take(50) + if (resource.url.length > 50) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = resource.type.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Completed badge
                        if (resource.completed) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "✓ Completed",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Open icon
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = "Open resource",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Error message
            AnimatedVisibility(
                visible = showError,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingInfoCard(
    meetingInfo: MeetingInfo
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var passcodeVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.VideoCall,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Meeting Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            // Platform
            if (!meetingInfo.platform.isNullOrEmpty()) {
                InfoRow(
                    label = "Platform",
                    value = meetingInfo.platform,
                    icon = Icons.Default.Computer
                )
                Spacer(Modifier.height(12.dp))
            }

            // Meeting ID
            if (!meetingInfo.meetingId.isNullOrEmpty()) {
                InfoRow(
                    label = "Meeting ID",
                    value = meetingInfo.meetingId,
                    icon = Icons.Default.Tag,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(meetingInfo.meetingId))
                    }
                )
                Spacer(Modifier.height(12.dp))
            }

            // Passcode
            if (!meetingInfo.passcode.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Passcode",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (passcodeVisible) meetingInfo.passcode else "••••••",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(
                        onClick = { passcodeVisible = !passcodeVisible }
                    ) {
                        Icon(
                            if (passcodeVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle passcode visibility",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(meetingInfo.passcode))
                        }
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy passcode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Join button
            if (!meetingInfo.meetingLink.isNullOrEmpty()) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(meetingInfo.meetingLink))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handle error
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.VideoCall,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Join Meeting",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Gray
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        if (onCopy != null) {
            IconButton(onClick = onCopy) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyContentMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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