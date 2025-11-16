package com.example.learnverse.ui.screen.tutor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.learnverse.data.model.Activity
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.CommunityViewModel
import com.example.learnverse.viewmodel.MyTutorProfileViewModel
import com.example.learnverse.viewmodel.TutorDashboardViewModel
import com.example.learnverse.viewmodel.TutorViewModel
import com.example.learnverse.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorDashboardScreen(
    mainNavController: NavController,
    authViewModel: AuthViewModel,
    tutorViewModel: TutorViewModel,
    activitiesViewModel: ActivitiesViewModel,
    mytutorProfileViewModel: MyTutorProfileViewModel,
    communityViewModel: CommunityViewModel,
    tutorDashboardViewModel: TutorDashboardViewModel
) {
    val myActivities by tutorViewModel.myActivities.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
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
                        Icon(Icons.Default.AccountCircle, contentDescription = "My Profile")
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
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
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "You haven't created any activities yet.\nTap the '+' to add one!",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ✅ ADD EARNINGS CARD AS FIRST ITEM
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        mainNavController.navigate("tutor_earnings_dashboard")
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            tint = Color(0xFF4CAF50)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                "View Earnings Dashboard",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Track your revenue & analytics",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Activities List
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
                                onDelete = { activityToDelete = activity },
                                mainNavController = mainNavController  // ✅ ADD THIS
                            )
                        }
                    }
                }
            }

            // Delete Confirmation Dialog
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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

@Composable
fun ActivityCard(
    activity: Activity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    mainNavController: NavController
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Active/Inactive Badge
                        val isActive = activity.isActive ?: true
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    if (isActive) "Active" else "Inactive",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isActive)
                                    Color(0xFF10B981).copy(alpha = 0.15f)
                                else
                                    Color(0xFFEF4444).copy(alpha = 0.15f),
                                labelColor = if (isActive) Color(0xFF10B981) else Color(0xFFEF4444),
                                leadingIconContentColor = if (isActive) Color(0xFF10B981) else Color(0xFFEF4444)
                            ),
                            modifier = Modifier.height(28.dp)
                        )

                        // Public/Private Badge
                        val isPublic = activity.isPublic ?: true
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    if (isPublic) "Public" else "Private",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (isPublic) Icons.Default.Public else Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isPublic)
                                    Color(0xFF6366F1).copy(alpha = 0.15f)
                                else
                                    Color(0xFF64748B).copy(alpha = 0.15f),
                                labelColor = if (isPublic) Color(0xFF6366F1) else Color(0xFF64748B),
                                leadingIconContentColor = if (isPublic) Color(0xFF6366F1) else Color(0xFF64748B)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }

                    Text(
                        activity.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        activity.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Stats row
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ActivityStatItem(
                            Icons.Default.People,
                            "${activity.enrollmentInfo?.enrolledCount ?: 0} Students"
                        )
                        ActivityStatItem(
                            Icons.Default.VideoLibrary,
                            "${activity.videoContent?.recordedVideos?.size ?: 0} Videos"
                        )
                    }
                }

                // Edit/Delete menu
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Options")
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
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                onClick = {
                    mainNavController.navigate("tutor/manage_course/${activity.id}")
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Manage Course",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityStatItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


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