package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

data class BhojanshalaSubmissionRequest(
    @SerializedName("name") val name: String,
    @SerializedName("city") val city: String,
    @SerializedName("address") val address: String,
    @SerializedName("timings") val timings: String?,
    @SerializedName("openingTime") val openingTime: String?,
    @SerializedName("closingTime") val closingTime: String?,
    @SerializedName("contact") val contact: String?,
    @SerializedName("description") val description: String?
)
