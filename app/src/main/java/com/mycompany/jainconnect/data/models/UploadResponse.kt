package com.mycompany.jainconnect.data.models

data class UploadResponse(
    val success: Boolean,
    val imageUrl: String? = null,
    val message: String? = null
)
