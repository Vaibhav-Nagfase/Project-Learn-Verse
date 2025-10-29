package com.example.learnverse.ui.screen.enrollment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learnverse.data.model.Activity
import com.example.learnverse.ui.screen.search.ActivityResultCard
import com.example.learnverse.viewmodel.ActivitiesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    navController: NavController,
    activitiesViewModel: ActivitiesViewModel
) {
    val enrolledActivities by activitiesViewModel.myEnrolledActivities.collectAsState()

    // Fetch the courses when the screen is first displayed
    LaunchedEffect(Unit) {
        activitiesViewModel.fetchMyEnrollments()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Courses") }) }
    ) { paddingValues ->
        if (enrolledActivities.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("You are not enrolled in any courses yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(enrolledActivities) { activity ->
                    ActivityResultCard(activity = activity) {
                        navController.navigate("activityDetail/${activity.id}")
                    }
                }
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EnrolledActivityCard(activity: Activity, onClick: () -> Unit) {
//    Card(
//        onClick = onClick,
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(Modifier.padding(16.dp)) {
//            Text(activity.title, style = MaterialTheme.typography.titleMedium)
//            Text("by ${activity.tutorName}", style = MaterialTheme.typography.bodySmall)
//            // TODO: You could add a progress bar here later
//        }
//    }
//}