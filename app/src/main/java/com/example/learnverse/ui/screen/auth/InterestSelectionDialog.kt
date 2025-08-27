package com.example.learnverse.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestSelectionDialog(
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val allInterests = listOf("chess", "geometry", "programming", "machine learning", "photography", "Music", "Art", "VFX", "Swimming", "Karate", "Coding", "Science", "Dance", "Chess", "Photography")
    val selectedInterests = remember { mutableStateListOf<String>() }

    // This boolean will control the button's state
    val isSaveButtonEnabled = selectedInterests.size >= 3

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Your Interests") },
        text = {
            Column {
                // NEW: Instructional text to guide the user
                Text(
                    text = "Please select at least 3 interests to continue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    allInterests.forEach { interest ->
                        FilterChip(
                            selected = interest in selectedInterests,
                            onClick = {
                                if (interest in selectedInterests) {
                                    selectedInterests.remove(interest)
                                } else {
                                    selectedInterests.add(interest)
                                }
                            },
                            label = { Text(interest) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            // UPDATED: The button is now enabled/disabled based on the selection count
            Button(
                onClick = { onSave(selectedInterests) },
                enabled = isSaveButtonEnabled
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}