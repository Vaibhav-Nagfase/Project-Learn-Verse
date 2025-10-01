package com.example.learnverse.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.learnverse.viewmodel.AuthViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import com.example.learnverse.R // Add your own image to res/drawable
import com.example.learnverse.viewmodel.LoginUiState

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Observe the new UI state for this screen
    val loginUiState by viewModel.loginUiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()) // 1. Make the Column scrollable
                .imePadding(),                         // 2. Add padding when the keyboard is open
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Illustration
            Image(
                painter = painterResource(id = R.drawable.login_img), // Replace with your image
                contentDescription = "Sign In Illustration",
                modifier = Modifier.size(220.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 2. Title
            Text(text = "Sign In", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // 3. Subtitle
            Text(
                text = "Enter valid user name & password to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 4. Email/Username TextField
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User icon") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 5. Password TextField
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password icon") },
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 6. Login Button
            Button(
                onClick = { viewModel.login(email, password) },
                shape = RoundedCornerShape(12.dp),
                // The button is only disabled when the UI state is loading
                enabled = loginUiState !is LoginUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = if (loginUiState is LoginUiState.Loading) "Logging in..." else "Login",
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 7. Error Message
            if (loginUiState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (loginUiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // 8. Clickable Sign Up Text
            ClickableLoginText {
                navController.navigate("signup")
            }
        }
    }
}