package com.mycompany.jainconnect

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


        viewModel.filteredMaharaj.observe(this) {
            maharajAdapter.updateData(it ?: emptyList())
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
