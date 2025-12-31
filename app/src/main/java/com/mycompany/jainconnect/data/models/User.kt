
package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a User in the system.
 * This data class matches the 'User' object structure returned by the backend.
 *
 * @property id Unique identifier (maps to MongoDB `_id`).
 * @property name User's full name.
 * @property email User's email address (unique).
 * @property phone User's phone number.
 * @property location User's city or location.
 * @property dob Date of Birth.
 * @property gender User's gender.
 * @property password Password (Note: Usually not returned by backend for security, but kept in model if needed for local logic).
 * @property profileImage URL to the user's profile image (Cloudinary).
 */
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
) : java.io.Serializable

/**
 * Represents the response from Login/Register APIs.
 *
 * @property success Indicates if the request was successful.
 * @property message Server message (e.g., "Login successful").
 * @property token JWT Token for authentication (used in subsequent requests).
 * @property user The user object containing profile details.
 */
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: User?
)

/**
 * Request body for the Login API.
 */
data class LoginRequest(
    val email: String,
    val password: String
)
