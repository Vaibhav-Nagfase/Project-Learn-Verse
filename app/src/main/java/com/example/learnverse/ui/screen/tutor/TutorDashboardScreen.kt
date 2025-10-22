package com.example.learnverse.ui.screen.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.data.model.Activity
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.TutorViewModel
import com.example.learnverse.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    tutorViewModel: TutorViewModel
) {
    // Collect state from the ViewModel in a lifecycle-aware manner
    val myActivities by tutorViewModel.myActivities.collectAsStateWithLifecycle()
    val uiState by tutorViewModel.uiState.collectAsStateWithLifecycle()

    // Trigger the data fetch when the screen first appears
    LaunchedEffect(Unit) {
        tutorViewModel.fetchMyActivities()
    }

    // State to control the delete confirmation dialog
    var activityToDelete by remember { mutableStateOf<Activity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutor Dashboard") },
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
                    Text(
                        "You haven't created any activities yet. Tap the '+' to add one!",
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
                                    // Navigate to the edit screen with the activityId
                                    navController.navigate("create_activity?activityId=${activity.id}")
                                },
                                onDelete = {
                                    // Set the activity to be deleted to show the dialog
                                    activityToDelete = activity
                                }
                            )
                        }
                    }
                }
            }
        }

        // Show the confirmation dialog when an activity is selected for deletion
        if (activityToDelete != null) {
            DeleteConfirmationDialog(
                activityName = activityToDelete!!.title,
                onConfirm = {
                    tutorViewModel.deleteActivity(activityToDelete!!.id)
                    activityToDelete = null // Dismiss the dialog
                },
                onDismiss = {
                    activityToDelete = null // Dismiss the dialog
                }
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

