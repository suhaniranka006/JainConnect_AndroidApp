package com.mycompany.jainconnect

import com.google.gson.annotations.SerializedName

data class SunResponse(
    @SerializedName("daily") val daily: DailyData
)

data class DailyData(
    @SerializedName("time") val dates: List<String>,       // ["2025-12-04", "2025-12-05"...]
    @SerializedName("sunrise") val sunrise: List<String>,  // ["2025-12-04T07:05", ...]
    @SerializedName("sunset") val sunset: List<String>     // ["2025-12-04T17:35", ...]
)


data class HorizonItem(
    val date: String,
    val sunrise: String,
    val sunset: String
)
