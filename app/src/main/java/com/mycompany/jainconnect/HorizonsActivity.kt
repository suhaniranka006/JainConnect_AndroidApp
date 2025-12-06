package com.mycompany.jainconnect

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HorizonsActivity : AppCompatActivity() {

    // Define ViewModel and Adapter
    private lateinit var viewModel: JainViewModel
    private lateinit var adapter: HorizonsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Make sure this matches your XML filename (activity_horizons vs activity_horizons2)
        setContentView(R.layout.activity_horizons)

        // 1. Handle Window Insets (For Edge-to-Edge display)
        // Ensure your root layout in XML has an ID (e.g., android:id="@+id/main") if you use this specific block
        // If your XML root doesn't have ID 'main', you can remove this block or add the ID to XML.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Setup Toolbar (Back Button)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 3. Setup RecyclerView (The Grid)
        val recyclerView = findViewById<RecyclerView>(R.id.rvHorizons)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Use GridLayout with 2 columns
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = HorizonsAdapter()
        recyclerView.adapter = adapter

        // 4. Initialize ViewModel
        // We use JainViewModel which already has the Repository logic inside it
        viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        // 5. Observe Data
        // Show loader initially
        progressBar.visibility = View.VISIBLE

        viewModel.horizonList.observe(this) { list ->
            // Update Adapter with new list
            adapter.submitList(list)

            // Hide ProgressBar once data is loaded
            if (list.isNotEmpty()) {
                progressBar.visibility = View.GONE
            }
        }

        // 6. Trigger the API Call
        // Default location is Jaipur. You can pass (lat, lng) here if needed.
        viewModel.fetchSunData()
    }
}
