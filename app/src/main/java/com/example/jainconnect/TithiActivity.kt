package com.example.jainconnect

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TithiActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel
    private lateinit var tithiAdapter: TithiAdapter
    private lateinit var recyclerViewTithi: RecyclerView

    private val TAG = "TithiActivity_UI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")
        setContentView(R.layout.activity_tithi)

        // Init RecyclerView & Adapter
        recyclerViewTithi = findViewById(R.id.recyclerViewTithi)
        recyclerViewTithi.layoutManager = LinearLayoutManager(this)
        tithiAdapter = TithiAdapter(emptyList())
        recyclerViewTithi.adapter = tithiAdapter
        Log.d(TAG, "RecyclerView + Adapter set")

        // Init ViewModel
        viewModel = ViewModelProvider(this)[JainViewModel::class.java]
        Log.d(TAG, "ViewModel initialized")

        // Observe FULL tithiList
        viewModel.tithiList.observe(this) { tithis ->
            if (tithis != null) {
                Log.d(TAG, "Fetched tithis count: ${tithis.size}")
            }
        }

        // ✅ Observe FILTERED list (updates UI)
        viewModel.filteredTithis.observe(this) { filteredList ->
            tithiAdapter.updateData(filteredList)
            Log.d(TAG, "Filtered list observed, count = ${filteredList.size}")
        }



        // 🔍 SearchView setup
        val searchView = findViewById<SearchView>(R.id.searchViewTithi)

// Ensure searchView is focused when clicked anywhere
        searchView.setOnClickListener {
            searchView.isIconified = false
            searchView.requestFocus()
        }

// Optional: show keyboard automatically when focused
        searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(searchView.findFocus(), 0)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterTithisByQuery(newText ?: "")
                return true
            }
        })



        // 📅 Filter Button setup
        val filterButton = findViewById<Button>(R.id.buttonFilterTithi)
        filterButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Filter")
            val options = arrayOf("Next 7 Days", "Next 15 Days", "Show All")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.filterTithisByDays(7)
                    1 -> viewModel.filterTithisByDays(15)
                    2 -> viewModel.filterTithisByDays(0)
                }
            }
            builder.show()
        }

        // 📦 Fetch data initially
        viewModel.fetchTithis()
    }
}
