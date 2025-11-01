package com.example.learnverse.data.model

import com.google.gson.annotations.SerializedName

data class CommunityPost(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorType: String, // Should be "TUTOR"
    val authorProfilePicture: String?,
    val content: String?, // Content can be empty if there's only media
    val mediaUrl: String?,
    val mediaType: String, // "image", "video", "none"
    val likedBy: List<String>, // List of user IDs who liked the post
    // val shares: List<String>, // Included in API but maybe not needed in app yet
    val comments: List<Comment>,
    val commentsCount: Int,
    val createdAt: String, // Consider converting to a Date/Time object later
    val updatedAt: String
)

data class Comment(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorType: String, // "USER" or "TUTOR"
    val authorProfilePicture: String?,
    val content: String,
    val likedBy: List<String>, // List of user IDs who liked the comment
    val createdAt: String // Consider converting later
)

// Data class for the response of the feed endpoint (includes pagination info)
data class CommunityFeedResponse(
    val content: List<CommunityPost>,
    val pageable: PageableInfo,
    val totalElements: Int,
    val totalPages: Int,
    val last: Boolean,
    val first: Boolean,
    val size: Int,
    val number: Int, // Current page number
    val numberOfElements: Int,
    val empty: Boolean
)

// Helper for pagination info (can be simplified if not all fields are needed)
data class PageableInfo(
    val pageNumber: Int,
    val pageSize: Int,
    val offset: Long,
    val paged: Boolean,
    val unpaged: Boolean
    // sort info omitted for brevity, add if needed
)

// Data class for the POST request body when adding a comment
data class AddCommentRequest(
    val content: String
)