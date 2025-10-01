package com.example.learnverse.ui.screen.search

import android.Manifest
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

    val activities = activitiesViewModel.activities
    val searchQuery = activitiesViewModel.searchQuery
    val isLoading = activitiesViewModel.isLoading

    val isFiltered by activitiesViewModel.isFiltered


    // Permission state for location (needed for natural search)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)


    Column(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()) {

        // --- Top Bar with Search and Logout Icon ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    // You now set the value directly on the ViewModel
                    activitiesViewModel.searchQuery = newValue  },
                label = { Text("Describe the activity you're looking for...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Default.Mic, contentDescription = null) },
                modifier = Modifier.weight(1f), // Use weight to fill available space
                minLines = 2,
                keyboardActions = KeyboardActions(onSearch = {
                    // Check for location permission before searching
                    if (locationPermissionState.status.isGranted) {
                        activitiesViewModel.performNaturalSearch(context)
                    } else {
                        locationPermissionState.launchPermissionRequest()
                    }
                }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
            IconButton(onClick = { showLogoutDialog = true }) {
                Icon(
                    Icons.Default.Logout, // Using the correct Logout icon
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

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
                Text(text = activity.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(text = activity.tutorName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = "Duration", modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        val durationText = activity.durationInfo?.totalDuration?.let { "${it / 60} Hr." } ?: "N/A"
                        Text(text = durationText, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = "Users", modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        val userCountText = activity.enrollmentInfo?.enrolledCount?.let { "$it User" } ?: "N/A"
                        Text(text = userCountText, style = MaterialTheme.typography.bodySmall)
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