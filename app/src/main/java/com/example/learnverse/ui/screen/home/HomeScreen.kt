package com.example.learnverse.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learnverse.R
import com.example.learnverse.viewmodel.AuthViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.learnverse.viewmodel.ActivitiesViewModel


@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    activitiesViewModel: ActivitiesViewModel
) {
    // This state would come from a ViewModel later
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            // We will add the Bottom Navigation Bar here later if needed
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // 1. Header with Profile and Logout
            item {
                HomeHeader(authViewModel = authViewModel, navController = navController)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. Main Search Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)) // Light cyan
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Find an Activities",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "as per your interest",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search Course") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = { Icon(Icons.Default.Mic, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    navController.navigate("search/$searchQuery")
                                }
                            })
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. Recommended Activities (Static for now)
            item {
                Text("Recommended Activities", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("Swimming")
                    Chip("Karate")
                    Chip("VFX")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 4. Activities Near You
            item {
                Text("Activities Near You", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                // This would be a LazyRow with data from a ViewModel
                Text("(Horizontal list of activities will go here)", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun HomeHeader(authViewModel: AuthViewModel, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Hello, Ronaldo", style = MaterialTheme.typography.headlineMedium) // TODO: Get user name
        }
        // Profile picture now handles the logout
        IconButton(onClick = {
            authViewModel.logout()
        }) {
            Image(
                painter = painterResource(id = R.drawable.boy), // Add a placeholder profile pic
                contentDescription = "Profile",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        color = Color.Transparent
    ) {
        Text(text = text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    }
}
