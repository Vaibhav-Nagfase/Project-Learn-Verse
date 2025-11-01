package com.example.learnverse.ui.screen.community

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.learnverse.viewmodel.CommunityUiState
import com.example.learnverse.viewmodel.CommunityViewModel
import android.net.Uri
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCreatePostScreen(
    navController: NavController,
    communityViewModel: CommunityViewModel,
    postIdToEdit: String? = null
) {
    val context = LocalContext.current
    val uiState by communityViewModel.postCreationUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = postIdToEdit != null
    val postContent = communityViewModel.postContent
    val postMediaUri = communityViewModel.postMediaUri
    val postMediaType = communityViewModel.postMediaType

    // Animation states
    var contentFocused by remember { mutableStateOf(false) }
    var showMediaOptions by remember { mutableStateOf(false) }

    // Media picker launcher
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            communityViewModel.postMediaUri = uri
            val mimeType = context.contentResolver.getType(uri)
            communityViewModel.postMediaType = mimeType
            showMediaOptions = false
        }
    }

    // Load post for editing
    LaunchedEffect(postIdToEdit) {
        if (isEditMode) {
            communityViewModel.loadPostForEditing(postIdToEdit!!)
        } else {
            communityViewModel.resetPostCreationState()
        }
    }

    // Handle success/error states
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CommunityUiState.Success -> {
                snackbarHostState.showSnackbar(
                    message = state.message ?: if (isEditMode) "Post updated!" else "Post created!",
                    duration = SnackbarDuration.Short
                )
                communityViewModel.resetPostCreationState()
                navController.popBackStack()
            }
            is CommunityUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
                communityViewModel.resetPostCreationState()
            }
            else -> {}
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            communityViewModel.resetPostCreationState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Post" else "Create Post",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Post button in app bar
                    TextButton(
                        onClick = {
                            if (isEditMode) {
                                communityViewModel.updatePost(
                                    postIdToEdit!!,
                                    context,
                                    postContent.ifBlank { null },
                                    postMediaUri,
                                    postMediaType
                                )
                            } else {
                                communityViewModel.createPost(
                                    context,
                                    postContent.ifBlank { null },
                                    postMediaUri,
                                    postMediaType
                                )
                            }
                        },
                        enabled = uiState !is CommunityUiState.Loading &&
                                (postContent.isNotBlank() || postMediaUri != null)
                    ) {
                        if (uiState is CommunityUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (isEditMode) "Update" else "Post",
                                fontWeight = FontWeight.Bold,
                                color = if (postContent.isNotBlank() || postMediaUri != null)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Content input field
            OutlinedTextField(
                value = postContent,
                onValueChange = { communityViewModel.postContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(min = 120.dp),
                placeholder = {
                    Text(
                        "What's on your mind?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Media preview
            AnimatedVisibility(
                visible = postMediaUri != null,
                enter = expandVertically(spring()) + fadeIn(),
                exit = shrinkVertically(spring()) + fadeOut()
            ) {
                MediaPreview(
                    uri = postMediaUri,
                    mediaType = postMediaType,
                    onRemove = {
                        communityViewModel.postMediaUri = null
                        communityViewModel.postMediaType = null
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Media options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MediaOptionButton(
                        icon = Icons.Outlined.Image,
                        label = "Photo",
                        onClick = { mediaPickerLauncher.launch("image/*") }
                    )

                    MediaOptionButton(
                        icon = Icons.Outlined.Videocam,
                        label = "Video",
                        onClick = { mediaPickerLauncher.launch("video/*") }
                    )

                    MediaOptionButton(
                        icon = Icons.Outlined.Gif,
                        label = "GIF",
                        onClick = { /* TODO: Implement GIF picker */ },
                        enabled = false
                    )
                }
            }

            // Tips card
            AnimatedVisibility(
                visible = postContent.isEmpty() && postMediaUri == null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Tips for great posts",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "• Share educational content\n• Use clear images or videos\n• Engage with your students",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaPreview(
    uri: Uri?,
    mediaType: String?,
    onRemove: () -> Unit
) {
    if (uri == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            when {
                mediaType?.startsWith("image/") == true -> {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                mediaType?.startsWith("video/") == true -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.Videocam,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Video selected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove media",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun MediaOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (enabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (enabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}