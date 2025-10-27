package com.example.learnverse.data.repository

import com.example.learnverse.data.model.*
import com.example.learnverse.data.remote.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

// Inject ApiService and potentially Context if needed for file operations
class CommunityRepository(private val api: ApiService) {

    suspend fun getFeedPosts(page: Int, size: Int): CommunityFeedResponse {
        val response = api.getCommunityFeed(page, size)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to fetch feed posts: ${response.errorBody()?.string()}")
    }

    suspend fun createPost(content: String?, file: File?, mediaType: String): CommunityPost {
        val contentPart = content?.toRequestBody("text/plain".toMediaTypeOrNull())

        val filePart = file?.let {
            val requestFile = it.asRequestBody(mediaType.toMediaTypeOrNull())
            // Use original file name or generate one
            MultipartBody.Part.createFormData("file", it.name, requestFile)
        }

        val response = api.createPost(contentPart, filePart)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to create post: ${response.errorBody()?.string()}")
    }

    suspend fun likePost(postId: String): CommunityPost {
        val response = api.likePost(postId)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        // Consider specific error handling for already liked/unliked if needed
        throw Exception("Failed to like/unlike post: ${response.errorBody()?.string()}")
    }

    suspend fun addComment(postId: String, commentText: String): CommunityPost {
//        val request = AddCommentRequest(content = commentText)

        val response = api.addComment(postId, commentText)

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        if (response.code() == 401) throw Exception("Unauthorized to add comment (Invalid Token?).")
        throw Exception("Failed to add comment: ${response.errorBody()?.string()}")
    }

    suspend fun likeComment(postId: String, commentId: String): CommunityPost {
        val response = api.likeComment(postId, commentId)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to like/unlike comment: ${response.errorBody()?.string()}")
    }


    suspend fun updatePost(postId: String, content: String?, file: File?, mediaType: String): CommunityPost {
        val contentPart = content?.toRequestBody("text/plain".toMediaTypeOrNull())
        val filePart = file?.let {
            val requestFile = it.asRequestBody(mediaType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", it.name, requestFile)
        }

        val response = api.updatePost(postId, contentPart, filePart)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to update post: ${response.errorBody()?.string()}")
    }


    suspend fun deletePost(postId: String) {
        val response = api.deletePost(postId)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete post: ${response.errorBody()?.string()}")
        }
    }

    suspend fun getUserPosts(userId: String, page: Int, size: Int): CommunityFeedResponse {
        val response = api.getUserPosts(userId, page, size)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to fetch user posts: ${response.errorBody()?.string()}")
    }


    suspend fun followUser(userIdToFollow: String): FollowResponse {
        val response = api.followUser(userIdToFollow)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to follow user: ${response.errorBody()?.string()}")
    }

    suspend fun unfollowUser(userIdToUnfollow: String) {
        val response = api.unfollowUser(userIdToUnfollow)
        if (!response.isSuccessful) {
            throw Exception("Failed to unfollow user: ${response.errorBody()?.string()}")
        }
    }

    suspend fun getFollowingList(): List<String> {
        val response = api.getFollowingList()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to get following list: ${response.errorBody()?.string()}")
    }

    suspend fun getFollowStats(userId: String): FollowStats {
        val response = api.getFollowStats(userId)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to get follow stats: ${response.errorBody()?.string()}")
    }

}