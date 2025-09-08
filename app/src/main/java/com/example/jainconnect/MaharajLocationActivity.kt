package com.example.jainconnect

import android.os.Bundle
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Activity to display Maharaj locations in a RecyclerView.
 * Supports searching by name, city, currentSthan, and filtering.
 */
class MaharajLocationActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel
    private lateinit var maharajAdapter: MaharajAdapter
    private lateinit var recyclerViewMaharaj: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var filterButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maharaj_location)

        // ------------------- View Bindings -------------------
        recyclerViewMaharaj = findViewById(R.id.recyclerViewMaharaj)
        searchView = findViewById(R.id.searchViewMaharaj)
        filterButton = findViewById(R.id.buttonFilterMaharaj)

        // ------------------- RecyclerView Setup -------------------
        recyclerViewMaharaj.layoutManager = LinearLayoutManager(this)
        maharajAdapter = MaharajAdapter(emptyList()) // start with empty list
        recyclerViewMaharaj.adapter = maharajAdapter

        // ------------------- ViewModel Setup -------------------
        viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        // ------------------- Observe LiveData -------------------
        viewModel.filteredMaharaj.observe(this) { filteredList ->
            // Update RecyclerView whenever filtered list changes
            maharajAdapter.updateData(filteredList ?: emptyList())
        }

        // ------------------- Fetch Initial Data -------------------
        viewModel.fetchMaharaj()

        // ------------------- Search Filter -------------------
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterMaharajByQuery(newText.orEmpty())
                return true
            }
        })

        // ------------------- Filter Button -------------------
        filterButton.setOnClickListener {
            val options = arrayOf("Upcoming Dates", "Filter by City", "Show All")
            AlertDialog.Builder(this)
                .setTitle("Filter Maharaj Data")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> viewModel.filterUpcomingMaharaj() // Show Maharaj with future dates
                        1 -> showCityFilterDialog()            // Filter by City
                        2 -> viewModel.resetMaharajFilter()    // Show all Maharaj
                    }
                }
                .show()
        }
    }

    /**
     * Shows a dialog with unique cities from the Maharaj list for filtering.
     */
    private fun showCityFilterDialog() {
        val currentList = viewModel.maharajList.value ?: return

        // Get unique non-null cities, sorted alphabetically
        val cities = currentList.map { it.city }.filterNotNull().toSet().sorted().toTypedArray()

        if (cities.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Select City")
                .setItems(cities) { _, index ->
                    // Filter Maharaj by selected city
                    viewModel.filterMaharajByCity(cities[index])
                }
                .show()
        }
    }
}
