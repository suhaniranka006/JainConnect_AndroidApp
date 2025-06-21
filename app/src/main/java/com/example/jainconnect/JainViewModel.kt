package com.example.jainconnect

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class JainViewModel : ViewModel() {

    private val repository = JainRepository()

    private val _tithiList = MutableLiveData<List<Tithi>>()
    val tithiList: LiveData<List<Tithi>> = _tithiList

    private val _eventList = MutableLiveData<List<Event>>()
    val eventList: LiveData<List<Event>> = _eventList

    private val _maharajList = MutableLiveData<List<Maharaj>>()
    val maharajList: LiveData<List<Maharaj>> = _maharajList

    fun fetchTithis() {
        Log.d("JainViewModel_Tithi", "STEP 1A: fetchTithis() function was called.")
        viewModelScope.launch {
            try {
                val tithisFromRepo = repository.getTithis()
                Log.d("JainViewModel_Tithi", "STEP 1B: Tithis from repository. Count = ${tithisFromRepo.size}")

                if (tithisFromRepo.isNotEmpty()) {
                    Log.d("JainViewModel_Tithi", "STEP 1C: First Tithi data = ${tithisFromRepo[0]}")
                }

                val filteredTithis = filterNext5Days(tithisFromRepo)
                _tithiList.value = filteredTithis

            } catch (e: Exception) {
                Log.e("JainViewModel_Tithi", "STEP 1D: ERROR fetching Tithis!", e)
                _tithiList.value = emptyList()
            }
        }
    }

    private fun filterNext5Days(allTithis: List<Tithi>): List<Tithi> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        val fiveDaysLater = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 4)
        }

        val todayStr = sdf.format(today.time)
        val endStr = sdf.format(fiveDaysLater.time)

        return allTithis.filter { tithi ->
            tithi.date >= todayStr && tithi.date <= endStr
        }
    }

    fun fetchEvents() {
        viewModelScope.launch {
            try {
                _eventList.value = repository.getEvents()
            } catch (e: Exception) {
                // handle error
                _eventList.value = emptyList()
            }
        }
    }

    fun fetchMaharaj() {
        viewModelScope.launch {
            try {
                _maharajList.value = repository.getMaharaj()
            } catch (e: Exception) {
                // handle error
                _maharajList.value = emptyList()
            }
        }
    }
}
