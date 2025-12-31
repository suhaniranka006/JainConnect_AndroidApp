package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a "Jain Legacy" Story.
 */
data class Story(
    @SerializedName("_id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("content") val content: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("source") val source: String?,
    @SerializedName("likes") val likes: Int,
    @SerializedName("createdAt") val createdAt: String,
    
    // Local state to track if the current user has liked this story
    var isLiked: Boolean = false 
)
