package com.example.learnverse.data.model

import com.google.gson.annotations.SerializedName

// This new "Activity" class replaces your old "ActivityResponse"
data class Activity(

//    @SerializedName("_id")
    val id: String,

    val tutorName: String,
    val title: String,
    val description: String,
    val subject: String,
    val mode: String,
    val difficulty: String,
    // Add any other top-level fields from the JSON here...

    @SerializedName("duration")
    val durationInfo: DurationInfo,

    @SerializedName("enrollmentInfo")
    val enrollmentInfo: EnrollmentInfo,

    // You can add the other nested JSON objects as data classes below
    val pricing: Pricing
    // val schedule: Schedule,
    // val instructorDetails: InstructorDetails,
)

// Helper data classes for nested JSON objects
data class DurationInfo(
    val totalDuration: Int,
    val durationDescription: String
)

data class EnrollmentInfo(
    val enrolledCount: Int,
    val maxCapacity: Int
)

data class Pricing(
    val price: Double,
    val currency: String,
    val discountPrice: Double?
)

data class ActivityFilter(
    // Category
    val subjects: List<String>? = null,
    val activityTypes: List<String>? = null,
    val modes: List<String>? = null,
    val difficulties: List<String>? = null,

    // Location
    val cities: List<String>? = null,

    // Price
    val minPrice: Int? = null,
    val maxPrice: Int? = null,

    // Features
    val demoAvailable: Boolean? = null,

    // Sorting
    val sortBy: String? = null,      // e.g., "price", "rating"
    val sortDirection: String? = null // "asc" or "desc"
)

// It's a generic class that can hold a "page" of any type of data.
data class PagedResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val totalPages: Int,
    val totalElements: Int
)