package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Represents a Jain Temple listing.
 * Implements [Serializable] for simple object passing.
 */
data class Temple(
    @SerializedName("_id") val _id: String,
    @SerializedName("name") val name: String,
    @SerializedName("city") val city: String,
    @SerializedName("address") val address: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("contact") val contact: String?,
    @SerializedName("isApproved") val isApproved: Boolean
) : Serializable
