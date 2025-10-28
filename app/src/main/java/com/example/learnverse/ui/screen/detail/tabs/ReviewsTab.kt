// ReviewsTab.kt
package com.example.learnverse.ui.screen.detail.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learnverse.data.model.Activity
import com.example.learnverse.data.model.Review
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.example.learnverse.viewmodel.AuthViewModel

@Composable
fun ReviewsTab(
    activity: Activity,
    authViewModel: AuthViewModel,
    activitiesViewModel: ActivitiesViewModel
) {
    // State from ViewModel
    val reviews by activitiesViewModel.activityReviews.collectAsState()
    val totalReviews by activitiesViewModel.totalReviews.collectAsState()
    val isLoadingReviews by activitiesViewModel.isLoadingReviews.collectAsState()
    val hasUserReviewed by activitiesViewModel.hasUserReviewed.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()

    var showAddReview by remember { mutableStateOf(false) }
    var showEditReview by remember { mutableStateOf<Review?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Review?>(null) }

    // Load reviews when tab is opened
    LaunchedEffect(activity.id) {
        activity.id?.let { activityId ->
            activitiesViewModel.fetchActivityReviews(activityId)
            activitiesViewModel.checkIfUserReviewed(activityId)
        }
    }

    // Calculate average rating from fetched reviews
    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else {
        0.0
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rating Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = String.format("%.1f", averageRating),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                Icon(
                                    if (index < averageRating.toInt()) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            "$totalReviews reviews",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddReview = true },
                        enabled = !hasUserReviewed // Disable if already reviewed
                    ) {
                        Icon(
                            if (hasUserReviewed) Icons.Default.CheckCircle else Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (hasUserReviewed) "Reviewed" else "Add Review")
                    }
                }
            }
        }

        // Loading state
        if (isLoadingReviews) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Empty state
        if (!isLoadingReviews && reviews.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.RateReview,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No reviews yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Be the first to review!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Reviews list
        if (!isLoadingReviews) {
            items(reviews.size) { index ->
                ReviewItem(
                    review = reviews[index],
                    isOwnReview = reviews[index].userId == currentUserId,
                    onEdit = { showEditReview = reviews[index] },
                    onDelete = { showDeleteConfirm = reviews[index] }
                )
            }
        }
    }

    // Add Review Dialog
    if (showAddReview) {
        AddReviewDialog(
            onDismiss = { showAddReview = false },
            onSubmit = { rating, feedback ->
                activity.id?.let { activityId ->
                    activitiesViewModel.addReview(activityId, rating, feedback)
                }
                showAddReview = false
            }
        )
    }

    // Edit Review Dialog
    showEditReview?.let { review ->
        EditReviewDialog(
            review = review,
            onDismiss = { showEditReview = null },
            onSubmit = { rating, feedback ->
                activitiesViewModel.updateReview(review.id, rating, feedback)
                showEditReview = null
            }
        )
    }

    // Delete Confirmation Dialog
    showDeleteConfirm?.let { review ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Review") },
            text = { Text("Are you sure you want to delete your review? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        activitiesViewModel.deleteReview(review.id)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ReviewItem(
    review: Review,
    isOwnReview: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // User avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.userName?.take(1)?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                review.userName ?: "Anonymous",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (review.isVerifiedEnrollment) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            formatReviewDate(review.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (review.isEdited) {
                            Text(
                                "Edited",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }

                // Rating stars and menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row {
                        repeat(5) { index ->
                            Icon(
                                if (index < review.rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Show menu only for own reviews
                    if (isOwnReview) {
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
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
                }
            }

            // Review text
            review.feedback?.let { feedback ->
                if (feedback.isNotBlank()) {
                    Text(
                        feedback,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, feedback: String?) -> Unit  // Already correct
) {
    var rating by remember { mutableStateOf(5) }
    var feedback by remember { mutableStateOf("") }  // Changed from 'comment' to 'feedback'

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Your Review") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rating selector
                Column {
                    Text("Your Rating", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { rating = index + 1 }
                            ) {
                                Icon(
                                    if (index < rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Feedback field - Changed variable name
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Your Review (Optional)") },
                    placeholder = { Text("Share your experience...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit(rating, feedback.ifBlank { null })  // Send as feedback, not comment
            }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditReviewDialog(
    review: Review,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int?, feedback: String?) -> Unit
) {
    var rating by remember { mutableStateOf(review.rating) }
    var feedback by remember { mutableStateOf(review.feedback ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Your Review") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rating selector
                Column {
                    Text("Your Rating", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { rating = index + 1 }
                            ) {
                                Icon(
                                    if (index < rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Feedback field
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Your Review (Optional)") },
                    placeholder = { Text("Share your experience...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit(rating, feedback.ifBlank { null })
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatReviewDate(dateString: String): String {
    return try {
        val instant = java.time.Instant.parse(dateString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        "Recently"
    }
}