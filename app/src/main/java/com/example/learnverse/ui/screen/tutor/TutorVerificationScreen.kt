package com.example.learnverse.ui.screen.tutor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.TutorVerificationViewModel
import com.example.learnverse.viewmodel.VerificationUiState

@Composable
fun TutorVerificationScreen(
    navController: NavController,
    viewModel: TutorVerificationViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Get the email from the AuthViewModel's state
    val userEmail by authViewModel.currentUserEmail.collectAsState()

    // Allowed file types
    val allowedMimeTypes = arrayOf(
        "application/pdf",
        "image/jpeg",
        "image/jpg",
        "image/png"
    )

    // --- ActivityResultLaunchers for File Picking ---
    val idDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.idDocumentUri = uri
    }

    val certificateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.certificateUri = uri
    }

    // --- UI State Handling ---
    LaunchedEffect(uiState) {
        if (uiState is VerificationUiState.Success) {
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Become a Tutor", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Submit your details for verification.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(32.dp))

            // --- Form Fields ---
            OutlinedTextField(
                value = viewModel.fullName,
                onValueChange = { viewModel.fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.phone,
                onValueChange = { viewModel.phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- File Picker Buttons ---
            FilePickerButton(
                label = "Upload ID Document",
                selectedFileName = viewModel.idDocumentUri?.lastPathSegment,
                onClick = { idDocumentLauncher.launch("*/*") } // allow all, but backend will filter
            )
            Spacer(modifier = Modifier.height(16.dp))

            FilePickerButton(
                label = "Upload Certificate",
                selectedFileName = viewModel.certificateUri?.lastPathSegment,
                onClick = { certificateLauncher.launch("*/*") }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- Terms and Conditions ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.termsAccepted,
                    onCheckedChange = { viewModel.termsAccepted = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "I accept the terms and conditions.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- Error Message Display ---
            if (uiState is VerificationUiState.Error) {
                Text(
                    text = (uiState as VerificationUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // --- Submit Button ---
            Button(
                onClick = {
                    userEmail?.let { email ->
                        viewModel.submitVerificationRequest(email)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState !is VerificationUiState.Loading
            ) {
                if (uiState is VerificationUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit for Verification", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun FilePickerButton(
    label: String,
    selectedFileName: String?,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold)
                if (selectedFileName != null) {
                    Text(
                        text = "File: ${selectedFileName.takeLast(25)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
