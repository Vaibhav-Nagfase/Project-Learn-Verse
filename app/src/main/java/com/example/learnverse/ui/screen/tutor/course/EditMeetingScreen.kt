package com.example.learnverse.ui.screen.tutor.course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learnverse.data.model.UpdateMeetingRequest
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.data.repository.VideoManagementRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMeetingScreen(
    activityId: String,
    navController: NavController,
    apiService: ApiService
) {
    val repository = remember { VideoManagementRepository(apiService) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var platform by remember { mutableStateOf("") }
    var meetingLink by remember { mutableStateOf("") }
    var meetingId by remember { mutableStateOf("") }
    var passcode by remember { mutableStateOf("") }
    var showPasscode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load existing meeting data
    LaunchedEffect(activityId) {
        try {
            val meetingInfo = repository.getMeetingInfo(activityId)
            platform = meetingInfo?.platform ?: ""
            meetingLink = meetingInfo?.meetingLink ?: ""
            meetingId = meetingInfo?.meetingId ?: ""
            passcode = meetingInfo?.passcode ?: ""
        } catch (e: Exception) {
            // Silent fail - form can be used to create new meeting info
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Meeting Info") },
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
                "Meeting Configuration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "Configure live meeting details for students. Supports Zoom, Google Meet, Teams, etc.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedTextField(
                value = platform,
                onValueChange = { platform = it },
                label = { Text("Platform") },
                placeholder = { Text("e.g., Zoom, Google Meet") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.VideoCall, null) }
            )

            OutlinedTextField(
                value = meetingLink,
                onValueChange = { meetingLink = it },
                label = { Text("Meeting Link") },
                placeholder = { Text("https://...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Link, null) }
            )

            OutlinedTextField(
                value = meetingId,
                onValueChange = { meetingId = it },
                label = { Text("Meeting ID") },
                placeholder = { Text("123 456 789") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Tag, null) }
            )

            OutlinedTextField(
                value = passcode,
                onValueChange = { passcode = it },
                label = { Text("Passcode") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showPasscode) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showPasscode = !showPasscode }) {
                        Icon(
                            if (showPasscode) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Toggle password"
                        )
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            errorMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            val request = UpdateMeetingRequest(
                                platform = platform.ifBlank { null },
                                meetingLink = meetingLink.ifBlank { null },
                                meetingId = meetingId.ifBlank { null },
                                passcode = passcode.ifBlank { null }
                            )

                            repository.updateMeeting(activityId, request)
                            snackbarHostState.showSnackbar("Meeting info updated")
                            navController.popBackStack()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to update meeting"
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isLoading) "Saving..." else "Save Changes")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
