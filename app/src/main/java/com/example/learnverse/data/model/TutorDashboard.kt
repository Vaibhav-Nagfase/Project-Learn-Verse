package com.example.learnverse.data.model

data class TutorDashboardStats(
    val totalEarnings: Double,
    val monthlyEarnings: Double,
    val pendingEarnings: Double,
    val totalStudents: Int,
    val totalActivities: Int,
    val topActivities: List<ActivityPerformance>,
    val recentEnrollments: List<RecentEnrollment>,
    val revenueChart: List<MonthlyRevenue>
)

data class ActivityPerformance(
    val activityId: String,
    val activityTitle: String,
    val enrolledStudents: Int,
    val totalRevenue: Double,
    val averageRating: Double
)

data class RecentEnrollment(
    val enrollmentId: String,
    val studentName: String,
    val activityTitle: String,
    val enrolledAt: String,
    val amountEarned: Double
)

data class MonthlyRevenue(
    val month: String,
    val year: Int,
    val revenue: Double,
    val enrollments: Int
)
