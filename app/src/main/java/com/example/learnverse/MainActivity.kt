package com.example.learnverse

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.learnverse.data.remote.ApiClient
import com.example.learnverse.ui.theme.LearnVerseTheme
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.ui.screen.auth.LoginScreen
import com.example.learnverse.ui.screen.auth.SignUpScreen
import com.example.learnverse.ui.screen.home.HomeScreen
import com.example.learnverse.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnVerseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                    ) {

                    LearnVerseApp()

                }
            }
        }
    }
}

@Composable
fun LearnVerseApp() {

    val navController: NavHostController = rememberNavController()

    // Get context in the composable
    val context = LocalContext.current.applicationContext // Use applicationContext for safety

    // Create repository + ViewModel
    val repository = remember {
        AuthRepository(ApiClient.api, context)
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(repository) // <-- FIX
    )

    // ✅ Auto redirect if token already exists
    val startDestination =
        if (authViewModel.token.value != null) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("signup") {
            SignUpScreen(navController, authViewModel)
        }
        composable("home") {
            HomeScreen(navController, authViewModel)
        }
    }
}
