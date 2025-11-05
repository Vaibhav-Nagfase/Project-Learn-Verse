package com.example.learnverse.data.model

data class StudentCourseViewResponse(
    val activityId: String,
    val activityTitle: String,
    val description: String,
    val tutorName: String,
    val bannerImageUrl: String?,
    val completionPercentage: Double,
    val totalVideos: Int,
    val completedVideos: Int,
    val totalResources: Int,
    val completedResourcesCount: Int,
    val videos: List<VideoWithProgress>,
    val resources: List<ResourceWithProgress>,
    val meetingInfo: MeetingInfo?,
    val continueFromVideoId: String?,
    val continueFromVideoTitle: String?
)

data class VideoWithProgress(
    val videoId: String,
    val title: String,
    val description: String,
    val duration: Int, // in seconds
    val videoUrl: String,
    val thumbnailUrl: String?,
    val order: Int,
    val isPreview: Boolean,
    val completed: Boolean,
    val watchedSeconds: Long,
    val progressPercentage: Double,
    val lastWatched: String?,
    val resources: List<VideoResource>
)

data class VideoResource(
    val type: String,
    val title: String,
    val url: String,
    val downloaded: Boolean
)

data class ResourceWithProgress(
    val resourceId: String,
    val type: String,
    val title: String,
    val url: String,
    val completed: Boolean
)

data class MeetingInfo(
    val platform: String?,
    val meetingLink: String?,
    val meetingId: String?,
    val passcode: String?
)