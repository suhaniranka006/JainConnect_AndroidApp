package com.mycompany.jainconnect.data.models

data class BhojanshalaSubmissionRequest(
    val name: String,
    val city: String,
    val address: String,
    val timings: String?,
    val openingTime: String?,
    val closingTime: String?,
    val contact: String?,
    val description: String?
)
