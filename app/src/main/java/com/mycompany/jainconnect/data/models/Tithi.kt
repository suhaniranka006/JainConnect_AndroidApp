package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a Tithi (Jain Calendar Event).
 */
data class Tithi(
    // Optional ID field, in case backend provides one.
    @SerializedName("_id") 
    val id: String? = null, 

    @SerializedName("tithi") // Field name matches JSON 'tithi' (The actual Tithi name)
    val name: String,

    @SerializedName("date") // Date of the Tithi (YYYY-MM-DD)
    val date: String,

    @SerializedName("description") // Details/Significance of the Tithi
    val details: String?,

    @SerializedName("isMajor") // Boolean to highlight important festivals
    val isMajor: Boolean = false
)
