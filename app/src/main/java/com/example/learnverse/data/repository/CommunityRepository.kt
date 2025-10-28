package com.example.learnverse.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.learnverse.data.model.*
import com.example.learnverse.data.remote.ApiService
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.InputStream

// Inject ApiService and potentially Context if needed for file operations
class CommunityRepository(private val api: ApiService) {

    suspend fun getFeedPosts(page: Int, size: Int): CommunityFeedResponse {
        val response = api.getCommunityFeed(page, size)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to fetch feed posts: ${response.errorBody()?.string()}")
    }

    // --- MODIFY createPost ---
    suspend fun createPost(context: Context, content: String?, fileUri: Uri?, finalMediaType: String): CommunityPost {
        val contentPart = content?.toRequestBody("text/plain".toMediaTypeOrNull())

        // --- Create RequestBody directly from Uri InputStream ---
        val filePart = fileUri?.let { uri ->
            val fileName = getFileName(context, uri)
            val requestBody = object : RequestBody() {
                override fun contentType(): MediaType? = finalMediaType.toMediaTypeOrNull() // <-- Use finalMediaType

                // Try to get content length from ContentResolver
                override fun contentLength(): Long {
                    return try {
                        context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: -1
                    } catch (e: Exception) {
                        -1 // Unknown length
                    }
                }

                // Write the InputStream directly to the network sink
                override fun writeTo(sink: BufferedSink) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        sink.writeAll(inputStream.source())
                    }
                }
            }
            MultipartBody.Part.createFormData("file", fileName, requestBody)
        }
        // --- End of new RequestBody logic ---

        val response = api.createPost(contentPart, filePart) // Interceptor handles auth
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to create post: ${response.errorBody()?.string()}")
    }

    // --- MODIFY updatePost ---
    suspend fun updatePost(postId: String, context: Context, content: String?, fileUri: Uri?, mediaType: String): CommunityPost {
        val contentPart = content?.toRequestBody("text/plain".toMediaTypeOrNull())

        // Create RequestBody directly from Uri InputStream
        val filePart = fileUri?.let { uri ->
            val fileName = getFileName(context, uri)
            val requestBody = object : RequestBody() {
                override fun contentType(): MediaType? = mediaType.toMediaTypeOrNull()
                override fun contentLength(): Long {
                    return try {
                        context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: -1
                    } catch (e: Exception) { -1 }
                }
                override fun writeTo(sink: BufferedSink) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        sink.writeAll(inputStream.source())
                    }
                }
            }
            MultipartBody.Part.createFormData("file", fileName, requestBody)
        }
        // If fileUri is null, filePart will be null, and the backend should
        // know to keep the old file (confirm this logic with backend).

        val response = api.updatePost(postId, contentPart, filePart) // Interceptor handles auth
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Failed to update post: ${response.errorBody()?.string()}")
    }


    // --- ADD (or move) getFileName helper function ---
    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "temp_media_file_${System.currentTimeMillis()}"
    }


    suspend fun deletePost(postId: String) {
        val response = api.deletePost(postId)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete post: ${response.errorBody()?.string()}")
        }
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