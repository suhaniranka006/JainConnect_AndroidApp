package com.mycompany.jainconnect.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.TithiAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.Context
import android.view.View
import javax.inject.Inject


/**
 * TithiActivity displays a list of Tithis in a RecyclerView.
 * It provides search and filter functionality and observes the JainViewModel for data updates.
 */
@AndroidEntryPoint
class TithiActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()         // ViewModel instance
    private lateinit var tithiAdapter: TithiAdapter      // Adapter for RecyclerView
    private lateinit var recyclerViewTithi: RecyclerView // RecyclerView UI component

    private val TAG = "TithiActivity_UI"                 // Logging tag

    @Inject
    lateinit var savedRepository: com.mycompany.jainconnect.data.repository.SavedRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")
        setContentView(R.layout.activity_tithi)

        // -------------------- RecyclerView Setup --------------------
        recyclerViewTithi = findViewById(R.id.recyclerViewTithi)
        recyclerViewTithi.layoutManager = LinearLayoutManager(this) // Vertical list
        tithiAdapter = TithiAdapter(emptyList())                   // Initially empty
        
        // --- SYNC LOADING OF SAVED STATE ---
        // Load saved IDs immediately to prevent latency/flicker
        val savedIds = savedRepository.getSavedIds(com.mycompany.jainconnect.data.repository.SavedRepository.KEY_TITHIS)
        tithiAdapter.updateSavedIds(savedIds) // Set IDs before adapter is even populated fully if needed, or right here
        // -----------------------------------
        
        recyclerViewTithi.adapter = tithiAdapter
        Log.d(TAG, "RecyclerView + Adapter set")

        // -------------------- ViewModel Setup --------------------

        Log.d(TAG, "ViewModel initialized")

        val shimmerViewContainer = findViewById<com.facebook.shimmer.ShimmerFrameLayout>(R.id.shimmerViewContainer)

        shimmerViewContainer.startShimmer()

        // --- CACHE LOAD ---
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val cachedData = sharedPref.getString("cached_tithis", null)
        if (cachedData != null) {
            val type = object : TypeToken<List<com.mycompany.jainconnect.data.models.Tithi>>() {}.type
            try {
                val list: List<com.mycompany.jainconnect.data.models.Tithi> = gson.fromJson(cachedData, type)
                if (list.isNotEmpty()) {
                    tithiAdapter.updateData(list)
                    shimmerViewContainer.stopShimmer()
                    shimmerViewContainer.visibility = View.GONE
                    recyclerViewTithi.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
               Log.e(TAG, "Cache Load Failed", e)
            }
        }
        // ------------------

        // Observe FULL tithiList for logging/debugging
        viewModel.tithiList.observe(this) { tithis ->
            if (tithis != null) {
                Log.d(TAG, "Fetched tithis count: ${tithis.size}")
            }
        }

        // Observe FILTERED tithis for updating RecyclerView UI
        viewModel.filteredTithis.observe(this) { filteredList ->
            shimmerViewContainer.stopShimmer()
            shimmerViewContainer.visibility = android.view.View.GONE
            recyclerViewTithi.visibility = android.view.View.VISIBLE

            tithiAdapter.updateData(filteredList)                // Update adapter
            Log.d(TAG, "Filtered list observed, count = ${filteredList.size}")

             // --- CACHE SAVE ---
            if (filteredList.isNotEmpty()) {
                val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val gson = Gson()
                val json = gson.toJson(filteredList)
                sharedPref.edit().putString("cached_tithis", json).apply()
            }
            // ------------------
        }
        
        // --- SAVED TITHIS OBSERVATION (Kept for updates) ---
        viewModel.savedTithis.observe(this) { savedList ->
             val ids = savedList.mapNotNull { it.id }.toSet()
             tithiAdapter.updateSavedIds(ids)
        }
        
        // Setup Save Click Listener
        tithiAdapter.setOnSaveClickListener { tithi ->
             // Safe check for ID
             val id = tithi.id ?: return@setOnSaveClickListener
             viewModel.toggleSaveState(id, com.mycompany.jainconnect.data.repository.SavedRepository.KEY_TITHIS)
        }
        
        // Fetch Saved Tithis initially (Async update)
        viewModel.fetchSavedTithis()

        // -------------------- SearchView Setup --------------------
        val searchView = findViewById<SearchView>(R.id.searchViewTithi)

        // Expand searchView when clicked anywhere
        searchView.setOnClickListener {
            searchView.isIconified = false
            searchView.requestFocus()
        }

        // Show keyboard automatically when searchView is focused
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val imm =
                    getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(searchView.findFocus(), 0)
            }
        }

        // Listen to text changes and update filtered list in ViewModel
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterTithisByQuery(newText ?: "")
                return true
            }
        })


        // -------------------- Filter Buttons Setup --------------------

// Show All
        findViewById<MaterialButton>(R.id.buttonShowAll).setOnClickListener {
            viewModel.filterTithisByDays(0) // 0 = show all
        }

// Next 7 Days
        findViewById<MaterialButton>(R.id.buttonNext7Days).setOnClickListener {
            viewModel.filterTithisByDays(7)
        }

// Next 15 Days
// Next 15 Days
        findViewById<MaterialButton>(R.id.buttonNext15Days).setOnClickListener {
            viewModel.filterTithisByDays(15)
        }

// Major Parva
        findViewById<MaterialButton>(R.id.buttonMajorParva).setOnClickListener {
            viewModel.filterMajorParva()
        }

// -------------------- Fetch Initial Data --------------------
        viewModel.fetchTithis() // Fetch tithis from backend via ViewModel

    }
}