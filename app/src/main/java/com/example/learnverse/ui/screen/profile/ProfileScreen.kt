// ProfileScreen.kt (COMPLETE REPLACEMENT)
package com.example.learnverse.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.data.model.profile.UserProfile
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.ProfileViewModel
import com.example.learnverse.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val userRole by authViewModel.currentUserRole.collectAsState()
    val userEmail by authViewModel.currentUserEmail.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Load profile on start
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                isEditMode = false
                profileViewModel.loadProfile() // Reload to show updated data
                profileViewModel.resetUiState()
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
                    if (userProfile != null && !isEditMode) {
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
            when {
                uiState is UiState.Loading && userProfile == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                userProfile == null && !isEditMode -> {
                    // Empty profile state - show create option
                    EmptyProfileView(
                        onCreateProfile = { isEditMode = true },
                        onCheckVerification = { navController.navigate("verificationStatus") },
                        userRole = userRole,
                        onBecomeTutor = { navController.navigate("tutorVerification") }
                    )
                }
                isEditMode -> {
                    // Edit mode - show form
                    ProfileEditForm(
                        profileViewModel = profileViewModel,
                        uiState = uiState,
                        onSave = { profileViewModel.saveProfile() },
                        onCancel = {
                            isEditMode = false
                            if (userProfile != null) {
                                profileViewModel.loadProfile() // Reset form
                            }
                        }
                    )
                }
                else -> {
                    // View mode - show profile details
                    ProfileViewMode(
                        profile = userProfile!!,
                        userRole = userRole,
                        onEdit = { isEditMode = true },
                        onCheckVerification = { navController.navigate("verificationStatus") },
                        onBecomeTutor = { navController.navigate("tutorVerification") },
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyProfileView(
    onCreateProfile: () -> Unit,
    onCheckVerification: () -> Unit,
    userRole: String?,
    onBecomeTutor: () -> Unit
) {
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
            "Complete your profile for better personalized recommendations and AI assistance",
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
            Text("Create Profile")
        }

        Spacer(Modifier.height(16.dp))

        // Show verification status option for users
        if (userRole == "USER") {
            OutlinedButton(
                onClick = onCheckVerification,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.School, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Check Tutor Verification")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onBecomeTutor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Work, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Become a Tutor")
            }
        }
    }
}

@Composable
fun ProfileViewMode(
    profile: UserProfile,
    userRole: String?,
    onEdit: () -> Unit,
    onCheckVerification: () -> Unit,
    onBecomeTutor: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth() // ✅ Changed from padding to fillMaxWidth
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally // ✅ Center everything
                ) {

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${profile.firstName?.first()?.uppercaseChar() ?: ""}${profile.lastName?.first()?.uppercaseChar() ?: ""}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "${profile.firstName} ${profile.lastName}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center // ✅ Center text
                    )

                    profile.currentRole?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center // ✅ Center text
                        )
                    }
                }
            }
        }

        // Profile details
        item {
            ProfileDetailCard(
                title = "Personal Information",
                items = listOf(
                    "Location" to (profile.location ?: "Not set"),
                    "Education" to (profile.currentEducationLevel ?: "Not set"),
                    "Current Role" to (profile.currentRole ?: "Not set")
                )
            )
        }

        item {
            ProfileDetailCard(
                title = "Interests & Goals",
                items = listOf(
                    "Interests" to (profile.interests?.joinToString(", ") ?: "Not set"),
                    "Career Goal" to (profile.careerGoal ?: "Not set"),
                    "Target Skills" to (profile.targetSkills?.joinToString(", ") ?: "Not set"),
                    "Focus Area" to (profile.currentFocusArea ?: "Not set")
                )
            )
        }

        item {
            ProfileDetailCard(
                title = "Learning Preferences",
                items = listOf(
                    "Communication Style" to (profile.communicationStyle ?: "Not set"),
                    "Step-by-Step Guidance" to if (profile.wantsStepByStepGuidance == true) "Yes" else "No"
                )
            )
        }

        // Tutor verification section (only for USERs)
        if (userRole == "USER") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCheckVerification
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    "Tutor Verification Status",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Check your application",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBecomeTutor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Work,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text(
                                    "Become a Tutor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Start teaching and earn",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }

        // Logout
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLogout,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Logout",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

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

@Composable
fun ProfileEditForm(
    profileViewModel: ProfileViewModel,
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
        OutlinedTextField(value = profileViewModel.firstName, onValueChange = { profileViewModel.firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = profileViewModel.lastName, onValueChange = { profileViewModel.lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = profileViewModel.location, onValueChange = { profileViewModel.location = it }, label = { Text("Location (e.g., Nagpur, India)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = profileViewModel.currentRole, onValueChange = { profileViewModel.currentRole = it }, label = { Text("Current Role (e.g., Student, Professional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = profileViewModel.interests, onValueChange = { profileViewModel.interests = it }, label = { Text("Interests (comma-separated)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = profileViewModel.careerGoal, onValueChange = { profileViewModel.careerGoal = it }, label = { Text("Career Goal") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = profileViewModel.targetSkills, onValueChange = { profileViewModel.targetSkills = it }, label = { Text("Skills You Want to Learn (comma-separated)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = profileViewModel.currentFocusArea, onValueChange = { profileViewModel.currentFocusArea = it }, label = { Text("Current Focus Area") }, modifier = Modifier.fillMaxWidth())

        ExposedDropdownMenu(label = "Education Level", selectedValue = profileViewModel.educationLevel, options = profileViewModel.educationOptions, onValueChange = { profileViewModel.educationLevel = it })
        ExposedDropdownMenu(label = "Communication Style", selectedValue = profileViewModel.communicationStyle, options = profileViewModel.communicationStyleOptions, onValueChange = { profileViewModel.communicationStyle = it })

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
                    Text("Save")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenu(label: String, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onValueChange(option)
                    expanded = false
                })
            }
        }
    }
}