package com.example.learnverse.ui.screen.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import necessary icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.learnverse.data.model.Activity
// Import screens needed for nested navigation
import com.example.learnverse.ui.screen.community.DiscoverScreen
import com.example.learnverse.ui.screen.community.MyPostsScreen
// Import ViewModels
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.CommunityViewModel
import com.example.learnverse.viewmodel.TutorViewModel
import com.example.learnverse.viewmodel.UiState // Assuming UiState is from TutorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorDashboardScreen(
    mainNavController: NavController, // Renamed: NavController for outer graph
    authViewModel: AuthViewModel,
    tutorViewModel: TutorViewModel,
    communityViewModel: CommunityViewModel // Needed for Discover/MyPosts
) {
    val nestedNavController = rememberNavController() // Controller for internal tabs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutor Area") },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            // New bottom navigation bar using nestedNavController
            TutorBottomNavigationBar(navController = nestedNavController)
        },
        floatingActionButton = {
            val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Show FAB based on the current nested route
            when (currentRoute) {
                TutorScreenRoutes.Dashboard.route -> { // FAB for Activities Dashboard
                    FloatingActionButton(onClick = { mainNavController.navigate("create_activity") }) {
                        Icon(Icons.Default.Add, contentDescription = "Create New Activity")
                    }
                }
                TutorScreenRoutes.Discover.route, // FAB for Discover AND My Posts
                TutorScreenRoutes.MyPosts.route -> {
                    FloatingActionButton(onClick = { mainNavController.navigate("createPost") }) { // Use mainNavController
                        Icon(Icons.Default.Add, contentDescription = "Create Post")
                    }
                }
            }
        }
    ) { paddingValues ->
        // Nested NavHost switches content based on bottom nav selection
        NavHost(
            navController = nestedNavController,
            startDestination = TutorScreenRoutes.Dashboard.route,
            modifier = Modifier.padding(paddingValues) // Apply scaffold padding
        ) {
            // Destination 1: The original Dashboard content
            composable(TutorScreenRoutes.Dashboard.route) {
                TutorDashboardContent(
                    tutorViewModel = tutorViewModel,
                    mainNavController = mainNavController // Pass outer NavController for actions
                )
            }
            // Destination 2: The Discover feed screen
            composable(TutorScreenRoutes.Discover.route) {
                DiscoverScreen(
                    navController = mainNavController, // Pass outer NavController
                    communityViewModel = communityViewModel,
                    authViewModel = authViewModel
                )
            }
            // Destination 3: The Tutor's own posts screen
            composable(TutorScreenRoutes.MyPosts.route) {
                MyPostsScreen(
                    navController = mainNavController, // Pass outer NavController
                    communityViewModel = communityViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

// --- Define Routes for Nested Navigation ---
sealed class TutorScreenRoutes(val route: String) {
    data object Dashboard : TutorScreenRoutes("tutor_dashboard_content")
    data object Discover : TutorScreenRoutes("tutor_discover")
    data object MyPosts : TutorScreenRoutes("tutor_my_posts") // New route
}

// --- Tutor Specific Bottom Navigation Bar ---
@Composable
fun TutorBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define the items for the tutor's bottom nav
    val items = listOf(
        Triple(TutorScreenRoutes.Dashboard.route, "Dashboard", Icons.Default.Dashboard),
        Triple(TutorScreenRoutes.Discover.route, "Discover", Icons.Default.Groups),
        Triple(TutorScreenRoutes.MyPosts.route, "My Posts", Icons.Default.Article) // New item
    )

    NavigationBar {
        items.forEach { (route, title, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = currentRoute == route,
                onClick = {
                    // Navigate within the nested NavHost
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

// --- Extracted Dashboard Content (Original Logic) ---
@Composable
fun TutorDashboardContent(
    tutorViewModel: TutorViewModel,
    mainNavController: NavController
) {
    val myActivities by tutorViewModel.myActivities.collectAsStateWithLifecycle()
    val uiState by tutorViewModel.uiState.collectAsStateWithLifecycle()
    var activityToDelete by remember { mutableStateOf<Activity?>(null) }

    // Trigger data fetch for activities
    LaunchedEffect(Unit) {
        tutorViewModel.fetchMyActivities()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState is UiState.Loading && myActivities.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            myActivities.isEmpty() -> {
                Text(
                    "You haven't created any activities yet.\nTap the '+' to add one!",
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp)
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myActivities) { activity ->
                        ActivityCard(
                            activity = activity,
                            onUpdate = {
                                // Use mainNavController to navigate outside
                                mainNavController.navigate("create_activity?activityId=${activity.id}")
                            },
                            onDelete = { activityToDelete = activity }
                        )
                    }
                }
            }
        }
        // Delete confirmation dialog logic remains the same
        if (activityToDelete != null) {
            DeleteConfirmationDialog(
                activityName = activityToDelete!!.title,
                onConfirm = {
                    tutorViewModel.deleteActivity(activityToDelete!!.id)
                    activityToDelete = null
                },
                onDismiss = { activityToDelete = null }
            )
        }
    }
}

/**
 * A Composable card that displays a tutor's activity and provides update/delete actions.
 */
@Composable
fun ActivityCard(activity: Activity, onUpdate: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Column for text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(activity.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Subject: ${activity.subject}", style = MaterialTheme.typography.bodyMedium)
                activity.pricing?.let {
                    Text("Price: ${it.price} ${it.currency}", style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = if (activity.isPublic == true) "Status: Public" else "Status: Draft",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (activity.isPublic == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            // Column for action buttons
            Column {
                IconButton(onClick = onUpdate, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Activity")
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Activity", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/**
 * A confirmation dialog for deleting an activity.
 */
@Composable
fun DeleteConfirmationDialog(
    activityName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Activity") },
        text = { Text("Are you sure you want to delete \"$activityName\"? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

