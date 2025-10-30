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
fun AddResourceDialog(
    activityId: String,
    videoId: String,
    onDismiss: () -> Unit,
    onAddWithUrl: (String, String, String) -> Unit,
    onUploadFile: (Uri, String, String) -> Unit,
    isUploading: Boolean
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var resourceUrl by remember { mutableStateOf("") }
    var resourceType by remember { mutableStateOf("PDF") }
    var uploadMode by remember { mutableStateOf("url") } // "url" or "file"
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val resourceTypes = listOf("PDF", "DOC", "PPT", "XLS", "TXT", "OTHER")
    var showTypeMenu by remember { mutableStateOf(false) }

    // File picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = context.contentResolver.query(
                it, null, null, null, null
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }

            // Auto-detect file type from extension
            selectedFileName?.let { name ->
                when {
                    name.endsWith(".pdf", ignoreCase = true) -> resourceType = "PDF"
                    name.endsWith(".doc", ignoreCase = true) || name.endsWith(".docx", ignoreCase = true) -> resourceType = "DOC"
                    name.endsWith(".ppt", ignoreCase = true) || name.endsWith(".pptx", ignoreCase = true) -> resourceType = "PPT"
                    name.endsWith(".xls", ignoreCase = true) || name.endsWith(".xlsx", ignoreCase = true) -> resourceType = "XLS"
                    name.endsWith(".txt", ignoreCase = true) -> resourceType = "TXT"
                    else -> resourceType = "OTHER"
                }
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
                .fillMaxHeight(0.7f),
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
                        text = "Add Resource",
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
                        label = { Text("Resource Title *") },
                        leadingIcon = { Icon(Icons.Default.Description, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUploading,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Resource type dropdown
                    ExposedDropdownMenuBox(
                        expanded = showTypeMenu,
                        onExpandedChange = { showTypeMenu = !showTypeMenu && !isUploading }
                    ) {
                        OutlinedTextField(
                            value = resourceType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Resource Type *") },
                            leadingIcon = { Icon(Icons.Default.Category, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            enabled = !isUploading,
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = showTypeMenu,
                            onDismissRequest = { showTypeMenu = false }
                        ) {
                            resourceTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        resourceType = type
                                        showTypeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Resource URL or File picker
                    if (uploadMode == "url") {
                        OutlinedTextField(
                            value = resourceUrl,
                            onValueChange = { resourceUrl = it },
                            label = { Text("Resource URL *") },
                            leadingIcon = { Icon(Icons.Default.Link, null) },
                            placeholder = { Text("https://example.com/file.pdf") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUploading,
                            singleLine = true
                        )
                    } else {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { if (!isUploading) filePickerLauncher.launch("*/*") }
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
                                            text = selectedFileName ?: "Select File",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (selectedFileName != null) FontWeight.Medium else FontWeight.Normal
                                        )
                                        Text(
                                            text = if (selectedFileName != null) "Tap to change" else "PDF, DOC, PPT, etc.",
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
                }

                // Upload indicator
                if (isUploading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Uploading resource...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                            if (uploadMode == "url") {
                                onAddWithUrl(resourceType, title, resourceUrl)
                            } else {
                                selectedFileUri?.let { uri ->
                                    onUploadFile(uri, resourceType, title)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isUploading &&
                                title.isNotBlank() &&
                                ((uploadMode == "url" && resourceUrl.isNotBlank()) ||
                                        (uploadMode == "file" && selectedFileUri != null))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Resource")
                    }
                }
            }
        }
    }
}
