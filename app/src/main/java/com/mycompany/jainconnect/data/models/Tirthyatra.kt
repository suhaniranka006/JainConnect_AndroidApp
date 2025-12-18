package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Tirthyatra(
    @SerializedName("_id") val id: String? = null,
    val title: String,
    val creatorId: String, // ID of the creator
    val admins: List<String> = emptyList(), // List of Admin User IDs
    val participants: List<String> = emptyList(), // List of Participant User IDs
    val startDate: Date? = null,
    val endDate: Date? = null,
    val visibility: String = "Private", // "Public" or "Private"
    val joinMode: String = "Open", // "Open" or "Approval"
    val itinerary: List<ItineraryDay> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val pendingRequests: List<String> = emptyList(),
    val chatId: String? = null,
    val notes: String? = null
)

data class ItineraryDay(
    val day: Int,
    val date: Date? = null,
    val title: String? = null,
    val activities: List<Activity> = emptyList()
)

data class Activity(
    val type: String, // 'Temple', 'Dharamshala', 'Travel', 'Custom'
    val time: String? = null,
    val name: String,
    val details: String? = null,
    val locationId: String? = null,
    val locationModel: String? = null,
    val notes: String? = null
)

data class ChecklistItem(
    val item: String,
    var isChecked: Boolean = false,
    val assignedTo: String? = null
)
