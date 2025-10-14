package com.example.jainconnect

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

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
}