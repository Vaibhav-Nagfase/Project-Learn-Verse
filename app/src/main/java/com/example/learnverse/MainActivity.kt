package com.example.learnverse

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.learnverse.data.remote.ApiClient
import com.example.learnverse.data.repository.ActivitiesRepository
import com.example.learnverse.data.repository.AdminRepository
import com.example.learnverse.ui.theme.LearnVerseTheme
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.data.repository.AuthRepository
import com.example.learnverse.data.repository.TutorRepository
import com.example.learnverse.ui.screen.admin.AdminDashboardScreen
import com.example.learnverse.ui.screen.auth.InterestSelectionDialog
import com.example.learnverse.ui.screen.auth.LoginScreen
import com.example.learnverse.ui.screen.auth.SignUpScreen
import com.example.learnverse.ui.screen.home.HomeScreen
import com.example.learnverse.ui.screen.detail.ActivityDetailScreen
import com.example.learnverse.ui.screen.filter.FilterScreen
import com.example.learnverse.ui.screen.interest.InterestManagementScreen
import com.example.learnverse.ui.screen.search.SearchScreen
import com.example.learnverse.ui.screen.tutor.TutorVerificationScreen
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.example.learnverse.viewmodel.ActivitiesViewModelFactory
import com.example.learnverse.viewmodel.AdminViewModel
import com.example.learnverse.viewmodel.AdminViewModelFactory
import com.example.learnverse.viewmodel.AuthState
import com.example.learnverse.viewmodel.AuthViewModelFactory
import com.example.learnverse.viewmodel.FilterViewModel
import com.example.learnverse.viewmodel.FilterViewModelFactory
import com.example.learnverse.viewmodel.TutorVerificationViewModel
import com.example.learnverse.viewmodel.TutorVerificationViewModelFactory


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

    // Get the application instance needed for the AndroidViewModel
    val application = LocalContext.current.applicationContext as Application


//    val apiService = ApiClient.getInstance(context)
    val authRepository = remember { AuthRepository(ApiClient.api, context) }
    val tutorRepository = remember { TutorRepository(ApiClient.api) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository, tutorRepository))

    val activitiesRepository = remember { ActivitiesRepository(ApiClient.api) }
    val activitiesViewModel: ActivitiesViewModel = viewModel(
        factory = ActivitiesViewModelFactory(activitiesRepository, context)
    )

    // Filter ViewModel Setup
    val filterViewModel: FilterViewModel = viewModel(
        factory = FilterViewModelFactory(activitiesRepository, context)
    )

    // Tutor Verification ViewModel Setup

    val tutorVerificationViewModel: TutorVerificationViewModel = viewModel(
        factory = TutorVerificationViewModelFactory(application, tutorRepository, authRepository)
    )

    // --- ADD THIS ADMIN VIEWMODEL SETUP ---
    val adminRepository = remember { AdminRepository(ApiClient.api) }
    val adminViewModel: AdminViewModel = viewModel(
        factory = AdminViewModelFactory(application, adminRepository, authRepository)
    )

    // Observe the single source of truth for navigation
    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.currentUserRole.collectAsState()

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

            when (userRole) {
                "ADMIN" -> {
                    // If the user is an ADMIN, show the AdminNavGraph
                    AdminNavGraph(authViewModel = authViewModel, adminViewModel = adminViewModel)
                }

                "TUTOR" -> {
                    // If the user is a TUTOR, show the TutorNavGraph
                    TutorNavGraph(authViewModel = authViewModel)
                }

                else -> {
                    // Otherwise, they are a regular USER, show the MainNavGraph
                    val startDestination = when {
                        authViewModel.navigateToFeedAfterOnboarding -> "feed"
                        authViewModel.interestSelectionCancelled -> "home"
                        else -> "home"
                    }

                    MainNavGraph(
                        authViewModel = authViewModel,
                        activitiesViewModel = activitiesViewModel,
                        startDestination = startDestination,
                        filterViewModel = filterViewModel,
                        tutorVerificationViewModel = tutorVerificationViewModel
                    )
                }
            }
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
                 filterViewModel: FilterViewModel,
                 tutorVerificationViewModel: TutorVerificationViewModel) {

    val navController = rememberNavController()
    // The start destination is now "feed" which will be our repurposed SearchScreen
    NavHost(navController = navController, startDestination = startDestination) {
        composable(route = "feed?query={query}", // route string to accept an optional argument
            arguments = listOf(navArgument("query") { nullable = true }) // Define the argument
        ) {backStackEntry ->
            SearchScreen(
                navController = navController,
                // 3. Extract the argument and pass it to the screen
                searchQueryFromHome = backStackEntry.arguments?.getString("query"),
                activitiesViewModel = activitiesViewModel,
                authViewModel = authViewModel
            )
        }

        composable("home") {
            HomeScreen(navController, authViewModel, activitiesViewModel)
        }

        composable("tutorVerification") {
            TutorVerificationScreen(
                navController = navController,
                viewModel = tutorVerificationViewModel,
                authViewModel = authViewModel
            )
        }

        composable("interestManagement") {
            InterestManagementScreen(navController, authViewModel)
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

    // This effect runs once when MainNavGraph is first displayed.
    // Its job is to call the reset function in your ViewModel.
    LaunchedEffect(Unit) {
        authViewModel.onNavigationToFeedComplete()
    }
}

@Composable
fun TutorNavGraph(authViewModel: AuthViewModel) {
    // For now, this is a placeholder. In the future, it will have its own
    // NavHost with screens for creating activities, a dashboard, etc.
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome, Tutor!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { authViewModel.logout() }) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun AdminNavGraph(authViewModel: AuthViewModel, adminViewModel: AdminViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "admin_dashboard") {
        composable("admin_dashboard") {
            // We will build this screen in the next step
            AdminDashboardScreen(
                navController = navController,
                adminViewModel = adminViewModel,
                authViewModel = authViewModel
            )
        }
        // TODO: Add other admin-specific routes here later
    }
}
