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
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Event
import com.mycompany.jainconnect.data.models.Maharaj
import com.mycompany.jainconnect.data.models.Tithi
import com.mycompany.jainconnect.data.models.User
import com.mycompany.jainconnect.data.models.ApiResponse
import com.mycompany.jainconnect.data.models.AuthResponse
import com.mycompany.jainconnect.data.models.EventSubmissionRequest
import com.mycompany.jainconnect.data.models.LoginRequest
import com.mycompany.jainconnect.data.models.MaharajSubmissionRequest
import com.mycompany.jainconnect.data.models.RsvpResponse
import com.mycompany.jainconnect.data.models.SunResponse
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

    @GET("api/maharajs")
    suspend fun getMaharaj(): List<Maharaj>

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
    @POST("api/maharajs") // Check your exact route path
    suspend fun submitMaharaj(
        @Header("Authorization") token: String,
        @Body data: MaharajSubmissionRequest
    ): Response<ApiResponse>
}