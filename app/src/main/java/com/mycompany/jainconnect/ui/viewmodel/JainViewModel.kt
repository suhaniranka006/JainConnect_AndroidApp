package com.mycompany.jainconnect.ui.viewmodel

// 1. Android & Lifecycle Components
import android.util.Log
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext // Added ApplicationContext import

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
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Event
import com.mycompany.jainconnect.data.models.Maharaj
import com.mycompany.jainconnect.data.network.NetworkResult
import com.mycompany.jainconnect.data.models.RsvpResponse // Added Import
import com.mycompany.jainconnect.data.models.Bhojanshala
import com.mycompany.jainconnect.data.models.Temple
import com.mycompany.jainconnect.data.models.Carpool
import com.mycompany.jainconnect.data.models.CarpoolRequest
import com.mycompany.jainconnect.data.models.Story
import com.mycompany.jainconnect.data.models.Tithi
import com.mycompany.jainconnect.data.models.User
import com.mycompany.jainconnect.data.repository.JainRepository
import com.mycompany.jainconnect.data.models.AuthResponse
import com.mycompany.jainconnect.data.models.HorizonItem
import com.mycompany.jainconnect.data.models.SunResponse
import com.mycompany.jainconnect.data.models.ApiResponse

/**
 * HiltViewModel for managing UI-related data in a lifecycle-conscious way.
 * @Inject constructor tells Hilt how to create instances of this ViewModel.
 * Hilt automatically provides the [JainRepository] dependency.
 */
