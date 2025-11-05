package com.example.learnverse.ui.screen.my_course

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.learnverse.data.model.UserProgressResponse
import com.example.learnverse.viewmodel.MyCoursesUiState
import com.example.learnverse.viewmodel.MyCoursesViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    navController: NavController,
    viewModel: MyCoursesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sortOrder by viewModel.selectedSortOrder.collectAsStateWithLifecycle()
    val filterType by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing = uiState is MyCoursesUiState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "My Courses",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Continue your learning journey",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MyCoursesUiState.Loading -> {
                    LoadingState()
                }
                is MyCoursesUiState.Success -> {
                    if (viewModel.getAllCourses().isEmpty()) {
                        EmptyState(navController)
                    } else {
                        CoursesContent(
                            courses = state.courses,
                            sortOrder = sortOrder,
                            filterType = filterType,
                            searchQuery = searchQuery,
                            onSortChange = { viewModel.setSortOrder(it) },
                            onFilterChange = { viewModel.setFilter(it) },
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
                is MyCoursesUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.refresh() }
                    )
                }
            }
        }
    }
}


@Composable
private fun CoursesContent(
    courses: List<UserProgressResponse>,
    sortOrder: MyCoursesViewModel.SortOrder,
    filterType: MyCoursesViewModel.FilterType,
    searchQuery: String,
    onSortChange: (MyCoursesViewModel.SortOrder) -> Unit,
    onFilterChange: (MyCoursesViewModel.FilterType) -> Unit,
    onSearchChange: (String) -> Unit,
    navController: NavController,
    viewModel: MyCoursesViewModel // ✅ Pass viewModel to get all courses
) {
    // ✅ Get original unfiltered courses for counts
    val allCourses = viewModel.getAllCourses()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Bar
        item {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchChange
            )
        }

        // Filters and Sort
        item {
            FiltersAndSort(
                currentSort = sortOrder,
                currentFilter = filterType,
                onSortChange = onSortChange,
                onFilterChange = onFilterChange,
                // ✅ Use allCourses for accurate counts
                totalCourses = allCourses.size,
                inProgressCount = allCourses.count { it.completionPercentage in 1.0..99.0 },
                completedCount = allCourses.count { it.completionPercentage == 100.0 }
            )
        }

        // Continue Learning Section
        if (searchQuery.isEmpty() && filterType == MyCoursesViewModel.FilterType.ALL) {
            item {
                val continueLearning = allCourses.find { it.completionPercentage in 1.0..99.0 }
                if (continueLearning != null) {
                    ContinueLearningCard(
                        course = continueLearning,
                        onContinue = {
                            navController.navigate("student_course/${continueLearning.activityId}")
                        }
                    )
                }
            }
        }

        // Statistics Card
        if (filterType == MyCoursesViewModel.FilterType.ALL && searchQuery.isEmpty()) {
            item {
                StatisticsCard(
                    totalCourses = allCourses.size,
                    completedCourses = allCourses.count { it.completionPercentage == 100.0 },
                    inProgressCourses = allCourses.count { it.completionPercentage in 1.0..99.0 }
                )
            }
        }

        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when {
                        searchQuery.isNotEmpty() -> "Search Results (${courses.size})"
                        filterType == MyCoursesViewModel.FilterType.IN_PROGRESS -> "In Progress (${courses.size})"
                        filterType == MyCoursesViewModel.FilterType.COMPLETED -> "Completed (${courses.size})"
                        else -> "All Courses (${courses.size})"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Courses List or Empty State
        if (courses.isEmpty()) {
            item {
                EmptyCourseState(
                    message = when {
                        searchQuery.isNotEmpty() -> "No courses match \"$searchQuery\""
                        filterType == MyCoursesViewModel.FilterType.IN_PROGRESS -> "No courses in progress"
                        filterType == MyCoursesViewModel.FilterType.COMPLETED -> "No completed courses yet"
                        else -> "You haven't enrolled in any courses yet"
                    },
                    showExploreButton = allCourses.isEmpty(),
                    onExploreClick = { navController.navigate("home") }
                )
            }
        } else {
            items(courses) { course ->
                EnhancedCourseCard(
                    course = course,
                    onClick = {
                        navController.navigate("student_course/${course.activityId}")
                    }
                )
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ✅ UPDATED: Filter chips with counts
@Composable
private fun FiltersAndSort(
    currentSort: MyCoursesViewModel.SortOrder,
    currentFilter: MyCoursesViewModel.FilterType,
    onSortChange: (MyCoursesViewModel.SortOrder) -> Unit,
    onFilterChange: (MyCoursesViewModel.FilterType) -> Unit,
    totalCourses: Int,
    inProgressCount: Int,
    completedCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ✅ Filter Chips - Only show if relevant
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All - always show
            FilterChip(
                selected = currentFilter == MyCoursesViewModel.FilterType.ALL,
                onClick = { onFilterChange(MyCoursesViewModel.FilterType.ALL) },
                label = { Text("All ($totalCourses)") },
                leadingIcon = if (currentFilter == MyCoursesViewModel.FilterType.ALL) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )

            // In Progress - only show if there are in-progress courses
            if (inProgressCount > 0) {
                FilterChip(
                    selected = currentFilter == MyCoursesViewModel.FilterType.IN_PROGRESS,
                    onClick = { onFilterChange(MyCoursesViewModel.FilterType.IN_PROGRESS) },
                    label = { Text("In Progress ($inProgressCount)") },
                    leadingIcon = if (currentFilter == MyCoursesViewModel.FilterType.IN_PROGRESS) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }

            // Completed - only show if there are completed courses
            if (completedCount > 0) {
                FilterChip(
                    selected = currentFilter == MyCoursesViewModel.FilterType.COMPLETED,
                    onClick = { onFilterChange(MyCoursesViewModel.FilterType.COMPLETED) },
                    label = { Text("Completed ($completedCount)") },
                    leadingIcon = if (currentFilter == MyCoursesViewModel.FilterType.COMPLETED) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        // Sort Options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                MyCoursesViewModel.SortOrder.RECENT to "Recent",
                MyCoursesViewModel.SortOrder.PROGRESS to "Progress",
                MyCoursesViewModel.SortOrder.ALPHABETICAL to "A-Z"
            ).forEach { (sort, label) ->
                FilterChip(
                    selected = currentSort == sort,
                    onClick = { onSortChange(sort) },
                    label = { Text(label) },
                    leadingIcon = if (currentSort == sort) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }
    }
}

// ✅ UPDATED: Empty state with conditional button
@Composable
private fun EmptyCourseState(
    message: String,
    showExploreButton: Boolean = false,
    onExploreClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // ✅ Only show explore button if truly no courses exist
            if (showExploreButton) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onExploreClick) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Explore Courses")
                }
            }
        }
    }
}

