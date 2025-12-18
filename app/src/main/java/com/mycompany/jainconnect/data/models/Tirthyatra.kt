package com.mycompany.jainconnect.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Tirthyatra(
    @SerializedName("_id") val id: String? = null,
    val title: String,
    val imageUrl: String? = null,
    val creatorId: TirthyatraUser? = null, // Changed from String

    val admins: List<String> = emptyList(), // List of Admin User IDs - Keeping as String for now or update? Admins are subset of participants usually.
    val participants: List<TirthyatraUser> = emptyList(), // Changed to Object List
    val startDate: Date? = null,
    val endDate: Date? = null,
    val visibility: String = "Private", // "Public" or "Private"
    val joinMode: String = "Open", // "Open" or "Approval"
    val itinerary: List<ItineraryDay> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val pendingRequests: List<JoinRequest> = emptyList(), // Updated to Object List
    val chatId: String? = null,
    val notes: String? = null,
    val durationDays: Int = 1,
    val templateId: String? = null
) : Parcelable

@Parcelize
data class JoinRequest(
    @SerializedName("_id") val id: String? = null, // Request ID if any (or just user object)
    val userId: TirthyatraUser? = null, // Populated User
    val message: String? = null,
    val contactNumber: String? = null,
    val status: String = "Pending"
) : Parcelable

@Parcelize
data class TirthyatraUser(
    @SerializedName("_id") val id: String,
    val name: String,
    val profileImage: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val phone: String? = null,
    val mobileNumber: String? = null // Backend sometimes uses mobileNumber or phone? Stick to backend response. Controller sends 'phone' and 'mobileNumber'.
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
    val type: String, // 'Temple', 'Dharamshala', 'Travel', 'Custom'
    val time: String? = null,
    val name: String,
    val details: String? = null,
    val locationId: String? = null,
    val locationModel: String? = null,
    val notes: String? = null
) : Parcelable

@Parcelize
data class ChecklistItem(
    val item: String,
    var isChecked: Boolean = false,
    val assignedTo: String? = null
) : Parcelable
