package com.example.learnverse.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.ProfileViewModel
import com.example.learnverse.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    isUpdating: Boolean = false // This flag determines if we are in "setup" or "update" mode
) {
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // This effect runs once to load the user's data if they are updating their profile
    LaunchedEffect(isUpdating) {
        if (isUpdating) {
            profileViewModel.loadProfile()
        }
    }

    // This effect listens for success/error states from the ViewModel to show messages and navigate
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                if (!isUpdating) {
                    // If this was the initial setup, we tell AuthViewModel the profile now exists.
                    authViewModel.onProfileSetupComplete()
                }
                profileViewModel.resetUiState()
                navController.popBackStack()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                profileViewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (isUpdating) "Update Profile" else "Setup Your Profile") }) },
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
            // Form fields that are bound to the ProfileViewModel state
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

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Do you prefer step-by-step guidance?", modifier = Modifier.weight(1f))
                Switch(checked = profileViewModel.wantsStepByStepGuidance, onCheckedChange = { profileViewModel.wantsStepByStepGuidance = it })
            }

            Button(
                onClick = { profileViewModel.saveProfile() },
                enabled = uiState !is UiState.Loading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (isUpdating) "Update Profile" else "Save and Continue")
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

