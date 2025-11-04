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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.CommunityPost
import com.example.learnverse.viewmodel.*
import com.example.learnverse.ui.screen.community.EnhancedCommunityPostCard
import com.example.learnverse.data.remote.ApiClient
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.CommunityRepository
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

    LaunchedEffect(tutorId) {
        tutorProfileViewModel.loadTutorProfile(tutorId)
    }

    // 🔥 FIXED: Reduced collapsing range for better visibility
    val maxHeaderHeight = 350.dp
    val minHeaderHeight = 80.dp // Keep mini header visible
    val collapsingRange = with(LocalDensity.current) { (maxHeaderHeight - minHeaderHeight).toPx() }
    var headerOffsetHeightPx by remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = headerOffsetHeightPx + delta
                val previousOffset = headerOffsetHeightPx
                headerOffsetHeightPx = newOffset.coerceIn(-collapsingRange, 0f)
                val consumed = headerOffsetHeightPx - previousOffset
                return Offset(0f, consumed)
            }
        }
    }

    val collapseFraction = (-headerOffsetHeightPx / collapsingRange).coerceIn(0f, 1f)

    when (val state = uiState) {
        is TutorProfileUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is TutorProfileUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        is TutorProfileUiState.Success -> {
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

            val tabs = listOf("About", "Posts")
            val pagerState = rememberPagerState()
            val coroutineScope = rememberCoroutineScope()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                // 🔥 NEW: Status bar padding for camera notch
                Spacer(modifier = Modifier.statusBarsPadding())

                // 🔥 FIXED: Collapsing Header with mini profile staying visible
                CollapsingTutorHeader(
                    instructorDetails = instructorDetails,
                    tutorName = tutorName,
                    followersCount = state.followStats?.followersCount ?: 0,
                    followingCount = state.followStats?.followingCount ?: 0,
                    studentsCount = instructorDetails?.socialProof?.totalStudentsTaught ?: 0,
                    coursesCount = instructorDetails?.socialProof?.coursesCount ?: 0,
                    isFollowing = state.isCurrentUserFollowing,
                    currentUserId = currentUserId,
                    tutorId = tutorId,
                    collapseFraction = collapseFraction,
                    maxHeaderHeight = maxHeaderHeight,
                    minHeaderHeight = minHeaderHeight,
                    onBackClick = { navController.navigateUp() },
                    onFollowClick = {
                        if (state.isCurrentUserFollowing) {
                            tutorProfileViewModel.unfollowThisTutor()
                        } else {
                            tutorProfileViewModel.followThisTutor()
                        }
                    }
                )

                // Sticky Tabs
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
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

                // Tab Content
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
                            navController = navController
                        )
                        1 -> TutorPostsTab(
                            posts = posts,
                            currentUserId = currentUserId,
                            isCurrentUserFollowing = state.isCurrentUserFollowing,
                            navController = navController,
                            viewModel = tutorProfileViewModel
                        )
                    }
                }
            }
        }
    }
}

// 🔥 FIXED: Collapsing Header with mini profile visible when collapsed
@Composable
fun CollapsingTutorHeader(
    instructorDetails: Activity.InstructorDetails?,
    tutorName: String,
    followersCount: Int,
    followingCount: Int,
    studentsCount: Int,
    coursesCount: Int,
    isFollowing: Boolean,
    currentUserId: String?,
    tutorId: String,
    collapseFraction: Float,
    maxHeaderHeight: androidx.compose.ui.unit.Dp,
    minHeaderHeight: androidx.compose.ui.unit.Dp,
    onBackClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    val headerHeight = lerp(maxHeaderHeight, minHeaderHeight, collapseFraction)
    val profileSize = lerp(100.dp, 40.dp, collapseFraction)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // 🔥 EXPANDED STATE: Full profile (visible when not scrolled)
        if (collapseFraction < 0.5f) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .alpha(1f - (collapseFraction * 2)), // Fades out faster
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture
                AsyncImage(
                    model = instructorDetails?.profileImage,
                    contentDescription = "Tutor Profile",
                    modifier = Modifier
                        .size(profileSize)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Name
                Text(
                    text = tutorName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // Bio
                instructorDetails?.bio?.let { bio ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                // Stats Row
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Default.People,
                        value = followersCount.toString(),
                        label = "Followers"
                    )
                    StatItem(
                        icon = Icons.Default.PersonAdd,
                        value = followingCount.toString(),
                        label = "Following"
                    )
                    StatItem(
                        icon = Icons.Default.School,
                        value = studentsCount.toString(),
                        label = "Students"
                    )
                    StatItem(
                        icon = Icons.Default.MenuBook,
                        value = coursesCount.toString(),
                        label = "Courses"
                    )
                }

                // 🔥 FIXED: Follow Button always visible in expanded state
                if (currentUserId != null && tutorId != currentUserId) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(40.dp)
                    ) {
                        Icon(
                            if (isFollowing) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isFollowing) "Following" else "Follow",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 🔥 COLLAPSED STATE: Mini profile (visible when scrolled)
        if (collapseFraction > 0.5f) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .padding(horizontal = 56.dp, vertical = 8.dp) // Space for back button
                    .alpha((collapseFraction - 0.5f) * 2), // Fades in when collapsing
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mini profile info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = instructorDetails?.profileImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = tutorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 🔥 FIXED: Follow button in collapsed state
                if (currentUserId != null && tutorId != currentUserId) {
                    Button(
                        onClick = onFollowClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            if (isFollowing) "Following" else "Follow",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// 🔄 EXISTING COMPONENTS (unchanged)

@Composable
fun TutorAboutTab(
    instructorDetails: com.example.learnverse.data.model.Activity.InstructorDetails?,
    tutorActivities: List<Activity>,
    tutorName: String,
    navController: NavController
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                            items(specializations) { specialization ->
                                SuggestionChip(onClick = {}, label = { Text(specialization) })
                            }
                        }
                    }
                }
            }
        }

        item {
            ProfileSection("Courses by $tutorName") {
                Text("${tutorActivities.size} courses available")
            }
        }
        items(tutorActivities) { activity ->
            TutorCourseCard(
                activity = activity,
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
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (posts.isNotEmpty()) {
            items(posts, key = { it.id }) { post ->
                val isLiked = currentUserId != null && post.likedBy.contains(currentUserId)
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
                    onDeleteClick = {}
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "This tutor hasn't posted anything yet.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

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
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .width(100.dp)
                    .aspectRatio(16f / 9f)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    activity.title ?: "Untitled",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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
