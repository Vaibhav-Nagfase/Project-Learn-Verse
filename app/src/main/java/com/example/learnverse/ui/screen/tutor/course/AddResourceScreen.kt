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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learnverse.data.model.AddResourceRequest
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.data.repository.VideoManagementRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResourceScreen(
    activityId: String,
    videoId: String,
    navController: NavController,
    apiService: ApiService
) {
    val repository = remember { VideoManagementRepository(apiService) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var resourceUrl by remember { mutableStateOf("") }
    var resourceType by remember { mutableStateOf("PDF") }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val resourceTypes = listOf("PDF", "Link", "Document", "Code", "Image")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Resource") },
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
                "Resource Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Resource Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = title.isBlank() && errorMessage != null
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = resourceType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Resource Type *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    resourceTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                resourceType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = resourceUrl,
                onValueChange = { resourceUrl = it },
                label = { Text("Resource URL *") },
                placeholder = { Text("https://...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = resourceUrl.isBlank() && errorMessage != null,
                leadingIcon = { Icon(Icons.Default.Link, null) }
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
                    when {
                        title.isBlank() -> errorMessage = "Title is required"
                        resourceUrl.isBlank() -> errorMessage = "Resource URL is required"
                        else -> {
                            errorMessage = null
                            isLoading = true

                            scope.launch {
                                try {
                                    val request = AddResourceRequest(
                                        type = resourceType,
                                        title = title,
                                        url = resourceUrl
                                    )

                                    repository.addResource(activityId, videoId, request)
                                    snackbarHostState.showSnackbar("Resource added successfully")
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Failed to add resource"
                                    isLoading = false
                                }
                            }
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
                Text(if (isLoading) "Adding..." else "Add Resource")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
