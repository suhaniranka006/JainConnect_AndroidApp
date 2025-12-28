package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("_id") val id: String,
    @SerializedName("type") val type: String, // CARPOOL_REQUEST, CARPOOL_UPDATE, INFO
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("relatedId") val relatedId: String?,
    @SerializedName("isRead") val isRead: Boolean,
    @SerializedName("createdAt") val createdAt: String
)

data class NotificationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("count") val count: Int,
    @SerializedName("data") val data: List<Notification>
)
