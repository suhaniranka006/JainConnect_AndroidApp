package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

data class CarpoolRequest(
    @SerializedName("driverName") val driverName: String,
    @SerializedName("source") val source: String,
    @SerializedName("destination") val destination: String,
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String,
    @SerializedName("vehicleType") val vehicleType: String,
    @SerializedName("seatsAvailable") val seatsAvailable: Int,
    @SerializedName("contactNumber") val contactNumber: String,
    @SerializedName("isLadiesOnly") val isLadiesOnly: Boolean,
    @SerializedName("sourceLat") val sourceLat: Double? = null,
    @SerializedName("sourceLng") val sourceLng: Double? = null,
    @SerializedName("destLat") val destLat: Double? = null,
    @SerializedName("destLng") val destLng: Double? = null
)
