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
import com.mycompany.jainconnect.data.models.Bhojanshala
import com.mycompany.jainconnect.data.models.Temple
import com.mycompany.jainconnect.data.models.Carpool
import com.mycompany.jainconnect.data.models.CarpoolRequest
import com.mycompany.jainconnect.data.models.Story
import com.mycompany.jainconnect.data.models.Tithi
import com.mycompany.jainconnect.data.models.User
import com.mycompany.jainconnect.data.network.ApiService
import com.mycompany.jainconnect.data.network.RetrofitInstance
import com.mycompany.jainconnect.data.models.ApiResponse
import com.mycompany.jainconnect.data.models.AuthResponse
import com.mycompany.jainconnect.data.models.EventSubmissionRequest
import com.mycompany.jainconnect.data.models.LoginRequest
import com.mycompany.jainconnect.data.models.MaharajSubmissionRequest
import com.mycompany.jainconnect.data.models.BhojanshalaSubmissionRequest
import com.mycompany.jainconnect.data.models.RsvpResponse
import com.mycompany.jainconnect.data.models.SeatRequest
import com.mycompany.jainconnect.data.models.SunResponse
import com.mycompany.jainconnect.data.models.Tirthyatra
import com.mycompany.jainconnect.data.models.TemplateListResponse
import com.mycompany.jainconnect.data.models.SingleYatraResponse
import com.mycompany.jainconnect.data.models.YatraListResponse



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
    suspend fun getBhojanshalas(): List<Bhojanshala> = api.getBhojanshalas()
    suspend fun getTemples(): List<Temple> = api.getTemples()
    suspend fun getCarpools(
        source: String? = null,
        destination: String? = null,
        date: String? = null,
        ladiesOnly: Boolean? = null,
        lat: Double? = null,
        lng: Double? = null,
        radius: Int? = null
    ): List<Carpool> = api.getCarpools(source, destination, date, ladiesOnly, lat, lng, radius)

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
        startDate: String,
        endDate: String,
        time: String,
        desc: String,
        contact: String,
        latitude: Double? = null,
        longitude: Double? = null
    ): Response<ApiResponse> {
        // Map the function args to the Data Class
        val request = EventSubmissionRequest(title, city, date, startDate, endDate, time, contact, desc, latitude, longitude)
        return api.submitEvent("Bearer $token", request)
    }

    suspend fun submitEventWithImage(
        token: String,
        title: String,
        city: String,
        date: String,
        startDate: String,
        endDate: String,
        time: String,
        desc: String,
        contact: String,
        imageFile: File?,
        latitude: Double? = null,
        longitude: Double? = null
    ): Response<ApiResponse> {
        val partsMap = mutableMapOf<String, RequestBody>()
        partsMap["title"] = title.toRequestBody("text/plain".toMediaTypeOrNull()) // Note: Backend expects 'title'
        partsMap["city"] = city.toRequestBody("text/plain".toMediaTypeOrNull())  // Backend expects 'city'
        partsMap["date"] = date.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["startDate"] = startDate.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["endDate"] = endDate.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["time"] = time.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["contact"] = contact.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["description"] = desc.toRequestBody("text/plain".toMediaTypeOrNull())

        if (latitude != null) partsMap["latitude"] = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        if (longitude != null) partsMap["longitude"] = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart: MultipartBody.Part? = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestFile)
        }

        return api.submitEventWithImage("Bearer $token", partsMap, imagePart)
    }


    suspend fun submitMaharaj(
        token: String,
        name: String,
        title: String,
        city: String,
        date: String,
        contact: String,
        arrivalDate: String,
        viharDate: String,
        description: String
    ): Response<ApiResponse> {

        val request = MaharajSubmissionRequest(
            name,
            title,
            city,
            date,
            contact,
            arrivalDate,
            viharDate,
            description
        )
        return api.submitMaharaj("Bearer $token", request)
    }

    suspend fun submitMaharajWithImage(
        token: String,
        name: String,
        title: String,
        city: String,
        date: String,
        contact: String,
        arrivalDate: String,
        viharDate: String,
        description: String,
        imageFile: File?
    ): Response<ApiResponse> {
        val partsMap = mutableMapOf<String, RequestBody>()
        partsMap["name"] = name.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["title"] = title.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["city"] = city.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["date"] = date.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["contactInfo"] = contact.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["arrivalDate"] = arrivalDate.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["viharDate"] = viharDate.toRequestBody("text/plain".toMediaTypeOrNull())
        partsMap["description"] = description.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart: MultipartBody.Part? = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestFile)
        }

        return api.submitMaharajWithImage("Bearer $token", partsMap, imagePart)
    }

    suspend fun submitBhojanshala(
        token: String,
        name: String,
        city: String,
        address: String,
        timings: String,
        openingTime: String,
        closingTime: String,
        contact: String,
        description: String
    ): Response<ApiResponse> {
        val request = BhojanshalaSubmissionRequest(name, city, address, timings, openingTime, closingTime, contact, description)
        return api.submitBhojanshala("Bearer $token", request)
    }

    suspend fun submitBhojanshalaWithImage(
        token: String, name: String, city: String, address: String, timings: String, openingTime: String, closingTime: String, contact: String, description: String, imageFile: java.io.File
    ): Response<ApiResponse> {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val cityPart = city.toRequestBody("text/plain".toMediaTypeOrNull())
        val addressPart = address.toRequestBody("text/plain".toMediaTypeOrNull())
        val timingsPart = timings.toRequestBody("text/plain".toMediaTypeOrNull())
        val openingTimePart = openingTime.toRequestBody("text/plain".toMediaTypeOrNull())
        val closingTimePart = closingTime.toRequestBody("text/plain".toMediaTypeOrNull())
        val contactPart = contact.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        return api.submitBhojanshalaWithImage("Bearer $token", imagePart, namePart, cityPart, addressPart, timingsPart, openingTimePart, closingTimePart, contactPart, descPart)
    }

    suspend fun submitTempleWithImage(
        token: String, name: String, city: String, address: String, contact: String, description: String, imageFile: java.io.File
    ): Response<ApiResponse> {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val cityPart = city.toRequestBody("text/plain".toMediaTypeOrNull())
        val addressPart = address.toRequestBody("text/plain".toMediaTypeOrNull())
        val contactPart = contact.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        return api.submitTempleWithImage("Bearer $token", imagePart, namePart, cityPart, addressPart, contactPart, descPart)
    }

    suspend fun sendChatNotification(token: String, title: String, message: String): Response<ApiResponse> {
        val body = mapOf(
            "title" to title,
            "message" to message
        )
        return api.sendChatNotification("Bearer $token", body)
    }

    suspend fun createCarpool(token: String, request: CarpoolRequest): Response<ApiResponse> {
        return api.createCarpool("Bearer $token", request)
    }

    suspend fun requestSeat(
        token: String, 
        id: String, 
        name: String, 
        contact: String, 
        gender: String, 
        seats: Int
    ): Response<ApiResponse> {
        val request = SeatRequest(name, contact, gender, seats)
        return api.requestSeat("Bearer $token", id, request)
    }

    suspend fun manageRequest(token: String, id: String, requestId: String, action: String): Response<ApiResponse> {
        val body = mapOf(
            "requestId" to requestId,
            "action" to action
        )
        return api.manageRequest("Bearer $token", id, body)
    }

    suspend fun deleteCarpool(token: String, id: String): Response<ApiResponse> {
        return api.deleteCarpool("Bearer $token", id)
    }

    suspend fun updateCarpool(token: String, id: String, request: CarpoolRequest): Response<ApiResponse> {
        return api.updateCarpool("Bearer $token", id, request)
    }

    // --- Jain Legacy ---
    suspend fun getStories(token: String): List<Story> {
        return api.getStories("Bearer $token")
    }

    suspend fun likeStory(token: String, id: String): Response<Map<String, Any>> {
        return api.likeStory("Bearer $token", id)
    }

    // --- Tirthyatra Planner ---
    suspend fun getTirthyatraTemplates(isPopular: Boolean? = null): Response<TemplateListResponse> {
        return api.getTirthyatraTemplates(isPopular)
    }

    suspend fun createYatra(token: String, yatra: Tirthyatra): Response<SingleYatraResponse> {
        return api.createYatra("Bearer $token", yatra)
    }

    suspend fun getMyYatras(token: String, type: String? = null): Response<YatraListResponse> {
        return api.getMyYatras("Bearer $token", type)
    }

    suspend fun getYatraDetails(token: String, id: String): Response<SingleYatraResponse> {
        return api.getYatraDetails("Bearer $token", id)
    }

    suspend fun joinYatra(token: String, id: String, message: String, contactNumber: String, peopleCount: Int, name: String, age: String, gender: String): Response<ApiResponse> {
        val body: Map<String, Any> = mapOf(
            "message" to message, 
            "contactNumber" to contactNumber, 
            "peopleCount" to peopleCount,
            "name" to name,
            "age" to age,
            "gender" to gender
        )
        return api.joinYatra("Bearer $token", id, body)
    }

    suspend fun cancelRequest(token: String, id: String): Response<ApiResponse> {
        return api.cancelRequest("Bearer $token", id)
    }

    suspend fun toggleCompanionship(token: String, id: String, enable: Boolean, name: String? = null, age: String? = null, gender: String? = null, contact: String? = null): Response<SingleYatraResponse> {
        val body = mutableMapOf<String, Any>("enable" to enable)
        if (name != null) body["name"] = name
        if (age != null) body["age"] = age
        if (gender != null) body["gender"] = gender
        if (contact != null) body["contact"] = contact
        
        return api.toggleCompanionship(token = "Bearer $token", id, body)
    }

    suspend fun deleteYatra(token: String, id: String): Response<ApiResponse> {
        return api.deleteYatra("Bearer $token", id)
    }

    suspend fun uploadTirthyatraImage(token: String, file: File): Response<com.mycompany.jainconnect.data.models.UploadResponse> {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
        return api.uploadTirthyatraImage("Bearer $token", body)
    }

    // --- Notifications ---
    suspend fun getNotifications(token: String): Response<com.mycompany.jainconnect.data.models.NotificationResponse> {
        return api.getNotifications("Bearer $token")
    }

    suspend fun markNotificationRead(token: String, id: String): Response<ApiResponse> {
        return api.markNotificationRead("Bearer $token", id)
    }







    suspend fun takePachkhan(token: String, name: String): Response<ApiResponse> {
        val body = mapOf("pachkhanName" to name)
        return api.takePachkhan("Bearer $token", body)
    }

    suspend fun getPachkhanStatus(token: String): Response<com.mycompany.jainconnect.data.models.PachkhanStatusResponse> {
        return api.getPachkhanStatus("Bearer $token")
    }

    suspend fun getLeaderboard(token: String, type: String): Response<com.mycompany.jainconnect.data.models.LeaderboardResponse> {
        return api.getLeaderboard("Bearer $token", type)
    }
}
