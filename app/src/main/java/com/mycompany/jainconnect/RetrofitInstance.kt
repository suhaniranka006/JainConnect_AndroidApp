package com.mycompany.jainconnect

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitInstance is a singleton object that provides a single instance of Retrofit
 * configured to communicate with the backend server of JainConnect.
 *
 * Using a singleton ensures that the app uses the same Retrofit configuration
 * throughout, avoiding unnecessary re-creation of Retrofit or OkHttpClient instances.
 */
object RetrofitInstance {

    // 🔹 The base URL of your backend server.
    // All endpoints in ApiService are relative to this URL.
    // IMPORTANT: Must end with a "/" for Retrofit to work correctly.
    private const val BASE_URL = "https://jainconnect-backened-2.onrender.com/"

    /**
     * HttpLoggingInterceptor logs HTTP request and response data.
     * This is useful for debugging network calls.
     */
    private val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            // Logs the message in Android Logcat under the "OkHttp" tag
            Log.d("OkHttp", message)
        }
    }).apply {
        // Logs the full request and response body including headers and URL
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttpClient is the HTTP client used by Retrofit.
     * Here, we add logging and configure timeouts.
     */
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)        // Add logging interceptor for debugging
        .connectTimeout(30, TimeUnit.SECONDS)      // Max time to establish a connection
        .readTimeout(30, TimeUnit.SECONDS)         // Max time to read data from server
        .writeTimeout(30, TimeUnit.SECONDS)        // Max time to send data to server
        .build()                                   // Build the OkHttpClient instance

    /**
     * 'api' provides a lazily initialized instance of ApiService.
     * This is how the app communicates with backend endpoints.
     */
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)                     // Set the base URL for all API calls
            .client(httpClient)                    // Use the custom OkHttpClient with logging & timeouts
            .addConverterFactory(GsonConverterFactory.create()) // Convert JSON to Kotlin objects automatically
            .build()                               // Build the Retrofit instance
            .create(ApiService::class.java)        // Generate the implementation of ApiService interface
    }
}
