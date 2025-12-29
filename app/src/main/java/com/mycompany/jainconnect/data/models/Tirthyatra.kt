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
    val participantDetails: List<ParticipantDetail>? = null, // New
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
    val templateId: String? = null,
    val creatorDetails: CreatorDetails? = null // New
) : Parcelable

@Parcelize
data class CreatorDetails(
    val name: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val contact: String? = null,
    val message: String? = null,
    val peopleCount: String? = null 
) : Parcelable

@Parcelize
data class JoinRequest(
    @SerializedName("_id") val id: String? = null,
    val userId: TirthyatraUser? = null,
    val message: String? = null,
    val contactNumber: String? = null,
    val peopleCount: Int = 1,
    val status: String = "Pending",
    val name: String? = null,
    val age: String? = null,
    val gender: String? = null
) : Parcelable

@Parcelize
data class TirthyatraUser(
    @SerializedName("_id") val id: String,
    val name: String,
    val profileImage: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val phone: String? = null,
    val mobileNumber: String? = null,
    val message: String? = null, // Transient field for Yatra details
    val peopleCount: String? = null // Transient field for Yatra details
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
    val type: String,
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

@Parcelize
data class ParticipantDetail(
    val userId: String? = null,
    val name: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val contact: String? = null,
    val message: String? = null,
    val peopleCount: String? = null // Backend sends string or int? Controller sends what comes from request.
    // Backend schema says String/Number. Let's use Any? or String for safety, or Int if confident.
    // Schema said 'peopleCount: String' initially then I updated to Number? No I used String in Schema. 
    // Wait, step 208 I put 'peopleCount: String' in Schema.
    // So let's use String here to be safe and convert if needed, or Int if Gson handles it. 
    // Let's us String to match Schema.
) : Parcelable
