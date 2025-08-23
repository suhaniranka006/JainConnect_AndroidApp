package com.example.jainconnect

import android.os.Bundle
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MaharajLocationActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel
    private lateinit var maharajAdapter: MaharajAdapter
    private lateinit var recyclerViewMaharaj: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var filterButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maharaj_location)

        // View bindings
        recyclerViewMaharaj = findViewById(R.id.recyclerViewMaharaj)
        searchView = findViewById(R.id.searchViewMaharaj)
        filterButton = findViewById(R.id.buttonFilterMaharaj)

        recyclerViewMaharaj.layoutManager = LinearLayoutManager(this)
        maharajAdapter = MaharajAdapter(emptyList())
        recyclerViewMaharaj.adapter = maharajAdapter

        // ViewModel
        viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        // Observe LiveData
        viewModel.filteredMaharaj.observe(this) { filteredList ->
            maharajAdapter.updateData(filteredList ?: emptyList())
        }

        // Fetch initial data
        viewModel.fetchMaharaj()

        // Search Filter
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterMaharajByQuery(newText.orEmpty())
                return true
            }
        })

        // Filter Button
        filterButton.setOnClickListener {
            val options = arrayOf("Upcoming Dates", "Filter by City", "Show All")
            AlertDialog.Builder(this)
                .setTitle("Filter Maharaj Data")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> viewModel.filterUpcomingMaharaj()
                        1 -> showCityFilterDialog()
                        2 -> viewModel.resetMaharajFilter()
                    }
                }
                .show()
        }
    }

    private fun showCityFilterDialog() {
        val currentList = viewModel.maharajList.value ?: return
        val cities = currentList.map { it.city }.filterNotNull().toSet().sorted().toTypedArray()

        if (cities.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Select City")
                .setItems(cities) { _, index ->
                    viewModel.filterMaharajByCity(cities[index])
                }
                .show()
        }
    }
}
