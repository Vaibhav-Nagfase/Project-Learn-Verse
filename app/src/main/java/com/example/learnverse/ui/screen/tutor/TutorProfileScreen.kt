package com.example.learnverse.ui.screen.tutor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.CommunityPost
import com.example.learnverse.data.model.FollowStats
import com.example.learnverse.viewmodel.*
import com.example.learnverse.data.remote.ApiClient
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.CommunityRepository
import com.example.learnverse.ui.screen.community.EnhancedCommunityPostCard
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun TutorProfileScreen(
    tutorId: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    activitiesViewModel: ActivitiesViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext

    // --- Setup repositories + TutorProfileViewModel ---
    val authRepository = remember {
        AuthRepository(
            ApiClient.getInstance(context).retrofit.create(com.example.learnverse.data.remote.ApiService::class.java),
            context
        )
    }
    val communityRepository = remember {
        CommunityRepository(
            ApiClient.getInstance(context).retrofit.create(com.example.learnverse.data.remote.ApiService::class.java)
        )
    }

    val tutorProfileViewModel: TutorProfileViewModel = viewModel(
        factory = TutorProfileViewModelFactory(communityRepository, authRepository)
    )

    val uiState by tutorProfileViewModel.uiState.collectAsStateWithLifecycle()
    val currentUserId by authViewModel.currentUserId.collectAsStateWithLifecycle()


    // Load tutor posts + profile
    LaunchedEffect(tutorId) {
        tutorProfileViewModel.loadTutorProfile(tutorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutor Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is TutorProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is TutorProfileUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }

                is TutorProfileUiState.Success -> {

                    // --- Data Caching Logic (Unchanged) ---
                    val feedActivities by activitiesViewModel.activities.collectAsStateWithLifecycle()
                    val homeFeed by activitiesViewModel.homeFeed.collectAsStateWithLifecycle()
                    val allActivities = remember(feedActivities, homeFeed) {
                        buildList {
                            addAll(feedActivities)
                            homeFeed?.let {
                                addAll(it.recommended)
                                addAll(it.popular)
                                addAll(it.topRated)
                                addAll(it.newActivities)
                                addAll(it.featured)
                            }
                        }.distinctBy { it.id }
                    }
                    val tutorActivities = allActivities.filter { it.tutorId == tutorId }
                    val tutorInfo = tutorActivities.firstOrNull()
                    val instructorDetails = tutorInfo?.instructorDetails
                    val posts = state.posts
                    val tutorName = tutorInfo?.tutorName ?: posts.firstOrNull()?.authorName ?: "Tutor"
                    // --- End Data Caching Logic ---


                    // --- Tab Layout (Unchanged) ---
                    val tabs = listOf("About", "Posts")
                    val pagerState = rememberPagerState()
                    val coroutineScope = rememberCoroutineScope()

                    Column(modifier = Modifier.fillMaxSize()) {

                        // Header section
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    AsyncImage(
                                        model = instructorDetails?.profileImage,
                                        contentDescription = "Tutor Profile",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .border(
                                                4.dp,
                                                MaterialTheme.colorScheme.surface,
                                                CircleShape
                                            ),
                                        contentScale = ContentScale.Crop
                                    )

                                    Text(
                                        text = tutorName,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                        StatItem(
                                            icon = Icons.Default.People,
                                            value = "${state.followStats?.followersCount ?: 0}",
                                            label = "Followers"
                                        )
                                        StatItem(
                                            icon = Icons.Default.PersonAdd,
                                            value = "${state.followStats?.followingCount ?: 0}",
                                            label = "Following"
                                        )
                                        instructorDetails?.socialProof?.let { proof ->
                                            StatItem(
                                                icon = Icons.Default.School,
                                                value = "${proof.totalStudentsTaught ?: 0}",
                                                label = "Students"
                                            )
                                            StatItem(
                                                icon = Icons.Default.MenuBook,
                                                value = "${proof.coursesCount ?: 0}",
                                                label = "Courses"
                                            )
                                        }
                                    }

                                    // Follow Button (Unchanged)
                                    if (currentUserId != null && tutorId != currentUserId) {
                                        OutlinedButton(
                                            onClick = {
                                                if (state.isCurrentUserFollowing)
                                                    tutorProfileViewModel.unfollowThisTutor()
                                                else tutorProfileViewModel.followThisTutor()
                                            }
                                        ) {
                                            Text(if (state.isCurrentUserFollowing) "Unfollow" else "Follow")
                                        }
                                    }
                                }
                            }
                        }

                        // TabRow (Unchanged)
                        TabRow(selectedTabIndex = pagerState.currentPage) {
                            tabs.forEachIndexed { index, title ->
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

                        // HorizontalPager
                        HorizontalPager(
                            count = tabs.size,
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (page) {
                                0 -> TutorAboutTab(
                                    instructorDetails = instructorDetails,
                                    tutorActivities = tutorActivities,
                                    tutorName = tutorName,
                                    navController = navController // ✅ Pass NavController
                                )
                                1 -> TutorPostsTab(
                                    posts = posts,
                                    currentUserId = currentUserId,
                                    isCurrentUserFollowing = state.isCurrentUserFollowing,
                                    navController = navController,
                                    viewModel = tutorProfileViewModel
                                    // --- ⛔️ DELETED `onDeleteClick` ---
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun TutorAboutTab(
    instructorDetails: com.example.learnverse.data.model.Activity.InstructorDetails?,
    tutorActivities: List<Activity>,
    tutorName: String,
    navController: NavController // ✅ Added NavController parameter
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Info Sections (Unchanged) ---
        instructorDetails?.bio?.let { bio ->
            item { ProfileSection("About") { Text(bio) } }
        }
        instructorDetails?.qualifications?.let { qualifications ->
            if (qualifications.isNotEmpty()) {
                item {
                    ProfileSection("Qualifications") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            qualifications.forEach {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(it)
                                }
                            }
                        }
                    }
                }
            }
        }
        instructorDetails?.experience?.let { exp ->
            item { ProfileSection("Experience") { Text(exp) } }
        }
        instructorDetails?.specializations?.let { specializations ->
            if (specializations.isNotEmpty()) {
                item {
                    ProfileSection("Specializations") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Changed forEach to items() for LazyRow
                            items(specializations) { specialization ->
                                SuggestionChip(onClick = {}, label = { Text(specialization) })
                            }
                        }
                    }
                }
            }
        }

        // --- Courses Section ---
        item {
            ProfileSection("Courses by $tutorName") {
                Text("${tutorActivities.size} courses available")
            }
        }
        items(tutorActivities) { activity ->
            TutorCourseCard(
                activity = activity,
                // ✅ Pass the navigation click action
                onClick = { navController.navigate("activityDetail/${activity.id}") }
            )
        }
    }
}

