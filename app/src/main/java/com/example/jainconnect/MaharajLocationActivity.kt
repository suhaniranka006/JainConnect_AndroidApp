package com.example.jainconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// Import MaharajAdapter, Maharaj data class, JainViewModel

class MaharajLocationActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel
    private lateinit var maharajAdapter: MaharajAdapter // You need to create MaharajAdapter
    private lateinit var recyclerViewMaharaj: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maharaj_location)

        recyclerViewMaharaj = findViewById(R.id.recyclerViewMaharaj)
        recyclerViewMaharaj.layoutManager = LinearLayoutManager(this)

        // Initialize MaharajAdapter (Make sure MaharajAdapter.kt exists)
        maharajAdapter = MaharajAdapter(emptyList()) // MaharajAdapter needs an updateData method
        recyclerViewMaharaj.adapter = maharajAdapter

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(JainViewModel::class.java)

        // Observe LiveData for maharaj locations
        viewModel.maharajList.observe(this) { maharajs -> // Make sure maharajList LiveData exists in JainViewModel
            maharajAdapter.updateData(maharajs ?: emptyList())
        }

        // Fetch maharaj location data
        viewModel.fetchMaharaj() // Make sure fetchMaharajs() method exists in JainViewModel
    }
}