@Composable
private fun StatisticsCard(
    totalCourses: Int,
    completedCourses: Int,
    inProgressCourses: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.School,
                value = totalCourses.toString(),
                label = "Total",
                color = MaterialTheme.colorScheme.primary
            )
            Divider(
                modifier = Modifier
                    .height(50.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            StatItem(
                icon = Icons.Default.TrendingUp,
                value = inProgressCourses.toString(),
                label = "In Progress",
                color = Color(0xFF2196F3)
            )
            Divider(
                modifier = Modifier
                    .height(50.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            StatItem(
                icon = Icons.Default.CheckCircle,
                value = completedCourses.toString(),
                label = "Completed",
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = color
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EnhancedCourseCard(
    course: UserProgressResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Banner Image with Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Background (placeholder or image)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    // You can add AsyncImage here if you have banner URLs
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                        )
                    }
                }

                // Gradient Overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                // Status Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        course.completionPercentage == 100.0 -> Color(0xFF4CAF50)
                        course.completionPercentage >= 50.0 -> Color(0xFF2196F3)
                        course.completionPercentage > 0.0 -> Color(0xFFFF9800)
                        else -> Color(0xFF9E9E9E)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            when {
                                course.completionPercentage == 100.0 -> Icons.Default.CheckCircle
                                course.completionPercentage > 0.0 -> Icons.Default.PlayArrow
                                else -> Icons.Default.Circle
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Text(
                            when {
                                course.completionPercentage == 100.0 -> "Completed"
                                course.completionPercentage > 0.0 -> "In Progress"
                                else -> "Not Started"
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Title at bottom (over gradient)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = course.activityTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Course Details
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Progress",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${course.completionPercentage.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                course.completionPercentage == 100.0 -> Color(0xFF4CAF50)
                                course.completionPercentage >= 50.0 -> Color(0xFF2196F3)
                                else -> Color(0xFFFF9800)
                            }
                        )
                    }

                    LinearProgressIndicator(
                        progress = { ((course.completionPercentage / 100.0).toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            course.completionPercentage == 100.0 -> Color(0xFF4CAF50)
                            course.completionPercentage >= 50.0 -> Color(0xFF2196F3)
                            else -> Color(0xFFFF9800)
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricBadge(
                            icon = Icons.Default.PlayCircle,
                            label = "${course.completedVideos}/${course.totalVideos}",
                            color = Color(0xFF2196F3)
                        )
                        MetricBadge(
                            icon = Icons.Default.Description,
                            label = "${course.completedResourcesCount}/${course.totalResources}",
                            color = Color(0xFF9C27B0)
                        )
                    }

                    // Continue Button
                    Button(
                        onClick = onClick,
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            if (course.completionPercentage == 100.0) Icons.Default.Replay else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (course.completionPercentage == 100.0) "Review" else "Continue",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Last Accessed
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Last accessed: ${formatLastAccessed(course.lastAccessed)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth(),
        placeholder = {
            Text(
                "Search courses...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        singleLine = true
    )
}

@Composable
private fun FiltersAndSort(
    currentSort: MyCoursesViewModel.SortOrder,
    currentFilter: MyCoursesViewModel.FilterType,
    onSortChange: (MyCoursesViewModel.SortOrder) -> Unit,
    onFilterChange: (MyCoursesViewModel.FilterType) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                MyCoursesViewModel.FilterType.ALL to "All",
                MyCoursesViewModel.FilterType.IN_PROGRESS to "In Progress",
                MyCoursesViewModel.FilterType.COMPLETED to "Completed"
            ).forEach { (filter, label) ->
                FilterChip(
                    selected = currentFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(label) },
                    leadingIcon = if (currentFilter == filter) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }

        // Sort Options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                MyCoursesViewModel.SortOrder.RECENT to "Recent",
                MyCoursesViewModel.SortOrder.PROGRESS to "Progress",
                MyCoursesViewModel.SortOrder.ALPHABETICAL to "A-Z"
            ).forEach { (sort, label) ->
                FilterChip(
                    selected = currentSort == sort,
                    onClick = { onSortChange(sort) },
                    label = { Text(label) },
                    leadingIcon = if (currentSort == sort) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun ContinueLearningCard(
    course: UserProgressResponse,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onContinue),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFF6B6B)
                    )
                    Text(
                        "Continue Learning",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text(
                        course.activityTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { ((course.completionPercentage / 100.0).toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF4CAF50),
                            trackColor = Color.White.copy(alpha = 0.3f),
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                        )

                        Text(
                            "${course.completionPercentage.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCourseState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricBadge(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                "Loading your courses...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun EmptyState(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Text(
                "No Courses Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Start learning by enrolling in courses",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Explore Courses")
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                "Failed to load courses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

// Helper function to format date
fun formatLastAccessed(timestamp: String): String {
    return try {
        // Simple formatting - you can enhance this with proper date parsing
        val dateTime = timestamp.substringBefore('T')
        dateTime
    } catch (e: Exception) {
        "Recently"
    }
}