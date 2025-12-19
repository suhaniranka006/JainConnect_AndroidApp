package com.mycompany.jainconnect.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.CarpoolAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.Context
import android.view.View

@AndroidEntryPoint
class CarpoolActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()
    private lateinit var adapter: CarpoolAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carpool)

        // Setup Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup Validations / Initial State...

        // Setup RecyclerView
        val rvCarpools = findViewById<RecyclerView>(R.id.rvCarpools)
        adapter = CarpoolAdapter(emptyList())
        rvCarpools.layoutManager = LinearLayoutManager(this)
        rvCarpools.adapter = adapter

        // --- CACHE LOAD ---
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val cachedData = sharedPref.getString("cached_carpools", null)
        if (cachedData != null) {
            val type = object : TypeToken<List<com.mycompany.jainconnect.data.models.Carpool>>() {}.type
            val list: List<com.mycompany.jainconnect.data.models.Carpool> = gson.fromJson(cachedData, type)
            if (list.isNotEmpty()) {
                adapter.updateList(list)
            }
        }
        // ------------------

        // Observe Data
        viewModel.carpoolList.observe(this) { carpools ->
            adapter.updateList(carpools)

            // --- CACHE SAVE ---
            if (carpools.isNotEmpty()) {
                val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val gson = Gson()
                val json = gson.toJson(carpools)
                sharedPref.edit().putString("cached_carpools", json).apply()
            }
            // ------------------
        }

        // Fetch Data
        viewModel.fetchCarpools()

        // Setup FAB
        val fab = findViewById<FloatingActionButton>(R.id.fabAddRide)
        fab.setOnClickListener {
            startActivity(Intent(this, AddCarpoolActivity::class.java))
        }

        // Setup Search
        val etSearch = findViewById<EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterCarpools(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchCarpools()
    }
}
