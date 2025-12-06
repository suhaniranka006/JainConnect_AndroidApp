package com.mycompany.jainconnect // Or your actual package for Event.kt

import com.google.gson.annotations.SerializedName

data class Event(

    // === NAYA FIELD ===
    // Humein event ki ID chahiye taaki hum API ko bata sakein ki KIS event par click hua
    @SerializedName("_id")
    val _id: String,

    @SerializedName("title") // Maps JSON "title" to Kotlin "name"
    val name: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("time")
    val time: String? = null,

    @SerializedName("city") // Maps JSON "city" to Kotlin "location"
    val location: String,

    @SerializedName("description")
    val description: String?,

    // === NAYA FIELD ===
    // Yeh backend ke 'virtual' field (rsvpCount) se match karega
    @SerializedName("rsvpCount")
    val rsvpCount: Int,

    // === NAYA FIELD ===
    // Yeh unn users ki list hai jo event mein jaa rahe hain
    @SerializedName("rsvps")
    val rsvps: List<String>
)


// --- Helper Classes (Add at bottom of file) ---
// Inside ApiService.kt (at the bottom)

data class EventSubmissionRequest(
    val title: String, // Was 'name'
    val city: String,  // Was 'location'
    val date: String,
    val time: String,  // ✅ Added
    val description: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String
)
