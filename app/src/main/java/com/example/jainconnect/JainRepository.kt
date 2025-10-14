package com.example.jainconnect

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class JainRepository {

    // --- Tithi, Event, Maharaj Functions ---
    suspend fun getTithis(): List<Tithi> = RetrofitInstance.api.getTithis()
    suspend fun getEvents(): List<Event> = RetrofitInstance.api.getEvents()
    suspend fun getMaharaj(): List<Maharaj> = RetrofitInstance.api.getMaharaj()

    // --- User Authentication Functions ---

    suspend fun registerUser(
        name: String, email: String, password: String, phone: String,
        location: String, dob: String, gender: String, imageFile: File?
    ): Response<AuthResponse> {
        val partsMap = mutableMapOf<String, RequestBody>()
        partsMap["name"] = name.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["email"] = email.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["password"] = password.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["gender"] = gender.toRequestBody("text/plain".toMediaTypeOrNull())

        if (phone.isNotEmpty()) {
            partsMap["phone"] = phone.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        if (location.isNotEmpty()) {
            partsMap["location"] = location.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        if (dob.isNotEmpty()) {
            partsMap["dob"] = dob.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        val imagePart: MultipartBody.Part? = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profileImage", it.name, requestFile)
        }

        return RetrofitInstance.api.registerUser(
            parts = partsMap,
            profileImage = imagePart
        )
    }

    suspend fun loginUser(email: String, password: String): Response<AuthResponse> {
        val loginRequest = LoginRequest(email = email, password = password)
        return RetrofitInstance.api.loginUser(loginRequest)
    }

    suspend fun getUserProfile(token: String): Response<AuthResponse> {
        return RetrofitInstance.api.getUserProfile("Bearer $token")
    }

    suspend fun updateUserProfile(
        token: String, name: String, phone: String, location: String,
        dob: String, gender: String, imageFile: File?
    ): Response<AuthResponse> {
        val partsMap = mutableMapOf<String, RequestBody>()
        if (name.isNotEmpty()) partsMap["name"] = name.toRequestBody("text/plain".toMediaTypeOrNull())
        if (phone.isNotEmpty()) partsMap["phone"] = phone.toRequestBody("text/plain".toMediaTypeOrNull())
        if (location.isNotEmpty()) partsMap["location"] = location.toRequestBody("text/plain".toMediaTypeOrNull())
        if (dob.isNotEmpty()) partsMap["dob"] = dob.toRequestBody("text/plain".toMediaTypeOrNull())
        if (gender.isNotEmpty()) partsMap["gender"] = gender.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart: MultipartBody.Part? = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profileImage", it.name, requestFile)
        }

        return RetrofitInstance.api.updateUserProfile("Bearer $token", partsMap, imagePart)
    }
}