// MeetingTab.kt
package com.example.learnverse.ui.screen.detail.tabs

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learnverse.data.model.Activity

@Composable
fun MeetingTab(
    activity: Activity,
    context: Context
) {
    val videoContent = activity.videoContent
    val hasMeeting = videoContent?.meetingLink != null

    var showChooser by remember { mutableStateOf(false) }

    if (!hasMeeting) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.VideoCall,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No meeting link available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Meeting Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VideoCall,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Live Meeting",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider()

                // Platform
                videoContent.platform?.let { platform ->
                    InfoRowMeeting(
                        icon = Icons.Default.Computer,
                        label = "Platform",
                        value = platform
                    )
                }

                // Meeting ID
                videoContent.meetingId?.let { meetingId ->
                    InfoRowMeeting(
                        icon = Icons.Default.Numbers,
                        label = "Meeting ID",
                        value = meetingId
                    )
                }

                // Passcode
                videoContent.passcode?.let { passcode ->
                    InfoRowMeeting(
                        icon = Icons.Default.Lock,
                        label = "Passcode",
                        value = passcode
                    )
                }
            }
        }

        // Join Button with Chooser
        Button(
            onClick = { showChooser = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Launch, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                "Join Meeting",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "How to Join",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "1. Click 'Join Meeting' button above",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "2. Choose to open in browser or app",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "3. Enter Meeting ID and Passcode if required",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Chooser Dialog
    if (showChooser) {
        AlertDialog(
            onDismissRequest = { showChooser = false },
            title = { Text("Open Meeting Link") },
            text = { Text("How would you like to open the meeting?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Open in app (deep link)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoContent.meetingLink))
                        context.startActivity(intent)
                        showChooser = false
                    }
                ) {
                    Text("Open in App")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Open in browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoContent.meetingLink))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.setPackage("com.android.chrome") // Force browser
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to default browser
                            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoContent.meetingLink))
                            context.startActivity(fallbackIntent)
                        }
                        showChooser = false
                    }
                ) {
                    Text("Open in Browser")
                }
            }
        )
    }
}

@Composable
fun InfoRowMeeting(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}