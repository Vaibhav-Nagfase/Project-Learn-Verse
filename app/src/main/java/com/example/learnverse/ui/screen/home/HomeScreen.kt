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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.learnverse.R
import com.example.learnverse.data.model.Activity
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.example.learnverse.viewmodel.AuthViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    activitiesViewModel: ActivitiesViewModel
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // ADD THIS BLOCK TO AUTOMATICALLY FETCH THE FEED
    LaunchedEffect(Unit) {
        activitiesViewModel.fetchMyFeed()
    }

    // Get the data from the ViewModel
    val recommendedActivities = activitiesViewModel.activities // Reusing the main feed for recommendations
    val nearbyActivities = activitiesViewModel.nearbyActivities

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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val verificationStatus by authViewModel.verificationStatus.collectAsState()
    val hasProfile by authViewModel.hasProfile.collectAsStateWithLifecycle()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // 3. Define the content of the drawer
            ModalDrawerSheet {
                Text("LearnVerse Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)

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

                Divider() // Optional: for visual separation

                // --- THIS IS THE NEW DYNAMIC LOGIC ---
                when (verificationStatus?.status) {
                    "PENDING" -> {
                        NavigationDrawerItem(
                            label = { Text(text = "Verification Pending") },
                            selected = false,
                            onClick = { /* TODO: Navigate to a status screen */ }
                        )
                    }
                    "REJECTED" -> {
                        NavigationDrawerItem(
                            label = {
                                Column{
                                    Row(modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween){
                                        Text(text = "Verification Rejected Reapply")
                                        Icon(Icons.Default.ArrowForward, contentDescription = "direction")
                                    }
                                    Text(text = "Reason: ${verificationStatus?.rejectionReason}")
                                }
                            },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("tutorVerification")
                            }
                        )
                    }
                    else -> {
                        // This shows if status is null (never applied) or anything else
                        NavigationDrawerItem(
                            label = { Text(text = "Become a Tutor") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("tutorVerification")
                            }
                        )
                    }
                }

                // TODO: Add other drawer items like "Profile", "Settings", etc.
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    
                }) {
                    Icon(Icons.Default.Chat, contentDescription = "Learning Assistant")
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
                        }
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

                // 3. Recommended Activities - Displaying first 5 items from the main feed
                item {
                    Text("Recommended Activities", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (recommendedActivities.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(recommendedActivities.take(5)) { activity ->
                                NearbyActivityCard(activity = activity) {
                                    navController.navigate("activityDetail/${activity.id}")
                                }
                            }
                        }
                    } else {
                        Text(
                            "No recommendations found yet.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // 4. Activities Near You - Dynamic horizontal list
                item {
                    Text("Activities Near You", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (locationPermissionState.status.isGranted) {
                        if (nearbyActivities.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(nearbyActivities) { activity ->
                                    NearbyActivityCard(activity = activity) {
                                        navController.navigate("activityDetail/${activity.id}")
                                    }
                                }
                            }
                        } else {
                            // You could show a loading indicator here while it fetches
                            Text(
                                "Searching for nearby activities...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Text(
                            "Enable location permission to see activities near you.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


// --- Reusable and Updated Composables for HomeScreen ---

@Composable
fun HomeHeader(onProfileClick: () -> Unit) {
    // This would ideally get the user's name from a UserProfile object
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Hello, Ronaldo", style = MaterialTheme.typography.headlineMedium) // TODO: Get user name
        }
        Image(
            painter = painterResource(id = R.drawable.boy),
            contentDescription = "Profile",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onProfileClick() }
        )
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


    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Find an Activity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("as per your interest", style = MaterialTheme.typography.titleMedium)
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
                            tint = if (recordAudioPermissionState.status.isGranted) MaterialTheme.colorScheme.primary else Color.Gray // Optional: Change color based on permission
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

@Composable
fun NearbyActivityCard(activity: Activity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("Image", color = MaterialTheme.colorScheme.onSecondaryContainer) // Placeholder for Image
            }
            Column(Modifier.padding(12.dp)) {
                Text(text = activity.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = activity.tutorName, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
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