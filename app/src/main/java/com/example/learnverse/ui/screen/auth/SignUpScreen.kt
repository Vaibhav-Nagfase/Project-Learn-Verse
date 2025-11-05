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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, viewModel: AuthViewModel) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // 🔥 NEW: Password visibility
    var confirmPasswordVisible by remember { mutableStateOf(false) } // 🔥 NEW: Confirm password visibility

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
                painter = painterResource(id = R.drawable.signup_img),
                contentDescription = "Sign Up Illustration",
                modifier = Modifier.size(220.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Title
            Text(
                text = "Sign Up",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Subtitle
            Text(
                text = "Create your account to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Full Name TextField
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "User icon")
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email icon")
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 6. Password TextField with visibility toggle
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Password icon")
                },
                // 🔥 NEW: Visibility toggle
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 7. Confirm Password TextField with visibility toggle
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Password icon")
                },
                // 🔥 NEW: Visibility toggle for confirm password
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 8. Create Account Button
            Button(
                onClick = { viewModel.register(fullName, email, password) },
                shape = RoundedCornerShape(12.dp),
                enabled = loginUiState !is LoginUiState.Loading && password == confirmPassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (loginUiState is LoginUiState.Loading) "Creating..." else "Create Account",
                    fontSize = 16.sp
                )
            }

            // 🔥 Password mismatch warning
            if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 9. Error Message
            if (loginUiState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (loginUiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 10. Sign In Link
            ClickableSignUpText(onSignInClicked = {
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
    }
}