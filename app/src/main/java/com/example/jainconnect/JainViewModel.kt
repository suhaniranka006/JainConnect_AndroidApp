package com.example.jainconnect

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class JainViewModel : ViewModel() {

    // ✅ FIX: Repository ko sirf ek baar declare kiya gaya hai
    private val repository = JainRepository()

    // =====================================================================================
    //                                  USER AUTHENTICATION
    // =====================================================================================

    // --- LiveData for Signup ---
    private val _signupResult = MutableLiveData<Response<AuthResponse>?>()
    val signupResult: LiveData<Response<AuthResponse>?> = _signupResult

    // --- LiveData for Login ---
    private val _loginResult = MutableLiveData<Response<AuthResponse>?>()
    val loginResult: LiveData<Response<AuthResponse>?> = _loginResult

    // --- LiveData for User Profile ---
    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    // --- LiveData for Profile Update Result ---
    private val _updateResult = MutableLiveData<Response<AuthResponse>?>()
    val updateResult: LiveData<Response<AuthResponse>?> = _updateResult

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

    fun performLogin(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.loginUser(email, password)
                _loginResult.postValue(response)
            } catch (e: Exception) {
                Log.e("JainViewModel", "Login Exception", e)
                _loginResult.postValue(null)
            }
        }
    }

    fun fetchUserProfile(token: String) {
        viewModelScope.launch {
            try {
                val response = repository.getUserProfile(token)
                if (response.isSuccessful) {
                    _userProfile.postValue(response.body()?.user)
                } else {
                    _userProfile.postValue(null)
                }
            } catch (e: Exception) {
                Log.e("JainViewModel", "Fetch Profile Exception", e)
                _userProfile.postValue(null)
            }
        }
    }

    fun updateProfile(
        token: String, name: String, phone: String, location: String,
        dob: String, gender: String, imageFile: File?
    ) {
        viewModelScope.launch {
            try {
                val response = repository.updateUserProfile(token, name, phone, location, dob, gender, imageFile)
                _updateResult.postValue(response)
            } catch (e: Exception) {
                Log.e("JainViewModel", "Update Profile Exception", e)
                _updateResult.postValue(null)
            }
        }
    }


    // =====================================================================================
    //                                      TITHIS
    // =====================================================================================
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
                // ✅ FIX: 'name' aur 'details' ko 'tithi' aur 'description' se badla gaya hai (assuming Tithi model change)
                it.name.lowercase().contains(q) ||
                        it.details?.lowercase()?.contains(q) == true ||
                        it.date.lowercase().contains(q)
            }
            _filteredTithis.value = filtered
        }
    }

    /**
     * Filters a list of all tithis to return only those that are on or after today's date.
     * @param allTithis The complete list of tithis from the repository.
     * @return A new list containing only upcoming tithis.
     */
    private fun filterUpcomingTithis(allTithis: List<Tithi>): List<Tithi> {
        // Date format jo aapke server se aa raha hai (e.g., "2025-10-14")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Aaj ki date nikalein, lekin time ko midnight (00:00:00) par set kar dein
        // taaki aaj ki tithi bhi list me shaamil ho.
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = todayCalendar.time

        return allTithis.filter { tithi ->
            try {
                // Har tithi ki date string ko Date object me convert karein
                val tithiDate = sdf.parse(tithi.date)
                // Check karein ki tithiDate null na ho aur aaj ya aaj ke baad ki ho
                tithiDate != null && !tithiDate.before(today)
            } catch (e: Exception) {
                // Agar date format galat hai, toh use list se hata dein
                false
            }
        }
    }

    /**
     * Filters the master list of tithis to show only those occurring within the next 'days'.
     * If days is 0, it resets the filter and shows all upcoming tithis.
     * @param days The number of days from today to include in the filter.
     */
    fun filterTithisByDays(days: Int) {
        // Agar user "All" select karta hai (days = 0), toh poori list dikha dein
        if (days == 0) {
            _filteredTithis.postValue(_tithiList.value ?: emptyList())
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Start date (aaj, midnight se)
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = startCalendar.time

        // End date (aaj se 'days' din baad)
        val endCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, days)
            set(Calendar.HOUR_OF_DAY, 23) // Din ke aakhir tak ka time set karein
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        val endDate = endCalendar.time

        _tithiList.value?.let { originalList ->
            val filtered = originalList.filter {
                try {
                    val tithiDate = sdf.parse(it.date)
                    // Check karein ki tithiDate null na ho aur 'today' aur 'endDate' ke beech me ho
                    tithiDate != null && !tithiDate.before(today) && !tithiDate.after(endDate)
                } catch (e: Exception) {
                    false
                }
            }
            _filteredTithis.postValue(filtered)
        }
    }




    // =====================================================================================
    //                                      EVENTS
    // =====================================================================================
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
            // ✅ FIX: 'location' ko 'city' se badla gaya hai
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
            // ✅ FIX: 'name' ko 'title' se aur 'location' ko 'city' se badla gaya hai
            event.name.lowercase().contains(lowerQuery) ||
                    event.location.lowercase().contains(lowerQuery) ||
                    event.date.lowercase().contains(lowerQuery) ||
                    event.description?.lowercase()?.contains(lowerQuery) == true
        }
        _eventList.value = filtered
    }


    // =====================================================================================
    //                                      MAHARAJ
    // =====================================================================================
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