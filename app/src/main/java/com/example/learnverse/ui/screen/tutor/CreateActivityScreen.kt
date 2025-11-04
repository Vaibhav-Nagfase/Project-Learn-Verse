package com.example.learnverse.ui.screen.tutor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.TutorViewModel
import com.example.learnverse.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateActivityScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    tutorViewModel: TutorViewModel,
    activityId: String?
) {
    val uiState by tutorViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val tutorId by authViewModel.currentUserId.collectAsStateWithLifecycle()
    val tutorName by authViewModel.currentUserName.collectAsStateWithLifecycle()

    val isEditMode = activityId != null

    // Image pickers
    val bannerImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { tutorViewModel.selectedBannerImageUri = it }
    }

    val profileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { tutorViewModel.selectedProfileImageUri = it }
    }

    // Expandable sections state
    var basicInfoExpanded by remember { mutableStateOf(true) }
    var locationExpanded by remember { mutableStateOf(false) }
    var pricingExpanded by remember { mutableStateOf(false) }
    var durationExpanded by remember { mutableStateOf(false) }
    var ageGroupExpanded by remember { mutableStateOf(false) }
    var instructorExpanded by remember { mutableStateOf(false) }
    var contactExpanded by remember { mutableStateOf(false) }
    var visibilityExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(activityId) {
        if (isEditMode) {
            tutorViewModel.loadActivityForEdit(activityId!!)
        }
    }

    DisposableEffect(Unit) {
        onDispose { tutorViewModel.resetForm() }
    }

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
                title = { Text(if (isEditMode) "Update Activity" else "Create Activity") },
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
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // === BANNER IMAGE ===
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Activity Banner", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (tutorViewModel.selectedBannerImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(tutorViewModel.selectedBannerImageUri),
                            contentDescription = "Banner preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { bannerImagePicker.launch("image/*") }) {
                            Icon(Icons.Default.Edit, "Change")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change Banner")
                        }
                    } else {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clickable { bannerImagePicker.launch("image/*") },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Add banner",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tap to add banner image", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // === BASIC INFORMATION ===
            ExpandableSection(
                title = "Basic Information",
                icon = Icons.Default.Info,
                expanded = basicInfoExpanded,
                onToggle = { basicInfoExpanded = !basicInfoExpanded }
            ) {
                BasicInformationSection(tutorViewModel)
            }

            // === LOCATION ===
            ExpandableSection(
                title = "Location (for offline/hybrid)",
                icon = Icons.Default.LocationOn,
                expanded = locationExpanded,
                onToggle = { locationExpanded = !locationExpanded }
            ) {
                LocationSection(tutorViewModel)
            }

            // === PRICING ===
            ExpandableSection(
                title = "Pricing",
                icon = Icons.Default.AttachMoney,
                expanded = pricingExpanded,
                onToggle = { pricingExpanded = !pricingExpanded }
            ) {
                PricingSection(tutorViewModel)
            }

            // === DURATION & SCHEDULE ===
            ExpandableSection(
                title = "Duration & Schedule",
                icon = Icons.Default.Schedule,
                expanded = durationExpanded,
                onToggle = { durationExpanded = !durationExpanded }
            ) {
                DurationScheduleSection(tutorViewModel)
            }

            // === AGE GROUP ===
            ExpandableSection(
                title = "Age Group",
                icon = Icons.Default.Group,
                expanded = ageGroupExpanded,
                onToggle = { ageGroupExpanded = !ageGroupExpanded }
            ) {
                AgeGroupSection(tutorViewModel)
            }

            // === INSTRUCTOR DETAILS ===
            ExpandableSection(
                title = "Instructor Profile",
                icon = Icons.Default.Person,
                expanded = instructorExpanded,
                onToggle = { instructorExpanded = !instructorExpanded }
            ) {
                InstructorSection(tutorViewModel, profileImagePicker)
            }

            // === CONTACT INFORMATION ===
            ExpandableSection(
                title = "Contact Information",
                icon = Icons.Default.ContactPhone,
                expanded = contactExpanded,
                onToggle = { contactExpanded = !contactExpanded }
            ) {
                ContactSection(tutorViewModel)
            }

            // === VISIBILITY SETTINGS ===
            ExpandableSection(
                title = "Visibility & Publishing",
                icon = Icons.Default.Visibility,
                expanded = visibilityExpanded,
                onToggle = { visibilityExpanded = !visibilityExpanded }
            ) {
                VisibilitySection(tutorViewModel)
            }

            // === SUBMIT BUTTON ===
            Button(
                onClick = {
                    val id = tutorId
                    val name = tutorName ?: "Tutor"
                    if (!id.isNullOrBlank()) {
                        tutorViewModel.saveActivity(id, name)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = uiState !is UiState.Loading && !tutorId.isNullOrBlank()
            ) {
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isEditMode) "Save Changes" else "Create Activity")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// === EXPANDABLE SECTION COMPONENT ===
@Composable
private fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            // Content
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

// === SECTION COMPONENTS ===

@Composable
private fun BasicInformationSection(tutorViewModel: TutorViewModel) {
    OutlinedTextField(
        value = tutorViewModel.title,
        onValueChange = { tutorViewModel.title = it },
        label = { Text("Title *") },
        placeholder = { Text("Java Programming Masterclass") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = tutorViewModel.description,
        onValueChange = { tutorViewModel.description = it },
        label = { Text("Description *") },
        placeholder = { Text("Comprehensive course covering...") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )

    OutlinedTextField(
        value = tutorViewModel.subject,
        onValueChange = { tutorViewModel.subject = it },
        label = { Text("Subject *") },
        placeholder = { Text("Programming") },
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

    OutlinedTextField(
        value = tutorViewModel.prerequisites,
        onValueChange = { tutorViewModel.prerequisites = it },
        label = { Text("Prerequisites (comma-separated)") },
        placeholder = { Text("Basic computer knowledge") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2
    )

    OutlinedTextField(
        value = tutorViewModel.tags,
        onValueChange = { tutorViewModel.tags = it },
        label = { Text("Tags (comma-separated) *") },
        placeholder = { Text("Java, Programming, Backend") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LocationSection(tutorViewModel: TutorViewModel) {
    OutlinedTextField(
        value = tutorViewModel.address,
        onValueChange = { tutorViewModel.address = it },
        label = { Text("Address") },
        placeholder = { Text("123 Main Street") },
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = tutorViewModel.city,
            onValueChange = { tutorViewModel.city = it },
            label = { Text("City") },
            placeholder = { Text("Mumbai") },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = tutorViewModel.state,
            onValueChange = { tutorViewModel.state = it },
            label = { Text("State") },
            placeholder = { Text("Maharashtra") },
            modifier = Modifier.weight(1f)
        )
    }

    OutlinedTextField(
        value = tutorViewModel.landmark,
        onValueChange = { tutorViewModel.landmark = it },
        label = { Text("Landmark") },
        placeholder = { Text("Near Central Park") },
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = tutorViewModel.latitude,
            onValueChange = { tutorViewModel.latitude = it },
            label = { Text("Latitude") },
            placeholder = { Text("19.0760") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        OutlinedTextField(
            value = tutorViewModel.longitude,
            onValueChange = { tutorViewModel.longitude = it },
            label = { Text("Longitude") },
            placeholder = { Text("72.8777") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }

    OutlinedTextField(
        value = tutorViewModel.facilities,
        onValueChange = { tutorViewModel.facilities = it },
        label = { Text("Facilities (comma-separated)") },
        placeholder = { Text("WiFi, Parking, AC Rooms") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PricingSection(tutorViewModel: TutorViewModel) {
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
            label = { Text("Discount Price") },
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

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = tutorViewModel.installmentAvailable,
            onCheckedChange = { tutorViewModel.installmentAvailable = it }
        )
        Text("Installment Available")
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = tutorViewModel.demoAvailable,
            onCheckedChange = { tutorViewModel.demoAvailable = it }
        )
        Text("Demo Available")
    }

    if (tutorViewModel.demoAvailable) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
}

@Composable
private fun DurationScheduleSection(tutorViewModel: TutorViewModel) {
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

    Row(verticalAlignment = Alignment.CenterVertically) {
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

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = tutorViewModel.lifetimeAccess,
            onCheckedChange = { tutorViewModel.lifetimeAccess = it }
        )
        Text("Lifetime Access")
    }
}

@Composable
private fun AgeGroupSection(tutorViewModel: TutorViewModel) {
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
        placeholder = { Text("Best for college students") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun InstructorSection(
    tutorViewModel: TutorViewModel,
    profileImagePicker: androidx.activity.result.ActivityResultLauncher<String>
) {
    // Profile image
    if (tutorViewModel.selectedProfileImageUri != null) {
        Image(
            painter = rememberAsyncImagePainter(tutorViewModel.selectedProfileImageUri),
            contentDescription = "Profile preview",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(50.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { profileImagePicker.launch("image/*") }) {
            Text("Change Photo")
        }
    } else {
        OutlinedButton(
            onClick = { profileImagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AddAPhoto, "Add photo")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Profile Picture")
        }
    }

    OutlinedTextField(
        value = tutorViewModel.instructorBio,
        onValueChange = { tutorViewModel.instructorBio = it },
        label = { Text("Bio") },
        placeholder = { Text("Tell students about yourself") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )

    OutlinedTextField(
        value = tutorViewModel.qualifications,
        onValueChange = { tutorViewModel.qualifications = it },
        label = { Text("Qualifications (comma-separated)") },
        placeholder = { Text("B.Tech CSE, Oracle Certified") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = tutorViewModel.experience,
        onValueChange = { tutorViewModel.experience = it },
        label = { Text("Experience") },
        placeholder = { Text("5+ years of teaching") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = tutorViewModel.specializations,
        onValueChange = { tutorViewModel.specializations = it },
        label = { Text("Specializations (comma-separated)") },
        placeholder = { Text("Java, Spring Boot, Microservices") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ContactSection(tutorViewModel: TutorViewModel) {
    OutlinedTextField(
        value = tutorViewModel.enrollmentLink,
        onValueChange = { tutorViewModel.enrollmentLink = it },
        label = { Text("Enrollment Link") },
        placeholder = { Text("https://...") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = tutorViewModel.whatsappNumber,
        onValueChange = { tutorViewModel.whatsappNumber = it },
        label = { Text("WhatsApp Number") },
        placeholder = { Text("+91 98765 43210") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )

    OutlinedTextField(
        value = tutorViewModel.email,
        onValueChange = { tutorViewModel.email = it },
        label = { Text("Contact Email") },
        placeholder = { Text("contact@example.com") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )

    OutlinedTextField(
        value = tutorViewModel.youtubeLink,
        onValueChange = { tutorViewModel.youtubeLink = it },
        label = { Text("YouTube Channel") },
        placeholder = { Text("https://youtube.com/@channel") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = tutorViewModel.instagramLink,
        onValueChange = { tutorViewModel.instagramLink = it },
        label = { Text("Instagram Profile") },
        placeholder = { Text("https://instagram.com/profile") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun VisibilitySection(tutorViewModel: TutorViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = tutorViewModel.isPublic,
            onCheckedChange = { tutorViewModel.isPublic = it }
        )
        Text("Make Public (visible to all)")
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = tutorViewModel.featured,
            onCheckedChange = { tutorViewModel.featured = it }
        )
        Text("Request to Feature")
    }
}

// === DROPDOWN MENU COMPONENT ===
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