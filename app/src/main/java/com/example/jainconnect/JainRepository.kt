package com.example.jainconnect




class JainRepository {
    suspend fun getTithis() = RetrofitInstance.api.getTithis()
    suspend fun getEvents() = RetrofitInstance.api.getEvents()
    suspend fun getMaharaj() = RetrofitInstance.api.getMaharaj()
}
