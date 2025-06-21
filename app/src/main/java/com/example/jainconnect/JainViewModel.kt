package com.example.jainconnect


import android.util.Log
import androidx.lifecycle.*

import kotlinx.coroutines.launch

class JainViewModel : ViewModel() {

    private val repository = JainRepository()

    private val _tithiList = MutableLiveData<List<Tithi>>()
    val tithiList: LiveData<List<Tithi>> = _tithiList

    private val _eventList = MutableLiveData<List<Event>>()
    val eventList: LiveData<List<Event>> = _eventList

    private val _maharajList = MutableLiveData<List<Maharaj>>()
    val maharajList: LiveData<List<Maharaj>> = _maharajList



    fun fetchTithis() {
        Log.d("JainViewModel_Tithi", "STEP 1A: fetchTithis() function was called.") // Log A
        viewModelScope.launch {
            try {
                val tithisFromRepo = repository.getTithis()
                // Log B - How many items came from the repository?
                Log.d(
                    "JainViewModel_Tithi",
                    "STEP 1B: Tithis from repository. Count = ${tithisFromRepo.size}"
                )

                if (tithisFromRepo.isNotEmpty()) {
                    // Log C - What does the first tithi look like?
                    Log.d("JainViewModel_Tithi", "STEP 1C: First Tithi data = ${tithisFromRepo[0]}")
                }
                _tithiList.value = tithisFromRepo
            } catch (e: Exception) {
                // Log D - Was there an error?
                Log.e("JainViewModel_Tithi", "STEP 1D: ERROR fetching Tithis!", e)
                _tithiList.value = emptyList()
            }
        }
    }

    fun fetchEvents() {
        viewModelScope.launch {
            try {
                _eventList.value = repository.getEvents()
            } catch (e: Exception) {
                // handle error
            }
        }
    }





    fun fetchMaharaj() {
        viewModelScope.launch {
            try {
                _maharajList.value = repository.getMaharaj()
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
