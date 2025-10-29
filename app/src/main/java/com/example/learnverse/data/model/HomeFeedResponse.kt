package com.example.learnverse.data.model

import com.google.gson.annotations.SerializedName

data class HomeFeedResponse(
    @SerializedName("recommended")
    val recommended: List<Activity>,

    @SerializedName("popular")
    val popular: List<Activity>,

    @SerializedName("topRated")
    val topRated: List<Activity>,

    @SerializedName("newActivities")
    val newActivities: List<Activity>,

    @SerializedName("featured")
    val featured: List<Activity>,

    @SerializedName("categories")
    val categories: List<String>
)