package com.mycompany.jainconnect.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Maharaj
import com.mycompany.jainconnect.ui.adapters.MaharajAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.Context
import android.view.View
import com.google.android.material.chip.ChipGroup
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import android.widget.Toast
import android.location.Address

/**
 * Activity to display Maharaj locations in a RecyclerView.
 * Supports searching by name, city, currentSthan, and filtering.
 */
@AndroidEntryPoint
class MaharajLocationActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()
    private lateinit var maharajAdapter: MaharajAdapter
    private lateinit var recyclerViewMaharaj: RecyclerView
    private lateinit var chipGroupFilters: ChipGroup
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Permission launcher for Location
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            applyNearMeFilter()
        } else {
            Toast.makeText(this, "Location permission required for 'Near Me'", Toast.LENGTH_SHORT).show()
            // Revert chip to 'All' or previous state if needed.
             findViewById<View>(R.id.chipAll).performClick()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maharaj_location)

        // 1. Initialize Views
        recyclerViewMaharaj = findViewById(R.id.recyclerViewMaharaj)
        chipGroupFilters = findViewById(R.id.chipGroupFilters)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupFilters()

        // 2. Setup RecyclerView
        recyclerViewMaharaj.layoutManager = LinearLayoutManager(this)
        maharajAdapter = MaharajAdapter(emptyList())
        recyclerViewMaharaj.adapter = maharajAdapter

        // 3. Initialize ViewModel

        val shimmerViewContainer = findViewById<com.facebook.shimmer.ShimmerFrameLayout>(R.id.shimmerViewContainer)
        shimmerViewContainer.startShimmer()

        // --- CACHE LOAD ---
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val cachedData = sharedPref.getString("cached_maharaj", null)
        if (cachedData != null) {
            val type = object : TypeToken<List<Maharaj>>() {}.type
            val list: List<Maharaj> = gson.fromJson(cachedData, type)
            if (list.isNotEmpty()) {
                maharajAdapter.updateData(list)
                shimmerViewContainer.stopShimmer()
                shimmerViewContainer.visibility = View.GONE
                recyclerViewMaharaj.visibility = View.VISIBLE
            }
        }
        // ------------------

        // 2b. Setup Adapter Logic
        maharajAdapter.setOnSaveClickListener { maharaj ->
            viewModel.toggleSaveState(maharaj.id ?: "", com.mycompany.jainconnect.data.repository.SavedRepository.KEY_MONKS)
            // Optional: The observe below will auto-update UI if we were observing savedMonks list for IDs
            // But here we are observing filteredMaharaj list which is the main list.
            // We need to fetch saved monks to get the updated set of IDs for visual feedback
            viewModel.fetchSavedMonks()
        }
        
        // Observe Saved Monks to update icons
        viewModel.savedMonks.observe(this) { savedList ->
             val ids = savedList.mapNotNull { it.id }.toSet()
             maharajAdapter.updateSavedIds(ids)
        }
        
        // Initial fetch of saved to ensure icons are correct on load
        viewModel.fetchSavedMonks()

        viewModel.filteredMaharaj.observe(this) {
            shimmerViewContainer.stopShimmer()
            shimmerViewContainer.visibility = android.view.View.GONE
            recyclerViewMaharaj.visibility = android.view.View.VISIBLE

            maharajAdapter.updateData(it ?: emptyList())

            // --- CACHE SAVE ---
            if (!it.isNullOrEmpty()) {
                val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val gson = Gson()
                val json = gson.toJson(it)
                sharedPref.edit().putString("cached_maharaj", json).apply()
            }
            // ------------------
        }

        viewModel.fetchMaharaj()

        // 4. Setup Search functionality
        val searchView: SearchView = findViewById(R.id.searchViewMaharaj)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterBySearch(newText.orEmpty())
                return true
            }
        })



        // 6. ✅ FIXED: Floating Action Button logic moved INSIDE onCreate
        // FAB Logic removed - moved to Volunteer Panel
        
        // 7. Try to fetch location for distances (Silent)
        fetchLocationForDistance()
    }




    private fun setupFilters() {
        chipGroupFilters.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipAll -> applyFilter("ALL")
                R.id.chipOngoing -> applyFilter("ONGOING")
                R.id.chipUpcoming -> applyFilter("UPCOMING")
                R.id.chipNearMe -> checkLocationPermissionAndFilter()
            }
        }
    }

    private fun checkLocationPermissionAndFilter() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            applyNearMeFilter()
        }
    }

    private fun applyFilter(mode: String) {
        val allMonks = viewModel.filteredMaharaj.value ?: return
        val today = Date()
        
        // Helper to zero out time for fair comparison
        fun resetTime(date: Date): Date {
            val cal = java.util.Calendar.getInstance()
            cal.time = date
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            return cal.time
        }
        val todayZero = resetTime(today)

        val filteredList = when (mode) {
            "ALL" -> allMonks
            "ONGOING" -> allMonks.filter { monk ->
                try {
                    val arrDate = monk.relevantDate?.let { tryParseDate(it) } 
                                  ?: monk.arrivalDate?.let { tryParseDate(it) }
                                  // Fallback to relevantDate if arrivalDate distinct? 
                                  // Data model has relevantDate as serialized "date" and arrivalDate. 
                                  // Let's check both or prioritize arrivalDate logic.
                                  // Actually let's stick to arrivalDate if available, else relevantDate.
                    
                    val vihDate = monk.viharDate?.let { tryParseDate(it) }
                    
                    // Logic: Arrived on/before today AND (Still here OR Vihar is in future)
                    // If arrived is null, assume ongoing? Or skip? Let's assume safe skip.
                    if (arrDate == null) return@filter true // Show if date missing? Or hide? Let's show.
                    
                    val arrZero = resetTime(arrDate)
                    val logic1 = !arrZero.after(todayZero) // Arrived <= Today
                    
                    val logic2 = if (vihDate != null) {
                        val vihZero = resetTime(vihDate)
                        vihZero.after(todayZero) // Vihar > Today
                    } else {
                        true // No vihar date means indefinitely here
                    }
                    
                    logic1 && logic2
                } catch (e: Exception) { true }
            }
            "UPCOMING" -> allMonks.filter { monk ->
                try {
                     val arrDate = monk.relevantDate?.let { tryParseDate(it) } 
                                  ?: monk.arrivalDate?.let { tryParseDate(it) }
                    
                    if (arrDate == null) return@filter false
                    
                    val arrZero = resetTime(arrDate)
                    arrZero.after(todayZero) // Arrived > Today
                } catch (e: Exception) { false }
            }
            else -> allMonks
        }
        maharajAdapter.updateData(filteredList)
    }

     private fun tryParseDate(dateString: String): Date? {
        val formats = arrayOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        )
        for (format in formats) {
            try {
                return format.parse(dateString)
            } catch (e: Exception) {
                // Ignore and try next
            }
        }
        return null
    }

    private fun applyNearMeFilter() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Pass location to adapter for distance calculation (if monks have coords)
                maharajAdapter.setUserLocation(location)
                
                val allMonks = viewModel.filteredMaharaj.value ?: emptyList()
                val nearbyMonks = mutableListOf<Maharaj>()
                val geocoder = Geocoder(this, Locale.getDefault())

                 Thread {
                     allMonks.forEach { monk ->
                         // Use backend coords if available, else geocode
                         var lat = monk.latitude
                         var lng = monk.longitude
                         
                         if (lat == null || lng == null) {
                             val city = monk.city
                             if (!city.isNullOrEmpty()) {
                                 try {
                                     @Suppress("DEPRECATION")
                                     val addresses: List<Address>? = geocoder.getFromLocationName(city, 1)
                                     if (!addresses.isNullOrEmpty()) {
                                         lat = addresses[0].latitude
                                         lng = addresses[0].longitude
                                     }
                                 } catch (e: Exception) {
                                     e.printStackTrace()
                                 }
                             }
                         }

                         if (lat != null && lng != null) {
                             val results = FloatArray(1)
                             Location.distanceBetween(
                                 location.latitude, location.longitude,
                                 lat!!, lng!!,
                                 results
                             )
                             
                             // 50km = 50,000 meters
                             if (results[0] < 50000) {
                                 // Create copy with coords so Adapter can show distance
                                 val updatedMonk = monk.copy(latitude = lat, longitude = lng)
                                 nearbyMonks.add(updatedMonk)
                             }
                         }
                     }
                     
                     runOnUiThread {
                         if (nearbyMonks.isEmpty()) {
                             Toast.makeText(this, "No monks found within 50km", Toast.LENGTH_SHORT).show()
                         }
                         maharajAdapter.updateData(nearbyMonks)
                     }
                 }.start()
                 
            } else {
                Toast.makeText(this, "Current location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchLocationForDistance() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let { maharajAdapter.setUserLocation(it) }
            }
        }
    }
}