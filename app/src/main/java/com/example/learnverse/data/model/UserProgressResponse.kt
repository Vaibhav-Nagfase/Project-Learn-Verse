package com.example.learnverse.data.model

data class UserProgressResponse(
    val activityId: String,
    val activityTitle: String,
    val completionPercentage: Double,
    val totalVideos: Int,
    val completedVideos: Int,
    val totalResources: Int,
    val completedResourcesCount: Int,
    val videoProgress: List<VideoProgress>,
    val completedResources: List<String>,
    val lastAccessed: String,
    val lastWatchedVideoId: String?,
    val continueFromVideoId: String?
)

data class VideoProgress(
    val videoId: String,
    val videoTitle: String,
    val watchedSeconds: Long,
    val totalSeconds: Long,
    val completed: Boolean,
    val lastWatched: String,
    val progressPercentage: Double
)

data class MyCourseCard(
    val activityId: String,
    val activityTitle: String,
    val tutorName: String,
    val bannerImageUrl: String?,
    val completionPercentage: Double,
    val totalVideos: Int,
    val completedVideos: Int,
    val lastAccessed: String,
    val isContinuing: Boolean
)

data class UpdateVideoProgressRequest(
    val videoId: String,
    val videoTitle: String,
    val watchedSeconds: Long,
    val totalSeconds: Long,
    val completed: Boolean
)

data class UserProgress(
    val id: String,
    val userId: String,
    val activityId: String,
    val activityTitle: String,
    val videoProgress: List<VideoProgress>,
    val completedResources: List<String>,
    val completionPercentage: Double,
    val lastAccessed: String,
    val createdAt: String,
    val updatedAt: String,
    val totalVideos: Int,
    val completedVideos: Int,
    val totalResources: Int,
    val completedResourcesCount: Int
)

