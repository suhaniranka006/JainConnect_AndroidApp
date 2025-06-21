package com.example.jainconnect

import android.os.Bundle
import android.util.Log // Import Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider // Already here
import androidx.recyclerview.widget.LinearLayoutManager // Already here
import androidx.recyclerview.widget.RecyclerView // Already here
// Make sure TithiAdapter is in this package or imported correctly
// Make sure JainViewModel is in this package or imported correctly
// Make sure Tithi data class is available

class TithiActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel
    private lateinit var tithiAdapter: TithiAdapter
    private lateinit var recyclerViewTithi: RecyclerView

    private val TAG = "TithiActivity_UI" // Tag for logs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started") // Log
        setContentView(R.layout.activity_tithi)

        recyclerViewTithi = findViewById(R.id.recyclerViewTithi)
        recyclerViewTithi.layoutManager = LinearLayoutManager(this)
        Log.d(TAG, "RecyclerView and LayoutManager initialized") // Log

        // Initialize adapter
        tithiAdapter = TithiAdapter(emptyList()) // Pass an empty list initially
        recyclerViewTithi.adapter = tithiAdapter
        Log.d(TAG, "TithiAdapter initialized and set to RecyclerView") // Log

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(JainViewModel::class.java)
        Log.d(TAG, "JainViewModel initialized") // Log

        // Observe LiveData
        // Make sure your JainViewModel has a LiveData property named 'tithiList'
        // e.g., val tithiList: LiveData<List<Tithi>>
        viewModel.tithiList.observe(this) { tithis ->
            Log.d(TAG, "LiveData observed! Tithi count from ViewModel: ${tithis?.size ?: "null"}") // Log
            if (tithis != null && tithis.isNotEmpty()) {
                Log.d(TAG, "First tithi in observer: Name: ${tithis[0].name}, Date: ${tithis[0].date}") // Log
            } else if (tithis != null) { // tithis is not null but empty
                Log.d(TAG, "Tithis list from ViewModel is empty in observer.") // Log
            } else { // tithis is null
                Log.d(TAG, "Tithis list from ViewModel is NULL in observer.") // Log
            }

            // Update the adapter with the new list of tithis
            // The null check and providing an empty list is good practice
            tithiAdapter.updateData(tithis ?: emptyList())
            Log.d(TAG, "tithiAdapter.updateData() called with ${tithis?.size ?: 0} items.") // Log
        }
        Log.d(TAG, "LiveData observer set up") // Log

        // Fetch data
        Log.d(TAG, "Calling viewModel.fetchTithis()") // Log
        viewModel.fetchTithis()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart") // Log
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume") // Log
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause") // Log
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop") // Log
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy") // Log
    }
}