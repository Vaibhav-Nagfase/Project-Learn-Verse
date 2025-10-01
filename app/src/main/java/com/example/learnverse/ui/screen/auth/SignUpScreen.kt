package com.example.learnverse.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learnverse.viewmodel.AuthViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import com.example.learnverse.R // Add your own image to res/drawable
import com.example.learnverse.viewmodel.LoginUiState

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var fullName by remember { mutableStateOf("") }
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
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Illustration
            Image(
                painter = painterResource(id = R.drawable.signup_img), // Replace with your image
                contentDescription = "Sign Up Illustration",
                modifier = Modifier.size(220.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 2. Title
            Text(text = "Sign Up", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // 3. Subtitle
            Text(
                text = "Use proper information to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 4. Full Name TextField
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User icon") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 5. Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email icon") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 6. Password TextField
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

            // 7. Create Account Button
            Button(
                onClick = { viewModel.register(fullName, email, password) },
                shape = RoundedCornerShape(12.dp),
                // The button is only disabled when the UI state is loading
                enabled = loginUiState !is LoginUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = if (loginUiState is LoginUiState.Loading) "Creating..." else "Create Account",
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 8. Error Message
            if (loginUiState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (loginUiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 9. Clickable Sign In Text
            // You would create a "ClickableSignUpText" similar to the login one
            ClickableSignUpText {
                navController.navigate("login") { popUpTo("login") { inclusive = true } }
            }
        }
    }
}