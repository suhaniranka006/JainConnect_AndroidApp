package com.mycompany.jainconnect

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

/**
 * TithiActivity displays a list of Tithis in a RecyclerView.
 * It provides search and filter functionality and observes the JainViewModel for data updates.
 */
class TithiActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel         // ViewModel instance
    private lateinit var tithiAdapter: TithiAdapter      // Adapter for RecyclerView
    private lateinit var recyclerViewTithi: RecyclerView // RecyclerView UI component

    private val TAG = "TithiActivity_UI"                 // Logging tag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")
        setContentView(R.layout.activity_tithi)

        // -------------------- RecyclerView Setup --------------------
        recyclerViewTithi = findViewById(R.id.recyclerViewTithi)
        recyclerViewTithi.layoutManager = LinearLayoutManager(this) // Vertical list
        tithiAdapter = TithiAdapter(emptyList())                   // Initially empty
        recyclerViewTithi.adapter = tithiAdapter
        Log.d(TAG, "RecyclerView + Adapter set")

        // -------------------- ViewModel Setup --------------------
        viewModel = ViewModelProvider(this)[JainViewModel::class.java]
        Log.d(TAG, "ViewModel initialized")

        // Observe FULL tithiList for logging/debugging
        viewModel.tithiList.observe(this) { tithis ->
            if (tithis != null) {
                Log.d(TAG, "Fetched tithis count: ${tithis.size}")
            }
        }

        // Observe FILTERED tithis for updating RecyclerView UI
        viewModel.filteredTithis.observe(this) { filteredList ->
            tithiAdapter.updateData(filteredList)                // Update adapter
            Log.d(TAG, "Filtered list observed, count = ${filteredList.size}")
        }

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
        findViewById<MaterialButton>(R.id.buttonNext15Days).setOnClickListener {
            viewModel.filterTithisByDays(15)
        }

// -------------------- Fetch Initial Data --------------------
        viewModel.fetchTithis() // Fetch tithis from backend via ViewModel

    }
}
