package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

data class SeatRequest(
    @SerializedName("name") val name: String,
    @SerializedName("contact") val contact: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("seats") val seats: Int
)
