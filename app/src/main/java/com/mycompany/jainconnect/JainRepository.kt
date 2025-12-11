package com.mycompany.jainconnect

import com.mycompany.jainconnect.RetrofitInstance.api
import com.google.gson.annotations.SerializedName // <-- Naya Import
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File



//repo is used to abstract data resource , used as mediator bw rest of the app and data resource
//useful for decoupling,single source of truth, testability
import javax.inject.Inject

//repo is used to abstract data resource , used as mediator bw rest of the app and data resource
//useful for decoupling,single source of truth, testability
class JainRepository @Inject constructor(
    private val api: ApiService
) {

    // --- Tithi, Event, Maharaj Functions ---
    suspend fun getTithis(): List<Tithi> = api.getTithis()
    suspend fun getEvents(): List<Event> = api.getEvents()
    suspend fun getMaharaj(): List<Maharaj> = api.getMaharaj()

    // === YEH NAYA FUNCTION HAI ===
    /**
     * "I'm Going" (RSVP) button ke click ko handle karta hai.
     * @param token User ka authentication token (e.g., "Bearer <token>")
     * @param eventId Jiss event par click hua, uski ID.
     */
    suspend fun toggleEventRsvp(token: String, eventId: String): Response<RsvpResponse> {
        // API call ke liye token ko "Bearer " prefix ke saath bhejein
        return api.toggleEventRsvp("Bearer $token", eventId)
    }
    // =============================


    // --- User Authentication Functions ---

    suspend fun registerUser(
        name: String, email: String, password: String, phone: String,
        location: String, dob: String, gender: String, imageFile: File?
    ): Response<AuthResponse> {
        val partsMap = mutableMapOf<String, RequestBody>()  //mutable map to hold everything that i need to send in multipart request
        partsMap["name"] = name.toRequestBody("text/plain".toMediaTypeOrNull())   //convert string into request body object which retrofit needs
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


        //handles image part
        val imagePart: MultipartBody.Part? = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profileImage", it.name, requestFile)
        }


        //calls api and pass parts and imagepart
        return api.registerUser(
            parts = partsMap,
            profileImage = imagePart
        )
    }





    //ask for login credentials and do api calls
    suspend fun loginUser(email: String, password: String): Response<AuthResponse> {
        val loginRequest = LoginRequest(email = email, password = password)
        return api.loginUser(loginRequest)
    }


    //it asks for bearer token from api and then authneticate and gives realted profile
    suspend fun getUserProfile(token: String): Response<AuthResponse> {
        return api.getUserProfile("Bearer $token")
    }




    //works similar with register user but it also passes bearer token for auth
    suspend fun updateUserProfile(
        token: String, name: String, phone: String, location: String,
        dob: String, gender: String, imageFile: File?
    ): Response<AuthResponse> {
        val partsMap = mutableMapOf<String, RequestBody>()

        //we are using nonempty so that only the part we are updating will go to backened, helpe to avoid override existing data to empty string
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







// === YEH NAYI DATA CLASS HAI ===
// API se aane waale response ko handle karne ke liye
// (e.g., { "message": "RSVP added", "rsvpCount": 16 })
data class RsvpResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("rsvpCount")
    val rsvpCount: Int
)
