package com.example.learnverse.ui.screen.tutor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.learnverse.data.model.CommunityPost
import com.example.learnverse.data.model.FollowStats
import com.example.learnverse.viewmodel.*
import com.example.learnverse.ui.screen.community.CommunityPostCard
import com.example.learnverse.data.remote.ApiClient
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.CommunityRepository

@OptIn(ExperimentalMaterial3Api::class)
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

    // --- Tutor Details (from ActivitiesViewModel) ---
    val tutorActivities = activitiesViewModel.activities.collectAsState().value
        .filter { it.tutorId == tutorId }
    val tutorInfo = tutorActivities.firstOrNull()
    val instructorDetails = tutorInfo?.instructorDetails

    val listState = rememberLazyListState()
    var postToDelete by remember { mutableStateOf<CommunityPost?>(null) }

    // Load tutor posts + profile
    LaunchedEffect(tutorId) {
        tutorProfileViewModel.loadTutorProfile(tutorId)
    }

    // Infinite scroll for posts
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: -1
                val totalItemCount = listState.layoutInfo.totalItemsCount
                if (lastVisibleItemIndex >= totalItemCount - 2 && totalItemCount > 0 && uiState is TutorProfileUiState.Success) {
                    tutorProfileViewModel.loadMoreTutorPosts(tutorId)
                }
            }
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
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // --- Header Section (Enhanced UI) ---
                        item {
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
                                            text = tutorInfo?.tutorName ?: "Unknown Tutor",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        // Combined Stats (Follow + Social Proof)
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
                                            }
                                        }

                                        // Follow/Unfollow Button
                                        if (currentUserId != null && tutorId != currentUserId) {
                                            Button(
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
                        }

                        // --- Tutor Bio / Details Section ---
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
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            specializations.take(5).forEach {
                                                SuggestionChip(onClick = {}, label = { Text(it) })
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // --- Tutor Posts ---
                        if (state.posts.isNotEmpty()) {
                            item {
                                ProfileSection("Posts") { Divider(Modifier.padding(vertical = 8.dp)) }
                            }
                            items(state.posts, key = { it.id }) { post ->
                                val isLiked =
                                    currentUserId != null && post.likedBy.contains(currentUserId)
                                CommunityPostCard(
                                    post = post,
                                    currentUserId = currentUserId,
                                    isLiked = isLiked,
                                    isFollowed = state.isCurrentUserFollowing,
                                    onLikeClick = { tutorProfileViewModel.likeOrUnlikePost(post.id) },
                                    onCommentClick = { navController.navigate("postDetail/${post.id}") },
                                    onFollowClick = {},
                                    onUnfollowClick = {},
                                    onAuthorClick = {},
                                    onPostClick = {},
                                    onEditClick = { navController.navigate("createPost?postId=${post.id}") },
                                    onDeleteClick = { postToDelete = post }
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

                        // --- Courses Section ---
                        item {
                            ProfileSection("Courses by ${tutorInfo?.tutorName ?: "Tutor"}") {
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
