package com.mycompany.jainconnect.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Event
import com.mycompany.jainconnect.data.models.Maharaj
import com.mycompany.jainconnect.data.models.Tithi
import com.mycompany.jainconnect.data.models.Carpool
import com.mycompany.jainconnect.data.models.CarpoolRequest
import com.mycompany.jainconnect.data.models.User
import com.mycompany.jainconnect.data.models.ApiResponse
import com.mycompany.jainconnect.data.models.AuthResponse
import com.mycompany.jainconnect.data.models.EventSubmissionRequest
import com.mycompany.jainconnect.data.models.LoginRequest
import com.mycompany.jainconnect.data.models.MaharajSubmissionRequest
import com.mycompany.jainconnect.data.models.RsvpResponse
import com.mycompany.jainconnect.data.models.SunResponse
import com.mycompany.jainconnect.data.models.Bhojanshala
import com.mycompany.jainconnect.data.models.Temple
import com.mycompany.jainconnect.data.models.BhojanshalaSubmissionRequest
import com.mycompany.jainconnect.data.models.Story
import com.mycompany.jainconnect.data.models.Tirthyatra
import com.mycompany.jainconnect.data.models.TirthyatraTemplate
import com.mycompany.jainconnect.data.models.TemplateListResponse
import com.mycompany.jainconnect.data.models.YatraListResponse
import com.mycompany.jainconnect.data.models.SingleYatraResponse
import retrofit2.http.DELETE

/**
 * ApiService defines all the network API endpoints for the JainConnect app.
 * Retrofit uses this interface to generate the actual HTTP requests.
 *
 * Each function corresponds to a specific endpoint on your backend server.
 * The functions are marked with 'suspend' so they can be called inside coroutines.
 */

/**
 * Defines the API endpoints for the Jain Connect application.
 *
 * NOTE: The BASE_URL for Retrofit should be your server's root address,
 * for example: "https://your-backend.onrender.com/"
 */
interface ApiService {

    @GET("api/tithis")
    suspend fun getTithis(): List<Tithi>

    @GET("api/events")
    suspend fun getEvents(): List<Event>

    @GET("api/maharaj")
    suspend fun getMaharaj(): List<Maharaj>

    @GET("api/bhojanshala")
    suspend fun getBhojanshalas(): List<Bhojanshala>

    @GET("api/temples")
    suspend fun getTemples(): List<Temple>

    /**
     * Fetches a user profile by their email address.
     * The full URL will be: <BASE_URL>user/profile/some.email@example.com
     */
    /**
     * Fetches a user profile by their email address.
     * The full URL will be: <BASE_URL>api/user/profile/some.email@example.com
     */
    @GET("api/users/profile/{email}") // Corrected Path
    suspend fun getProfileByEmail(@Path("email") email: String): Response<User>

    /**
     * Updates a user's profile information. This is a multipart request
     * used for uploading a profile image along with other data.
     * The full URL will be: <BASE_URL>api/user/profile
     */

    // Yeh aapka naya Register function hai
    @Multipart
    @POST("api/users/register")
    suspend fun registerUser(
        // Sabhi text fields (name, email, etc.) ab is dynamic map ke through jayenge.
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,

        // Image file pehle ki tarah alag se hi rahega.
        @Part profileImage: MultipartBody.Part?
    ): Response<AuthResponse>



