package com.example.learnverse.ui.screen.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learnverse.viewmodel.FilterViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterScreen(
    navController: NavController,
    viewModel: FilterViewModel
) {
    val isLoading by viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filters") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // This bottom bar contains the Clear and Apply buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.clearFilters() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear all")
                }
                Button(
                    onClick = {
                        viewModel.applyFilters { filteredActivities ->
                            // This sends the results back to the SearchScreen
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("filtered_activities", filteredActivities)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Apply")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // --- MODE Section ---
            item {
                FilterSection("Mode") {
                    val allModes = listOf("Online", "Offline")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        allModes.forEach { mode ->
                            FilterChip(
                                selected = mode in viewModel.selectedModes,
                                onClick = {
                                    if (mode in viewModel.selectedModes) viewModel.selectedModes.remove(mode)
                                    else viewModel.selectedModes.add(mode)
                                },
                                label = { Text(mode) }
                            )
                        }
                    }
                }
            }

            // --- DIFFICULTY Section ---
            item {
                FilterSection("Difficulty") {
                    val allDifficulties = listOf("Beginner", "Intermediate", "Advanced")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        allDifficulties.forEach { difficulty ->
                            FilterChip(
                                selected = difficulty in viewModel.selectedDifficulties,
                                onClick = {
                                    if (difficulty in viewModel.selectedDifficulties) viewModel.selectedDifficulties.remove(difficulty)
                                    else viewModel.selectedDifficulties.add(difficulty)
                                },
                                label = { Text(difficulty) }
                            )
                        }
                    }
                }
            }

            // --- PRICE RANGE Section ---
            item {
                FilterSection("Price Range (INR)") {
                    RangeSlider(
                        value = viewModel.priceRange.value,
                        onValueChange = { viewModel.priceRange.value = it },
                        valueRange = 0f..50000f, // 0 to 50,000 INR
                        steps = 49 // Creates 50 steps (e.g., in increments of 1000)
                    )
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("₹${viewModel.priceRange.value.start.toInt()}")
                        Text("₹${viewModel.priceRange.value.endInclusive.toInt()}")
                    }
                }
            }

            // --- FEATURES Section ---
            item {
                FilterSection("Features") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = viewModel.demoAvailable.value,
                            onCheckedChange = { viewModel.demoAvailable.value = it }
                        )
                        Text("Demo Available")
                    }
                }
            }
        }
    }
}

// A reusable composable to create consistent section headers
@Composable
fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(text = title.uppercase(), style = MaterialTheme.typography.titleSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}