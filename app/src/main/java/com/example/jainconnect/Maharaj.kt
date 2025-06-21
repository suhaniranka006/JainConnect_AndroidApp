package com.example.jainconnect // Or your actual package

import com.google.gson.annotations.SerializedName

data class Maharaj(
    @SerializedName("id") // Assuming no id from JSON for now
    val id: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("Location") // Case-sensitive: Maps JSON "Location" (capital L)
    val currentSthan: String,

    @SerializedName("title") // Mapping JSON "title" to sampraday
    val sampraday: String?,

    // "contactInfo" is not in the JSON, so it will be null (as it's nullable)
    @SerializedName("contactInfo")
    val contactInfo: String? = null,

    // What to do with JSON "city" and "date"?
    // Option A: Ignore them if not immediately needed.
    // Option B: Add new fields for them:
    @SerializedName("city")
    val city: String?, // Add this if you want to store the city separately

    @SerializedName("date")
    val relevantDate: String? // Add this if the date from JSON is important
    // and give it a meaningful name
)