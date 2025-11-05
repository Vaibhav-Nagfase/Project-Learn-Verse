package com.example.learnverse.ui.screen.home

import android.Manifest // Make sure this is imported
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.learnverse.R
import com.example.learnverse.data.model.Activity
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.example.learnverse.viewmodel.AuthViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

// Animation imports
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    activitiesViewModel: ActivitiesViewModel
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val homeFeed by activitiesViewModel.homeFeed.collectAsState()
    val isLoading by remember { derivedStateOf { activitiesViewModel.isLoading } }

    // ADD THIS BLOCK TO AUTOMATICALLY FETCH THE FEED
    LaunchedEffect(Unit) {
        activitiesViewModel.fetchMyFeed()
    }

    // Get the data from the ViewModel
    val recommendedActivities by activitiesViewModel.activities.collectAsStateWithLifecycle()
    val nearbyActivities by remember { derivedStateOf { activitiesViewModel.nearbyActivities } }

    // Set up and launch the location permission request
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    // When the permission status changes, fetch nearby activities if it was granted
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            activitiesViewModel.fetchNearbyActivities(context)
        }
    }

    LaunchedEffect(Unit) {
        activitiesViewModel.fetchHomeFeed()
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val verificationStatus by authViewModel.verificationStatus.collectAsState()
    val hasProfile by authViewModel.hasProfile.collectAsStateWithLifecycle()
    val currentUserName by authViewModel.currentUserName.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                activitiesViewModel.clearData()
                authViewModel.logout()
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // 3. Define the content of the drawer
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        "LearnVerse Menu",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Divider()

                    // --- NEW "MY PROFILE" ITEM ---
                    NavigationDrawerItem(
                        label = { Text(text = "My Profile") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            // Navigate to update profile if it exists, otherwise go to setup
                            if (hasProfile == true) {
                                navController.navigate("my_profile")
                            } else {
                                navController.navigate("profile_setup")
                            }
                        }
                    )

                    Divider()
                    NavigationDrawerItem(
                        label = { Text(text = "My Interests") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("interestManagement")
                        }
                    )

                    Divider() // Optional: for visual separation

                    // --- ADD THIS NEW ITEM ---
                    NavigationDrawerItem(
                        label = { Text(text = "My Courses") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("my_courses")
                        }
                    )

                    // Add spacer to push logout to bottom
                    Spacer(modifier = Modifier.weight(1f))

                    // Logout button (similar to profile screen)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        onClick = {
                            showLogoutDialog = true
                            scope.launch { drawerState.close() }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Logout",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // TODO: Add other drawer items like "Profile", "Settings", etc.
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                    when (hasProfile) {
                        true -> navController.navigate("chat")      // Profile exists, go to chat
                        false -> navController.navigate("profile_setup") // No profile, go to setup
                        null -> { /* Do nothing while profile status is loading */ }
                    }
                    },
                    modifier = Modifier.padding(8.dp),
                    containerColor = Color.White
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.chatbot),
                        contentDescription = "Chatbot",
                        modifier = Modifier.size(40.dp),  // Match your icon's ideal size
                        tint = Color.Unspecified  // Don't apply tint to actual image
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // 1. Header with Profile
                item {
                    HomeHeader(
                        onProfileClick = {
                            scope.launch { drawerState.open() }
                        },
                        currentUserName = currentUserName
                    )
                }

                // 2. Main Search Card
                item {
                    SearchCard(
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                // Navigate to the main feed/search screen with the query
                                navController.navigate("feed?query=$searchQuery")
                            }
                        }
                    )
                }

                // Loading State
                if (isLoading && homeFeed == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Home Feed Content
                homeFeed?.let { feed ->
                    // 3. Categories
                    if (feed.categories.isNotEmpty()) {
                        item {
                            CategoriesSection(
                                categories = feed.categories,
                                onCategoryClick = { category ->
                                    navController.navigate("feed?category=$category")
                                }
                            )
                        }
                    }

                    // 5. Recommended For You
                    if (feed.recommended.isNotEmpty()) {
                        item {
                            HorizontalActivitySection(
                                title = "Recommended For You",
                                activities = feed.recommended,
                                onActivityClick = { activity ->
                                    navController.navigate("activityDetail/${activity.id}")
                                },
                                onSeeAllClick = { navController.navigate("feed") }
                            )
                        }
                    }

                    // 6. Popular This Week
                    if (feed.popular.isNotEmpty()) {
                        item {
                            HorizontalActivitySection(
                                title = "Popular This Week",
                                activities = feed.popular,
                                onActivityClick = { activity ->
                                    navController.navigate("activityDetail/${activity.id}")
                                },
                                onSeeAllClick = { navController.navigate("feed?sort=popular") }
                            )
                        }
                    }

                    // 7. Top Rated
                    if (feed.topRated.isNotEmpty()) {
                        item {
                            HorizontalActivitySection(
                                title = "Top Rated",
                                icon = Icons.Default.Star,
                                activities = feed.topRated,
                                onActivityClick = { activity ->
                                    navController.navigate("activityDetail/${activity.id}")
                                },
                                onSeeAllClick = { navController.navigate("feed?sort=rating") }
                            )
                        }
                    }

                    // 8. New on LearnVerse
                    if (feed.newActivities.isNotEmpty()) {
                        item {
                            HorizontalActivitySection(
                                title = "New on LearnVerse",
                                icon = Icons.Default.FiberNew,
                                activities = feed.newActivities,
                                onActivityClick = { activity ->
                                    navController.navigate("activityDetail/${activity.id}")
                                },
                                onSeeAllClick = { navController.navigate("feed?sort=newest") }
                            )
                        }
                    }
                }
            }
        }
    }
}


