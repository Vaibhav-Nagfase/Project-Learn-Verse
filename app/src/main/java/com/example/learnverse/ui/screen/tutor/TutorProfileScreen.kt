// TutorProfileScreen.kt
package com.example.learnverse.ui.screen.tutor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.learnverse.viewmodel.AuthViewModel
import com.example.learnverse.viewmodel.ActivitiesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorProfileScreen(
    tutorId: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    activitiesViewModel: ActivitiesViewModel
) {
    // Get tutor's first activity to extract instructor details
    val tutorActivities = activitiesViewModel.activities.collectAsState().value
        .filter { it.tutorId == tutorId }

    val tutorInfo = tutorActivities.firstOrNull()
    val instructorDetails = tutorInfo?.instructorDetails

    if (tutorInfo == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutor Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with Profile Picture (Animated Entry)
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Large profile picture
                            AsyncImage(
                                model = instructorDetails?.profileImage,
                                contentDescription = "Tutor Profile",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                text = tutorInfo.tutorName ?: "Unknown Tutor",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Social Proof
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                instructorDetails?.socialProof?.let { proof ->
                                    StatItem(
                                        icon = Icons.Default.School,
                                        value = "${proof.totalStudentsTaught ?: 0}",
                                        label = "Students"
                                    )
                                    StatItem(
                                        icon = Icons.Default.MenuBook,
                                        value = "${proof.coursesCount ?: 0}",
                                        label = "Courses"
                                    )
                                    StatItem(
                                        icon = Icons.Default.Star,
                                        value = "4.8",
                                        label = "Rating"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bio Section
            item {
                instructorDetails?.bio?.let { bio ->
                    ProfileSection(title = "About") {
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Qualifications
            item {
                instructorDetails?.qualifications?.let { qualifications ->
                    if (qualifications.isNotEmpty()) {
                        ProfileSection(title = "Qualifications") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                qualifications.forEach { qualification ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            qualification,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Experience
            item {
                instructorDetails?.experience?.let { experience ->
                    ProfileSection(title = "Experience") {
                        Text(
                            text = experience,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Specializations
            item {
                instructorDetails?.specializations?.let { specializations ->
                    if (specializations.isNotEmpty()) {
                        ProfileSection(title = "Specializations") {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                specializations.take(5).forEach { specialization ->
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(specialization) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Courses by this tutor
            item {
                ProfileSection(title = "Courses by ${tutorInfo.tutorName}") {
                    Text(
                        "${tutorActivities.size} courses available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(tutorActivities) { activity ->
                TutorCourseCard(
                    activity = activity,
                    onClick = {
                        navController.navigate("activityDetail/${activity.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
fun TutorCourseCard(
    activity: com.example.learnverse.data.model.Activity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    activity.title ?: "Untitled",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    activity.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                activity.pricing?.let { pricing ->
                    Text(
                        "₹${pricing.price}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}