package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.network.ApiService

data class Event(

    // === NAYA FIELD ===
    // Humein event ki ID chahiye taaki hum API ko bata sakein ki KIS event par click hua
    @SerializedName("_id")
    val _id: String,

    @SerializedName("title") // Maps JSON "title" to Kotlin "name"
    val name: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("startDate")
    val startDate: String?,

    @SerializedName("endDate")
    val endDate: String?,

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
    // === NAYA FIELD ===
    // Yeh unn users ki list hai jo event mein jaa rahe hain
    @SerializedName("rsvps")
    val rsvps: List<String>,

    @SerializedName("image") 
    val image: String?,

    @SerializedName("contact")
    val contact: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?
) : java.io.Serializable

// Moved from JainRepository
data class RsvpResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("rsvpCount")
    val rsvpCount: Int
)


// --- Helper Classes (Add at bottom of file) ---
// Inside ApiService.kt (at the bottom)

data class EventSubmissionRequest(
    val title: String, // Was 'name'
    val city: String,  // Was 'location'
    val date: String,
    val startDate: String,
    val endDate: String,
    val time: String,  // ✅ Added
    val contact: String,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

