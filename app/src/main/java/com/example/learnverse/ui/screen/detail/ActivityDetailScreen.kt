package com.example.learnverse.ui.screen.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learnverse.data.model.Activity
import com.example.learnverse.viewmodel.ActivitiesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityId: String,
    activitiesViewModel: ActivitiesViewModel,
    navController: NavController
) {
    // Get the specific activity from the shared ViewModel using the ID.
    // This is fast because the data is already in memory.
    val activity = activitiesViewModel.getActivityById(activityId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* TODO: Handle enrollment click, navigate to payment gateway in future */ },
                icon = { Icon(Icons.Default.Check, contentDescription = "Enroll") },
                text = { Text("Enroll Now") }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        // Check if the activity was found before trying to display it
        if (activity != null) {
            ActivityDetailContent(
                activity = activity,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // Show an error if the activity wasn't in the list
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Activity not found.")
            }
        }
    }
}

@Composable
fun ActivityDetailContent(activity: Activity, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Header Section ---
        item {
            // Placeholder for a big image or video player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("Activity Image", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = activity.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = "By ${activity.tutorName}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // --- Info Chips ---
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(activity.mode)
                Chip(activity.difficulty)
                Chip(activity.subject)
            }
        }

        // --- Description ---
        item {
            DetailSection(title = "About this Activity") {
                Text(text = activity.description, style = MaterialTheme.typography.bodyLarge)
            }
        }

        // --- Pricing Section (Null-Safe) ---
        item {
            DetailSection(title = "Pricing") {
                // Safely access pricing information
                val priceText = activity.pricing?.let {
                    it.discountPrice?.let { discount ->
                        "Discounted Price: ${it.currency} $discount (Original: ${it.price})"
                    } ?: "Price: ${it.currency} ${it.price}"
                } ?: "Pricing not available" // Default text if pricing is null
                Text(priceText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }

        // --- Duration Section (Null-Safe) ---
        item {
            DetailSection(title = "Duration") {
                // Safely access duration description
                val durationText = activity.durationInfo?.durationDescription ?: "Duration not available"
                Text(durationText, style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Spacer at the bottom so content doesn't hide behind the FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        content()
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}