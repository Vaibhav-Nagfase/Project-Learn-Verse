package com.example.learnverse.ui.screen.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Update Activity" else "Create New Activity") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === BASIC INFO ===
            Text(
                "Basic Information",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = tutorViewModel.title,
                onValueChange = { tutorViewModel.title = it },
                label = { Text("Activity Title *") },
                placeholder = { Text("e.g., Java Programming Masterclass") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tutorViewModel.description,
                onValueChange = { tutorViewModel.description = it },
                label = { Text("Description *") },
                placeholder = { Text("Describe what students will learn...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = tutorViewModel.subject,
                onValueChange = { tutorViewModel.subject = it },
                label = { Text("Subject *") },
                placeholder = { Text("e.g., Programming") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenu(
                    label = "Activity Type *",
                    selectedValue = tutorViewModel.activityType,
                    options = tutorViewModel.activityTypeOptions,
                    onValueChange = { tutorViewModel.activityType = it },
                    modifier = Modifier.weight(1f)
                )

                ExposedDropdownMenu(
                    label = "Mode *",
                    selectedValue = tutorViewModel.mode,
                    options = tutorViewModel.modeOptions,
                    onValueChange = { tutorViewModel.mode = it },
                    modifier = Modifier.weight(1f)
                )
            }

            ExposedDropdownMenu(
                label = "Difficulty *",
                selectedValue = tutorViewModel.difficulty,
                options = tutorViewModel.difficultyOptions,
                onValueChange = { tutorViewModel.difficulty = it }
            )

            Divider()

            // === PRICING ===
            Text(
                "Pricing",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = tutorViewModel.price,
                    onValueChange = { tutorViewModel.price = it },
                    label = { Text("Price (₹) *") },
                    placeholder = { Text("999") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = tutorViewModel.discountPrice,
                    onValueChange = { tutorViewModel.discountPrice = it },
                    label = { Text("Discount Price (₹)") },
                    placeholder = { Text("799") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            ExposedDropdownMenu(
                label = "Price Type *",
                selectedValue = tutorViewModel.priceType,
                options = tutorViewModel.priceTypeOptions,
                onValueChange = { tutorViewModel.priceType = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = tutorViewModel.installmentAvailable,
                    onCheckedChange = { tutorViewModel.installmentAvailable = it }
                )
                Text("Installment Available")
            }

            Divider()

            // === DURATION & SCHEDULE ===
            Text(
                "Duration & Schedule",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = tutorViewModel.totalSessions,
                    onValueChange = { tutorViewModel.totalSessions = it },
                    label = { Text("Total Sessions *") },
                    placeholder = { Text("40") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = tutorViewModel.estimatedDuration,
                    onValueChange = { tutorViewModel.estimatedDuration = it },
                    label = { Text("Duration (hours) *") },
                    placeholder = { Text("120") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            OutlinedTextField(
                value = tutorViewModel.durationDescription,
                onValueChange = { tutorViewModel.durationDescription = it },
                label = { Text("Duration Description") },
                placeholder = { Text("40 sessions over 3 months") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = tutorViewModel.selfPaced,
                    onCheckedChange = { tutorViewModel.selfPaced = it }
                )
                Text("Self-Paced Learning")
            }

            if (tutorViewModel.selfPaced) {
                OutlinedTextField(
                    value = tutorViewModel.accessDuration,
                    onValueChange = { tutorViewModel.accessDuration = it },
                    label = { Text("Access Duration (days)") },
                    placeholder = { Text("365") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = tutorViewModel.lifetimeAccess,
                    onCheckedChange = { tutorViewModel.lifetimeAccess = it }
                )
                Text("Lifetime Access")
            }

            Divider()

            // === AGE GROUP ===
            Text(
                "Suitable Age Group",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = tutorViewModel.minAge,
                    onValueChange = { tutorViewModel.minAge = it },
                    label = { Text("Min Age") },
                    placeholder = { Text("15") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = tutorViewModel.maxAge,
                    onValueChange = { tutorViewModel.maxAge = it },
                    label = { Text("Max Age") },
                    placeholder = { Text("50") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            OutlinedTextField(
                value = tutorViewModel.ageDescription,
                onValueChange = { tutorViewModel.ageDescription = it },
                label = { Text("Age Description") },
                placeholder = { Text("Best suited for college students") },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            // === PREREQUISITES ===
            Text(
                "Prerequisites",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = tutorViewModel.prerequisites,
                onValueChange = { tutorViewModel.prerequisites = it },
                label = { Text("Prerequisites (comma-separated)") },
                placeholder = { Text("Basic computer knowledge, Interest in programming") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Divider()

            // === DEMO & TRIAL ===
            Text(
                "Demo & Free Trial",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = tutorViewModel.demoAvailable,
                    onCheckedChange = { tutorViewModel.demoAvailable = it }
                )
                Text("Demo Available")
            }

            if (tutorViewModel.demoAvailable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tutorViewModel.freeTrial,
                        onCheckedChange = { tutorViewModel.freeTrial = it }
                    )
                    Text("Free Trial")
                }

                if (tutorViewModel.freeTrial) {
                    OutlinedTextField(
                        value = tutorViewModel.trialDuration,
                        onValueChange = { tutorViewModel.trialDuration = it },
                        label = { Text("Trial Duration (days)") },
                        placeholder = { Text("7") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Divider()

            // === TAGS ===
            OutlinedTextField(
                value = tutorViewModel.tags,
                onValueChange = { tutorViewModel.tags = it },
                label = { Text("Tags (comma-separated) *") },
                placeholder = { Text("Java, Programming, Backend, Spring Boot") },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            // === VISIBILITY ===
            Text(
                "Visibility Settings",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = tutorViewModel.isPublic,
                    onCheckedChange = { tutorViewModel.isPublic = it }
                )
                Text("Make Public (visible to all users)")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = tutorViewModel.featured,
                    onCheckedChange = { tutorViewModel.featured = it }
                )
                Text("Request to Feature")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === SUBMIT BUTTON ===
            Button(
                onClick = {
                    val id = tutorId
                    val name = tutorName ?: "Tutor"
                    if (!id.isNullOrBlank()) {
                        tutorViewModel.saveActivity(id, name)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is UiState.Loading && !tutorId.isNullOrBlank()
            ) {
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
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
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
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