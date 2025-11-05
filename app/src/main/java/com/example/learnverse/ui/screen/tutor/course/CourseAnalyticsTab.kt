package com.example.learnverse.ui.screen.tutor.course

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learnverse.data.model.CourseStats

@Composable
fun CourseAnalyticsTab(stats: CourseStats) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            ModernAnalyticsHeader()
        }

        item {
            ModernMetricsGrid(stats = stats)
        }

        item {
            ModernCompletionChart(stats = stats)
        }

        item {
            ModernStudentDistribution(stats = stats)
        }

        item {
            ModernContentStats(stats = stats)
        }

        item {
            ModernProgressInsights(stats = stats)
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun ModernAnalyticsHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
                    ),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Analytics,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Column {
            Text(
                "Course Analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Track performance and engagement",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModernMetricsGrid(stats: CourseStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.People,
                value = stats.totalStudents,
                label = "Students",
                gradient = Brush.linearGradient(
                    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                )
            )
            AnimatedMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.AttachMoney,
                value = "₹${String.format("%.0f", stats.totalRevenue)}",
                label = "Revenue",
                gradient = Brush.linearGradient(
                    listOf(Color(0xFF10B981), Color(0xFF059669))
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.TrendingUp,
                value = "${stats.averageProgress.toInt()}%",
                label = "Avg Progress",
                gradient = Brush.linearGradient(
                    listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
                )
            )
            AnimatedMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CheckCircle,
                value = stats.completedStudents,
                label = "Completed",
                gradient = Brush.linearGradient(
                    listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
                )
            )
        }
    }
}

@Composable
fun AnimatedMetricCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Any,
    label: String,
    gradient: Brush
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradient.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(gradient, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        value.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ModernCompletionChart(stats: CourseStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.DonutLarge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Completion Rate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            AnimatedCircularProgress(
                progress = stats.completionRate / 100f,
                label = "${stats.completionRate.toInt()}%"
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${stats.completedStudents} of ${stats.totalStudents} students completed the course",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedCircularProgress(
    progress: Double,
    label: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.toFloat(),
        animationSpec = tween(1500, easing = EaseOutCubic),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val strokeWidth = 24.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Background circle
            drawCircle(
                color = Color(0xFFE5E7EB),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Gradient progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFF6366F1),
                        Color(0xFF8B5CF6),
                        Color(0xFFEC4899)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = Stroke.DefaultCap)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Completed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModernStudentDistribution(stats: CourseStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Student Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            AnimatedDistributionBar(
                completed = stats.completedStudents,
                inProgress = stats.inProgressStudents,
                total = stats.totalStudents
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DistributionLegend(
                    color = Color(0xFF10B981),
                    label = "Completed",
                    value = stats.completedStudents
                )
                DistributionLegend(
                    color = Color(0xFF6366F1),
                    label = "In Progress",
                    value = stats.inProgressStudents
                )
                DistributionLegend(
                    color = Color(0xFFE5E7EB),
                    label = "Not Started",
                    value = stats.totalStudents - stats.completedStudents - stats.inProgressStudents
                )
            }
        }
    }
}

@Composable
fun AnimatedDistributionBar(
    completed: Int,
    inProgress: Int,
    total: Int
) {
    val animatedCompleted by animateFloatAsState(
        targetValue = if (total > 0) completed.toFloat() / total else 0f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "completed"
    )

    val animatedInProgress by animateFloatAsState(
        targetValue = if (total > 0) inProgress.toFloat() / total else 0f,
        animationSpec = tween(1000, delayMillis = 200, easing = EaseOutCubic),
        label = "inProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE5E7EB))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (animatedCompleted > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedCompleted)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF10B981), Color(0xFF059669))
                            ),
                            RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        )
                )
            }

            if (animatedInProgress > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedInProgress / (1f - animatedCompleted))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun DistributionLegend(
    color: Color,
    label: String,
    value: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
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
fun ModernContentStats(stats: CourseStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.LibraryBooks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "Course Content",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ContentStatBubble(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.PlayArrow,
                    value = stats.totalVideos,
                    label = "Videos",
                    color = Color(0xFF6366F1)
                )
                ContentStatBubble(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Description,
                    value = stats.totalResources,
                    label = "Resources",
                    color = Color(0xFFF59E0B)
                )
                ContentStatBubble(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.People,
                    value = stats.totalStudents,
                    label = "Learners",
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
fun ContentStatBubble(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Int,
    label: String,
    color: Color
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(spring(dampingRatio = 0.5f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(6.dp, CircleShape)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = color
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    value.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}


@Composable
fun ModernProgressInsights(stats: CourseStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Insights,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    "Key Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            InsightRow(
                icon = Icons.Default.TrendingUp,
                label = "Average Progress",
                value = "${stats.averageProgress.toInt()}%",
                color = Color(0xFF10B981)
            )

            Spacer(Modifier.height(12.dp))

            InsightRow(
                icon = Icons.Default.School,
                label = "Completion Rate",
                value = "${stats.completionRate.toInt()}%",
                color = Color(0xFF6366F1)
            )

            Spacer(Modifier.height(12.dp))

            InsightRow(
                icon = Icons.Default.AttachMoney,
                label = "Total Revenue",
                value = "₹${String.format("%.0f", stats.totalRevenue)}",
                color = Color(0xFFF59E0B)
            )
        }
    }
}

@Composable
fun InsightRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = color
                )
            }

            Text(
                label,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun Brush.copy(alpha: Float): Brush {
    return this
}