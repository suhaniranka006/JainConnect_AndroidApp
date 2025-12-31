package com.mycompany.jainconnect.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Represents a Bhojanshala (Community Dining Hall).
 * Implements [Parcelable] to be passed between Activities/Fragments.
 */
@Parcelize
data class Bhojanshala(
    @SerializedName("_id")
    val _id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("city")
    val city: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("timings") // Combined string e.g., "10:00 AM - 5:00 PM"
    val timings: String?,

    @SerializedName("openingTime")
    val openingTime: String?,

    @SerializedName("closingTime")
    val closingTime: String?,

    @SerializedName("contact")
    val contact: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("image")
    val image: String?
) : Parcelable
