package com.example.learnverse.data.model

import com.google.gson.annotations.SerializedName

// This new "Activity" class replaces your old "ActivityResponse"
data class Activity(

    @SerializedName("tutorId")
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