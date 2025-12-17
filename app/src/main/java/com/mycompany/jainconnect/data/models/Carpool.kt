package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

data class Carpool(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("driverName") val driverName: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("destination") val destination: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("vehicleType") val vehicleType: String? = null,
    @SerializedName("seatsAvailable") val seatsAvailable: Int? = null,
    @SerializedName("contactNumber") val contactNumber: String? = null,
    @SerializedName("userId") val userId: String? = null
)
