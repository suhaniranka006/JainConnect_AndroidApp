package com.example.jainconnect

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import retrofit2.Response // ✅ Sahi Response class import ki gayi hai
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * JainViewModel is responsible for providing UI-related data
 * for Tithis, Events, and Maharaj.
 *
 * It fetches data from JainRepository and exposes it via LiveData.
 * It also provides filtering functions for search and date-based filtering.
 */
class JainViewModel : ViewModel() {

    // Repository instance to fetch data from backend
    private val repository = JainRepository()

    // ---------------------- USER AUTHENTICATION ----------------------
    // ✅ SIGNUP code ko class ke andar sahi jagah par add kiya gaya
    private val _signupResult = MutableLiveData<Response<AuthResponse>?>()
    val signupResult: LiveData<Response<AuthResponse>?> = _signupResult

    fun performSignup(
        name: String, email: String, password: String, phone: String,
        location: String, dob: String, gender: String, imageFile: File?
    ) {
        viewModelScope.launch {
            try {
                val response = repository.registerUser(name, email, password, phone, location, dob, gender, imageFile)
                _signupResult.postValue(response)
            } catch (e: Exception) {
                Log.e("JainViewModel", "Signup Exception", e)
                _signupResult.postValue(null)
            }
        }
    }

    // ❌ Extra profile waala code (`_uploadResult`, `_profile`, etc.) yahan se hata diya gaya hai.


    // ---------------------- TITHIS (No Changes) ----------------------
    private val _tithiList = MutableLiveData<List<Tithi>>()
    val tithiList: LiveData<List<Tithi>> = _tithiList

    private val _filteredTithis = MutableLiveData<List<Tithi>>()
    val filteredTithis: LiveData<List<Tithi>> get() = _filteredTithis

    fun fetchTithis() {
        viewModelScope.launch {
            try {
                val tithisFromRepo = repository.getTithis()
                val upcomingTithis = filterUpcomingTithis(tithisFromRepo)
                _tithiList.value = upcomingTithis
                _filteredTithis.value = upcomingTithis
            } catch (e: Exception) {
                _tithiList.value = emptyList()
                _filteredTithis.value = emptyList()
            }
        }
    }

    fun filterTithisByQuery(query: String) {
        val q = query.trim().lowercase()
        _tithiList.value?.let { originalList ->
            val filtered = originalList.filter {
                // Note: Agar aapne Tithi.kt mein 'name' aur 'details' ko 'tithi' aur 'description' kar diya hai,
                // to yahan bhi unhe change karna hoga.
                it.name.lowercase().contains(q) ||
                        it.details?.lowercase()?.contains(q) == true ||
                        it.date.lowercase().contains(q)
            }
            _filteredTithis.value = filtered
        }
    }

    fun filterTithisByDays(days: Int) {
        if (days == 0) {
            _filteredTithis.postValue(_tithiList.value ?: emptyList())
            return
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance().time
        val endDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, days) }.time
        _tithiList.value?.let { originalList ->
            val filtered = originalList.filter {
                val date = try { sdf.parse(it.date) } catch (e: Exception) { null }
                date != null && date >= today && date <= endDate
            }
            _filteredTithis.postValue(filtered)
        }
    }

    private fun filterUpcomingTithis(allTithis: List<Tithi>): List<Tithi> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance().time
        return allTithis.filter {
            try {
                val tithiDate = sdf.parse(it.date)
                tithiDate != null && !tithiDate.before(today)
            } catch (e: Exception) {
                false
            }
        }
    }


    // ---------------------- EVENTS (No Changes) ----------------------
    private val _eventList = MutableLiveData<List<Event>>()
    val eventList: LiveData<List<Event>> = _eventList
    private val _allEvents = mutableListOf<Event>()

    fun fetchEvents() {
        viewModelScope.launch {
            try {
                val result = repository.getEvents()
                _allEvents.clear()
                _allEvents.addAll(result)
                _eventList.value = result
            } catch (e: Exception) {
                _eventList.value = emptyList()
            }
        }
    }

    fun filterEventsByState(state: String) {
        val filtered = _allEvents.filter {
            it.location.contains(state, ignoreCase = true)
        }
        _eventList.value = filtered
    }

    fun filterUpcomingEvents() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Calendar.getInstance().time)
        val upcoming = _allEvents.filter { it.date > today }
        _eventList.value = upcoming
    }

    fun filterEvents(query: String) {
        val lowerQuery = query.trim().lowercase()
        val filtered = _allEvents.filter { event ->
            event.name.lowercase().contains(lowerQuery) ||
                    event.location.lowercase().contains(lowerQuery) ||
                    event.date.lowercase().contains(lowerQuery) ||
                    event.description?.lowercase()?.contains(lowerQuery) == true
        }
        _eventList.value = filtered
    }


    // ---------------------- MAHARAJ (No Changes) ----------------------
    private val _maharajList = MutableLiveData<List<Maharaj>>()
    val maharajList: LiveData<List<Maharaj>> = _maharajList

    private val _filteredMaharaj = MutableLiveData<List<Maharaj>>()
    val filteredMaharaj: LiveData<List<Maharaj>> = _filteredMaharaj

    fun fetchMaharaj() {
        viewModelScope.launch {
            try {
                val list = repository.getMaharaj()
                _maharajList.value = list
                _filteredMaharaj.value = list
            } catch (e: Exception) {
                _maharajList.value = emptyList()
                _filteredMaharaj.value = emptyList()
            }
        }
    }

    fun filterBySearch(query: String) {
        val q = query.trim().lowercase()
        _filteredMaharaj.value = _maharajList.value?.filter { maharaj ->
            maharaj.name.lowercase().contains(q) ||
                    maharaj.city?.lowercase()?.contains(q) == true
        }
    }

    fun filterByCity(city: String) {
        _maharajList.value?.let { list ->
            _filteredMaharaj.value = list.filter {
                it.city?.contains(city, ignoreCase = true) == true
            }
        }
    }

    fun resetFilters() {
        _filteredMaharaj.value = _maharajList.value
    }
}