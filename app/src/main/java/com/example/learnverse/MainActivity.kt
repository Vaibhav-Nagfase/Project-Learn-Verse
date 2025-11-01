package com.example.learnverse

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.learnverse.data.remote.ApiClient
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.data.repository.*
import com.example.learnverse.ui.screen.admin.AdminDashboardScreen
import com.example.learnverse.ui.screen.auth.InterestSelectionDialog
import com.example.learnverse.ui.screen.auth.LoginScreen
import com.example.learnverse.ui.screen.auth.SignUpScreen
import com.example.learnverse.ui.screen.community.EnhancedCreatePostScreen
import com.example.learnverse.ui.screen.chatbot.ChatScreen
import com.example.learnverse.ui.screen.detail.ActivityDetailScreen
import com.example.learnverse.ui.screen.enrollment.MyCoursesScreen
import com.example.learnverse.ui.screen.filter.FilterScreen
import com.example.learnverse.ui.screen.home.HomeScreen
import com.example.learnverse.ui.screen.community.EnhancedCreatePostScreen
import com.example.learnverse.ui.screen.community.EnhancedDiscoverScreen
import com.example.learnverse.ui.screen.community.PostDetailScreen
import com.example.learnverse.ui.screen.interest.InterestManagementScreen
import com.example.learnverse.ui.screen.profile.ProfileScreen
import com.example.learnverse.ui.screen.search.SearchScreen
import com.example.learnverse.ui.screen.tutor.CreateActivityScreen
import com.example.learnverse.ui.screen.tutor.TutorDashboardScreen
import com.example.learnverse.ui.screen.tutor.TutorProfileScreen
import com.example.learnverse.ui.screen.tutor.TutorVerificationScreen
import com.example.learnverse.ui.screen.tutor.VerificationStatusScreen
import com.example.learnverse.ui.screen.video.VideoPlayerScreen
import com.example.learnverse.ui.theme.LearnVerseTheme
import com.example.learnverse.viewmodel.*


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LearnVerseApp() {
    val context = LocalContext.current.applicationContext
    val application = context as Application

    // --- API Client & Service ---

    // 1. Get the singleton ApiClient instance which holds OkHttp and Retrofit
    val apiClient = remember { ApiClient.getInstance(context) }

    // 2. Get the actual ApiService interface from Retrofit
    val apiService = remember { apiClient.retrofit.create(ApiService::class.java) }

    // 3. Get the OkHttpClient for the chat streamer
    val okHttpClient = remember { apiClient.okHttpClient }

    // --- REPOSITORIES ---
    val authRepository = remember { AuthRepository(apiService, context) }
    val tutorRepository = remember { TutorRepository(apiService) }
    val activitiesRepository = remember { ActivitiesRepository(apiService) }
    val adminRepository = remember { AdminRepository(apiService) }
    val profileRepository = remember { ProfileRepository(apiService) }
    val communityRepository = remember { CommunityRepository(apiService) }
    val chatRepository = remember { ChatRepository(apiService, okHttpClient) }

    // --- VIEWMODELS ---
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository, tutorRepository, profileRepository)
    )
    val activitiesViewModel: ActivitiesViewModel = viewModel(
        factory = ActivitiesViewModelFactory(activitiesRepository, context)
    )
    val filterViewModel: FilterViewModel = viewModel(
        factory = FilterViewModelFactory(activitiesRepository)
    )
    val tutorVerificationViewModel: TutorVerificationViewModel = viewModel(
        factory = TutorVerificationViewModelFactory(application, tutorRepository)
    )
    val adminViewModel: AdminViewModel = viewModel(
        factory = AdminViewModelFactory(application, adminRepository)
    )
    val tutorViewModel: TutorViewModel = viewModel(factory = TutorViewModelFactory(tutorRepository))
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(profileRepository)
    )
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(chatRepository, authRepository)
    )

    val communityViewModel: CommunityViewModel = viewModel(
        factory = CommunityViewModelFactory(communityRepository, authRepository)
    )

    // --- State Observation ---
    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.currentUserRole.collectAsState()

    // --- Main App Navigation ---
    when (authState) {
        AuthState.Loading -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }
        AuthState.Unauthenticated -> {
            LoginNavGraph(authViewModel = authViewModel)
        }
        AuthState.NeedsInterestSelection -> {
            InterestSelectionDialog(
                onDismiss = { authViewModel.onInterestSelectionCancelled() },
                onSave = { selectedInterests -> authViewModel.saveInterests(selectedInterests) }
            )
        }
        AuthState.Authenticated -> {
            when (userRole) {
                "ADMIN" -> {
                    AdminNavGraph(authViewModel = authViewModel, adminViewModel = adminViewModel)
                }
                "TUTOR" -> {
                    TutorNavGraph(authViewModel = authViewModel, tutorViewModel = tutorViewModel, communityViewModel = communityViewModel, activitiesViewModel = activitiesViewModel)
                }
                else -> {
                    val startDestination = if (authViewModel.navigateToFeedAfterOnboarding || authViewModel.interestSelectionCancelled) "home" else "home"
                    MainNavGraph(
                        authViewModel = authViewModel,
                        activitiesViewModel = activitiesViewModel,
                        startDestination = startDestination,
                        filterViewModel = filterViewModel,
                        tutorVerificationViewModel = tutorVerificationViewModel,
                        profileViewModel = profileViewModel,
                        communityViewModel = communityViewModel,
                        chatViewModel = chatViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun LoginNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("signup") { SignUpScreen(navController, authViewModel) }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavGraph(
    authViewModel: AuthViewModel,
    activitiesViewModel: ActivitiesViewModel,
    startDestination: String,
    filterViewModel: FilterViewModel,
    tutorVerificationViewModel: TutorVerificationViewModel,
    profileViewModel: ProfileViewModel,
    communityViewModel: CommunityViewModel,
    chatViewModel: ChatViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("home") {
            HomeScreen(navController, authViewModel, activitiesViewModel)
        }
        composable("my_courses") {
            MyCoursesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
        }
        composable(
            route = "feed?query={query}",
            arguments = listOf(navArgument("query") { nullable = true })
        ) { backStackEntry ->
            SearchScreen(
                navController = navController,
                searchQueryFromHome = backStackEntry.arguments?.getString("query"),
                activitiesViewModel = activitiesViewModel,
                authViewModel = authViewModel
            )
        }

        composable("discover") { // Or whatever name you choose for the feed
            EnhancedDiscoverScreen(navController, communityViewModel, authViewModel) // Pass necessary ViewModels
        }

        composable(
            route = "postDetail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            if (postId != null) {
                PostDetailScreen(
                    postId = postId,
                    navController = navController,
                    communityViewModel = communityViewModel,
                    authViewModel = authViewModel
                )
            } else {
                // Handle error: postId not found, maybe navigate back or show error screen
                Text("Error: Post ID missing")
            }
        }

        composable("activityDetail/{activityId}") { backStackEntry ->
            ActivityDetailScreen(
                activityId = backStackEntry.arguments?.getString("activityId") ?: "",
                activitiesViewModel = activitiesViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(
            route = "video_player/{activityId}/{videoId}",
            arguments = listOf(
                navArgument("activityId") { type = NavType.StringType },
                navArgument("videoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            VideoPlayerScreen(
                activityId = backStackEntry.arguments?.getString("activityId") ?: "",
                videoId = backStackEntry.arguments?.getString("videoId") ?: "",
                activitiesViewModel = activitiesViewModel,
                navController = navController
            )
        }

        composable("tutorVerification") {
            TutorVerificationScreen(navController, tutorVerificationViewModel, authViewModel)
        }
        composable("interestManagement") {
            InterestManagementScreen(navController, authViewModel, activitiesViewModel)
        }
        composable("filter") {
            FilterScreen(navController = navController, viewModel = filterViewModel)
        }

        // --- NEW ROUTES FOR PROFILE AND CHAT ---
        composable("profile_setup") {
            ProfileScreen(navController, profileViewModel, authViewModel)
        }
        composable("my_profile") {
            ProfileScreen(navController, profileViewModel, authViewModel)
        }
        composable("chat") {
            ChatScreen(navController, chatViewModel)
        }
        composable("verificationStatus") {
            VerificationStatusScreen(navController, authViewModel)
        }

        composable(
            route = "tutorProfile/{tutorId}",
            arguments = listOf(navArgument("tutorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tutorIdArg = backStackEntry.arguments?.getString("tutorId")
            if (tutorIdArg != null) {
                TutorProfileScreen(
                    tutorId = tutorIdArg,
                    navController = navController,
                    authViewModel = authViewModel, // Pass authViewModel
                    activitiesViewModel = activitiesViewModel
                )
            } else {
                Text("Error: Tutor ID missing") // Handle error
            }
        }

    }

    LaunchedEffect(Unit) {
        authViewModel.onNavigationToFeedComplete()
    }
}

@Composable
fun TutorNavGraph(
    authViewModel: AuthViewModel,
    tutorViewModel: TutorViewModel,
    activitiesViewModel: ActivitiesViewModel,
    communityViewModel: CommunityViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "tutor_dashboard_main") {
        composable("tutor_dashboard_main") {
            TutorDashboardScreen(
                mainNavController = navController,
                authViewModel = authViewModel,
                tutorViewModel = tutorViewModel,
                activitiesViewModel = activitiesViewModel,
                communityViewModel = communityViewModel
            )
        }

        composable("activityDetail/{activityId}") { backStackEntry ->
            ActivityDetailScreen(
                activityId = backStackEntry.arguments?.getString("activityId") ?: "",
                activitiesViewModel = activitiesViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(
            route = "create_activity?activityId={activityId}",
            arguments = listOf(navArgument("activityId") { nullable = true })
        ) { backStackEntry ->
            CreateActivityScreen(
                navController = navController,
                authViewModel = authViewModel,
                tutorViewModel = tutorViewModel,
                activityId = backStackEntry.arguments?.getString("activityId")
            )
        }

        // ✅ Video Player Route (Keep this)
        composable(
            route = "video_player/{activityId}/{videoId}",
            arguments = listOf(
                navArgument("activityId") { type = NavType.StringType },
                navArgument("videoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            VideoPlayerScreen(
                activityId = backStackEntry.arguments?.getString("activityId") ?: "",
                videoId = backStackEntry.arguments?.getString("videoId") ?: "",
                activitiesViewModel = activitiesViewModel,
                navController = navController
            )
        }

        // ✅ Keep Create Post Route
        composable(
            route = "createPost?postId={postId}",
            arguments = listOf(navArgument("postId") { nullable = true; type = NavType.StringType })
        ) { backStackEntry ->
            EnhancedCreatePostScreen(
                navController = navController,
                communityViewModel = communityViewModel,
                postIdToEdit = backStackEntry.arguments?.getString("postId")
            )
        }

        // ✅ Keep Post Detail Route
        composable(
            route = "postDetail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            if (postId != null) {
                PostDetailScreen(
                    postId = postId,
                    navController = navController,
                    communityViewModel = communityViewModel,
                    authViewModel = authViewModel
                )
            } else {
                Text("Error: Post ID missing")
            }
        }

        // ✅ Keep Tutor Profile Route
        composable(
            route = "tutorProfile/{tutorId}",
            arguments = listOf(navArgument("tutorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tutorIdArg = backStackEntry.arguments?.getString("tutorId")
            if (tutorIdArg != null) {
                TutorProfileScreen(
                    tutorId = tutorIdArg,
                    navController = navController,
                    authViewModel = authViewModel,
                    activitiesViewModel = activitiesViewModel
                )
            } else {
                Text("Error: Tutor ID missing")
            }
        }
    }
}

@Composable
fun AdminNavGraph(authViewModel: AuthViewModel, adminViewModel: AdminViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "admin_dashboard") {
        composable("admin_dashboard") {
            AdminDashboardScreen(navController, adminViewModel, authViewModel)
        }
    }
}
