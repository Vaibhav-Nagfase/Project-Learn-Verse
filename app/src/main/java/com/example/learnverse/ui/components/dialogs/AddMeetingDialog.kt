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
fun AddMeetingDialog(
    existingMeeting: Activity.VideoContent??,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String?, String?) -> Unit,
    isUpdating: Boolean
) {
    var platform by remember { mutableStateOf(existingMeeting?.platform ?: "") }
    var meetingLink by remember { mutableStateOf(existingMeeting?.meetingLink ?: "") }
    var meetingId by remember { mutableStateOf(existingMeeting?.meetingId ?: "") }
    var passcode by remember { mutableStateOf(existingMeeting?.passcode ?: "") }

    val platforms = listOf("Zoom", "Google Meet", "Microsoft Teams", "Webex", "Other")
    var showPlatformMenu by remember { mutableStateOf(false) }

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
                        text = if (existingMeeting != null) "Edit Meeting" else "Add Meeting",
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

                // Info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Students will see this meeting link when they enroll in the activity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Platform dropdown
                    ExposedDropdownMenuBox(
                        expanded = showPlatformMenu,
                        onExpandedChange = { showPlatformMenu = !showPlatformMenu && !isUpdating }
                    ) {
                        OutlinedTextField(
                            value = platform,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Platform") },
                            leadingIcon = { Icon(Icons.Default.Videocam, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPlatformMenu) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            enabled = !isUpdating,
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = showPlatformMenu,
                            onDismissRequest = { showPlatformMenu = false }
                        ) {
                            platforms.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = {
                                        platform = p
                                        showPlatformMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Meeting link
                    OutlinedTextField(
                        value = meetingLink,
                        onValueChange = { meetingLink = it },
                        label = { Text("Meeting Link *") },
                        leadingIcon = { Icon(Icons.Default.Link, null) },
                        placeholder = { Text("https://zoom.us/j/...") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Meeting ID (optional)
                    OutlinedTextField(
                        value = meetingId,
                        onValueChange = { meetingId = it },
                        label = { Text("Meeting ID") },
                        leadingIcon = { Icon(Icons.Default.Fingerprint, null) },
                        placeholder = { Text("123 456 7890") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating,
                        singleLine = true,
                        supportingText = { Text("Optional") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Passcode (optional)
                    OutlinedTextField(
                        value = passcode,
                        onValueChange = { passcode = it },
                        label = { Text("Passcode") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        placeholder = { Text("Enter passcode") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating,
                        singleLine = true,
                        supportingText = { Text("Optional") }
                    )
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
                            onSave(
                                platform.ifBlank { null },
                                meetingLink.ifBlank { null },
                                meetingId.ifBlank { null },
                                passcode.ifBlank { null }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isUpdating && meetingLink.isNotBlank()
                    ) {
                        Icon(
                            if (existingMeeting != null) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (existingMeeting != null) "Update" else "Add")
                    }
                }
            }
        }
    }
}