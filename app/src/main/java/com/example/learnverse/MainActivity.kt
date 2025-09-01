package com.example.learnverse

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.learnverse.data.remote.ApiClient
import com.example.learnverse.data.repository.ActivitiesRepository
import com.example.learnverse.ui.theme.LearnVerseTheme
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.ui.screen.auth.InterestSelectionDialog
import com.example.learnverse.ui.screen.auth.LoginScreen
import com.example.learnverse.ui.screen.auth.SignUpScreen
import com.example.learnverse.ui.screen.home.HomeScreen
import com.example.learnverse.ui.screen.detail.ActivityDetailScreen
import com.example.learnverse.ui.screen.filter.FilterScreen
import com.example.learnverse.ui.screen.search.SearchScreen
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.example.learnverse.viewmodel.ActivitiesViewModelFactory
import com.example.learnverse.viewmodel.AuthState
import com.example.learnverse.viewmodel.AuthViewModelFactory
import com.example.learnverse.viewmodel.FilterViewModel
import com.example.learnverse.viewmodel.FilterViewModelFactory


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
    // --- ViewModel Setup (remains the same) ---
    val context = LocalContext.current.applicationContext
//    val apiService = ApiClient.getInstance(context)
    val authRepository = remember { AuthRepository(ApiClient.api, context) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository))

    val activitiesRepository = remember { ActivitiesRepository(ApiClient.api) }
    val activitiesViewModel: ActivitiesViewModel = viewModel(
        factory = ActivitiesViewModelFactory(activitiesRepository, context)
    )

    // Filter ViewModel Setup
    val filterViewModel: FilterViewModel = viewModel(
        factory = FilterViewModelFactory(activitiesRepository, context)
    )

    // Observe the single source of truth for navigation
    val authState by authViewModel.authState.collectAsState()

    // A when statement now controls the entire app flow
    when (authState) {
        AuthState.Loading -> {
            // Show a full-screen loading indicator while checking for a token
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }
        AuthState.Unauthenticated -> {
            // If the user is logged out, show the login/signup screens
            LoginNavGraph(authViewModel = authViewModel)
        }
        AuthState.NeedsInterestSelection -> {
            // If the user needs to select interests, show the dialog
            InterestSelectionDialog(
                onDismiss = { authViewModel.onInterestSelectionCancelled() },
                onSave = { selectedInterests -> authViewModel.saveInterests(selectedInterests) }
            )
        }
        AuthState.Authenticated -> {
            // If the user is fully logged in, show the main part of the app
            val startRoute = if (authViewModel.interestSelectionCancelled) "home" else "feed"
            MainNavGraph(
                authViewModel = authViewModel,
                activitiesViewModel = activitiesViewModel,
                startDestination = startRoute,
                filterViewModel = filterViewModel
            )
        }
    }
}

// Helper composable for the login/signup navigation
@Composable
fun LoginNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("signup") { SignUpScreen(navController, authViewModel) }
    }
}

// Helper composable for the main app navigation
@Composable
fun MainNavGraph(authViewModel: AuthViewModel,
                 activitiesViewModel: ActivitiesViewModel,
                 startDestination: String,
                 filterViewModel: FilterViewModel) {

    val navController = rememberNavController()
    // The start destination is now "feed" which will be our repurposed SearchScreen
    NavHost(navController = navController, startDestination = startDestination) {
        composable("feed") {
            SearchScreen(
                navController = navController,
                // The initial query is empty, the ViewModel will fetch the personalized feed
                initialQuery = "",
                activitiesViewModel = activitiesViewModel,
                authViewModel = authViewModel
            )
        }
        composable("home") {
            HomeScreen(navController, authViewModel, activitiesViewModel)
        }

        composable("activityDetail/{activityId}") { backStackEntry ->
            // 1. Get the activityId from the navigation route
            val activityId = backStackEntry.arguments?.getString("activityId") ?: ""

            // 2. Call the new ActivityDetailScreen
            ActivityDetailScreen(
                activityId = activityId,
                // 3. Pass the shared ViewModel and NavController
                activitiesViewModel = activitiesViewModel,
                navController = navController
            )
        }

        composable("filter") {
            FilterScreen(navController = navController, viewModel = filterViewModel)
        }

    }
}
