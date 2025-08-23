package com.example.jainconnect

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // Import this

object RetrofitInstance {
    // 🔹 Use your Render backend instead of Glitch
    private const val BASE_URL = "https://jainconnect-backened.onrender.com/"

    private val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            Log.d("OkHttp", message)
        }
    }).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // Increase timeouts
        .connectTimeout(30, TimeUnit.SECONDS) // Time to establish a connection
        .readTimeout(30, TimeUnit.SECONDS)    // Time to read data from the server
        .writeTimeout(30, TimeUnit.SECONDS)   // Time to write data to the server
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 👈 Make sure it ends with /
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
