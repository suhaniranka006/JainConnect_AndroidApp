package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName
import com.mycompany.jainconnect.R

data class Maharaj(
    @SerializedName("id") // Assuming no id from JSON for now
    val id: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("title") // Mapping JSON "title" to sampraday
    val sampraday: String?,

    @SerializedName("contactInfo")
    val contactInfo: String? = null,

    @SerializedName("city")
    val city: String?, 

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null, 

    @SerializedName("date")
    val relevantDate: String?, 

    @SerializedName("image")
    val image: String?,

    @SerializedName("arrivalDate")
    val arrivalDate: String? = null,

    @SerializedName("viharDate")
    val viharDate: String? = null,

    @SerializedName("description")
    val description: String? = null
) : java.io.Serializable

// Update this Data Class at the bottom
data class MaharajSubmissionRequest(
    val name: String,
    val title: String,       // ✅ Added
    val city: String,    // This maps to 'city' or 'location' in backend
    val date: String,        // ✅ Added
    val contactInfo: String,
    val arrivalDate: String?,
    val viharDate: String?,
    val description: String?
)