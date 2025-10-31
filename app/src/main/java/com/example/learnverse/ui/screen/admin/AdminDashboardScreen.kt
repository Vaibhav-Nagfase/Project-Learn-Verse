package com.example.learnverse.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.Intent
import android.net.Uri
import com.example.learnverse.data.model.PendingVerification
import com.example.learnverse.viewmodel.AdminViewModel
import com.example.learnverse.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    adminViewModel: AdminViewModel,
    authViewModel: AuthViewModel
) {
    val pendingRequests by adminViewModel.pendingVerifications.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val errorMessage by adminViewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        adminViewModel.fetchPendingVerifications()
    }

    var requestToReject by remember { mutableStateOf<PendingVerification?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(errorMessage!!, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
            } else if (pendingRequests.isEmpty()) {
                Text("No pending verifications", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Pending Verifications (${pendingRequests.size})", style = MaterialTheme.typography.titleLarge)
                    }
                    items(pendingRequests) { request ->
                        VerificationRequestCard(
                            request = request,
                            onApprove = { adminViewModel.approveRequest(request.id) },
                            onReject = { requestToReject = request },
                            context = context  // ✅ Pass context
                        )
                    }
                }
            }

            // Rejection dialog
            if (requestToReject != null) {
                RejectReasonDialog(
                    onDismiss = { requestToReject = null },
                    onConfirm = { reason ->
                        adminViewModel.rejectRequest(requestToReject!!.id, reason)
                        requestToReject = null
                    }
                )
            }
        }
    }
}

@Composable
fun VerificationRequestCard(
    request: PendingVerification,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    context: android.content.Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(request.fullName ?: "N/A", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Email: ${request.email}", style = MaterialTheme.typography.bodyMedium)
            Text("Phone: ${request.phone ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            Text("Submitted: ${request.createdAt}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            // Qualifications
            if (request.qualifications.isNullOrEmpty()) {
                Text("Qualifications:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                request.qualifications?.forEach { qual ->
                    Text("• $qual", style = MaterialTheme.typography.bodySmall)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ✅ UPDATED: Document Links - Open Cloudinary directly
            Text("Documents:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                request.documents.idDocument?.let { doc ->
                    Button(
                        onClick = {
                            // ✅ Open Cloudinary URL directly in browser
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(doc.url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("View ID")
                    }
                }

                request.documents.certificate?.let { doc ->
                    Button(
                        onClick = {
                            // ✅ Open Cloudinary URL directly in browser
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(doc.url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("View Cert")
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onReject,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onApprove,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF008000))
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Approve")
                }
            }
        }
    }
}

@Composable
fun RejectReasonDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reason for Rejection") },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Please provide a reason") },
                placeholder = { Text("E.g., Invalid document, Unclear image") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank()
            ) {
                Text("Confirm Reject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}