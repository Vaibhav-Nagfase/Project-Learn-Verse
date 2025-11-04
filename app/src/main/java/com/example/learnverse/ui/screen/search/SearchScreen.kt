package com.example.learnverse.ui.screen.search

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import all default icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.learnverse.R
import androidx.navigation.NavController
import com.example.learnverse.data.model.Activity
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.example.learnverse.viewmodel.AuthViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    searchQueryFromHome: String?,
    activitiesViewModel: ActivitiesViewModel,
    authViewModel: AuthViewModel
) {

    // --- THIS BLOCK to listen for results from the FilterScreen ---
    val filterResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<List<Activity>>("filtered_activities")
        ?.observeAsState()

    // When a result is received, update the ViewModel's list
    LaunchedEffect(filterResult) {
        filterResult?.value?.let { activities ->
            activitiesViewModel.updateActivitiesList(activities)
            // Clear the result so it's not processed again on recomposition
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<List<Activity>>("filtered_activities")
        }
    }


    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // THIS IS THE ONLY LaunchedEffect NEEDED FOR INITIAL LOAD
    LaunchedEffect(Unit) {
        if (!searchQueryFromHome.isNullOrBlank()) {
            // If a search query was passed from the home screen, set the state directly
            activitiesViewModel.searchQuery = searchQueryFromHome // <-- CORRECTED LINE
            activitiesViewModel.performNaturalSearch(context)
        } else {
            // Otherwise, fetch the default personalized feed
            activitiesViewModel.fetchMyFeed()
        }
    }

    val activities by activitiesViewModel.activities.collectAsStateWithLifecycle()
    var currentSearchQuery by remember { mutableStateOf(activitiesViewModel.searchQuery) }
    val isLoading = activitiesViewModel.isLoading

    val isFiltered by activitiesViewModel.isFiltered


    // Permission state for location (needed for natural search)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // --- Add Speech Recognition Logic ---
    val recordAudioPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val results: ArrayList<String>? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.firstOrNull()?.let { recognizedText ->
                // Update the ViewModel's query
                activitiesViewModel.searchQuery = recognizedText
                // Update local state for TextField immediately
                currentSearchQuery = recognizedText
                // Trigger search
                if (locationPermissionState.status.isGranted) {
                    activitiesViewModel.performNaturalSearch(context)
                } else {
                    locationPermissionState.launchPermissionRequest() // Ask for location if needed for search
                }
            }
        }
    }
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
            println("Speech recognition not available: ${e.message}")
            // Show Snackbar or Toast
        }
    }
    // --- End of Speech Recognition Logic ---

    Column(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()) {

        // 🔥 NEW: Modern Search Card (Replaces old OutlinedTextField + Logout button)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 🔥 NEW: Header section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Find Your Perfect Activity",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Describe what you're looking for...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔥 NEW: Modern search field with better styling
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    OutlinedTextField(
                        value = currentSearchQuery,
                        onValueChange = { newValue ->
                            currentSearchQuery = newValue
                            activitiesViewModel.searchQuery = newValue
                        },
                        placeholder = {
                            Text(
                                "e.g., 'Dance classes near me' or 'Online coding for kids'",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 🔥 NEW: Clear button
                                if (currentSearchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            currentSearchQuery = ""
                                            activitiesViewModel.searchQuery = ""
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // 🔥 Voice search button
                                IconButton(
                                    onClick = {
                                        if (recordAudioPermissionState.status.isGranted) {
                                            launchSpeechRecognizer()
                                        } else {
                                            recordAudioPermissionState.launchPermissionRequest()
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = "Voice search",
                                        tint = if (recordAudioPermissionState.status.isGranted)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (locationPermissionState.status.isGranted) {
                                    activitiesViewModel.performNaturalSearch(context)
                                } else {
                                    locationPermissionState.launchPermissionRequest()
                                }
                            }
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 🔥 NEW: Search button
                Button(
                    onClick = {
                        if (locationPermissionState.status.isGranted) {
                            activitiesViewModel.performNaturalSearch(context)
                        } else {
                            locationPermissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = currentSearchQuery.isNotEmpty()
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Search Activities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 🔥 REMOVED: Logout button is no longer here


        // --- Row for the Filter Button ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isFiltered) {
                OutlinedButton(onClick = {
                    activitiesViewModel.fetchMyFeed(forceRefresh = true)
                    activitiesViewModel.searchQuery = ""
                }) {
                    Text("Reset")
                }
                Spacer(Modifier.width(8.dp))
            }

            TextButton(onClick = { navController.navigate("filter") }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
                Spacer(Modifier.width(8.dp))
                Text("Filters")
            }
        }


        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    showLogoutDialog = false
                    activitiesViewModel.clearData()
                    authViewModel.logout()
                },
                onDismiss = { showLogoutDialog = false }
            )
        }

        // --- Results List ---
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activities) { activity ->
                    ActivityResultCard(activity = activity) {
                        navController.navigate("activityDetail/${activity.id}")
                    }
                }
            }
        }
    }
}


@Composable
fun ActivityResultCard(activity: Activity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // Placeholder for Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = colorResource(R.color.violet)),
                contentAlignment = Alignment.Center
            ) {
                Text("Image", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Text(
                    text = activity.tutorName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Duration",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val durationText = (activity.duration?.totalDuration?.let {
                            "${it / 60} Hr."
                        }) ?: "N/A"
                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Users",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        // FIXED: Added parentheses for proper precedence
                        val userCountText = (activity.enrollmentInfo?.enrolledCount?.let {
                            "$it User"
                        }) ?: "N/A"
                        Text(
                            text = userCountText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            IconButton(onClick = { /* TODO: Handle like */ }) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Like")
            }
        }
    }
}


@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Logout") },
        text = { Text("Are you sure you want to log out?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}