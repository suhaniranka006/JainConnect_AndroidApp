package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a Jain Monk/Saint (Maharaj).
 */
data class Maharaj(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("title") // E.g., Acharya, Muni (Maps to 'sampraday' in Kotlin for legacy reasons, but logically Title)
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

/**
 * Request body for submitting a new Maharaj entry.
 */
data class MaharajSubmissionRequest(
    @SerializedName("name") val name: String,
    @SerializedName("title") val title: String,
    @SerializedName("city") val city: String,
    @SerializedName("date") val date: String,
    @SerializedName("contactInfo") val contactInfo: String,
    @SerializedName("arrivalDate") val arrivalDate: String?,
    @SerializedName("viharDate") val viharDate: String?,
    @SerializedName("description") val description: String?
)
