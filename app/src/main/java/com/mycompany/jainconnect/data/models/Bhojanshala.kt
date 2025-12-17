package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

data class Bhojanshala(
    @SerializedName("_id")
    val _id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("city")
    val city: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("timings")
    val timings: String?,

    @SerializedName("contact")
    val contact: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("image")
    val image: String?
)
