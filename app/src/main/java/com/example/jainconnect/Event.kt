package com.example.jainconnect // Or your actual package for Event.kt

import com.google.gson.annotations.SerializedName

data class Event(
    // Assuming 'id' is not provided by this specific API endpoint for events.
    // If you need an ID client-side, you might generate it or get it from elsewhere.
    // For now, making it nullable as it's not in the JSON.
    @SerializedName("id") // If your JSON *might* someday have an 'id'
    val id: String? = null,

    @SerializedName("title") // Maps JSON "title" to Kotlin "name"
    val name: String,

    @SerializedName("date")
    val date: String,

    // "time" is not in the JSON. Make it nullable if it's optional for an Event.
    // If it's crucial and always missing, you might need to fetch it differently or reconsider its place.
    @SerializedName("time")
    val time: String? = null, // Making it nullable

    @SerializedName("city") // Maps JSON "city" to Kotlin "location"
    val location: String,

    @SerializedName("description")
    val description: String?
)