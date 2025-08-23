package com.example.jainconnect

import retrofit2.http.GET

interface ApiService {
    @GET("api/tithis")
    suspend fun getTithis(): List<Tithi>

    @GET("api/events")
    suspend fun getEvents(): List<Event>

    @GET("api/maharajs")
    suspend fun getMaharaj(): List<Maharaj>
}
