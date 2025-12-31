package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a Community Event.
 * Maps to the 'Event' schema in the MongoDB backend.
 */
data class Event(

    // Unique Identifier (maps to MongoDB `_id`)
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

    // Time of the event (e.g., "10:00 AM")
    @SerializedName("time")
    val time: String? = null,

    @SerializedName("city") // Maps JSON "city" to Kotlin "location"
    val location: String,

    @SerializedName("description")
    val description: String?,

    // Virtual Field: Total count of users attending
    @SerializedName("rsvpCount")
    val rsvpCount: Int,

    // List of User IDs who have RSVP'd
    @SerializedName("rsvps")
    val rsvps: List<String>,

    @SerializedName("image") 
    val image: String?,

    // Contact number for the event organizer
    @SerializedName("contact")
    val contact: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?
) : java.io.Serializable

/**
 * Response received when a user RSVPs to an event.
 */
data class RsvpResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("rsvpCount")
    val rsvpCount: Int
)

// --- Helper Models for API Requests ---

/**
 * Data class for creating or updating an Event.
 * This matches the FormData structure sent to the backend.
 */
data class EventSubmissionRequest(
    val title: String, 
    val city: String, 
    val date: String,
    val startDate: String,
    val endDate: String,
    val time: String,
    val contact: String,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

