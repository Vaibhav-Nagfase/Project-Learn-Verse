package com.example.learnverse.ui.screen.interest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learnverse.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InterestManagementScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Fetch the latest interests when the screen is first shown
    LaunchedEffect(Unit) {
        authViewModel.fetchUserInterests()
    }

    val userInterests by authViewModel.userInterests.collectAsState()

    // This is the master list of all possible interests
    val allInterests = listOf("Music", "Art", "VFX", "Swimming", "Karate", "Coding", "Science", "Dance", "Chess", "Photography")
    // We calculate the available interests by removing the ones the user already has
    val availableInterests = allInterests.filter { it !in userInterests }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage My Interests") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Simply navigate back. The changes are already saved with each click.
                navController.navigateUp()
            }) {
                Icon(Icons.Default.Check, contentDescription = "Done")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Section 1: User's Current Interests ---
            item {
                Text(
                    text = "MY INTERESTS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (userInterests.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        userInterests.forEach { interest ->
                            InputChip(
                                selected = false,
                                onClick = { /* Clicks on the main chip do nothing */ },
                                label = { Text(interest) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove interest",
                                        modifier = Modifier.size(InputChipDefaults.IconSize)
                                            .clickable {
                                                authViewModel.removeInterests(listOf(interest))
                                            }
                                    )
                                }
                            )
                        }
                    }
                } else {
                    Text("You haven't added any interests yet. Choose from the list below!")
                }
            }

            // --- Section 2: Available Interests to Add ---
            item {
                Text(
                    text = "AVAILABLE INTERESTS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableInterests.forEach { interest ->
                        FilterChip(
                            selected = false, // Not selectable in the traditional sense
                            onClick = { authViewModel.addInterests(listOf(interest)) },
                            label = { Text(interest) },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = "Add interest", modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                        )
                    }
                }
            }
        }
    }
}