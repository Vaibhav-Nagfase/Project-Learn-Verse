// Review.kt
package com.example.learnverse.data.model

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("id")
    val id: String,

    @SerializedName("activityId")
    val activityId: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("userName")
    val userName: String?,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("feedback")
    val feedback: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String?,

    @SerializedName("isEdited")
    val isEdited: Boolean = false,

    @SerializedName("isVerifiedEnrollment")
    val isVerifiedEnrollment: Boolean = false
)

// Response models
data class ReviewsResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("reviews")
    val reviews: List<Review>,

    @SerializedName("totalReviews")
    val totalReviews: Long,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("currentPage")
    val currentPage: Int
)

data class AddReviewResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("review")
    val review: Review
)

data class UpdateReviewResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("review")
    val review: Review
)

data class DeleteReviewResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)

data class MyReviewsResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("reviews")
    val reviews: List<Review>,

    @SerializedName("total")
    val total: Int
)

data class CheckReviewResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("hasReviewed")
    val hasReviewed: Boolean
)

// Request models
data class CreateReviewRequest(
    @SerializedName("rating")
    val rating: Int,

    @SerializedName("feedback")
    val feedback: String?
)

data class UpdateReviewRequest(
    @SerializedName("rating")
    val rating: Int?,

    @SerializedName("feedback")
    val feedback: String?
)