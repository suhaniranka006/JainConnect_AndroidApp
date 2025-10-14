package com.example.jainconnect

import android.util.Log
import com.example.jainconnect.RetrofitInstance.api
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response // Response import karna zaroori hai
import java.io.File

class JainRepository {

    // --- Tithi, Event, Maharaj Functions ---
    suspend fun getTithis() = RetrofitInstance.api.getTithis()
    suspend fun getEvents() = RetrofitInstance.api.getEvents()
    suspend fun getMaharaj() = RetrofitInstance.api.getMaharaj()

    // --- User Registration (Signup) Function ---
    suspend fun registerUser(
        name: String, email: String, password: String, phone: String,
        location: String, dob: String, gender: String, imageFile: File?
    ): Response<AuthResponse> {

        // Step 1: Create a mutable map to hold all the text parts.
        // The keys ("name", "email", etc.) must exactly match what the server expects.
        val partsMap = mutableMapOf<String, RequestBody>()

        // Step 2: Add the REQUIRED fields to the map.
        partsMap["name"] = name.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["email"] = email.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["password"] = password.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["gender"] = gender.toRequestBody("text/plain".toMediaTypeOrNull())

        // Step 3: Conditionally add the OPTIONAL fields ONLY IF they are not empty.
        // This is the key part of the fix.
        if (phone.isNotEmpty()) {
            partsMap["phone"] = phone.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        if (location.isNotEmpty()) {
            partsMap["location"] = location.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        if (dob.isNotEmpty()) {
            partsMap["dob"] = dob.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        // Step 4: Prepare the image file part, same as before.
        val imagePart: MultipartBody.Part? = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profileImage", it.name, requestFile)
        }

        // Step 5: Call the updated API with the map and the image part.
        // Notice how the parameters now match the new ApiService function.
        return RetrofitInstance.api.registerUser(
            parts = partsMap,
            profileImage = imagePart
        )
    }



    // YEH NAYA FUNCTION ADD KAREIN  -- login function
    suspend fun loginUser(email: String, password: String): Response<AuthResponse> {
        val loginRequest = LoginRequest(email = email, password = password)
        return RetrofitInstance.api.loginUser(loginRequest)
    }

    // Yahan Update Profile ka function bhi aayega...

}