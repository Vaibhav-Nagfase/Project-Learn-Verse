package com.example.learnverse.ui.screen.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.learnverse.data.model.Activity
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.CommunityViewModel
import com.example.learnverse.viewmodel.MyTutorProfileViewModel
import com.example.learnverse.viewmodel.TutorViewModel
import com.example.learnverse.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorDashboardScreen(
    mainNavController: NavController,
    authViewModel: AuthViewModel,
    tutorViewModel: TutorViewModel,
    activitiesViewModel: ActivitiesViewModel,
    mytutorProfileViewModel:MyTutorProfileViewModel,
    communityViewModel: CommunityViewModel // This is now unused, but safe to leave
) {
    val myActivities by tutorViewModel.myActivities.collectAsStateWithLifecycle()
    val uiState by tutorViewModel.uiState.collectAsStateWithLifecycle()
    var activityToDelete by remember { mutableStateOf<Activity?>(null) }

    // Trigger data fetch for activities
    LaunchedEffect(Unit) {
        tutorViewModel.fetchMyActivities()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {
                    IconButton(onClick = { mainNavController.navigate("my_tutor_profile") }) {
                        // Here you can use an AsyncImage to load the tutor's profile pic
                        Icon(Icons.Default.AccountCircle, contentDescription = "My Profile")
                    }
                },

                actions = {
                    // This is where you add action buttons at the end (right side)
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }

            )
        },
        // Add the BottomNavBar, passing the main NavController
        bottomBar = {
            TutorBottomNavigationBar(navController = mainNavController)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mainNavController.navigate("create_activity") }) {
                Icon(Icons.Default.Add, contentDescription = "Create New Activity")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
                        // Add a header

                        items(myActivities) { activity ->
                            ActivityCard(
                                activity = activity,
                                onClick = {
                                    activitiesViewModel.addActivityToCache(activity)
                                    mainNavController.navigate("activityDetail/${activity.id}")
                                },
                                onEdit = {
                                    mainNavController.navigate("create_activity?activityId=${activity.id}")
                                },
                                onDelete = { activityToDelete = activity }
                            )
                        }
                    }
                }
            }

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
}


@Composable
fun TutorBottomNavigationBar(navController: NavController) {
    // Get the current back stack entry to determine the selected route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define the items for the tutor's bottom nav
    val items = listOf(
        Triple("tutor_dashboard_main", "Dashboard", Icons.Default.Dashboard),
        Triple("tutor_discover", "Discover", Icons.Default.Groups),
        Triple("tutor_my_posts", "My Posts", Icons.Default.Article)
    )

    NavigationBar {
        items.forEach { (route, title, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}


/**
 * A Composable card that displays a tutor's activity and provides update/delete actions.
 */
@Composable
fun ActivityCard(
    activity: Activity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Subject: ${activity.subject}",
                    style = MaterialTheme.typography.bodyMedium
                )
                activity.pricing?.let {
                    Text(
                        "₹${it.price}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { },
                        label = { Text(activity.mode) }
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(activity.difficulty ?: "N/A") }
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "Menu")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    )
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