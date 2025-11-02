// This replaces your entire MyTutorProfileScreen.kt file

package com.example.learnverse.ui.screen.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.learnverse.data.model.TutorMyProfileResponse
import com.example.learnverse.viewmodel.MyTutorProfileViewModel
import com.example.learnverse.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTutorProfileScreen(
    navController: NavController,
    profileViewModel: MyTutorProfileViewModel
) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    var isEditMode by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                isEditMode = false // Exit edit mode on success
                profileViewModel.resetUiState()
                navController.navigateUp()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                profileViewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Profile" else "My Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Only show Edit button if a profile exists and we aren't already editing
                    if (profile != null && !isEditMode) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, "Edit Profile")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val currentProfile = profile
            val currentState = uiState

            if (currentState is UiState.Loading) {
                // 1. Show loading spinner
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            } else if (currentState is UiState.Error) {
                // 2. Show error
                Text(
                    text = currentState.message,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                // ✅ --- START OF FIX 1 ---
                // Check for isEditMode FIRST
            } else if (isEditMode) {
                // 3. Show EDIT mode (whether profile is null or not)
                TutorProfileEditForm(
                    profileViewModel = profileViewModel,
                    uiState = uiState,
                    onSave = {
                        profileViewModel.saveProfile()
                        // ✅ --- START OF FIX 2 ---
                        // REMOVED the loadProfile() call
                        // We will now rely on the LaunchedEffect to set isEditMode
                        // ✅ --- END OF FIX 2 ---
                    },
                    onCancel = {
                        isEditMode = false
                        profileViewModel.loadProfile() // Reset changes
                    }
                )

            } else if (currentProfile != null) {
                // 4. Show VIEW mode (only if not editing and profile exists)
                TutorProfileViewMode(profile = currentProfile)

            } else {
                // 5. Show EMPTY state (not editing, no profile, not loading)
                EmptyTutorProfileView(
                    onCreateProfile = { isEditMode = true }
                )
            }
            // ✅ --- END OF FIX 1 ---
        }
    }
}


// ✅ --- NEW COMPOSABLE for Empty State ---
// Inspired by your user ProfileScreen's EmptyProfileView
@Composable
fun EmptyTutorProfileView(onCreateProfile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Your Profile Looks Empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Add your professional details so students can learn more about you.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onCreateProfile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Create Profile Details")
        }
    }
}

// --- VIEW MODE ---
@Composable
fun TutorProfileViewMode(profile: TutorMyProfileResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = profile.profilePicture?.url,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = profile.fullName ?: "No Name",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Read-only Details
        ProfileDetailCard(
            title = "Contact & Status",
            items = listOf(
                // ✅ START FIX
                "Email" to (profile.email ?: "Not set"),
                "Phone" to (profile.phone ?: "Not set"),
                "Status" to (profile.status ?: "Not set")
                // ✅ END FIX
            )
        )

        // Editable Details (in View mode)
        ProfileDetailCard(
            title = "Professional Info",
            items = listOf(
                // ✅ START FIX
                "Bio" to (profile.bio ?: "Not set"),
                "Experience" to (profile.experience ?: "Not set"),
                // ✅ END FIX

                // ✅ --- START OF FIX ---
                "Qualifications" to (profile.qualifications
                    ?.joinToString(", ")
                    ?.replace("\"", "") ?: "Not set"), // Use ?: "Not set"

                "Specializations" to (profile.specializations
                    ?.joinToString(", ")
                    ?.replace("\"", "") ?: "Not set") // Use ?: "Not set"
                // ✅ --- END OF FIX ---

            )
        )
    }
}

// --- EDIT MODE ---
@Composable
fun TutorProfileEditForm(
    profileViewModel: MyTutorProfileViewModel,
    uiState: UiState,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "You can only update these four fields.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Bio
        OutlinedTextField(
            value = profileViewModel.bio,
            onValueChange = { profileViewModel.bio = it },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth().height(150.dp)
        )

        // Experience
        OutlinedTextField(
            value = profileViewModel.experience,
            onValueChange = { profileViewModel.experience = it },
            label = { Text("Experience") },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )

        // Qualifications
        OutlinedTextField(
            value = profileViewModel.qualifications,
            onValueChange = { profileViewModel.qualifications = it },
            label = { Text("Qualifications (comma-separated)") },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )

        // Specializations
        OutlinedTextField(
            value = profileViewModel.specializations,
            onValueChange = { profileViewModel.specializations = it },
            label = { Text("Specializations (comma-separated)") },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )

        // Save/Cancel Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = uiState !is UiState.Loading
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = uiState !is UiState.Loading
            ) {
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Changes")
                }
            }
        }
    }
}

// Reusable card from your ProfileScreen.kt
@Composable
fun ProfileDetailCard(title: String, items: List<Pair<String, String>>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.End
                    )
                }
                if (items.last() != (label to value)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}