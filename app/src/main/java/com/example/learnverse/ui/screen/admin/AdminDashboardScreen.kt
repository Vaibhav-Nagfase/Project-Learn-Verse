package com.example.learnverse.ui.screen.admin

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
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

    // Get the new states for the document viewer using .collectAsState()
    val isDocumentLoading by adminViewModel.isDocumentLoading.collectAsState()
    val documentBitmap by adminViewModel.documentBitmap.collectAsState()
    val documentToViewUrl by adminViewModel.documentToViewUrl.collectAsState()

    LaunchedEffect(Unit) {
        adminViewModel.fetchPendingVerifications()
    }

    // State to control which request is being rejected and show the reason dialog
    var requestToReject by remember { mutableStateOf<PendingVerification?>(null) }

    // State to control which document is being viewed and show the image dialog
    val fullBaseUrl = "https://learnverse-sy8l.onrender.com" // Your base URL

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
                            onViewDocument = { url -> adminViewModel.viewDocument(fullBaseUrl + url) }
                        )
                    }
                }
            }

            // Show the rejection dialog when a request is selected for rejection
            if (requestToReject != null) {
                RejectReasonDialog(
                    onDismiss = { requestToReject = null },
                    onConfirm = { reason ->
                        adminViewModel.rejectRequest(requestToReject!!.id, reason)
                        requestToReject = null
                    }
                )
            }

            // Show the document view dialog when a URL is selected
            if (documentToViewUrl != null) {
                DocumentViewDialog(
                    isDocumentLoading = isDocumentLoading,
                    bitmap = documentBitmap,
                    onDismiss = { adminViewModel.dismissDocumentView() }
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
    onViewDocument: (String) -> Unit
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

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Document Links
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                request.documents.idDocument?.let {
                    Button(onClick = { onViewDocument(it.viewUrl) }) { Text("View ID") }
                }
                request.documents.certificate?.let {
                    Button(onClick = { onViewDocument(it.viewUrl) }) { Text("View Certificate") }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onReject, colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Close, contentDescription = "Reject")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onApprove, colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF008000))) { // Green color
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
                modifier = Modifier.fillMaxWidth()
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

@Composable
fun DocumentViewDialog(
    isDocumentLoading: Boolean,
    bitmap: androidx.compose.ui.graphics.ImageBitmap?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {

        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Card {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isDocumentLoading) {
                    CircularProgressIndicator()
                } else if (bitmap != null) {
                    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
                        scale *= zoomChange
                        offset += offsetChange
                    }
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Verification Document",
                        modifier = Modifier
                            .fillMaxWidth()
                            // Apply the zoom and pan transformations
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            // This modifier detects the zoom and pan gestures
                            .transformable(state = state)
                    )
                } else {
                    Text("Failed to load document.")
                }
            }
        }
    }
}
