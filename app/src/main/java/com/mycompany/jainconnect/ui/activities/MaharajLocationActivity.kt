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

/**
 * Activity to display Maharaj locations in a RecyclerView.
 * Supports searching by name, city, currentSthan, and filtering.
 */
@AndroidEntryPoint
class MaharajLocationActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()
    private lateinit var maharajAdapter: MaharajAdapter
    private lateinit var recyclerViewMaharaj: RecyclerView
    private lateinit var buttonSelectCity: Button

    private var selectedCity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maharaj_location)

        // 1. Initialize Views
        recyclerViewMaharaj = findViewById(R.id.recyclerViewMaharaj)
        buttonSelectCity = findViewById(R.id.buttonSelectCity)

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

        // 5. Setup City Filter Button
        buttonSelectCity.setOnClickListener {
            val cities = arrayOf(
                "Mumbai", "Delhi", "Bangalore", "Pune", "Chennai",
                "Hyderabad", "Kolkata", "Jaipur", "Lucknow", "Ahmedabad"
            )
            AlertDialog.Builder(this)
                .setTitle("Select City")
                .setItems(cities) { _, index ->
                    selectedCity = cities[index]
                    buttonSelectCity.text = "🏙️ $selectedCity"
                    applyFilters()
                }
                .show()
        }

        // 6. ✅ FIXED: Floating Action Button logic moved INSIDE onCreate
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddMaharaj)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddMaharajActivity::class.java))
        }
    }

    private fun applyFilters() {
        if (selectedCity != null) {
            viewModel.filterByCity(selectedCity!!)
        } else {
            viewModel.resetFilters()
        }
    }
}