// ActivityDetailScreen.kt (COMPLETE WITH COLLAPSING ANIMATION)
package com.example.learnverse.ui.screen.detail

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    // ✅ Observe activity from StateFlow (THIS IS THE KEY CHANGE)
    val activity by activitiesViewModel.selectedActivity.collectAsState()
    val isRefreshing by activitiesViewModel.isRefreshing.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEnrolled = activitiesViewModel.isEnrolled(activityId)
    val userRole by authViewModel.currentUserRole.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val context = LocalContext.current

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    // Collapsing state
    val maxBannerHeight = 200.dp
    val minBannerHeight = 80.dp
    val collapsingRange = with(LocalDensity.current) { (maxBannerHeight - minBannerHeight).toPx() }

    var bannerOffsetHeightPx by remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = bannerOffsetHeightPx + delta
                val previousOffset = bannerOffsetHeightPx
                bannerOffsetHeightPx = newOffset.coerceIn(-collapsingRange, 0f)
                val consumed = bannerOffsetHeightPx - previousOffset
                return Offset(0f, consumed)
            }
        }
    }

    val collapseFraction = (-bannerOffsetHeightPx / collapsingRange).coerceIn(0f, 1f)

    LaunchedEffect(Unit) {
        activitiesViewModel.fetchMyEnrollments()
    }

    // ✅ FIXED: Simplified loading logic
    LaunchedEffect(activityId) {
        try {
            isLoading = true
            activitiesViewModel.fetchActivityById(activityId)
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message
            isLoading = false
        }
    }

    val isTutor = userRole == "TUTOR" && activity?.tutorId == currentUserId

    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { activitiesViewModel.uploadBanner(activityId, it, context) }
    }

    // ✅ Loading state
    if (isLoading || (activity == null && !isLoading)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // ✅ Error state
    if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Error, null, Modifier.size(64.dp), MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Text("Error: $errorMessage", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.navigateUp() }) { Text("Go Back") }
            }
        }
        return
    }

    // ✅ Activity not found
    if (activity == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SearchOff, null, Modifier.size(64.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Text("Activity not found", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.navigateUp() }) { Text("Go Back") }
            }
        }
        return
    }

    // ✅ Main Content (activity is guaranteed non-null here)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Top App Bar
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                if (isTutor) {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
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
                                onClick = { showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Collapsing Banner & Profile
        CollapsingBannerSection(
            activity = activity!!, // ✅ Safe because we checked null above
            isTutor = isTutor,
            collapseFraction = collapseFraction,
            onBannerEdit = { bannerLauncher.launch("image/*") },
            onProfileClick = { navController.navigate("tutorProfile/${activity!!.tutorId}") }
        )

        // Tabs
        TabRow(selectedTabIndex = pagerState.currentPage) {
            listOf("Info", "Videos", "Meeting", "Reviews").forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title) }
                )
            }
        }

        // Tab Content - FABs are handled INSIDE each tab
        HorizontalPager(count = 4, state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> ActivityInfoTab(activity!!, isEnrolled, isTutor) {
                    activitiesViewModel.enrollInActivity(activityId)
                }
                1 -> VideosTab(
                    activity = activity!!,
                    isTutor = isTutor,
                    isEnrolled = isEnrolled,
                    navController = navController,
                    viewModel = activitiesViewModel
                )
                2 -> MeetingTab(
                    activity = activity!!,
                    isTutor = isTutor,
                    isEnrolled = isEnrolled,
                    viewModel = activitiesViewModel
                )
                3 -> ReviewsTab(activity!!, authViewModel, activitiesViewModel)
            }
        }
    }
}

@Composable
fun CollapsingBannerSection(
    activity: Activity,
    isTutor: Boolean,
    collapseFraction: Float,
    onBannerEdit: () -> Unit,
    onProfileClick: () -> Unit
) {
    val maxBannerHeight = 200.dp
    val minBannerHeight = 80.dp
    val maxProfileSize = 120.dp
    val minProfileSize = 60.dp

    val bannerHeight = lerp(maxBannerHeight, minBannerHeight, collapseFraction)
    val profileSize = lerp(maxProfileSize, minProfileSize, collapseFraction)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight)
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
                            Icons.Default.Image, null,
                            Modifier.size(64.dp * (1f - collapseFraction * 0.5f)),
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                        if (isTutor) {
                            AnimatedVisibility(visible = collapseFraction < 0.5f) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Tap edit to add banner",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                if (isTutor) {
                    IconButton(
                        onClick = onBannerEdit,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, "Edit", Modifier.size(20.dp))
                    }
                }
            }

            // Spacing for profile overlap
            Spacer(modifier = Modifier.height(profileSize / 2 + 16.dp))
        }

        // Profile & Info - Always below profile picture
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = bannerHeight - profileSize / 2)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(profileSize)
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                if (!activity.instructorDetails?.profileImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = activity.instructorDetails?.profileImage,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, Modifier.size(profileSize / 2))
                }
            }

            // Name and contact info - always below profile
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    activity.tutorName ?: "Unknown",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    activity.contactInfo?.email?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Email, null, Modifier.size(18.dp), MaterialTheme.colorScheme.primary)
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    activity.contactInfo?.whatsappNumber?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, null, Modifier.size(18.dp), MaterialTheme.colorScheme.primary)
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

private fun lerp(start: Dp, stop: Dp, fraction: Float): Dp {
    return start + (stop - start) * fraction
}