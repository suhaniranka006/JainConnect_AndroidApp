package com.example.jainconnect

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity to display Maharaj locations in a RecyclerView.
 * Supports searching by name, city, currentSthan, and filtering.
 */
class MaharajLocationActivity : AppCompatActivity() {


    private lateinit var viewModel: JainViewModel
    private lateinit var maharajAdapter: MaharajAdapter
    private lateinit var recyclerViewMaharaj: RecyclerView
    private lateinit var buttonSelectCity: Button
    private lateinit var buttonSelectDate: Button

    private var selectedCity: String? = null
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maharaj_location)

        recyclerViewMaharaj = findViewById(R.id.recyclerViewMaharaj)
        buttonSelectCity = findViewById(R.id.buttonSelectCity)
        // buttonSelectDate = findViewById(R.id.buttonSelectDate)

        recyclerViewMaharaj.layoutManager = LinearLayoutManager(this)
        maharajAdapter = MaharajAdapter(emptyList())
        recyclerViewMaharaj.adapter = maharajAdapter

        viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        viewModel.filteredMaharaj.observe(this) {
            maharajAdapter.updateData(it ?: emptyList())
        }

        viewModel.fetchMaharaj()

        // Search functionality
        val searchView: SearchView = findViewById(R.id.searchViewMaharaj)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterBySearch(newText.orEmpty())
                return true
            }
        })


        buttonSelectCity.setOnClickListener {
            val cities = arrayOf(
                "Mumbai",
                "Delhi",
                "Bangalore",
                "Pune",
                "Chennai",
                "Hyderabad",
                "Kolkata",
                "Jaipur",
                "Lucknow",
                "Ahmedabad"
            )
            AlertDialog.Builder(this)
                .setTitle("Select City")
                .setItems(cities) { _, index ->
                    selectedCity = cities[index]
                    buttonSelectCity.text = "🏙️ $selectedCity"
                    applyFilters() // <-- filter applied here
                }
                .show()
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
