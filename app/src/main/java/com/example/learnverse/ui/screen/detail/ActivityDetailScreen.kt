// ActivityDetailScreen.kt
package com.example.learnverse.ui.screen.detail

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
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
import com.example.learnverse.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun ActivityDetailScreen(
    activityId: String,
    activitiesViewModel: ActivitiesViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val activity = activitiesViewModel.getActivityById(activityId)
    val isEnrolled = activitiesViewModel.isEnrolled(activityId)
    val userRole by authViewModel.currentUserRole.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val context = LocalContext.current

    // Check if current user is the tutor
    val isTutor = userRole == "TUTOR" && activity?.tutorId == currentUserId

    // Pager state for tabs
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    // Banner upload launcher
    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            activitiesViewModel.uploadBanner(activityId, it, context)
        }
    }

    if (activity == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Activity not found")
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            // Show FABs only for tutors
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

                    // Add Meeting Link FAB (visible in Meeting tab)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Banner & Profile Section
            item {
                BannerAndProfileSection(
                    activity = activity,
                    isTutor = isTutor,
                    onBannerEdit = { bannerLauncher.launch("image/*") },
                    onProfileClick = {
                        navController.navigate("tutorProfile/${activity.tutorId}")
                    }
                )
            }

            // Tab Row
            item {
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
            }

            // Tab Content
            item {
                HorizontalPager(
                    count = 4,
                    state = pagerState,
                    modifier = Modifier.height(600.dp)
                ) { page ->
                    when (page) {
                        0 -> ActivityInfoTab(
                            activity = activity,
                            isEnrolled = isEnrolled,
                            onEnroll = {
                                if (isEnrolled) {
                                    // Show toast
                                    android.widget.Toast.makeText(
                                        context,
                                        "Already enrolled!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    activitiesViewModel.enrollInActivity(activityId)
                                }
                            }
                        )
                        1 -> VideosTab(activity, navController)
                        2 -> MeetingTab(activity, context)
                        3 -> ReviewsTab(activity, authViewModel, activitiesViewModel)
                    }
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
            .height(300.dp) // ✅ Increased height for proper spacing
    ) {
        // Banner Image (200dp height, leaving 100dp for profile overlap)
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
                // Placeholder when no banner
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

            // ✅ Edit Banner Button (always visible for tutors, positioned properly)
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

        // ✅ Profile Picture & Info Section (positioned at bottom, half overlapping banner)
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
                // Profile Picture (half inside banner, half outside)
                Box(
                    modifier = Modifier
                        .size(120.dp) // ✅ Larger size for better visibility
                        .offset(y = (-60).dp) // ✅ Half goes up into banner
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

            // ✅ Tutor Name & Contact Info (below profile picture)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activity.tutorName ?: "Unknown Tutor",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Contact Info - Email & Phone with proper data source
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Email (from contactInfo or fallback to tutor verification email)
                val emailToShow = activity.contactInfo?.email
                    ?: activity.instructorDetails?.let { "No email provided" }

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
                        text = emailToShow ?: "No email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ✅ WhatsApp Number (from contactInfo)
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

