package com.example.jainconnect // Or your actual package

import com.google.gson.annotations.SerializedName

data class Tithi(
    // Assuming you might add an "id" to your JSON later, or you generate it client-side.
    // If your JSON will NEVER have an "id", you might reconsider if you need it here,
    // or make it nullable and handle its absence. For now, let's assume it might exist or you need it.
    // If your JSON truly has no ID and you don't generate one, you might remove this field
    // or make it String? if it's optional. Let's make it optional for now.
    @SerializedName("id") // If your JSON *did* have an 'id' field
    val id: String? = null, // Making it nullable as it's not in the current JSON

    @SerializedName("title") // Maps JSON "title" to Kotlin "name"
    val name: String,

    @SerializedName("date") // Maps JSON "date" to Kotlin "date" (already matches but good for clarity)
    val date: String,

    @SerializedName("description") // Maps JSON "description" to Kotlin "details"
    val details: String?
)