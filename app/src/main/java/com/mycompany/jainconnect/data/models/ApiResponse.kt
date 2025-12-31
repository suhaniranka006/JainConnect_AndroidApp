package com.mycompany.jainconnect.data.models

/**
 * Generic API Response for simple success/failure messages.
 */
data class ApiResponse(
    val success: Boolean,
    val message: String
)
