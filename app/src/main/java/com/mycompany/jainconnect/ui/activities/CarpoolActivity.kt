package com.mycompany.jainconnect.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.CarpoolAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.Context
import android.view.View
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager

@AndroidEntryPoint
class CarpoolActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()
    private lateinit var adapter: CarpoolAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var etFrom: com.google.android.material.textfield.TextInputEditText
    private lateinit var etTo: com.google.android.material.textfield.TextInputEditText
    private lateinit var etDateFilter: com.google.android.material.textfield.TextInputEditText
    private lateinit var chipLadiesOnly: com.google.android.material.chip.Chip
    private lateinit var tabLayout: com.google.android.material.tabs.TabLayout
    private lateinit var btnClearFilters: android.widget.Button

    private var currentUserIdStr: String = ""
    private var currentUserLocation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carpool)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup Validations / Initial State...

        // Setup RecyclerView
        val rvCarpools = findViewById<RecyclerView>(R.id.rvCarpools)
        
        // Initialize SharedPrefs & Token & Location
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", "") ?: ""
        currentUserIdStr = sharedPref.getString("user_id", "") ?: ""
        currentUserLocation = sharedPref.getString("user_location", null)

        adapter = CarpoolAdapter(
            carpools = emptyList(),
            currentUserId = currentUserIdStr,
            onCallClick = { contact ->
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = android.net.Uri.parse("tel:$contact")
                startActivity(intent)
            },
            onEditClick = { ride ->
                val intent = Intent(this, AddCarpoolActivity::class.java)
                intent.putExtra("IS_EDIT_MODE", true)
                intent.putExtra("RIDE_DATA", ride)
                startActivity(intent)
            },
            onDeleteClick = { ride ->
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Ride")
                    .setMessage("Are you sure you want to delete this ride?")
                    .setPositiveButton("Delete") { _, _ ->
                         val rideId = ride.id ?: ride._id
                         if (rideId != null) {
                             viewModel.deleteRide(token, rideId)
                         }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        rvCarpools.layoutManager = LinearLayoutManager(this)
        rvCarpools.adapter = adapter
        
        // Observe Delete/Update Result
        viewModel.rideActionResult.observe(this) { result ->
            android.widget.Toast.makeText(this, result, android.widget.Toast.LENGTH_SHORT).show()
        }

        // --- CACHE LOAD ---
        
        val gson = Gson()
        val cachedData = sharedPref.getString("cached_carpools", null)
        if (cachedData != null) {
            val type = object : TypeToken<List<com.mycompany.jainconnect.data.models.Carpool>>() {}.type
            try {
                val list: List<com.mycompany.jainconnect.data.models.Carpool> = gson.fromJson(cachedData, type)
                if (list.isNotEmpty()) {
                    adapter.updateList(list)
                }
            } catch (e: Exception) {
                // Ignore cache error
            }
        }
        // ------------------

        // Observe User Profile to get ID & Location
        viewModel.userProfile.observe(this) { user ->
            if (user?.id != null) {
                // SAVE ID/Location for next time (Fixes delay issue)
                sharedPref.edit()
                    .putString("user_id", user.id)
                    .putString("user_location", user.location)
                    .apply()
                
                currentUserIdStr = user.id
                currentUserLocation = user.location
                
                adapter.updateUserId(user.id)
                
                // Now that ID is confirmed, Fetch/Sort
                // If list is already there, just sort. If we waited, fetch.
                if (adapter.itemCount > 0) {
                     viewModel.sortCarpools(user.id, user.location)
                } else {
                     viewModel.fetchCarpools(currentUserId = user.id, currentUserLocation = user.location)
                }
            }
        }

        // Fetch User Profile if we have token
        if (token.isNotEmpty()) {
            viewModel.fetchUserProfile(token)
        }

        viewModel.carpoolRequestResult.observe(this) { msg ->
            android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
        }

        // Observe Data
        viewModel.carpoolList.observe(this) { carpools ->
            adapter.updateList(carpools)

            // --- CACHE SAVE ---
            if (carpools.isNotEmpty()) {
                val json = gson.toJson(carpools)
                sharedPref.edit().putString("cached_carpools", json).apply()
            }
            // ------------------
        }

        // Fetch Data ONLY if we have ID or if Guest
        // If Logged In but No ID yet -> WAIT for Observer (fixes "jumping" issue)
        val isGuest = token.isEmpty()
        val hasCachedId = currentUserIdStr.isNotEmpty()
        
        if (isGuest || hasCachedId) {
             viewModel.fetchCarpools(currentUserId = currentUserIdStr, currentUserLocation = currentUserLocation)
        }

        // Setup FAB
        val fab = findViewById<FloatingActionButton>(R.id.fabAddRide)
        fab.setOnClickListener {
            startActivity(Intent(this, AddCarpoolActivity::class.java))
        }

        // Setup Search
        // Initialize Inputs (Class Properties)
        etFrom = findViewById(R.id.etFrom)
        etTo = findViewById(R.id.etTo)
        etDateFilter = findViewById(R.id.etDateFilter)
        chipLadiesOnly = findViewById(R.id.chipLadiesOnly)
        // chipNearby removed
        
        // --- TABS SETUP ---
        tabLayout = findViewById(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("Nearby Rides"))
        tabLayout.addTab(tabLayout.newTab().setText("My Rides"))
        
        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                 if (tab.position == 0) {
                     adapter.setMyRidesTab(false)
                     fetchNearbyRides()
                 } else {
                     adapter.setMyRidesTab(true)
                     viewModel.fetchCarpools(currentUserId = currentUserIdStr, currentUserLocation = currentUserLocation, onlyMyRides = true)
                 }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                 onTabSelected(tab!!)
            }
        })
        
        val btnSearch = findViewById<android.widget.Button>(R.id.btnSearch)
        btnClearFilters = findViewById(R.id.btnClearFilters)

        // Date Picker Logic
        etDateFilter.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val datePickerDialog = android.app.DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                     val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                     etDateFilter.setText(formattedDate)
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Search Button Click
        btnSearch.setOnClickListener {
            val source = etFrom.text.toString().trim().ifEmpty { null }
            val destination = etTo.text.toString().trim().ifEmpty { null }
            val date = etDateFilter.text.toString().trim().ifEmpty { null }
            val ladiesOnly = if (chipLadiesOnly.isChecked) true else null

            // Show Clear Filters button if any filter is active
            if (source != null || destination != null || date != null || ladiesOnly != null) {
                btnClearFilters.visibility = android.view.View.VISIBLE
            }

            // Decide Search Mode
            val hasTextFilter = source != null || destination != null

            if (hasTextFilter) {
                 // TEXT FILTER MODE: Strict text match, Ignore GPS (Find specific route globally)
                 // This ensures users find "Goa -> Panji" even if they are far away or ride has no coords.
                 viewModel.fetchCarpools(source, destination, date, ladiesOnly, currentUserIdStr, currentUserLocation)
            } else if (tabLayout.selectedTabPosition == 0) {
                // NEARBY MODE: No text filters -> Show sorted rides near me
                fetchNearbyRides()
            } else {
                // MY RIDES MODE
                viewModel.fetchCarpools(source, destination, date, ladiesOnly, currentUserIdStr, currentUserLocation, onlyMyRides = true)
            }
        }
        
        // Clear Filters Logic
        btnClearFilters.setOnClickListener {
            etFrom.text = null
            etTo.text = null
            etDateFilter.text = null
            chipLadiesOnly.isChecked = false
            btnClearFilters.visibility = android.view.View.GONE
            
            if (tabLayout.selectedTabPosition == 0) {
                fetchNearbyRides()
            } else {
                viewModel.fetchCarpools(currentUserId = currentUserIdStr, currentUserLocation = currentUserLocation, onlyMyRides = true)
            }
        }
    }

