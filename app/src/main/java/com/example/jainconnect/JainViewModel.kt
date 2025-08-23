package com.example.jainconnect

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class JainViewModel : ViewModel() {

    private val repository = JainRepository()

    // ---------------------- TITHIS ----------------------

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
                _filteredTithis.value = upcomingTithis // ✅ show all by default
            } catch (e: Exception) {
                _tithiList.value = emptyList()
                _filteredTithis.value = emptyList()
            }
        }
    }

    fun filterTithisByQuery(query: String) {
        _tithiList.value?.let { originalList ->
            val filtered = originalList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.details?.contains(query, ignoreCase = true) == true ||
                        it.date.contains(query, ignoreCase = true)
            }
            _filteredTithis.postValue(filtered)
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
                val date = try {
                    sdf.parse(it.date)
                } catch (e: Exception) {
                    null
                }
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
                tithiDate != null && !tithiDate.before(today) // today ya future dates
            } catch (e: Exception) {
                false // agar parsing fail ho jaye
            }
        }
    }


    // ---------------------- EVENTS ----------------------

    private val _eventList = MutableLiveData<List<Event>>()   // Filtered list
    val eventList: LiveData<List<Event>> = _eventList
    private val _allEvents = mutableListOf<Event>()            // Full backup list for search

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

    // ---------------------- MAHARAJ ----------------------

    private val _maharajList = MutableLiveData<List<Maharaj>>()
    val maharajList: LiveData<List<Maharaj>> = _maharajList

    private val _filteredMaharaj = MutableLiveData<List<Maharaj>>()
    val filteredMaharaj: LiveData<List<Maharaj>> = _filteredMaharaj

    fun fetchMaharaj() {
        viewModelScope.launch {
            try {
                val list = repository.getMaharaj()
                _maharajList.value = list
                _filteredMaharaj.value = list // show all by default
            } catch (e: Exception) {
                _maharajList.value = emptyList()
                _filteredMaharaj.value = emptyList()
            }
        }
    }

    fun filterMaharajByQuery(query: String) {
        val q = query.trim().lowercase()
        _maharajList.value?.let { original ->
            _filteredMaharaj.value = original.filter {
                it.name.lowercase().contains(q) ||
                        (it.city?.lowercase() ?: "").contains(q) ||
                        (it.currentSthan?.lowercase() ?: "").contains(q)
            }
        }
    }


    fun filterMaharajByCity(city: String) {
        _maharajList.value?.let { list ->
            _filteredMaharaj.value = list.filter { it.city.equals(city, true) }
        }
    }

    fun filterUpcomingMaharaj() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = Calendar.getInstance().time

        _maharajList.value?.let { list ->
            _filteredMaharaj.value = list.filter {
                try {
                    val date = sdf.parse(it.relevantDate)
                    date != null && date.after(todayDate)
                } catch (e: Exception) {
                    false
                }
            }
        }
    }


    fun resetMaharajFilter() {
        _filteredMaharaj.value = _maharajList.value
    }
}
