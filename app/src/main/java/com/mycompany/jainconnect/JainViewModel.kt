package com.mycompany.jainconnect

// 1. Android & Lifecycle Components
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// 2. Coroutines (Background tasks)
import kotlinx.coroutines.launch

// 3. Retrofit (API Responses)
import retrofit2.Response

// 4. Java Utilities (Dates, Files, Lists)
import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale

// 5. Your Data Models
// (Ensure these classes exist in your 'models' package or root package)
import com.mycompany.jainconnect.HorizonItem
import com.mycompany.jainconnect.SunResponse // Might be needed if referenced explicitly
// If your other models (User, Tithi, etc.) are in the root package, you don't need to import them.
// But if they are in the 'models' folder, UNCOMMENT these lines:
/*
import com.mycompany.jainconnect.models.User
import com.mycompany.jainconnect.models.AuthResponse
import com.mycompany.jainconnect.models.Tithi
import com.mycompany.jainconnect.models.Event
import com.mycompany.jainconnect.models.Maharaj
*/



import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * HiltViewModel for managing UI-related data in a lifecycle-conscious way.
 * @Inject constructor tells Hilt how to create instances of this ViewModel.
 * Hilt automatically provides the [JainRepository] dependency.
 */
@HiltViewModel
class JainViewModel @Inject constructor(
    private val repository: JainRepository
) : ViewModel() {

    //
    //                                  USER AUTHENTICATION
    //



    // --- LiveData for Signup ---
    // MutableLiveData stores the data, while publicly exposed LiveData is immutable
    // to prevent external classes from modifying it directly.
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




    // Add logic for Monk Submission
    private val _addMaharajResult = MutableLiveData<String>()
    val addMaharajResult: LiveData<String> = _addMaharajResult

    // ... inside JainViewModel class

    fun submitNewMaharaj(
        token: String,
        name: String,
        title: String,
        city: String,
        date: String,
        contact: String

    ) {
        viewModelScope.launch {
            try {
                val response = repository.submitMaharaj(token, name, title, city, date, contact)
                if (response.isSuccessful && response.body()?.success == true) {
                    _addMaharajResult.value = "Success"
                } else {
                    _addMaharajResult.value = "Failed: ${response.message()}"
                }
            } catch (e: Exception) {
                _addMaharajResult.value = "Error: ${e.message}"
            }
        }
    }

// Inside JainViewModel class

    // --- Add Event Submission Logic ---
    private val _addEventResult = MutableLiveData<String>()
    val addEventResult: LiveData<String> = _addEventResult

    // Inside JainViewModel.kt

    fun submitNewEvent(
        token: String,
        title: String,
        city: String,
        date: String,
        time: String,
        desc: String
    ) {
        viewModelScope.launch {
            try {
                val response = repository.submitEvent(token, title, city, date, time, desc)
                if (response.isSuccessful && response.body()?.success == true) {
                    _addEventResult.value = "Success"
                } else {
                    _addEventResult.value = "Failed: ${response.message()}"
                }
            } catch (e: Exception) {
                _addEventResult.value = "Error: ${e.message}"
            }
        }
    }


    // =====================================================================================
    //                                      HORIZONS (Sunrise/Sunset)
    // =====================================================================================

    private val _horizonList = MutableLiveData<List<HorizonItem>>()
    val horizonList: LiveData<List<HorizonItem>> = _horizonList

    // Default values are set to Jaipur (26.9124, 75.7873). You can pass other values when calling.
    // Inside JainViewModel.kt

    fun fetchSunData(lat: Double = 26.9124, lng: Double = 75.7873) {
        viewModelScope.launch {
            try {
                val response = repository.getSunTimings(lat, lng)

                if (response.isSuccessful && response.body() != null) {
                    val dailyData = response.body()!!.daily
                    val mappedList = ArrayList<HorizonItem>()

                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

                    // We use a standard C-style loop to avoid iterator ambiguity
                    val size = dailyData.dates.size
                    for (i in 0 until size) {
                        val rawDate = dailyData.dates[i]

                        // 1. Format Date
                        var formattedDate = rawDate
                        try {
                            val dateObj = inputFormat.parse(rawDate)
                            if (dateObj != null) {
                                formattedDate = outputFormat.format(dateObj)
                            }
                        } catch (e: Exception) {
                            formattedDate = rawDate
                        }

                        // 2. Extract Time (Simple String manipulation to avoid Type Mismatch)
                        // API sends "2025-12-04T07:05". We split by 'T'.
                        val rawSunrise = dailyData.sunrise[i] // e.g., "2025-12-04T07:05"
                        val rawSunset = dailyData.sunset[i]

                        // Split returns a List<String>, so [1] is definitely a String.
                        val sunriseParts = rawSunrise.split("T")
                        val sunriseTime = if (sunriseParts.size > 1) sunriseParts[1] else rawSunrise

                        val sunsetParts = rawSunset.split("T")
                        val sunsetTime = if (sunsetParts.size > 1) sunsetParts[1] else rawSunset

                        mappedList.add(HorizonItem(formattedDate, sunriseTime, sunsetTime))
                    }

                    _horizonList.value = mappedList
                } else {
                    Log.e("JainViewModel", "Horizons Error: ${response.code()}")
                    _horizonList.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("JainViewModel", "Horizons Exception", e)
                _horizonList.value = emptyList()
            }
        }
    }

    /**
     * Performs user registration.
     * Takes all user details and calls the repository to register the user.
     * Updates [signupResult] with the API response.
     */
    fun performSignup(
        name: String, email: String, password: String, phone: String,
        location: String, dob: String, gender: String, imageFile: File?
    ) {

        // viewModelScope is a built-in CoroutineScope tied to the ViewModel's lifecycle.
        // It automatically cancels operations when the ViewModel is cleared (e.g., Activity destroyed),
        // preventing memory leaks.
        viewModelScope.launch {
            try {
                val response = repository.registerUser(name, email, password, phone, location, dob, gender, imageFile)  //calls repo fun
                _signupResult.postValue(response)
            } catch (e: Exception) {
                Log.e("JainViewModel", "Signup Exception", e)  //for debugging
                _signupResult.postValue(null)  // launch in background thread
            }
        }
    }




    /**
     * Performs user login.
     * Updates [loginResult] with the API response.
     */
    fun performLogin(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Call repository to perform login operation
                val response = repository.loginUser(email, password)

                //update live data with the result
                //.postvalue is used to safely update livedata from a backgorund thread
                _loginResult.postValue(response)
            } catch (e: Exception) {

                //handle any error
                Log.e("JainViewModel", "Login Exception", e)
                _loginResult.postValue(null)  //notify ui of failure
            }
        }
    }


    //flow view->viewmodel - repo-response - then update view with live data after observing login results




    //fetch use profile after authorization
    fun fetchUserProfile(token: String) {
        viewModelScope.launch {
            try {

                val response = repository.getUserProfile(token)  //call this fun for auth
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




    //used to update profile
    fun updateProfile(
        token: String, name: String, phone: String, location: String,
        dob: String, gender: String, imageFile: File?
    ) {
        viewModelScope.launch {
            try {
                val response = repository.updateUserProfile(token, name, phone, location, dob, gender, imageFile)  //token for auth
                _updateResult.postValue(response)
            } catch (e: Exception) {
                Log.e("JJainViewModel", "Update Profile Exception", e)
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




    // Fetches tithi data from the repository
    fun fetchTithis() {
        viewModelScope.launch {
            try {
                val tithisFromRepo = repository.getTithis()
                // Filter locally reduces network calls but consumes more memory
                val upcomingTithis = filterUpcomingTithis(tithisFromRepo)
                _tithiList.value = upcomingTithis
                _filteredTithis.value = upcomingTithis
            } catch (e: Exception) {
                _tithiList.value = emptyList()
                _filteredTithis.value = emptyList()
            }
        }
    }




    // Filters tithis based on a search query
    fun filterTithisByQuery(query: String) {

        val q = query.trim().lowercase()

        _tithiList.value?.let { originalList ->
            val filtered = originalList.filter {

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



    // Filters the list to show only upcoming tithis (including today)
    private fun filterUpcomingTithis(allTithis: List<Tithi>): List<Tithi> {

        // Date format from the server (e.g., "2025-10-14")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Set time to midnight (00:00:00) so that today's tithi is included

        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val today = todayCalendar.time


        return allTithis.filter { tithi ->
            try {

                //convert date string into date object

                val tithiDate = sdf.parse(tithi.date)

                //check nullability or it should be after today
                tithiDate != null && !tithiDate.before(today)
            } catch (e: Exception) {
                // if date format is wrong, remove it from list
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

        // If days is 0, show all tithis
        if (days == 0) {
            _filteredTithis.postValue(_tithiList.value ?: emptyList())
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Start date (Today from midnight)
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = startCalendar.time

        // End date (Today + 'days')
        val endCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, days)
            set(Calendar.HOUR_OF_DAY, 23) // Set to end of the day

            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        val endDate = endCalendar.time

        _tithiList.value?.let { originalList ->
            val filtered = originalList.filter {
                try {
                    val tithiDate = sdf.parse(it.date)

                    //check nullability and range
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

    // === RSVP LiveData ===
    // Notifies the Activity if the API call was successful (true) or failed (false)
    private val _rsvpResult = MutableLiveData<Boolean>()
    val rsvpResult: LiveData<Boolean> = _rsvpResult
    // ==========================


    //fetch events
    fun fetchEvents() {
        viewModelScope.launch {
            //network call on background thread
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



    //filter events by states

    fun filterEventsByState(state: String, stateToCities: Map<String, List<String>>) {
        val cityList = stateToCities[state] ?: emptyList()
        val filtered = _allEvents.filter { event ->
            val loc = event.location?.trim() ?: ""
            cityList.any { city -> loc.equals(city, ignoreCase = true) } ||
                    loc.contains(state, ignoreCase = true)
        }
        _eventList.value = filtered
    }




    //filter by date
    fun filterUpcomingEvents() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Calendar.getInstance().time)
        val upcoming = _allEvents.filter { it.date > today }
        _eventList.value = upcoming
    }


    // Filter events based on search query
    fun filterEvents(query: String) {
        val lowerQuery = query.trim().lowercase()
        val filtered = _allEvents.filter { event ->
            // Use safe calls (?.) to handle potential null values
            // ?.let { ... } runs the code block only if the value is not null

            val nameMatches = event.name?.lowercase()?.contains(lowerQuery) == true
            val locationMatches = event.location?.lowercase()?.contains(lowerQuery) == true
            val dateMatches = event.date?.lowercase()?.contains(lowerQuery) == true
            val descriptionMatches = event.description?.lowercase()?.contains(lowerQuery) == true

            // Keep the event if any field matches the query
            nameMatches || locationMatches || dateMatches || descriptionMatches
        }
        _eventList.value = filtered
    }


    // === RSVP Function ===
    /**
     * Handles the "I'm Going" (RSVP) button click.
     * Triggers the API call via the Repository.
     * @param eventId The ID of the event clicked.
     */
    fun toggleEventRsvp(token: String, eventId: String) {
        viewModelScope.launch {
            try {
                // Repository manages the token
                val response = repository.toggleEventRsvp(token, eventId)

                if (response.isSuccessful) {
                    // Notify Activity of success
                    _rsvpResult.postValue(true)
                    // Note: Activity calls 'fetchEvents()' on response
                    // to update the count.
                } else {
                    // Error occurred
                    Log.w("JainViewModel", "toggleEventRsvp failed: ${response.message()}")
                    _rsvpResult.postValue(false)
                }
            } catch (e: Exception) {
                // Network or other exception
                Log.e("JainViewModel", "RSVP Toggle Exception", e)
                _rsvpResult.postValue(false)
            }
        }
    }
    // ==========================


    // =====================================================================================
    //                                      MAHARAJ
    // =====================================================================================
    private val _maharajList = MutableLiveData<List<Maharaj>>()
    val maharajList: LiveData<List<Maharaj>> = _maharajList

    private val _filteredMaharaj = MutableLiveData<List<Maharaj>>()
    val filteredMaharaj: LiveData<List<Maharaj>> = _filteredMaharaj


    //fetch monks data on background thread
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


    //filter by search
    fun filterBySearch(query: String) {
        val q = query.trim().lowercase()
        _filteredMaharaj.value = _maharajList.value?.filter { maharaj ->
            maharaj.name.lowercase().contains(q) ||
                    maharaj.city?.lowercase()?.contains(q) == true
        }
    }


    //filter by city
    fun filterByCity(city: String) {
        _maharajList.value?.let { list ->
            _filteredMaharaj.value = list.filter {
                it.city?.contains(city, ignoreCase = true) == true
            }
        }
    }



    //reset all filters
    fun resetFilters() {
        _filteredMaharaj.value = _maharajList.value
    }
}
