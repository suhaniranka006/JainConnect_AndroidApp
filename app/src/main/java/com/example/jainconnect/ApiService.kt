package com.example.jainconnect

import retrofit2.http.GET

/**
 * ApiService defines all the network API endpoints for the JainConnect app.
 * Retrofit uses this interface to generate the actual HTTP requests.
 *
 * Each function corresponds to a specific endpoint on your backend server.
 * The functions are marked with 'suspend' so they can be called inside coroutines.
 */
interface ApiService {

    /**
     * Fetches the list of Tithis from the server.
     *
     * Endpoint: GET https://<BASE_URL>/api/tithis
     *
     * @return List<Tithi> - A list of Tithi objects parsed from JSON response.
     *
     * Usage:
     * val tithis = apiService.getTithis() // called inside a coroutine
     */
    @GET("api/tithis")
    suspend fun getTithis(): List<Tithi>

    /**
     * Fetches the list of Events from the server.
     *
     * Endpoint: GET https://<BASE_URL>/api/events
     *
     * @return List<Event> - A list of Event objects parsed from JSON response.
     */
    @GET("api/events")
    suspend fun getEvents(): List<Event>

    /**
     * Fetches the list of Maharajs from the server.
     *
     * Endpoint: GET https://<BASE_URL>/api/maharajs
     *
     * @return List<Maharaj> - A list of Maharaj objects parsed from JSON response.
     */
    @GET("api/maharajs")
    suspend fun getMaharaj(): List<Maharaj>
}
