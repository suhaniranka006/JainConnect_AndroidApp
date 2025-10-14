

// Ek nayi file banayein, jaise User.kt

package com.example.jainconnect

import com.google.gson.annotations.SerializedName

// Yeh class aapke backend ke 'user' object se match karti hai
data class User(
    @SerializedName("_id") val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val location: String?,
    val dob: String?,
    val gender: String?,
    val password: String?,
    val profileImage: String?
)

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