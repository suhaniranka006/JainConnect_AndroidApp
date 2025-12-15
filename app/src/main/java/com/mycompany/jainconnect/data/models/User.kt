

// Ek nayi file banayein, jaise User.kt

package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.AuthResponse
import com.mycompany.jainconnect.data.models.LoginRequest

// Yeh class aapke backend ke 'user' object se match karti hai
data class User(
    @SerializedName("_id") val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val location: String?,
    val dob: String?,
    val gender: String?,
    val password: String?, // Yeh server se nahi aayega, but model me rehne dein
    val profileImage: String?
) : java.io.Serializable

// Yeh class aapke poore Register/Login response se match karti hai
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: User?
)

data class LoginRequest(
    val email: String,
    val password: String
)