@Composable
fun TutorPostsTab(
    posts: List<CommunityPost>,
    currentUserId: String?,
    isCurrentUserFollowing: Boolean,
    navController: NavController,
    viewModel: TutorProfileViewModel
    // --- ⛔️ DELETED `onDeleteClick` ---
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (posts.isNotEmpty()) {
            items(posts, key = { it.id }) { post ->
                val isLiked =
                    currentUserId != null && post.likedBy.contains(currentUserId)
                EnhancedCommunityPostCard(
                    post = post,
                    currentUserId = currentUserId,
                    isLiked = isLiked,
                    isFollowed = isCurrentUserFollowing,
                    onLikeClick = { viewModel.likeOrUnlikePost(post.id) },
                    onCommentClick = { navController.navigate("postDetail/${post.id}") },
                    onFollowClick = {},
                    onUnfollowClick = {},
                    onAuthorClick = {},
                    onPostClick = {},
                    onEditClick = { navController.navigate("createPost?postId=${post.id}") },
                    onDeleteClick = {} // ✅ Set to an empty lambda
                )
            }
        } else {
            item {
                Text(
                    "This tutor hasn't posted anything yet.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// --- Reusable Helper Composables (Unchanged) ---

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme. typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
fun TutorCourseCard(activity: com.example.learnverse.data.model.Activity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.width(100.dp).aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(activity.title ?: "Untitled", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    activity.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                activity.pricing?.let {
                    Text(
                        "₹${it.price}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}