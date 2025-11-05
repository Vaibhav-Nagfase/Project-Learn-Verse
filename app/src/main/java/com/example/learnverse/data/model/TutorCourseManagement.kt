package com.example.learnverse.data.model

// Video Management - matches your VideoDTO.AddVideoRequest
data class AddVideoRequest(
    val title: String,
    val description: String,
    val videoUrl: String,
    val order: Int,
    val isPreview: Boolean,
    val resources: List<ResourceRequest> = emptyList()
)

data class ResourceRequest(
    val type: String,
    val title: String,
    val url: String
)

// Update Video Request - matches your VideoDTO.UpdateVideoRequest
data class UpdateVideoRequest(
    val title: String?,
    val description: String?,
    val duration: Int?,
    val videoUrl: String?,
    val thumbnailUrl: String?,
    val order: Int?,
    val isPreview: Boolean?,
    val resources: List<ResourceRequest>?
)

// Resource Management - matches your VideoDTO.AddResourceRequest
data class AddResourceRequest(
    val type: String,
    val title: String,
    val url: String
)

// Meeting Management - matches your MeetingDetailsRequest
data class UpdateMeetingRequest(
    val platform: String?,
    val meetingLink: String?,
    val meetingId: String?,
    val passcode: String?
)

// Add to your data models file
data class TutorCourseDetail(
    val activity: Activity,
    val enrolledStudents: List<EnrolledStudent>,
    val stats: CourseStats
)

data class EnrolledStudent(
    val userId: String,
    val name: String,
    val email: String,
    val enrolledAt: String,
    val completionPercentage: Double,
    val lastAccessed: String,
    val totalVideosWatched: Int,
    val totalResourcesCompleted: Int
)

data class CourseStats(
    val totalStudents: Int,
    val completedStudents: Int,
    val inProgressStudents: Int,
    val totalRevenue: Double,
    val averageProgress: Double,
    val completionRate: Double,
    val totalVideos: Int,
    val totalResources: Int,
    val averageCompletionTime: String?
)