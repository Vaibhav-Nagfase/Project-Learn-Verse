package com.example.learnverse.ui.screen.tutor.course

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.learnverse.data.remote.ApiService
import com.example.learnverse.data.repository.TutorCourseRepository
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.example.learnverse.viewmodel.TutorCourseUiState
import com.example.learnverse.viewmodel.TutorCourseViewModel
import com.example.learnverse.viewmodel.TutorCourseViewModelFactory
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorCourseManagementScreen(
    activityId: String,
    navController: NavController,
    apiService: ApiService,
    activityViewModel: ActivitiesViewModel
) {
    val repository = remember { TutorCourseRepository(apiService) }
    val viewModel: TutorCourseViewModel = viewModel(
        factory = TutorCourseViewModelFactory(repository, activityId)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    val tabs = listOf(
        "Content" to Icons.Default.VideoLibrary,
        "Students" to Icons.Default.People,
        "Settings" to Icons.Default.Settings,
        "Analytics" to Icons.Default.BarChart
    )
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ModernTopAppBar(
                title = if (uiState is TutorCourseUiState.Success) {
                    (uiState as TutorCourseUiState.Success).data.activity.title
                } else {
                    "Course Management"
                },
                onBack = { navController.popBackStack() },
                onRefresh = { viewModel.refresh() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is TutorCourseUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    ModernLoadingIndicator()
                }
            }
            is TutorCourseUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ModernErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadCourseData() }
                    )
                }
            }
            is TutorCourseUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Scrollable Tab Row
                    ModernTabRow(
                        tabs = tabs,
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )

                    // Clean tab content
                    AnimatedContent(
                        targetState = selectedTab,
                        label = "tab_content",
                        transitionSpec = {
                            slideInHorizontally(
                                initialOffsetX = { if (targetState > initialState) 300 else -300 }
                            ) + fadeIn() togetherWith
                                    slideOutHorizontally(
                                        targetOffsetX = { if (targetState > initialState) -300 else 300 }
                                    ) + fadeOut()
                        }
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> ContentManagementTab(
                                activity = state.data.activity,
                                apiService = apiService,
                                onRefresh = { viewModel.refresh() },
                                activityViewModel = activityViewModel
                            )
                            1 -> EnrolledStudentsTab(
                                students = state.data.enrolledStudents
                            )
                            2 -> CourseSettingsTab(
                                activity = state.data.activity,
                                viewModel = viewModel
                            )
                            3 -> CourseAnalyticsTab(
                                stats = state.data.stats
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopAppBar(
    title: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    // ✅ FIX: Surface without custom padding - Scaffold handles it
    Surface(
        modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Default.ArrowBack, "Back")
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Manage your course",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalIconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }
    }
}

@Composable
fun ModernTabRow(
    tabs: List<Pair<String, androidx.compose.ui.graphics.vector.ImageVector>>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 16.dp,
        divider = {},
        indicator = { tabPositions ->
            if (selectedTab < tabPositions.size) {
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(4.dp)
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                )
            }
        }
    ) {
        tabs.forEachIndexed { index, (title, icon) ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        title,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun ModernLoadingIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Text(
            "Loading course data...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ModernErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.padding(20.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Oops! Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}
