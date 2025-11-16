package com.example.learnverse.ui.screen.tutor.course

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
import androidx.navigation.NavController
import com.example.learnverse.viewmodel.ActivitiesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVideoScreen(
    activityId: String,
    navController: NavController,
    viewModel: ActivitiesViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var order by remember { mutableStateOf("") }
    var isPreview by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var uploadMode by remember { mutableStateOf(UploadMode.URL) }

    val isUploadingVideo by viewModel.isUploadingVideo.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
        uri?.let {
            uploadMode = UploadMode.FILE
            videoUrl = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Video") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Video Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Video Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = title.isBlank() && errorMessage != null,
                enabled = !isUploadingVideo
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = !isUploadingVideo
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Upload Method",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uploadMode == UploadMode.URL,
                            onClick = {
                                if (!isUploadingVideo) {
                                    uploadMode = UploadMode.URL
                                    selectedVideoUri = null
                                }
                            },
                            label = { Text("Video URL") },
                            modifier = Modifier.weight(1f),
                            enabled = !isUploadingVideo,
                            leadingIcon = {
                                Icon(Icons.Default.Link, null)
                            }
                        )
                        FilterChip(
                            selected = uploadMode == UploadMode.FILE,
                            onClick = {
                                if (!isUploadingVideo) {
                                    videoPickerLauncher.launch("video/*")
                                }
                            },
                            label = { Text("Upload File") },
                            modifier = Modifier.weight(1f),
                            enabled = !isUploadingVideo,
                            leadingIcon = {
                                Icon(Icons.Default.VideoLibrary, null)
                            }
                        )
                    }
                }
            }

            when (uploadMode) {
                UploadMode.URL -> {
                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        label = { Text("Video URL *") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = videoUrl.isBlank() && errorMessage != null,
                        leadingIcon = { Icon(Icons.Default.Link, null) },
                        enabled = !isUploadingVideo
                    )
                }
                UploadMode.FILE -> {
                    selectedVideoUri?.let { uri ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Video Selected",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        uri.lastPathSegment ?: "video.mp4",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                if (!isUploadingVideo) {
                                    IconButton(onClick = {
                                        selectedVideoUri = null
                                        uploadMode = UploadMode.URL
                                    }) {
                                        Icon(
                                            Icons.Default.Close,
                                            "Remove",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    } ?: run {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, null)
                                Spacer(Modifier.width(8.dp))
                                Text("No file selected. Tap 'Upload File' to choose.")
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = order,
                onValueChange = { order = it.filter { char -> char.isDigit() } },
                label = { Text("Order *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("1, 2, 3...") },
                enabled = !isUploadingVideo
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Free Preview",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Allow non-enrolled users to watch",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPreview,
                        onCheckedChange = { isPreview = it },
                        enabled = !isUploadingVideo
                    )
                }
            }

            // ✅ FIX: Show progress bar immediately when upload starts
            if (isUploadingVideo) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Uploading video...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "$uploadProgress%",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Please don't close this screen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            errorMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Button(
                onClick = {
                    when {
                        title.isBlank() -> errorMessage = "Title is required"
                        order.isBlank() -> errorMessage = "Order is required"
                        uploadMode == UploadMode.URL && videoUrl.isBlank() ->
                            errorMessage = "Video URL is required"
                        uploadMode == UploadMode.FILE && selectedVideoUri == null ->
                            errorMessage = "Please select a video file"
                        else -> {
                            errorMessage = null

                            if (uploadMode == UploadMode.URL) {
                                viewModel.addVideoWithUrl(
                                    activityId = activityId,
                                    title = title,
                                    description = description,
                                    videoUrl = videoUrl,
                                    order = order.toInt(),
                                    isPreview = isPreview,
                                    onSuccess = {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Video added successfully!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        navController.popBackStack()
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                    }
                                )
                            } else {
                                selectedVideoUri?.let { uri ->
                                    viewModel.uploadVideoFile(
                                        activityId = activityId,
                                        videoUri = uri,
                                        title = title,
                                        description = description,
                                        order = order.toInt(),
                                        isPreview = isPreview,
                                        context = context,
                                        onSuccess = {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Video uploaded successfully!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                            navController.popBackStack()
                                        },
                                        onError = { error ->
                                            errorMessage = error
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploadingVideo
            ) {
                if (isUploadingVideo) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    if (isUploadingVideo) "Uploading..." else "Add Video",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

enum class UploadMode {
    URL, FILE
}