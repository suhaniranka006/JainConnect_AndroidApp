package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

data class TirthyatraTemplate(
    @SerializedName("_id") val id: String? = null,
    val title: String,
    val description: String? = null,
    val durationDays: Int,
    val image: String? = null,
    val defaultItinerary: List<ItineraryDay> = emptyList(),
    val defaultChecklist: List<TemplateChecklistItem> = emptyList(),
    val isPopular: Boolean = false
)

data class TemplateChecklistItem(
    val item: String,
    val category: String? = null
)