    // YEH NAYA FUNCTION ADD KAREIN
    @POST("api/users/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<AuthResponse>


    // YEH DO NAYE FUNCTIONS ADD KAREIN

    // 1. Current user ka profile data fetch karne ke liye (Token zaroori hai)
    @GET("api/users/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<AuthResponse>

    // 2. User profile update karne ke liye (Token zaroori hai)
    @Multipart
    @PUT("api/users/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part profileImage: MultipartBody.Part?
    ): Response<AuthResponse>

    @DELETE("api/users/profile")
    suspend fun deleteProfile(@Header("Authorization") token: String): Response<ApiResponse>

    @POST("api/users/fix-user") // Assuming fix-user endpoint exists as per controller
    suspend fun fixUser(@Body request: LoginRequest): Response<AuthResponse>


    // === YEH NAYA FUNCTION ADD KAREIN ===
    @PUT("api/events/{id}/rsvp") // Backend route se match karein
    suspend fun toggleEventRsvp(
        @Header("Authorization") token: String, // "Bearer <token>"
        @Path("id") eventId: String             // Event ki ID
    ): Response<RsvpResponse>


    // ✅ FIX: Use the FULL URL here so Retrofit ignores your Node.js Base URL
    @GET("https://api.open-meteo.com/v1/forecast")
    suspend fun getSunTimes(
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double,
        @Query("daily") daily: String = "sunrise,sunset",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") days: Int = 14
    ): Response<SunResponse>



    @POST("api/events")
    suspend fun submitEvent(
        @Header("Authorization") token: String,
        @Body eventData: EventSubmissionRequest
    ): Response<ApiResponse>


    // Add this function to Interface
    @POST("api/maharaj") // Check your exact route path
    suspend fun submitMaharaj(
        @Header("Authorization") token: String,
        @Body data: MaharajSubmissionRequest
    ): Response<ApiResponse>

    @Multipart
    @POST("api/events/with-image")
    suspend fun submitEventWithImage(
        @Header("Authorization") token: String,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Response<ApiResponse>

    @Multipart
    @POST("api/maharaj/with-image")
    suspend fun submitMaharajWithImage(
        @Header("Authorization") token: String,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Response<ApiResponse>

    @POST("api/bhojanshala")
    suspend fun submitBhojanshala(
        @Header("Authorization") token: String,
        @Body request: BhojanshalaSubmissionRequest
    ): Response<ApiResponse>

    @POST("api/bhojanshala/with-image")
    @Multipart
    suspend fun submitBhojanshalaWithImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("city") city: RequestBody,
        @Part("address") address: RequestBody,
        @Part("timings") timings: RequestBody,
        @Part("openingTime") openingTime: RequestBody,
        @Part("closingTime") closingTime: RequestBody,
        @Part("contact") contact: RequestBody,
        @Part("description") description: RequestBody
    ): Response<ApiResponse>

    @POST("api/temples/with-image")
    @Multipart
    suspend fun submitTempleWithImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("city") city: RequestBody,
        @Part("address") address: RequestBody,
        @Part("contact") contact: RequestBody,
        @Part("description") description: RequestBody
    ): Response<ApiResponse>

    // --- Carpool ---
    @GET("api/carpool/all")
    suspend fun getCarpools(): List<Carpool>

    @POST("api/carpool/create")
    suspend fun createCarpool(
        @Header("Authorization") token: String,
        @Body request: CarpoolRequest
    ): Response<ApiResponse>

    // New Endpoint for Chat Notifications
    @POST("api/v1/notifications/send-chat")
    suspend fun sendChatNotification(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse>

    // --- Jain Legacy (Stories) ---
    @GET("api/stories/all")
    suspend fun getStories(@Header("Authorization") token: String): List<Story>

    @POST("api/stories/like/{id}")
    suspend fun likeStory(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Map<String, Any>>

    // --- Tirthyatra Planner ---
    @GET("api/tirthyatra/templates")
    suspend fun getTirthyatraTemplates(
        @Query("isPopular") isPopular: Boolean? = null
    ): Response<TemplateListResponse>

    @POST("api/tirthyatra")
    suspend fun createYatra(
        @Header("Authorization") token: String,
        @Body yatra: Tirthyatra
    ): Response<SingleYatraResponse>

    @GET("api/tirthyatra")
    suspend fun getMyYatras(
        @Header("Authorization") token: String,
        @Query("type") type: String? = null // "public" or null for my
    ): Response<YatraListResponse>

    @GET("api/tirthyatra/{id}")
    suspend fun getYatraDetails(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<SingleYatraResponse>

    @POST("api/tirthyatra/{id}/join")
    suspend fun joinYatra(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any> // message, contactNumber, peopleCount
    ): Response<ApiResponse>

    @POST("api/tirthyatra/{id}/cancel-request")
    suspend fun cancelRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ApiResponse>

    @POST("api/tirthyatra/{id}/leave")
    suspend fun leaveYatra(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ApiResponse>

    @PUT("api/tirthyatra/{id}/companionship")
    suspend fun toggleCompanionship(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any> // { enable: true/false, name, age, gender, contact }
    ): Response<SingleYatraResponse>

    @PUT("api/tirthyatra/{id}/members")
    suspend fun manageMember(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, String> // targetUserId, action
    ): Response<SingleYatraResponse>

    @DELETE("api/tirthyatra/{id}")
    suspend fun deleteYatra(@Header("Authorization") token: String, @Path("id") id: String): Response<ApiResponse>

    @Multipart
    @POST("api/tirthyatra/upload")
    suspend fun uploadTirthyatraImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<com.mycompany.jainconnect.data.models.UploadResponse>
}
