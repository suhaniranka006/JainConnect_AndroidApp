package com.mycompany.jainconnect.data.repository


import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Event
import com.mycompany.jainconnect.data.models.Maharaj
import com.mycompany.jainconnect.data.models.Tithi
import com.mycompany.jainconnect.data.models.User
import com.mycompany.jainconnect.data.network.ApiService
import com.mycompany.jainconnect.data.network.RetrofitInstance
import com.mycompany.jainconnect.data.models.ApiResponse
import com.mycompany.jainconnect.data.models.AuthResponse
import com.mycompany.jainconnect.data.models.EventSubmissionRequest
import com.mycompany.jainconnect.data.models.LoginRequest
import com.mycompany.jainconnect.data.models.MaharajSubmissionRequest
import com.mycompany.jainconnect.data.models.RsvpResponse
import com.mycompany.jainconnect.data.models.SunResponse



import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException

/**
 * Repository module for handling data operations.
 * It abstracts the data sources (API, Database) from the rest of the application.
 * @Inject constructor allows Hilt to pass the [ApiService] instance automatically.
 */
class JainRepository @Inject constructor(
    private val api: ApiService
) {

    // --- Tithi, Event, Maharaj Functions ---
    suspend fun getTithis(): List<Tithi> = api.getTithis()
    suspend fun getEvents(): List<Event> = api.getEvents()
    suspend fun getMaharaj(): List<Maharaj> = api.getMaharaj()

    // === RSVP FUNCTION ===
    /**
     * Handles the "I'm Going" (RSVP) button click.
     * @param token User authentication token (e.g., "Bearer <token>")
     * @param eventId The ID of the event.
     */
    suspend fun toggleEventRsvp(token: String, eventId: String): Response<RsvpResponse> {
        // Send token with "Bearer " prefix for API call
        return api.toggleEventRsvp("Bearer $token", eventId)
    }
    // =============================


    // --- User Authentication Functions ---

    suspend fun registerUser(
        name: String, email: String, password: String, phone: String,
        location: String, dob: String, gender: String, imageFile: File?
    ): Response<AuthResponse> {
        val partsMap = mutableMapOf<String, RequestBody>()  // Stores multipart request data
        partsMap["name"] = name.toRequestBody("text/plain".toMediaTypeOrNull()) // Convert string to request body
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


        // Handles image part for upload
        val imagePart: MultipartBody.Part? = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profileImage", it.name, requestFile)
        }


        // Call API passing parts and the image
        return api.registerUser(
            parts = partsMap,
            profileImage = imagePart
        )
    }





    // Authenticate user with credentials
    suspend fun loginUser(email: String, password: String): Response<AuthResponse> {
        val loginRequest = LoginRequest(email = email, password = password)
        return api.loginUser(loginRequest)
    }


    // Validate the token and fetch the user profile
    suspend fun getUserProfile(token: String): Response<AuthResponse> {
        return api.getUserProfile("Bearer $token")
    }






    // Updates user profile. Passes Bearer token for authentication.
    suspend fun updateUserProfile(
        token: String, name: String, phone: String, location: String,
        dob: String, gender: String, imageFile: File?
    ): Response<AuthResponse> {
        val partsMap = mutableMapOf<String, RequestBody>()

        // Use 'isNotEmpty' to ensure we only send updated fields.
        // This prevents overwriting existing data with empty strings.
        if (name.isNotEmpty()) partsMap["name"] = name.toRequestBody("text/plain".toMediaTypeOrNull())
        if (phone.isNotEmpty()) partsMap["phone"] = phone.toRequestBody("text/plain".toMediaTypeOrNull())
        if (location.isNotEmpty()) partsMap["location"] = location.toRequestBody("text/plain".toMediaTypeOrNull())
        if (dob.isNotEmpty()) partsMap["dob"] = dob.toRequestBody("text/plain".toMediaTypeOrNull())
        if (gender.isNotEmpty()) partsMap["gender"] = gender.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart: MultipartBody.Part? = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profileImage", it.name, requestFile)
        }

        return api.updateUserProfile("Bearer $token", partsMap, imagePart)
    }

    suspend fun deleteProfile(token: String): Response<ApiResponse> {
        return api.deleteProfile("Bearer $token")
    }

    suspend fun fixUser(email: String, password: String): Response<AuthResponse> {
        val request = LoginRequest(email, password) // Reusing LoginRequest as structure is same {email, password}
        return api.fixUser(request)
    }


    // Inside JainRepository class

    // Since we used the Full URL in the Interface, we can use the same api instance!
    suspend fun getSunTimings(lat: Double, lng: Double): Response<SunResponse> {
        return api.getSunTimes(lat, lng)
    }


// Inside JainRepository.kt

    suspend fun submitEvent(
        token: String,
        title: String,
        city: String,
        date: String,
        time: String,
        desc: String
    ): Response<ApiResponse> {
        // Map the function args to the Data Class
        val request = EventSubmissionRequest(title, city, date, time, desc)
        return api.submitEvent("Bearer $token", request)
    }


    suspend fun submitMaharaj(
        token: String,
        name: String,
        title: String,
        city: String,
        date: String,
        contact: String,

    ): Response<ApiResponse> {

        val request = MaharajSubmissionRequest(
            name,
            title,
            city,
            date,
            contact

        )
        return api.submitMaharaj("Bearer $token", request)
    }
}