/*
    private fun showManageDialog(carpool: com.mycompany.jainconnect.data.models.Carpool) {
        // ... (Logic Removed for Direct Call Implementation)
    }

    private fun showRequestDialog(carpool: com.mycompany.jainconnect.data.models.Carpool) {
        // ... (Logic Removed for Direct Call Implementation)
    }
*/

    override fun onResume() {
        super.onResume()
        if (::tabLayout.isInitialized) {
            if (tabLayout.selectedTabPosition == 0) {
                 fetchNearbyRides()
            } else {
                viewModel.fetchCarpools(currentUserId = currentUserIdStr, currentUserLocation = currentUserLocation, onlyMyRides = true)
            }
        }
    }

    private fun fetchNearbyRides() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        // Try Last Known Location FIRST (Instant)
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
            if (lastLoc != null) {
                // Success with Cache
                performSearchWithLocation(lastLoc)
            } else {
                // Fallback to Fresh Location (High Accuracy)
                 fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { freshLoc ->
                        if (freshLoc != null) {
                            performSearchWithLocation(freshLoc)
                        } else {
                            android.widget.Toast.makeText(this, "Could not determine location for Nearby rides.", android.widget.Toast.LENGTH_LONG).show()
                            // Only clear if we really failed
                            adapter.updateList(emptyList()) 
                        }
                    }
                    .addOnFailureListener {
                         android.widget.Toast.makeText(this, "Failed to get location.", android.widget.Toast.LENGTH_SHORT).show()
                         adapter.updateList(emptyList())
                    }
            }
        }.addOnFailureListener {
             // Try Fresh if lastLocation fails
             fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { freshLoc ->
                    if (freshLoc != null) performSearchWithLocation(freshLoc)
                    else adapter.updateList(emptyList()) 
                }
        }
    }

    private fun performSearchWithLocation(location: android.location.Location) {
        val source = etFrom.text.toString().trim().ifEmpty { null }
        val destination = etTo.text.toString().trim().ifEmpty { null }
        val date = etDateFilter.text.toString().trim().ifEmpty { null }
        val ladiesOnly = if (chipLadiesOnly.isChecked) true else null
        
        // Using large radius (50000 km) to show ALL rides but sorted by distance (Nearest First)
        viewModel.fetchCarpools(
            source, destination, date, ladiesOnly, 
            currentUserIdStr, null, location.latitude, location.longitude, 50000
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchNearbyRides()
            } else {
                android.widget.Toast.makeText(this, "Location permission denied. Nearby filter disabled.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
