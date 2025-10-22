package com.example.learnverse.ui.screen.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.TutorViewModel
import com.example.learnverse.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateActivityScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    tutorViewModel: TutorViewModel,
    activityId: String? // This parameter determines if we are creating or editing
) {
    val uiState by tutorViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val tutorId by authViewModel.currentUserId.collectAsStateWithLifecycle()
    val tutorName by authViewModel.currentUserName.collectAsStateWithLifecycle()

    // --- LOGIC TO DETERMINE SCREEN MODE ---
    val isEditMode = activityId != null

    // This effect runs once when the screen loads (or if activityId changes).
    // If we're in edit mode, it tells the ViewModel to load the activity's data into the form.
    LaunchedEffect(activityId) {
        if (isEditMode) {
            tutorViewModel.loadActivityForEdit(activityId!!)
        }
    }

    // This effect ensures the form is cleared when the user navigates away from the screen.
    // This prevents stale data from appearing next time.
    DisposableEffect(Unit) {
        onDispose {
            tutorViewModel.resetForm()
        }
    }

    // This effect handles showing snackbars and navigating back on success.
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                tutorViewModel.resetUiState()
                navController.popBackStack()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                tutorViewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        // The title is now dynamic based on the mode.
        topBar = { TopAppBar(title = { Text(if (isEditMode) "Update Activity" else "Create New Activity") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // All the form fields are the same as you provided.
            OutlinedTextField(
                value = tutorViewModel.title,
                onValueChange = { tutorViewModel.title = it },
                label = { Text("Activity Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tutorViewModel.description,
                onValueChange = { tutorViewModel.description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            OutlinedTextField(
                value = tutorViewModel.subject,
                onValueChange = { tutorViewModel.subject = it },
                label = { Text("Subject (e.g., programming, music)") },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(
                label = "Class Type",
                selectedValue = tutorViewModel.classType,
                options = tutorViewModel.classTypeOptions,
                onValueChange = { tutorViewModel.classType = it }
            )
            ExposedDropdownMenu(
                label = "Activity Type",
                selectedValue = tutorViewModel.activityType,
                options = tutorViewModel.activityTypeOptions,
                onValueChange = { tutorViewModel.activityType = it }
            )
            ExposedDropdownMenu(
                label = "Mode",
                selectedValue = tutorViewModel.mode,
                options = tutorViewModel.modeOptions,
                onValueChange = { tutorViewModel.mode = it }
            )
            ExposedDropdownMenu(
                label = "Difficulty",
                selectedValue = tutorViewModel.difficulty,
                options = tutorViewModel.difficultyOptions,
                onValueChange = { tutorViewModel.difficulty = it }
            )
            OutlinedTextField(
                value = tutorViewModel.tags,
                onValueChange = { tutorViewModel.tags = it },
                label = { Text("Tags (comma-separated)") },
                placeholder = {Text("e.g., java, backend, oop")},
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tutorViewModel.price,
                onValueChange = { tutorViewModel.price = it },
                label = { Text("Price (INR)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = tutorViewModel.totalSessions,
                onValueChange = { tutorViewModel.totalSessions = it },
                label = { Text("Total Number of Sessions") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val id = tutorId
                    val name = tutorName ?: "Tutor"
                    if (!id.isNullOrBlank()) {
                        // This single function now handles both create and update.
                        tutorViewModel.saveActivity(id, name)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is UiState.Loading && !tutorId.isNullOrBlank()
            ) {
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    // The button text is now dynamic.
                    Text(if (isEditMode) "Save Changes" else "Create Activity")
                }
            }
        }
    }
}

/**
 * A reusable Composable for a dropdown menu with an OutlinedTextField style.
 * (This is unchanged from your provided code).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenu(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
