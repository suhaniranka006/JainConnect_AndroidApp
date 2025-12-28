package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

data class Carpool(
    @SerializedName("_id") val _id: String? = null, // Using _id to match API, aliased for usage
    @SerializedName("driverName") val driverName: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("destination") val destination: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("vehicleType") val vehicleType: String? = null,
    @SerializedName("seatsAvailable") val seatsAvailable: Int? = null,
    @SerializedName("contactNumber") val contactNumber: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("isLadiesOnly") val isLadiesOnly: Boolean? = false,
    @SerializedName("status") val status: String? = "Open",
    @SerializedName("requests") val requests: List<CarpoolRequestItem>? = null,
    @SerializedName("sourceLocation") val sourceLocation: GeoLocation? = null,
    @SerializedName("destLocation") val destLocation: GeoLocation? = null
) : java.io.Serializable {
    val id: String? get() = _id
    var distanceFromUser: Float? = null // Transient field for UI
}

data class CarpoolRequestItem(
    @SerializedName("_id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("contact") val contact: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("seats") val seats: Int,
    @SerializedName("status") val status: String
) : java.io.Serializable

data class GeoLocation(
    @SerializedName("type") val type: String?,
    @SerializedName("coordinates") val coordinates: List<Double>? // [Lng, Lat]
) : java.io.Serializable
