package com.mycompany.jainconnect.data.models

import java.io.Serializable

data class Temple(
    val _id: String,
    val name: String,
    val city: String,
    val address: String?,
    val description: String?,
    val image: String?,
    val contact: String?,
    val isApproved: Boolean
) : Serializable
