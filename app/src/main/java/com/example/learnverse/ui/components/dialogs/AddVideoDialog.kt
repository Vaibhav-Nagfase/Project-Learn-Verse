package com.example.learnverse.ui.components.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVideoDialog(
    activityId: String,
    nextOrder: Int,
    onDismiss: () -> Unit,
    onVideoAdded: () -> Unit,
    onAddWithUrl: (String, String, String, Int, Boolean) -> Unit,
    onUploadFile: (Uri, String, String, Int, Boolean) -> Unit,
    isUploading: Boolean,
    uploadProgress: Int
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var order by remember { mutableStateOf(nextOrder.toString()) }
    var isPreview by remember { mutableStateOf(false) }
    var uploadMode by remember { mutableStateOf("url") } // "url" or "file"
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    // Video file picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedVideoUri = it
            selectedFileName = context.contentResolver.query(
                it, null, null, null, null
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isUploading,
            dismissOnClickOutside = !isUploading,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
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
                        text = "Add Video",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isUploading) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Upload mode selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uploadMode == "url",
                        onClick = { uploadMode = "url" },
                        label = { Text("Add URL") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = uploadMode == "file",
                        onClick = { uploadMode = "file" },
                        label = { Text("Upload File") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
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
                        enabled = !isUploading,
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
                        enabled = !isUploading,
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Video URL or File picker
                    if (uploadMode == "url") {
                        OutlinedTextField(
                            value = videoUrl,
                            onValueChange = { videoUrl = it },
                            label = { Text("Video URL *") },
                            leadingIcon = { Icon(Icons.Default.Link, null) },
                            placeholder = { Text("https://youtube.com/watch?v=...") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUploading,
                            singleLine = true
                        )
                    } else {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { if (!isUploading) videoPickerLauncher.launch("video/*") }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.UploadFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = selectedFileName ?: "Select Video File",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (selectedFileName != null) FontWeight.Medium else FontWeight.Normal
                                        )
                                        Text(
                                            text = if (selectedFileName != null) "Tap to change" else "MP4, AVI, MOV (Max 500MB)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (selectedFileName != null) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Order field
                    OutlinedTextField(
                        value = order,
                        onValueChange = { if (it.all { char -> char.isDigit() }) order = it },
                        label = { Text("Order *") },
                        leadingIcon = { Icon(Icons.Default.Sort, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUploading,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preview toggle - FIXED SPACING
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Free Preview",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Allow students to watch without enrollment",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Switch(
                            checked = isPreview,
                            onCheckedChange = { isPreview = it },
                            enabled = !isUploading
                        )
                    }
                }

                // Upload progress
                if (isUploading) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        LinearProgressIndicator(
                            progress = uploadProgress / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Uploading... $uploadProgress%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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
                        enabled = !isUploading
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val orderInt = order.toIntOrNull() ?: nextOrder

                            if (uploadMode == "url") {
                                onAddWithUrl(title, description, videoUrl, orderInt, isPreview)
                            } else {
                                selectedVideoUri?.let { uri ->
                                    onUploadFile(uri, title, description, orderInt, isPreview)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isUploading &&
                                title.isNotBlank() &&
                                description.isNotBlank() &&
                                ((uploadMode == "url" && videoUrl.isNotBlank()) ||
                                        (uploadMode == "file" && selectedVideoUri != null))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uploadMode == "url") "Add" else "Upload")
                    }
                }
            }
        }
    }
}