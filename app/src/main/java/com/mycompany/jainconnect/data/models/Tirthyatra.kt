package com.mycompany.jainconnect.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Represents a Tirthyatra (Pilgrimage) Trip.
 * This is a complex object containing participants, itinerary, checklist, etc.
 * Implements [Parcelable] for comprehensive data passing.
 */
@Parcelize
data class Tirthyatra(
    @SerializedName("_id") val id: String? = null,
    val title: String,
    val imageUrl: String? = null,
    
    // The user who created the trip
    val creatorId: TirthyatraUser? = null, 

    // List of User IDs who are admins
    val admins: List<String> = emptyList(), 
    
    // List of Participants (simplified user objects)
    val participants: List<TirthyatraUser> = emptyList(), 
    
    // Additional details for participants (Contact, Message, Count)
    val participantDetails: List<ParticipantDetail>? = null, 
    
    val startDate: Date? = null,
    val endDate: Date? = null,
    val visibility: String = "Private", // "Public" or "Private"
    val joinMode: String = "Open", // "Open" or "Approval"
    
    // Daily Plan
    val itinerary: List<ItineraryDay> = emptyList(),
    
    // Shared Checklist
    val checklist: List<ChecklistItem> = emptyList(),
    
    // Pending requests to join the trip
    val pendingRequests: List<JoinRequest> = emptyList(), 
    
    val chatId: String? = null,
    val notes: String? = null,
    val durationDays: Int = 1,
    val templateId: String? = null,
    
    // Detailed info about creator for Public trips
    val creatorDetails: CreatorDetails? = null 
) : Parcelable

/**
 * Creator contact/meta details for public display.
 */
@Parcelize
data class CreatorDetails(
    val name: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val contact: String? = null,
    val message: String? = null,
    val peopleCount: String? = null 
) : Parcelable

/**
 * A request from a user to join a trip.
 */
@Parcelize
data class JoinRequest(
    @SerializedName("_id") val id: String? = null,
    val userId: TirthyatraUser? = null,
    val message: String? = null,
    val contactNumber: String? = null,
    val peopleCount: Int = 1,
    val status: String = "Pending", // "Pending", "Accepted", "Rejected"
    val name: String? = null,
    val age: String? = null,
    val gender: String? = null
) : Parcelable

/**
 * Simplified User object for Tirthyatra contexts.
 */
@Parcelize
data class TirthyatraUser(
    @SerializedName("_id") val id: String,
    val name: String,
    val profileImage: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val phone: String? = null,
    val mobileNumber: String? = null,
    val message: String? = null, 
    val peopleCount: String? = null 
) : Parcelable

@Parcelize
data class ItineraryDay(
    val day: Int,
    val date: Date? = null,
    val title: String? = null,
    val activities: List<Activity> = emptyList()
) : Parcelable

@Parcelize
data class Activity(
    val type: String, // "Travel", "Temple", "Food", "Stay", "Other"
    val time: String? = null,
    val name: String,
    val details: String? = null,
    val locationId: String? = null,
    val locationModel: String? = null, // "Temple", "Bhojanshala", etc.
    val notes: String? = null
) : Parcelable

@Parcelize
data class ChecklistItem(
    val item: String,
    var isChecked: Boolean = false,
    val assignedTo: String? = null
) : Parcelable

@Parcelize
data class ParticipantDetail(
    val userId: String? = null,
    val name: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val contact: String? = null,
    val message: String? = null,
    val peopleCount: String? = null 
) : Parcelable

