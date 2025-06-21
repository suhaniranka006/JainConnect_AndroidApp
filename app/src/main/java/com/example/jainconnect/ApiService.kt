package com.example.jainconnect

import retrofit2.http.GET

interface ApiService {
    @GET("tithis")
    suspend fun getTithis(): List<Tithi>

    @GET("events")
    suspend fun getEvents(): List<Event>

    @GET("maharaj")
    suspend fun getMaharaj(): List<Maharaj>
}
