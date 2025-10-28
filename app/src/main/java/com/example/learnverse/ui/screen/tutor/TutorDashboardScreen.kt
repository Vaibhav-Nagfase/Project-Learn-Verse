// TutorDashboardScreen.kt
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
import com.example.learnverse.data.model.Activity
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.TutorViewModel
import com.example.learnverse.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    tutorViewModel: TutorViewModel,
    activitiesViewModel: ActivitiesViewModel
) {
    val myActivities by tutorViewModel.myActivities.collectAsStateWithLifecycle()
    val uiState by tutorViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        tutorViewModel.fetchMyActivities()
    }

    var activityToDelete by remember { mutableStateOf<Activity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Activities") },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("create_activity") }) {
                Icon(Icons.Default.Add, contentDescription = "Create New Activity")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState is UiState.Loading && myActivities.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                myActivities.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LibraryBooks,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No activities yet",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap the '+' button to create your first activity",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myActivities) { activity ->
                            ActivityCard(
                                activity = activity,
                                onClick = {
                                    activitiesViewModel.addActivityToCache(activity)
                                    navController.navigate("activityDetail/${activity.id}")
                                },
                                onEdit = {
                                    navController.navigate("create_activity?activityId=${activity.id}")
                                },
                                onDelete = {
                                    activityToDelete = activity
                                }
                            )
                        }
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
                onDismiss = {
                    activityToDelete = null
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
