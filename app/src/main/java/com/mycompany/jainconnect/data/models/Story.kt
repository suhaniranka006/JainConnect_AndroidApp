package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

data class Story(
    @SerializedName("_id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("content") val content: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("source") val source: String?,
    @SerializedName("likes") val likes: Int,
    @SerializedName("createdAt") val createdAt: String,
    var isLiked: Boolean = false // Local functionality
)