// --- Reusable and Updated Composables for HomeScreen ---

@Composable
fun HomeHeader(onProfileClick: () -> Unit, currentUserName: String?) {
    // This would ideally get the user's name from a UserProfile object

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                if (currentUserName == null) "Hello, Kid" else "Hello, $currentUserName",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // 🔥 FIXED: Use remember for interaction source and simpler approach
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.9f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6366F1), // Indigo
                            Color(0xFF8B5CF6), // Purple
                            Color(0xFFEC4899)  // Pink
                        )
                    )
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple()
                ) {
                    onProfileClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentUserName?.firstOrNull()?.uppercase() ?: "K",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchCard(searchQuery: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit) {
    // 1. Permission State for Recording Audio
    val recordAudioPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )

    // 2. Activity Result Launcher for Speech Recognition
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val results: ArrayList<String>? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            // Update the query with the first result, if available
            results?.firstOrNull()?.let { recognizedText ->
                onQueryChange(recognizedText) // Update the state
                onSearch() // Optionally trigger search immediately
            }
        }
    }

    // 3. Function to launch the speech recognizer intent
    val launchSpeechRecognizer: () -> Unit = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            // Handle case where speech recognition is not available
            println("Speech recognition not available: ${e.message}")
            // You might want to show a Snackbar or Toast message here
        }
    }


    // 🔥 ADDED: elevation for shadow effect
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 🔥 FIXED: Text color to black for visibility
            Text(
                "Find an Activity",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black // 🔥 Black text for visibility in both themes
            )
            Text(
                "as per your interest",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black.copy(alpha = 0.7f) // 🔥 Black text with slight transparency
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Search Course") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = {
                        if (recordAudioPermissionState.status.isGranted) {
                            launchSpeechRecognizer()
                        } else {
                            recordAudioPermissionState.launchPermissionRequest()
                        }
                    }) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Speak to search",
                            tint = if (recordAudioPermissionState.status.isGranted)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.Gray
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() })
            )
        }
    }
}

// 🔥 COMPLETELY REDESIGNED: NearbyActivityCard with actual image and modern UI
@Composable
fun NearbyActivityCard(activity: Activity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column {
            // 🔥 NEW: Banner Image with actual URL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (!activity.bannerImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = activity.bannerImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }

                // 🔥 NEW: Tags overlay
                if (!activity.tags.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        activity.tags.take(1).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // 🔥 NEW: Mode badge at bottom left
                activity.mode?.let { mode ->
                    Surface(
                        shape = RoundedCornerShape(topEnd = 8.dp),
                        color = if (mode.equals("Online", ignoreCase = true))
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f)
                        else
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (mode.equals("Online", ignoreCase = true))
                                    Icons.Default.Laptop
                                else
                                    Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (mode.equals("Online", ignoreCase = true))
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = mode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (mode.equals("Online", ignoreCase = true))
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // 🔥 NEW: Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Title
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Tutor info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activity.tutorName.firstOrNull()?.uppercase() ?: "T",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = activity.tutorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                activity.reviews?.averageRating?.let { rating ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rating",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFA000)
                        )
                        Text(
                            text = String.format("%.1f", rating),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(${activity.reviews?.totalReviews ?: 0})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    // Get the current back stack entry to determine the selected route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define the navigation items including "Discover"
    val items = listOf(
        // Route      Title       Icon
        Triple("home", "Explore", Icons.Default.Explore),
        Triple("feed", "Feed", Icons.Default.Article), // Or Search? Check icon
        Triple("discover", "Discover", Icons.Default.Groups), // <-- NEW ITEM
        // Triple("post", "Post", Icons.Default.AddCircle) // Keep or remove "Post" if not needed
    )

    NavigationBar {
        items.forEach { (route, title, icon) -> // Destructure the Triple
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                // Determine selected state based on the current route
                selected = currentRoute == route,
                onClick = {
                    // Navigate only if the selected item is different
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            // Pop up to the start destination to avoid building up back stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when reselecting
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CategoriesSection(
    categories: List<String>,
    onCategoryClick: (String) -> Unit
) {
    Column() {
        Text(
            "Browse by Category",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories.size) { index ->
                SuggestionChip(
                    onClick = { onCategoryClick(categories[index]) },
                    label = { Text(categories[index].capitalize()) }
                )
            }
        }
    }
}

@Composable
fun HorizontalActivitySection(
    title: String,
    icon: ImageVector? = null,
    activities: List<Activity>,
    onActivityClick: (Activity) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    Icon(
                        it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(onClick = onSeeAllClick) {
                Text("See All")
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activities.size) { index ->
                NearbyActivityCard(
                    activity = activities[index],
                    onClick = { onActivityClick(activities[index]) }
                )
            }
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Logout") },
        text = { Text("Are you sure you want to logout?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