@HiltViewModel
class JainViewModel @Inject constructor(
    @ApplicationContext private val context: Context, // Injected Context
    private val repository: JainRepository,
    private val tirthyatraRepository: com.mycompany.jainconnect.data.repository.TirthyatraRepository,
    private val savedRepository: com.mycompany.jainconnect.data.repository.SavedRepository
) : ViewModel() {

    // --- Saved Items LiveData ---
    private val _savedMonks = MutableLiveData<List<Maharaj>>()
    val savedMonks: LiveData<List<Maharaj>> = _savedMonks

    private val _savedEvents = MutableLiveData<List<Event>>()
    val savedEvents: LiveData<List<Event>> = _savedEvents

    private val _savedTithis = MutableLiveData<List<Tithi>>()
    val savedTithis: LiveData<List<Tithi>> = _savedTithis
    
    // Temples & Bhojanshalas
    private val _savedTemples = MutableLiveData<List<Temple>>()
    val savedTemples: LiveData<List<Temple>> = _savedTemples
    
    private val _savedBhojanshalas = MutableLiveData<List<Bhojanshala>>()
    val savedBhojanshalas: LiveData<List<Bhojanshala>> = _savedBhojanshalas

    private val _savedStories = MutableLiveData<List<Story>>()
    val savedStories: LiveData<List<Story>> = _savedStories

    // --- Stories LiveData ---
    private val _storyList = MutableLiveData<List<Story>>()
    val storyList: LiveData<List<Story>> = _storyList
    

    
    // Helper to check if an ID is saved (Single check)
    fun isCreatedSaved(id: String, type: String): Boolean {
        return savedRepository.isSaved(id, type)
    }

    fun toggleSaveState(id: String, type: String) {
        savedRepository.toggleSave(id, type)
        // After toggling, refresh the specific saved list if needed
        when(type) {
            com.mycompany.jainconnect.data.repository.SavedRepository.KEY_MONKS -> fetchSavedMonks()
            com.mycompany.jainconnect.data.repository.SavedRepository.KEY_EVENTS -> fetchSavedEvents()
            com.mycompany.jainconnect.data.repository.SavedRepository.KEY_TITHIS -> fetchSavedTithis()
            com.mycompany.jainconnect.data.repository.SavedRepository.KEY_TEMPLES -> fetchSavedTemples()
            com.mycompany.jainconnect.data.repository.SavedRepository.KEY_TEMPLES -> fetchSavedTemples()
            com.mycompany.jainconnect.data.repository.SavedRepository.KEY_FOOD -> fetchSavedBhojanshalas()
            com.mycompany.jainconnect.data.repository.SavedRepository.KEY_STORIES -> fetchSavedStories()
        }
    }

    fun fetchSavedMonks() {
        viewModelScope.launch {
            val allMonks = repository.getMaharaj()
            val savedIds = savedRepository.getSavedIds(com.mycompany.jainconnect.data.repository.SavedRepository.KEY_MONKS)
            val filtered = allMonks.filter { savedIds.contains(it.id) }
            _savedMonks.value = filtered
        }
    }
    
    fun fetchSavedEvents() {
        viewModelScope.launch {
            val allEvents = repository.getEvents()
            val savedIds = savedRepository.getSavedIds(com.mycompany.jainconnect.data.repository.SavedRepository.KEY_EVENTS)
            // Use _id for filtering
            val filtered = allEvents.filter { savedIds.contains(it._id) }
            _savedEvents.value = filtered
        }
    }

    // Event RSVP
    private val _rsvpStatus = MutableLiveData<NetworkResult<RsvpResponse>>()
    val rsvpStatus: LiveData<NetworkResult<RsvpResponse>> = _rsvpStatus

    private fun getToken(): String? {
        val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }

    fun toggleEventRsvp(eventId: String) {
        viewModelScope.launch {
            val token = getToken() // Now uses local helper
            if (token != null) {
                try {
                    val response = repository.toggleEventRsvp(token, eventId)
                    if (response.isSuccessful && response.body() != null) {
                        _rsvpStatus.postValue(NetworkResult.Success(response.body()!!))
                        // Refresh events to update list UI if needed
                        fetchEvents() // Corrected function name
                    } else {
                        _rsvpStatus.postValue(NetworkResult.Error(response.message()))
                    }
                } catch (e: Exception) {
                    _rsvpStatus.postValue(NetworkResult.Error(e.message ?: "Unknown Error"))
                }
            } else {
                 _rsvpStatus.postValue(NetworkResult.Error("Please login to join events"))
            }
        }
    }

    fun fetchSavedTithis() {
        viewModelScope.launch {
            val allTithis = repository.getTithis()
             val savedIds = savedRepository.getSavedIds(com.mycompany.jainconnect.data.repository.SavedRepository.KEY_TITHIS)
            // Tithi might not have _id, check model. If not, use date or name as key?
            // Assuming Tithi has some unique ID or we use Name+Date as key
            // Ideally backend provides ID, checking Tithi Model... 
            // Result: Tithi model usually has date/name. Let's assume ID exists or fallback
            // For now, let's filter by _id if it exists, otherwise skip (Tithi saving might be tricky without ID)
            // Updating Tithi model check later.
            val filtered = allTithis.filter { it.id != null && savedIds.contains(it.id) }
            _savedTithis.value = filtered
        }
    }

    fun fetchSavedTemples() {
        viewModelScope.launch {
            val allTemples = repository.getTemples() // Assuming this exists or similar
            val savedIds = savedRepository.getSavedIds(com.mycompany.jainconnect.data.repository.SavedRepository.KEY_TEMPLES)
            // Use _id or id
            val filtered = allTemples.filter { savedIds.contains(it._id) }
            _savedTemples.value = filtered
        }
    }

    fun fetchSavedBhojanshalas() {
        viewModelScope.launch {
            val allBhojanshalas = repository.getBhojanshalas() // Assuming this exists
            val savedIds = savedRepository.getSavedIds(com.mycompany.jainconnect.data.repository.SavedRepository.KEY_FOOD)
            val filtered = allBhojanshalas.filter { savedIds.contains(it._id) }
        }
    }

    // --- Pachkhan & Leaderboard ---
    fun takePachkhan(token: String, name: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.takePachkhan(token, name)
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess(response.body()?.message ?: "Vow taken successfully!")
                } else {
                    // Parse error body for message
                    val errorMsg = try {
                        val errorStr = response.errorBody()?.string()
                        val jsonObj = org.json.JSONObject(errorStr ?: "")
                        jsonObj.getString("message")
                    } catch (e: Exception) {
                        "Failed to take vow"
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }
    }

    // --- Taken Pachkhans State ---
    private val _takenPachkhans = MutableLiveData<Set<String>>(emptySet())
    val takenPachkhans: LiveData<Set<String>> get() = _takenPachkhans

    fun getTakenPachkhans(token: String) {
        viewModelScope.launch {
            try {
                val response = repository.getPachkhanStatus(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    val takenList = response.body()?.takenVows ?: emptyList()
                    _takenPachkhans.postValue(takenList.toSet())
                }
            } catch (e: Exception) {
                // Silently fail or log, as this is just UI state
                e.printStackTrace()
            }
        }
    }

    private val _leaderboardData = MutableLiveData<List<com.mycompany.jainconnect.data.models.LeaderboardUser>>()
    val leaderboardData: LiveData<List<com.mycompany.jainconnect.data.models.LeaderboardUser>> get() = _leaderboardData
    
    // Simple error live data if not exists, or reuse usage logic
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getLeaderboard(token: String, type: String) {
        viewModelScope.launch {
            try {
                val response = repository.getLeaderboard(token, type)
                if (response.isSuccessful && response.body()?.success == true) {
                    _leaderboardData.postValue(response.body()?.data ?: emptyList())
                } else {
                    _error.postValue("Failed to load leaderboard")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "An error occurred")
            }
        }
    }
    
    fun fetchSavedStories(token: String? = null) {
         viewModelScope.launch {
             // We need a token to fetch stories usually, but let's try to get from repo or rely on passed one
             // Actually getStories needs token. For now let's assume valid token is stored or passed.
             // Ideally we shouldn't need to pass token everywhere if we have a SessionManager/Interceptor
             // But following current pattern:
             // We'll read from SharedPrefs inside here if token is null
             
             // ... wait, fetchStories creates its own token read. Let's duplicate that logic slightly or refactor.
             // Simpler: Just read token here.
             
            try {
                // HACK: Hardcoding obtaining context? No, we don't have context here easily without passing it.
                // But wait, fetchStories takes context. That's bad design in ViewModel but ok.
                // SavedListFragment can call this. 
                // Let's change signature to take context OR just use a stored token if we had one.
                // But we don't have stored token in ViewModel.
                
                // Better approach: Make fetchSavedStories take context just like fetchStories
                // Or: assume the repository (if updated with interceptor) handles it.
                // The current `repository.getStories(token)` requires string.
                
                // Let's modify signature to take token. Fragment/Activity will provide it.
                // If token is empty, we can't fetch.
                
                if (token.isNullOrEmpty()) {
                     _savedStories.value = emptyList()
                     return@launch
                }
                
                val allStories = repository.getStories(token)
                val savedIds = savedRepository.getSavedIds(com.mycompany.jainconnect.data.repository.SavedRepository.KEY_STORIES)
                val filtered = allStories.filter { savedIds.contains(it.id) }
                _savedStories.value = filtered
            } catch(e: Exception) {
               _savedStories.value = emptyList()
            }
         }
    }

    // ... (Existing Auth Logic)

    // ===================================
    //       TIRTHYATRA PLANNER
    // ===================================

    private val _tirthyatraTemplates = MutableLiveData<List<com.mycompany.jainconnect.data.models.TirthyatraTemplate>>()
    val tirthyatraTemplates: LiveData<List<com.mycompany.jainconnect.data.models.TirthyatraTemplate>> = _tirthyatraTemplates

    private val _myYatras = MutableLiveData<List<com.mycompany.jainconnect.data.models.Tirthyatra>>()
    val myYatras: LiveData<List<com.mycompany.jainconnect.data.models.Tirthyatra>> = _myYatras

    private val _publicYatras = MutableLiveData<List<com.mycompany.jainconnect.data.models.Tirthyatra>>()
    val publicYatras: LiveData<List<com.mycompany.jainconnect.data.models.Tirthyatra>> = _publicYatras

    private val _yatraOperationResult = MutableLiveData<String>()
    val yatraOperationResult: LiveData<String> = _yatraOperationResult

    private val _yatraDetails = MutableLiveData<com.mycompany.jainconnect.data.models.Tirthyatra?>()
    val yatraDetails: LiveData<com.mycompany.jainconnect.data.models.Tirthyatra?> = _yatraDetails

    fun fetchTirthyatraTemplates(isPopular: Boolean? = null) {
        viewModelScope.launch {
            val result = tirthyatraRepository.getTemplates(isPopular)
            if (result is NetworkResult.Success && result.data != null) {
                 // Sort: Popular first
                 val sortedList = result.data.data.sortedByDescending { it.isPopular }
                 _tirthyatraTemplates.value = sortedList
            } else {
                 _tirthyatraTemplates.value = emptyList()
            }
        }
    }

    fun createYatra(token: String, yatra: com.mycompany.jainconnect.data.models.Tirthyatra) {
        viewModelScope.launch {
            val result = tirthyatraRepository.createYatra(token, yatra)
             if (result is NetworkResult.Success) {
                 _yatraOperationResult.value = "Success"
                 fetchMyYatras(token) // Refresh list
             } else {
                 _yatraOperationResult.value = result.message ?: "Failed"
             }
        }
    }

    private val _uploadedImageUrl = MutableLiveData<String?>()
    val uploadedImageUrl: LiveData<String?> = _uploadedImageUrl

    fun uploadYatraImage(token: String, file: File) {
        viewModelScope.launch {
            // Note: Using the main repository for this generic upload or create a specific one
            // We added it to JainRepository, so using 'repository'
            try {
                val response = repository.uploadTirthyatraImage(token, file)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uploadedImageUrl.value = response.body()!!.imageUrl
                } else {
                    _yatraOperationResult.value = "Image Upload Failed"
                }
            } catch (e: Exception) {
                 _yatraOperationResult.value = "Image Upload Error: ${e.message}"
            }
        }
    }

    fun fetchMyYatras(token: String) {
        viewModelScope.launch {
            val result = tirthyatraRepository.getMyYatras(token)
             if (result is NetworkResult.Success && result.data != null) {
                 _myYatras.value = result.data.data
             } else {
                 Log.e("JainViewModel", "fetchMyYatras failed: ${result.message}")
                 _myYatras.value = emptyList()
             }
        }
    }

    fun fetchPublicYatras(token: String) {
        viewModelScope.launch {
            val result = tirthyatraRepository.getPublicYatras(token)
             if (result is NetworkResult.Success && result.data != null) {
                 _publicYatras.value = result.data.data
             } else {
                 Log.e("JainViewModel", "fetchPublicYatras failed: ${result.message}")
                 _publicYatras.value = emptyList()
             }
        }
    }

    fun joinYatra(token: String, yatraId: String, message: String, contactNumber: String, peopleCount: Int, name: String, age: String, gender: String) {
        viewModelScope.launch {
             val result = tirthyatraRepository.joinYatra(token, yatraId, message, contactNumber, peopleCount, name, age, gender)
             if (result is NetworkResult.Success) {
                 _yatraOperationResult.value = "Joined"
                 fetchPublicYatras(token) // Refresh public list
                 fetchMyYatras(token) // Refresh my list
             } else {
                 _yatraOperationResult.value = result.message ?: "Failed to join"
             }
        }
    }

    fun cancelRequest(token: String, yatraId: String) {
        viewModelScope.launch {
            val result = tirthyatraRepository.cancelRequest(token, yatraId)
            if (result is NetworkResult.Success) {
                _yatraOperationResult.value = "Request Cancelled"
                fetchMyYatras(token)
                fetchPublicYatras(token) // Refresh statuses
            } else {
                _yatraOperationResult.value = result.message ?: "Failed to cancel request"
            }
        }
    }

    fun toggleCompanionship(token: String, yatraId: String, enable: Boolean, name: String? = null, age: String? = null, gender: String? = null, contact: String? = null, message: String? = null, peopleCount: Int? = null) {
        viewModelScope.launch {
            val result = tirthyatraRepository.toggleCompanionship(token, yatraId, enable, name, age, gender, contact, message, peopleCount)
            if (result is NetworkResult.Success) {
                _yatraOperationResult.value = if (enable) "Companionship Enabled" else "Companionship Disabled"
                // Refresh details? The activity observing this should fetch details or update UI.
                loadYatraDetails(token, yatraId)
            } else {
                _yatraOperationResult.value = result.message ?: "Failed to toggle"
            }
        }
    }

    fun manageMember(token: String, yatraId: String, targetUserId: String, action: String) {
        viewModelScope.launch {
            val result = tirthyatraRepository.manageMember(token, yatraId, targetUserId, action)
            if (result is NetworkResult.Success) {
                _yatraOperationResult.value = "Action Successful: $action"
                loadYatraDetails(token, yatraId)
            } else {
                _yatraOperationResult.value = result.message ?: "Action Failed"
            }
        }
    }

    fun deleteYatra(token: String, yatraId: String) {
        viewModelScope.launch {
             val result = tirthyatraRepository.deleteYatra(token, yatraId)
             if (result is NetworkResult.Success) {
                 _yatraOperationResult.value = "Deleted"
                 fetchMyYatras(token) // Refresh list to remove deleted item
             } else {
                 _yatraOperationResult.value = result.message ?: "Failed to delete"
             }
        }
    }

    fun leaveYatra(token: String, yatraId: String) {
        viewModelScope.launch {
            val result = tirthyatraRepository.leaveYatra(token, yatraId)
            if (result is NetworkResult.Success) {
                _yatraOperationResult.value = "Left Yatra"
                fetchMyYatras(token)
            } else {
                _yatraOperationResult.value = result.message ?: "Failed to leave"
            }
        }
    }

    fun loadYatraDetails(token: String, yatraId: String) {
        viewModelScope.launch {
            val result = tirthyatraRepository.getYatraDetails(token, yatraId)
             if (result is NetworkResult.Success && result.data != null) {
                 _yatraDetails.value = result.data.data
             } else {
                 // handle error?
             }
        }
    }


    //
    //                                  USER AUTHENTICATION
    //


    // --- LiveData for Signup ---
    // MutableLiveData stores the data, while publicly exposed LiveData is immutable
    // to prevent external classes from modifying it directly.
    private val _signupResult = MutableLiveData<Response<AuthResponse>?>()
    val signupResult: LiveData<Response<AuthResponse>?> = _signupResult


    // --- LiveData for Login ---
    private val _loginResult = MutableLiveData<NetworkResult<AuthResponse>>() // Updated type
    val loginResult: LiveData<NetworkResult<AuthResponse>> = _loginResult

    // --- LiveData for User Profile ---
    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile


    // --- LiveData for Profile Update Result ---
    private val _updateResult = MutableLiveData<Response<AuthResponse>?>()
    val updateResult: LiveData<Response<AuthResponse>?> = _updateResult

    // Add logic for Monk Submission
    private val _addMaharajResult = MutableLiveData<String>()
    val addMaharajResult: LiveData<String> = _addMaharajResult

    // --- Add Event Submission Logic ---
    private val _addEventResult = MutableLiveData<String>()
    val addEventResult: LiveData<String> = _addEventResult


    /**
     * Performs user login.
     * Updates [loginResult] with the API response wrapped in NetworkResult.
     */
    fun performLogin(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = NetworkResult.Loading() // Emit Loading State
            try {
                // Call repository to perform login operation
                val response = repository.loginUser(email, password)

                if (response.isSuccessful && response.body() != null) {
                    // Success
                    _loginResult.value = NetworkResult.Success(response.body()!!)
                } else {
                    // API Error (e.g., 401 Unauthorized)
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    _loginResult.value = NetworkResult.Error("Login Failed: $errorMsg")
                }
            } catch (e: Exception) {
                // Network/Exception Error
                Log.e("JainViewModel", "Login Exception", e)
                _loginResult.value = NetworkResult.Error("Network Error: ${e.message}")
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
                val response = repository.registerUser(
                    name,
                    email,
                    password,
                    phone,
                    location,
                    dob,
                    gender,
                    imageFile
                )  //calls repo fun
                _signupResult.postValue(response)
            } catch (e: Exception) {
                Log.e("JainViewModel", "Signup Exception", e)  //for debugging
                _signupResult.postValue(null)  // launch in background thread
            }
        }
    }


    fun submitNewMaharaj(
        token: String,
        name: String,
        title: String,
        city: String,
        date: String,
        contact: String,
        arrivalDate: String,
        viharDate: String,
        description: String
    ) {
        viewModelScope.launch {
            try {
                val response = repository.submitMaharaj(token, name, title, city, date, contact, arrivalDate, viharDate, description)
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


    fun submitNewCarpool(
        token: String, driver: String, source: String, dest: String, 
        date: String, time: String, vehicle: String, seats: Int, 
        contact: String, isLadiesOnly: Boolean,
        sourceLat: Double? = null, sourceLng: Double? = null,
        destLat: Double? = null, destLng: Double? = null
    ) {
        viewModelScope.launch {
            try {
                val request = CarpoolRequest(
                    driverName = driver,
                    source = source,
                    destination = dest,
                    date = date,
                    time = time,
                    vehicleType = vehicle,
                    seatsAvailable = seats,
                    contactNumber = contact,
                    isLadiesOnly = isLadiesOnly,
                    sourceLat = sourceLat,
                    sourceLng = sourceLng,
                    destLat = destLat,
                    destLng = destLng
                )
                val response = repository.createCarpool(token, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _addCarpoolResult.postValue("Success")
                    // If we have current filters that might hide this, we should consider that.
                    // For now, just fetching all again or keeping current filter might be best.
                    // Let's just fetch default to show the new addition if filters allow.
                    fetchCarpools() 
                } else {
                    _addCarpoolResult.postValue("Failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _addCarpoolResult.postValue("Error: ${e.message}")
            }
        }
    }

    fun submitNewMaharajWithImage(
        token: String,
        name: String,
        title: String,
        city: String,
        date: String,
        contact: String,
        arrivalDate: String,
        viharDate: String,
        description: String,
        imageFile: File?
    ) {
        viewModelScope.launch {
            try {
                val response = repository.submitMaharajWithImage(
                    token,
                    name,
                    title,
                    city,
                    date,
                    contact,
                    arrivalDate,
                    viharDate,
                    description,
                    imageFile
                )
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


    fun submitNewEvent(
        token: String,
        title: String,
        city: String,
        date: String,
        startDate: String,
        endDate: String,
        time: String,
        desc: String,
        contact: String,
        imageFile: File? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            try {
                val response = if (imageFile != null) {
                    repository.submitEventWithImage(
                        token,
                        title,
                        city,
                        date,
                        startDate,
                        endDate,
                        time,
                        desc,
                        contact,
                        imageFile,
                        latitude,
                        longitude
                    )
                } else {
                    repository.submitEvent(token, title, city, date, startDate, endDate, time, desc, contact, latitude, longitude)
                }

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
        // Cache Check: If we already have data, don't re-fetch
        if (!_horizonList.value.isNullOrEmpty()) return

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
                // _horizonList.value = emptyList()
            }
        }
    }


    //flow view->viewmodel - repo-response - then update view with live data after observing login results


    //fetch use profile after authorization
    fun fetchUserProfile(token: String) {
        // Cache Check: If profile is already loaded, skip
        if (_userProfile.value != null) return

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
                val response = repository.updateUserProfile(
                    token,
                    name,
                    phone,
                    location,
                    dob,
                    gender,
                    imageFile
                )  //token for auth
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
        // Cache Check: Use existing list if available
        if (!_tithiList.value.isNullOrEmpty()) return

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
        }
    }


    /**
     * Filters the list to show only Major Parva (Festivals).
     */
    fun filterMajorParva() {
        val currentList = _tithiList.value ?: return
        val filtered = currentList.filter { it.isMajor }
        _filteredTithis.postValue(filtered)
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
                // _eventList.value = emptyList() // PERSISTENCE FIX
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


    //filter events by states
    fun filterOngoingEvents() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Calendar.getInstance().time)
        
        val ongoing = _allEvents.filter { event ->
            // Case 1: Single Date Event (date == today)
            // Case 2: Multi Date Event (startDate <= today <= endDate)
            val isSingleDay = event.date == today
            val isMultiDay = (event.startDate != null && event.endDate != null) &&
                             (event.startDate <= today && event.endDate >= today)
            
            isSingleDay || isMultiDay
        }
        
        _eventList.value = ongoing
    }

    //filter by date
    fun filterUpcomingEvents() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Calendar.getInstance().time)
        val upcoming = _allEvents.filter { it.date > today }
        _eventList.value = upcoming
    }

    // Filter by Distance (e.g. 50km)
    fun filterEventsByDistance(userLat: Double, userLng: Double, limitKm: Double) {
        val filtered = _allEvents.filter { event ->
            if (event.latitude != null && event.longitude != null) {
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    userLat, userLng,
                    event.latitude, event.longitude,
                    results
                )
                val distanceInKm = results[0] / 1000
                distanceInKm <= limitKm
            } else {
                false
            }
        }
        _eventList.value = filtered
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
        if (_maharajList.value.isNullOrEmpty().not()) return // Cache check

        viewModelScope.launch {
            try {
                val list = repository.getMaharaj()
                _maharajList.value = list
                _filteredMaharaj.value = list
            } catch (e: Exception) {
                // _maharajList.value = emptyList()
                // _filteredMaharaj.value = emptyList()
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

    // =====================================================================================
    //                                      BHOJANSHALA
    // =====================================================================================
    private val _bhojanshalaList = MutableLiveData<List<Bhojanshala>>()
    val bhojanshalaList: LiveData<List<Bhojanshala>> = _bhojanshalaList

    // Backup list for filtering
    private val _allBhojanshalas = mutableListOf<Bhojanshala>()

    private val _addBhojanshalaResult = MutableLiveData<String>()
    val addBhojanshalaResult: LiveData<String> = _addBhojanshalaResult

    fun fetchBhojanshalas() {
        if (_bhojanshalaList.value.isNullOrEmpty().not()) return // Cache check

        viewModelScope.launch {
            try {
                val list = repository.getBhojanshalas()
                _allBhojanshalas.clear()
                _allBhojanshalas.addAll(list)
                _bhojanshalaList.value = list
            } catch (e: Exception) {
                // _bhojanshalaList.value = emptyList()
            }
        }
    }

    // Filter Bhojanshalas
    fun filterBhojanshalas(query: String) {
        val q = query.trim().lowercase()
        val filtered = _allBhojanshalas.filter { item ->
            val nameMatches = item.name.lowercase().contains(q)
            val cityMatches = item.city.lowercase().contains(q)
            val addressMatches = item.address.lowercase().contains(q)

            nameMatches || cityMatches || addressMatches
        }
        _bhojanshalaList.value = filtered
    }

    fun submitNewBhojanshala(
        token: String,
        name: String,
        city: String,
        address: String,
        openingTime: String,
        closingTime: String,
        contact: String,
        description: String,
        imageFile: File? = null
    ) {
        val timings = "$openingTime - $closingTime"
        viewModelScope.launch {
            try {
                val response = if (imageFile != null) {
                    repository.submitBhojanshalaWithImage(
                        token,
                        name,
                        city,
                        address,
                        timings,
                        openingTime,
                        closingTime,
                        contact,
                        description,
                        imageFile
                    )
                } else {
                    repository.submitBhojanshala(
                        token,
                        name,
                        city,
                        address,
                        timings,
                        openingTime,
                        closingTime,
                        contact,
                        description
                    )
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    _addBhojanshalaResult.value = "Success"
                } else {
                    _addBhojanshalaResult.value = "Failed: ${response.message()}"
                }
            } catch (e: Exception) {
                _addBhojanshalaResult.value = "Error: ${e.message}"
            }

        }
    }

    // =====================================================================================
    //                                      TEMPLE
    // =====================================================================================
    private val _templeList = MutableLiveData<List<Temple>>()
    val templeList: LiveData<List<Temple>> = _templeList

    // Backup list for filtering
    private val _allTemples = mutableListOf<Temple>()

    private val _addTempleResult = MutableLiveData<String>()
    val addTempleResult: LiveData<String> = _addTempleResult

    fun fetchTemples() {
        if (_templeList.value.isNullOrEmpty().not()) return // Cache check

        viewModelScope.launch {
            try {
                val list = repository.getTemples()
                _allTemples.clear()
                _allTemples.addAll(list)
                _templeList.value = list
            } catch (e: Exception) {
                // _templeList.value = emptyList()
            }
        }
    }

    // Filter Temples
    fun filterTemples(query: String) {
        val q = query.trim().lowercase()
        val filtered = _allTemples.filter { item ->
            val nameMatches = item.name.lowercase().contains(q)
            val cityMatches = item.city.lowercase().contains(q)
            val addressMatches = item.address?.lowercase()?.contains(q) == true

            nameMatches || cityMatches || addressMatches
        }
        _templeList.value = filtered
    }

    fun submitNewTemple(
        token: String,
        name: String,
        city: String,
        address: String,
        contact: String,
        description: String,
        imageFile: File?
    ) {
        viewModelScope.launch {
            try {
                if (imageFile != null) {
                    val response = repository.submitTempleWithImage(
                        token,
                        name,
                        city,
                        address,
                        contact,
                        description,
                        imageFile
                    )
                    if (response.isSuccessful && response.body()?.success == true) {
                        _addTempleResult.value = "Success"
                    } else {
                        _addTempleResult.value = "Failed: ${response.message()}"
                    }
                } else {
                    _addTempleResult.value = "Image is required"
                }

            } catch (e: Exception) {
                _addTempleResult.value = "Error: ${e.message}"
            }
        }
    }



    // =====================================================================================
    //                                      CARPOOL
    // =====================================================================================
    private val _carpoolList = MutableLiveData<List<Carpool>>()
    val carpoolList: LiveData<List<Carpool>> = _carpoolList

    // Backup list for filtering
    private val _allCarpools = mutableListOf<Carpool>()

    private val _addCarpoolResult = MutableLiveData<String>()
    val addCarpoolResult: LiveData<String> = _addCarpoolResult

    fun fetchCarpools(
        source: String? = null,
        destination: String? = null,
        date: String? = null,
        ladiesOnly: Boolean? = null,
        currentUserId: String? = null, // Added param
        currentUserLocation: String? = null, // Added param
        lat: Double? = null,
        lng: Double? = null,
        radius: Int? = null,
        onlyMyRides: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                // If filters are applied, ignore cache logic for now to get fresh results
                
                val list = repository.getCarpools(source, destination, date, ladiesOnly, lat, lng, radius)
                
                // Filter out Expired Rides (Past Date/Time) & Completed/Cancelled
                var activeList = list.filter { ride ->
                    // 1. Status Check
                    if (ride.status.equals("Completed", ignoreCase = true) || 
                        ride.status.equals("Cancelled", ignoreCase = true)) {
                        return@filter false
                    }
                
                    try {
                        val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val rideDateObj = sdfDate.parse(ride.date ?: "")
                        
                        // Parse Ride Date Components
                        val rideCal = java.util.Calendar.getInstance()
                        if (rideDateObj != null) {
                            rideCal.time = rideDateObj
                        }

                        val nowCal = java.util.Calendar.getInstance()
                        
                        val rideYear = rideCal.get(java.util.Calendar.YEAR)
                        val rideMonth = rideCal.get(java.util.Calendar.MONTH)
                        val rideDay = rideCal.get(java.util.Calendar.DAY_OF_MONTH)

                        val currYear = nowCal.get(java.util.Calendar.YEAR)
                        val currMonth = nowCal.get(java.util.Calendar.MONTH)
                        val currDay = nowCal.get(java.util.Calendar.DAY_OF_MONTH)
                        
                        if (rideDateObj != null) {
                            if (rideYear < currYear) {
                                false
                            } else if (rideYear == currYear && rideMonth < currMonth) {
                                false
                            } else if (rideYear == currYear && rideMonth == currMonth && rideDay < currDay) {
                                false
                            } else if (rideYear == currYear && rideMonth == currMonth && rideDay == currDay) {
                                // SAME DAY -> Check Time
                                val timeString = ride.time ?: ""
                                var isTimePast = false
                                
                                // Robust Parsing
                                if (timeString.contains(":")) {
                                    val parts = timeString.split(":")
                                    if (parts.size >= 2) {
                                        var rHour = parts[0].trim().toIntOrNull()
                                        val mPart = parts[1].trim()
                                        val rMinStr = mPart.filter { it.isDigit() }
                                        val finalMinStr = if (rMinStr.length > 2) rMinStr.take(2) else rMinStr
                                        var rMin = finalMinStr.toIntOrNull()
                                        
                                        val amPm = if (timeString.contains("PM", ignoreCase = true)) "PM" else "AM"
                                        
                                        if (rHour != null && rMin != null) {
                                            if (amPm == "PM" && rHour < 12) rHour += 12
                                            if (amPm == "AM" && rHour == 12) rHour = 0
                                            
                                            val cHour = nowCal.get(java.util.Calendar.HOUR_OF_DAY)
                                            val cMin = nowCal.get(java.util.Calendar.MINUTE)
                                            
                                            if (rHour < cHour) isTimePast = true
                                            else if (rHour == cHour && rMin < cMin) isTimePast = true
                                        }
                                    }
                                }
                                !isTimePast
                            } else {
                                true // Future Date
                            }
                        } else {
                            true // Parse failed -> Visible
                        }
                    } catch (e: Exception) {
                        true // Error -> Visible (Safety net)
                    }
                }

                // --- NEW FILTER: Only My Rides ---
                if (onlyMyRides && currentUserId != null) {
                    activeList = activeList.filter { it.userId == currentUserId }
                }

                // SORTING: 
                // 1. My Rides
                // 2. Rides from My Location (Source matches user location)
                // 3. Date/Time (Backend order)
                
                val finalDetail = if (lat != null && lng != null) {
                    // RIGID CLIENT-SIDE SORT BY DISTANCE
                    // This ensures "Nearest" is always first, regardless of backend default sort
                    val results = FloatArray(1)
                    activeList.sortedBy { ride ->
                        val coords = ride.sourceLocation?.coordinates
                        if (coords != null && coords.size >= 2) {
                            val rLng = coords[0] // GeoJSON: [Lng, Lat]
                            val rLat = coords[1]
                            android.location.Location.distanceBetween(lat, lng, rLat, rLng, results)
                            val dist = results[0]
                            ride.distanceFromUser = dist // SAVE DISTANCE
                            dist
                        } else {
                            ride.distanceFromUser = null
                            Float.MAX_VALUE // No coords -> Bottom
                        }
                    }
                } else if (currentUserId != null || currentUserLocation != null) {
                    activeList.sortedWith(
                        compareByDescending<Carpool> { currentUserId != null && it.userId == currentUserId }
                        .thenByDescending { 
                            if (!currentUserLocation.isNullOrEmpty() && !it.source.isNullOrEmpty()) {
                                it.source.contains(currentUserLocation, ignoreCase = true)
                            } else {
                                false
                            }
                        }
                    )
                } else {
                    activeList
                }

                _carpoolList.value = finalDetail
                
                // Only update backup list if fetching ALL (no filters)
                if (source == null && destination == null && date == null && ladiesOnly == null) {
                    _allCarpools.clear()
                    _allCarpools.addAll(finalDetail)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun sortCarpools(userId: String, userLocation: String? = null) {
        val currentList = _carpoolList.value
        if (currentList != null) {
            val sorted = currentList.sortedWith(
                compareByDescending<Carpool> { it.userId == userId }
                .thenByDescending { 
                     if (!userLocation.isNullOrEmpty() && !it.source.isNullOrEmpty()) {
                         it.source.contains(userLocation, ignoreCase = true)
                     } else {
                         false
                     }
                }
            )
            _carpoolList.value = sorted
        }
    }

    // Local Filter (Deprecated in favor of API filter, but kept for simple search bar if needed)
    fun filterCarpoolsLocal(query: String) {
        val q = query.trim().lowercase()
        val filtered = _allCarpools.filter { item ->
            val driverMatches = item.driverName?.lowercase()?.contains(q) == true
            val sourceMatches = item.source?.lowercase()?.contains(q) == true
            val destMatches = item.destination?.lowercase()?.contains(q) == true
            
            driverMatches || sourceMatches || destMatches
        }
    }

    // --- Carpool Booking ---
    private val _carpoolRequestResult = MutableLiveData<String>()
    val carpoolRequestResult: LiveData<String> = _carpoolRequestResult

    fun requestSeat(token: String, carpoolId: String, name: String, contact: String, gender: String, seats: Int) {
        viewModelScope.launch {
            try {
                val response = repository.requestSeat(token, carpoolId, name, contact, gender, seats)
                if (response.isSuccessful && response.body()?.success == true) {
                    _carpoolRequestResult.value = "Request Sent Successfully"
                    fetchCarpools() // Refresh list
                } else {
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    _carpoolRequestResult.value = "Failed: $errorMsg"
                }
            } catch (e: Exception) {
                _carpoolRequestResult.value = "Error: ${e.message}"
            }
        }
    }

    // --- Notifications ---
    private val _notificationList = MutableLiveData<List<com.mycompany.jainconnect.data.models.Notification>>()
    val notificationList: LiveData<List<com.mycompany.jainconnect.data.models.Notification>> get() = _notificationList

    fun fetchNotifications(token: String) {
        viewModelScope.launch {
            try {
                val response = repository.getNotifications(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    _notificationList.value = response.body()?.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markNotificationRead(token: String, id: String) {
        viewModelScope.launch {
            try {
                val response = repository.markNotificationRead(token, id)
                if (response.isSuccessful) {
                    // Update local list to reflect read status instantly
                    _notificationList.value = _notificationList.value?.map {
                        if (it.id == id) it.copy(isRead = true) else it
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun manageRequest(token: String, carpoolId: String, requestId: String, action: String) {
        viewModelScope.launch {
             try {
                 val response = repository.manageRequest(token, carpoolId, requestId, action)
                 if (response.isSuccessful && response.body()?.success == true) {
                     _carpoolRequestResult.value = if (action == "approve") "Request Approved" else "Request Rejected"
                     fetchCarpools()
                 } else {
                     _carpoolRequestResult.value = "Operation Failed: ${response.message()}"
                 }
             } catch (e: Exception) {
                 _carpoolRequestResult.value = "Error: ${e.message}"
             }
        }
    }






    // --- Account Management ---
    private val _deleteResult = MutableLiveData<NetworkResult<ApiResponse>>()
    val deleteResult: LiveData<NetworkResult<ApiResponse>> = _deleteResult

    fun deleteAccount(token: String) {
        viewModelScope.launch {
            _deleteResult.value = NetworkResult.Loading<ApiResponse>()
            try {
                val response = repository.deleteProfile(token)
                if (response.isSuccessful && response.body() != null) {
                    _deleteResult.value = NetworkResult.Success(response.body()!!)
                } else {
                    _deleteResult.value = NetworkResult.Error(response.message())
                }
            } catch (e: Exception) {
                _deleteResult.value = NetworkResult.Error(e.message ?: "Unknown Error")
            }
        }
    }

    private val _syncResult = MutableLiveData<NetworkResult<AuthResponse>>()
    val syncResult: LiveData<NetworkResult<AuthResponse>> = _syncResult

    fun syncUser(email: String, password: String) {
        viewModelScope.launch {
            _syncResult.value = NetworkResult.Loading<AuthResponse>()
            try {
                val response = repository.fixUser(email, password)
                if (response.isSuccessful && response.body()?.success == true) {
                    _syncResult.value = NetworkResult.Success(response.body()!!)
                } else {
                    _syncResult.value =
                        NetworkResult.Error(response.body()?.message ?: response.message())
                }
            } catch (e: Exception) {
                _syncResult.value = NetworkResult.Error(e.message ?: "Unknown Error")
            }
        }
    }

    // --- Chat Notifications ---
    fun sendChatNotification(token: String, title: String, message: String) {
        viewModelScope.launch {
            try {
                // Fire and forget
                val response = repository.sendChatNotification(token, title, message)
                if (response.isSuccessful) {
                    Log.d("JainViewModel", "Notification Sent Successfully")
                } else {
                    Log.e("JainViewModel", "Failed to send notification: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("JainViewModel", "Error sending notification", e)
            }
        }
    }

    // =====================================================================================
    //                                      STORIES (JAIN LEGACY)
    // =====================================================================================
    
    fun fetchStories(context: Context) {
        viewModelScope.launch {
            try {
                val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val token = sharedPref.getString("jwt_token", "") ?: ""
                
                if (token.isNotEmpty()) {
                    val stories = repository.getStories(token)
                    _storyList.value = stories
                } else {
                     _storyList.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("JainViewModel", "fetchStories error", e)
                // _storyList.value = emptyList() // Removed to prevent wiping cache on error
            }
        }
    }

    fun likeStory(context: Context, id: String) {
        viewModelScope.launch {
            try {
                 val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                 val token = sharedPref.getString("jwt_token", "") ?: ""
                 if (token.isNotEmpty()) {
                     repository.likeStory(token, id)
                 }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Carpool Actions ---
    private val _rideActionResult = MutableLiveData<String>()
    val rideActionResult: LiveData<String> = _rideActionResult

    fun deleteRide(token: String, rideId: String) {
        viewModelScope.launch {
            try {
                val response = repository.deleteCarpool(token, rideId)
                if (response.isSuccessful) {
                    _rideActionResult.value = "Deleted"
                    fetchCarpools() // Refresh
                } else {
                    _rideActionResult.value = "Error: ${response.message()}"
                }
            } catch (e: Exception) {
                _rideActionResult.value = "Exception: ${e.message}"
            }
        }
    }
    
    fun updateRide(token: String, rideId: String, currentRide: Carpool, 
                   driver: String, source: String, dest: String, 
                   date: String, time: String, vehicle: String, 
                   seats: Int, contact: String, isLadiesOnly: Boolean,
                   sourceLat: Double? = null, sourceLng: Double? = null,
                   destLat: Double? = null, destLng: Double? = null) {
        viewModelScope.launch {
            try {
                // Construct request with updated values
                // NOTE: Using CarpoolRequest, same as Create
                val request = CarpoolRequest(
                    driverName = driver,
                    source = source,
                    destination = dest,
                    date = date,
                    time = time,
                    vehicleType = vehicle,
                    seatsAvailable = seats,
                    contactNumber = contact,
                    isLadiesOnly = isLadiesOnly,
                    sourceLat = sourceLat,
                    sourceLng = sourceLng,
                    destLat = destLat,
                    destLng = destLng
                )
                
                val response = repository.updateCarpool(token, rideId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _rideActionResult.value = "Updated"
                    fetchCarpools()
                } else {
                    _rideActionResult.value = "Update Failed: ${response.message()}"
                }
            } catch (e: Exception) {
                _rideActionResult.value = "Update Error: ${e.message}"
            }
        }
    }

}
