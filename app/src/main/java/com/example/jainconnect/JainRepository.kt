package com.example.jainconnect

/**
 * JainRepository acts as a single source of truth for data in the JainConnect app.
 *
 * Its main role is to provide data to the ViewModel, abstracting the underlying data source.
 * Currently, it fetches data from the backend API via RetrofitInstance.
 *
 * In a production-ready app, you can also add:
 *  - Local caching (Room or SharedPreferences)
 *  - Error handling (try-catch, Result wrapper)
 *  - Data transformation/mapping
 */
class JainRepository {

    /**
     * Fetches the list of Tithis from the backend.
     *
     * @return List<Tithi> - The data returned from RetrofitInstance.api.getTithis()
     * Usage: Should be called inside a coroutine since it is a suspend function.
     */
    suspend fun getTithis() = RetrofitInstance.api.getTithis()

    /**
     * Fetches the list of Events from the backend.
     *
     * @return List<Event> - The data returned from RetrofitInstance.api.getEvents()
     * Usage: Should be called inside a coroutine.
     */
    suspend fun getEvents() = RetrofitInstance.api.getEvents()

    /**
     * Fetches the list of Maharajs from the backend.
     *
     * @return List<Maharaj> - The data returned from RetrofitInstance.api.getMaharaj()
     * Usage: Should be called inside a coroutine.
     */
    suspend fun getMaharaj() = RetrofitInstance.api.getMaharaj()
}
