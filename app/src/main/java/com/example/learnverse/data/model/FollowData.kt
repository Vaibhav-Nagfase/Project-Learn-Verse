package com.example.learnverse.data.model

data class FollowResponse(
    val id: String,
    val followerId: String,
    val followingId: String,
    val followedAt: String
)

data class FollowStats(
    val followingCount: Int,
    val followersCount: Int
)