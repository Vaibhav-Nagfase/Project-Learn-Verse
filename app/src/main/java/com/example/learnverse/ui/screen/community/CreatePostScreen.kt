package com.example.learnverse.ui.screen.community

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate // Icon for image
import androidx.compose.material.icons.filled.Close // Icon to remove selected media
import androidx.compose.material.icons.filled.Videocam // Icon for video
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.learnverse.viewmodel.CommunityUiState
import com.example.learnverse.viewmodel.CommunityViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    communityViewModel: CommunityViewModel,
    postIdToEdit: String? = null // Null for create, non-null for edit
) {
    val context = LocalContext.current
    val uiState by communityViewModel.postCreationUiState.collectAsStateWithLifecycle() // Observe state
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = postIdToEdit != null

    // --- State managed by ViewModel ---
    val postContent = communityViewModel.postContent
    val postMediaUri = communityViewModel.postMediaUri
    val postMediaType = communityViewModel.postMediaType // Store MIME type

    // --- Media Picker Launcher ---
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            communityViewModel.postMediaUri = uri
            // Determine media type (simple check, refine if needed)
            val mimeType = context.contentResolver.getType(uri)
            communityViewModel.postMediaType = mimeType
        }
    }

    // --- Load existing post data if in edit mode ---
    LaunchedEffect(postIdToEdit) {
        if (isEditMode) {
            communityViewModel.loadPostForEditing(postIdToEdit!!)
        } else {
            // Ensure form is clear when entering create mode
            communityViewModel.resetPostCreationState()
        }
    }

    // --- Handle Success/Error States ---
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CommunityUiState.Success -> {
                snackbarHostState.showSnackbar(state.message ?: if (isEditMode) "Post updated!" else "Post created!")
                communityViewModel.resetPostCreationState() // Clear state
                navController.popBackStack() // Go back after success
            }
            is CommunityUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                communityViewModel.resetPostCreationState() // Reset state even on error
            }
            else -> {}
        }
    }

    // --- Clean up state when leaving the screen ---
    DisposableEffect(Unit) {
        onDispose {
            communityViewModel.resetPostCreationState()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Post" else "Create Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Content Input ---
            OutlinedTextField(
                value = postContent,
                onValueChange = { communityViewModel.postContent = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                placeholder = { Text("What's on your mind?") },
                label = { Text("Post Content") }
            )

            // --- Media Selection ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button to select Image
                Button(onClick = { mediaPickerLauncher.launch("image/*") }) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Image")
                    Spacer(Modifier.width(8.dp))
                    Text("Image")
                }
                // Button to select Video
                Button(onClick = { mediaPickerLauncher.launch("video/*") }) {
                    Icon(Icons.Default.Videocam, contentDescription = "Add Video")
                    Spacer(Modifier.width(8.dp))
                    Text("Video")
                }
            }

            // --- Media Preview ---
            if (postMediaUri != null) {
                Box(contentAlignment = Alignment.TopEnd) {
                    when {
                        postMediaType?.startsWith("image/") == true -> {
                            Image(
                                painter = rememberAsyncImagePainter(postMediaUri),
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        }
                        postMediaType?.startsWith("video/") == true -> {
                            // TODO: Consider showing a video thumbnail or basic player here
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) { Text("Video Selected: ${getFileName(context, postMediaUri)}") }
                        }
                        else -> {
                            Text("Unsupported file type selected.")
                        }
                    }
                    // Button to remove selected media
                    IconButton(
                        onClick = {
                            communityViewModel.postMediaUri = null
                            communityViewModel.postMediaType = null
                        },
                        modifier = Modifier.padding(4.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove Media", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Push button to bottom

            // --- Submit Button ---
            Button(
                onClick = {
                    // Convert URI to File before calling ViewModel
//                    val file = postMediaUri?.let { uriToFile(context, it, getFileName(context, it)) }
                    if (isEditMode) {
                        communityViewModel.updatePost(postIdToEdit!!, context, postContent.ifBlank { null }, postMediaUri, postMediaType)
                    } else {
                        communityViewModel.createPost(context, postContent.ifBlank { null }, postMediaUri, postMediaType)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = uiState !is CommunityUiState.Loading && (postContent.isNotBlank() || postMediaUri != null)
            ) {
                if (uiState is CommunityUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isEditMode) "Update Post" else "Post")
                }
            }
        }
    }
}


// --- Helper functions to convert URI to File ---
// (Copied/Adapted from TutorVerificationViewModel - move to a common utils file later)
//private fun uriToFile(context: Context, uri: Uri, fileName: String): File? {
//    var inputStream: InputStream? = null
//    var outputStream: FileOutputStream? = null
//    return try {
//        inputStream = context.contentResolver.openInputStream(uri)
//        val file = File(context.cacheDir, fileName) // Save to cache directory
//        outputStream = FileOutputStream(file)
//        inputStream?.copyTo(outputStream)
//        file // Return the file
//    } catch (e: Exception) {
//        e.printStackTrace()
//        null
//    } finally {
//        inputStream?.close()
//        outputStream?.close()
//    }
//}
//
private fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use { // Use ensures cursor is closed
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex != -1) {
                    result = it.getString(columnIndex)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    // Add a fallback filename if everything else fails
    return result ?: "temp_media_file_${System.currentTimeMillis()}"
}