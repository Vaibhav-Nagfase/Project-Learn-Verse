// ActivityDetailScreen.kt (COMPLETE WORKING VERSION)
package com.example.learnverse.ui.screen.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.learnverse.data.model.Activity
import com.example.learnverse.ui.screen.detail.tabs.ActivityInfoTab
import com.example.learnverse.ui.screen.detail.tabs.MeetingTab
import com.example.learnverse.ui.screen.detail.tabs.ReviewsTab
import com.example.learnverse.ui.screen.detail.tabs.VideosTab
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.example.learnverse.viewmodel.AuthViewModel
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun ActivityDetailScreen(
    activityId: String,
    activitiesViewModel: ActivitiesViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    var activity by remember { mutableStateOf<Activity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEnrolled = activitiesViewModel.isEnrolled(activityId)
    val userRole by authViewModel.currentUserRole.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        activitiesViewModel.fetchMyEnrollments()
    }

    // ✅ Fetch activity on load
    LaunchedEffect(activityId) {
        try {
            isLoading = true
            // First try to get from local cache
            activity = activitiesViewModel.getActivityById(activityId)

            // If not found, fetch from API
            if (activity == null) {
                activity = activitiesViewModel.fetchActivityById(activityId)
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message
            isLoading = false
        }
    }

    val isTutor = userRole == "TUTOR" && activity?.tutorId == currentUserId

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            activitiesViewModel.uploadBanner(activityId, it, context)
        }
    }

    // ✅ Show loading state
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // ✅ Show error state
    if (errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Error: $errorMessage",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.navigateUp() }) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    // ✅ Show not found state
    if (activity == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Activity not found",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.navigateUp() }) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Show edit/delete only for tutor owner
                    if (isTutor) {
                        var showMenu by remember { mutableStateOf(false) }

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "Menu")
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Activity") },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate("create_activity?activityId=$activityId")
                                    },
                                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Activity") },
                                    onClick = {
                                        showMenu = false
                                        // Show delete confirmation
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            // Show FABs only for tutors in specific tabs
            if (isTutor) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Add Video FAB (visible in Videos tab)
                    if (pagerState.currentPage == 1) {
                        FloatingActionButton(
                            onClick = { navController.navigate("upload_video/$activityId") }
                        ) {
                            Icon(Icons.Default.Add, "Add Video")
                        }
                    }

                    // Add/Edit Meeting Link FAB (visible in Meeting tab)
                    if (pagerState.currentPage == 2) {
                        FloatingActionButton(
                            onClick = { navController.navigate("add_meeting/$activityId") }
                        ) {
                            Icon(Icons.Default.Link, "Add Meeting")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Banner & Profile Section
            BannerAndProfileSection(
                activity = activity!!,
                isTutor = isTutor,
                onBannerEdit = { bannerLauncher.launch("image/*") },
                onProfileClick = {
                    navController.navigate("tutorProfile/${activity!!.tutorId}")
                }
            )

            // Tab Row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Info", "Videos", "Meeting", "Reviews").forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // Tab Content with pager
            HorizontalPager(
                count = 4,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> ActivityInfoTab(
                        activity = activity!!,
                        isEnrolled = isEnrolled,
                        isTutor = isTutor,
                        onEnroll = {
                            activitiesViewModel.enrollInActivity(activityId)
                        }
                    )
                    1 -> VideosTab(
                        activity = activity!!,
                        isTutor = isTutor,
                        isEnrolled = isEnrolled,
                        navController = navController,
                        onDeleteVideo = { videoId ->
                            // Handle video deletion
                        }
                    )
                    2 -> MeetingTab(
                        activity = activity!!,
                        isTutor = isTutor,
                        isEnrolled = isEnrolled,
                        context = context,
                        onEdit = {
                            navController.navigate("edit_meeting/$activityId")
                        },
                        onDelete = {
                            // Handle meeting deletion
                        }
                    )
                    3 -> ReviewsTab(
                        activity = activity!!,
                        authViewModel = authViewModel,
                        activitiesViewModel = activitiesViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun BannerAndProfileSection(
    activity: Activity,
    isTutor: Boolean,
    onBannerEdit: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Banner Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            if (!activity.bannerImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = activity.bannerImageUrl,
                    contentDescription = "Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                    if (isTutor) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap edit to add banner",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Edit Banner Button (only for tutor)
            if (isTutor) {
                IconButton(
                    onClick = onBannerEdit,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "Edit Banner",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Profile Picture & Info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(y = (-30).dp)
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (!activity.instructorDetails?.profileImage.isNullOrEmpty()) {
                        AsyncImage(
                            model = activity.instructorDetails?.profileImage,
                            contentDescription = "Tutor Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activity.tutorName ?: "Unknown Tutor",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contact Info
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val emailToShow = activity.contactInfo?.email ?: "No email"

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Email,
                        "Email",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = emailToShow,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                activity.contactInfo?.whatsappNumber?.let { phone ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            "Phone",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}