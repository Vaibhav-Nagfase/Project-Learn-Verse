// ActivityInfoTab.kt (COMPLETE VERSION)
package com.example.learnverse.ui.screen.detail.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learnverse.data.model.Activity
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityInfoTab(
    activity: Activity,
    isEnrolled: Boolean,
    isTutor: Boolean,
    onEnroll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Description
        item {
            Text(
                text = activity.title ?: "Untitled",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = activity.description ?: "No description",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Pricing
        activity.pricing?.let { pricing ->
            item {
                InfoCard(title = "Pricing") {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "₹${pricing.price}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (pricing.discountPrice != null) {
                            Text(
                                text = "₹${pricing.discountPrice}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // Prerequisites
        activity.prerequisites?.let { prerequisites ->
            if (prerequisites.isNotEmpty()) {
                item {
                    InfoCard(title = "Prerequisites") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            prerequisites.forEach { prerequisite ->
                                Row {
                                    Text("• ", fontWeight = FontWeight.Bold)
                                    Text(prerequisite)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Duration Info
        activity.duration?.let { duration ->
            item {
                InfoCard(title = "Duration") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        duration.totalSessions?.let {
                            InfoRow("Sessions", "$it sessions")
                        }
                        duration.estimatedDuration?.let {
                            InfoRow("Duration", "$it hours")
                        }
                        duration.durationDescription?.let {
                            InfoRow("Details", it)
                        }
                    }
                }
            }
        }

        // Schedule
        activity.schedule?.let { schedule ->
            item {
                InfoCard(title = "Schedule") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        schedule.selfPaced?.let {
                            InfoRow("Mode", if (it) "Self-paced" else "Scheduled")
                        }
                        schedule.sessionDays?.let { days ->
                            InfoRow("Days", days.joinToString(", "))
                        }
                        schedule.sessionTime?.let {
                            InfoRow("Time", it)
                        }
                    }
                }
            }
        }

        // Suitable Age Group
        activity.suitableAgeGroup?.let { ageGroup ->
            item {
                InfoCard(title = "Suitable Age") {
                    Text("${ageGroup.minAge} - ${ageGroup.maxAge} years")
                    ageGroup.ageDescription?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Tags
        activity.tags?.let { tags ->
            if (tags.isNotEmpty()) {
                item {
                    InfoCard(title = "Tags") {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = Int.MAX_VALUE
                        ) {
                            tags.forEach { tag ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(tag) }
                                )
                            }
                        }
                    }
                }
            }
        }


        // Enroll button (only for non-tutors)
        if (!isTutor) {
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onEnroll,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isEnrolled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnrolled)
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isEnrolled) "Already Enrolled" else "Enroll Now",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}