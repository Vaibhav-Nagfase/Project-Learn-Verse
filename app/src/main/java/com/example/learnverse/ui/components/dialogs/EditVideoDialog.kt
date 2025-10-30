package com.example.learnverse.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.learnverse.data.model.Activity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVideoDialog(
    video: Activity.VideoContent.Video,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String?, Int, Boolean) -> Unit,
    isUpdating: Boolean
) {
    var title by remember { mutableStateOf(video.title ?: "") }
    var description by remember { mutableStateOf(video.description ?: "") }
    var videoUrl by remember { mutableStateOf(video.videoUrl ?: "") }
    var order by remember { mutableStateOf(video.order?.toString() ?: "1") }
    var isPreview by remember { mutableStateOf(video.isPreview ?: false) }

    Dialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isUpdating,
            dismissOnClickOutside = !isUpdating,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.75f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Video",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isUpdating) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Title field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Video Title *") },
                        leadingIcon = { Icon(Icons.Default.VideoLibrary, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description *") },
                        leadingIcon = { Icon(Icons.Default.Description, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating,
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Video URL (optional for uploaded videos)
                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        label = { Text("Video URL") },
                        leadingIcon = { Icon(Icons.Default.Link, null) },
                        placeholder = { Text("https://youtube.com/watch?v=...") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating,
                        singleLine = true,
                        supportingText = { Text("Leave empty if video was uploaded") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Order field
                    OutlinedTextField(
                        value = order,
                        onValueChange = { if (it.all { char -> char.isDigit() }) order = it },
                        label = { Text("Order *") },
                        leadingIcon = { Icon(Icons.Default.Sort, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preview toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Free Preview",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Allow students to watch without enrollment",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isPreview,
                            onCheckedChange = { isPreview = it },
                            enabled = !isUpdating
                        )
                    }
                }

                // Update indicator
                if (isUpdating) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isUpdating
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val orderInt = order.toIntOrNull() ?: 1
                            val urlToUpdate = if (videoUrl.isBlank()) null else videoUrl
                            onUpdate(title, description, urlToUpdate, orderInt, isPreview)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isUpdating && title.isNotBlank() && description.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update")
                    }
                }
            }
        }
    }
}