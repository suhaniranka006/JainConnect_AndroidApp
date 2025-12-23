package com.mycompany.jainconnect.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton // Import added

import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Event
import com.mycompany.jainconnect.ui.adapters.EventAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import com.mycompany.jainconnect.ui.adapters.OnRsvpButtonClickListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@AndroidEntryPoint
class EventActivity : AppCompatActivity(), OnRsvpButtonClickListener {

    private val viewModel: JainViewModel by viewModels()
    
    @javax.inject.Inject
    lateinit var savedRepository: com.mycompany.jainconnect.data.repository.SavedRepository

    private lateinit var eventAdapter: EventAdapter
    private lateinit var recyclerViewEvents: RecyclerView
    private lateinit var etSearchEvents: EditText

    private lateinit var shimmerViewContainer: com.facebook.shimmer.ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_events)

        shimmerViewContainer = findViewById(R.id.shimmerViewContainer)
        shimmerViewContainer.startShimmer() // Start animation

        // --- PRELOAD SAVED STATE (Synchronous) ---
        val initialSavedIds = savedRepository.getSavedIds(com.mycompany.jainconnect.data.repository.SavedRepository.KEY_EVENTS)
        // -----------------------------------------

        recyclerViewEvents = findViewById(R.id.recyclerViewEvents)
        recyclerViewEvents.layoutManager = LinearLayoutManager(this)
        eventAdapter = EventAdapter(emptyList(), this)
        eventAdapter.updateSavedIds(initialSavedIds) // Set initial state
        
        // Handle Save Click
        eventAdapter.setOnSaveClickListener { event ->
             viewModel.toggleSaveState(event._id, com.mycompany.jainconnect.data.repository.SavedRepository.KEY_EVENTS)
        }

        recyclerViewEvents.adapter = eventAdapter

        // Observe LiveData to keep UI in sync if changed elsewhere
        viewModel.savedEvents.observe(this) { savedList ->
             val ids = savedList.map { it._id }.toSet()
             eventAdapter.updateSavedIds(ids)
        }
        
        // Fetch latest saved data
        viewModel.fetchSavedEvents()

        // --- CACHE LOAD ---
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val cachedEvents = sharedPref.getString("cached_events", null)
        if (cachedEvents != null) {
            val type = object : TypeToken<List<Event>>() {}.type
            try {
                val list: List<Event> = gson.fromJson(cachedEvents, type)
                if (list.isNotEmpty()) {
                    eventAdapter.updateData(list)
                    shimmerViewContainer.stopShimmer()
                    shimmerViewContainer.visibility = android.view.View.GONE
                    recyclerViewEvents.visibility = android.view.View.VISIBLE
                }
            } catch (e: Exception) {
               Log.e("EventActivity", "Cache Load Failed", e)
            }
        }
        // ------------------

        // --- ADDED: Floating Action Button Logic ---
        val fabAddEvent = findViewById<FloatingActionButton>(R.id.fabAddEvent)
        fabAddEvent.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }
        // -------------------------------------------

        viewModel.eventList.observe(this) { events ->
            // Stop and Hide Shimmer
            shimmerViewContainer.stopShimmer()
            shimmerViewContainer.visibility = android.view.View.GONE
            recyclerViewEvents.visibility = android.view.View.VISIBLE

            eventAdapter.updateData(events)

            // --- CACHE SAVE ---
            if (events.isNotEmpty()) {
                val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val gson = Gson()
                val json = gson.toJson(events)
                sharedPref.edit().putString("cached_events", json).apply()
            }
            // ------------------
        }

        viewModel.rsvpResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "RSVP updated!", Toast.LENGTH_SHORT).show()
                viewModel.fetchEvents()
            } else {
                Toast.makeText(this, "Failed to update RSVP. Try again.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.fetchEvents()

        findViewById<Button>(R.id.buttonAll).setOnClickListener {
            viewModel.filterEvents("")
        }
        findViewById<Button>(R.id.buttonUpcoming).setOnClickListener {
            viewModel.filterUpcomingEvents()
        }

        etSearchEvents = findViewById(R.id.etSearchEvents)
        etSearchEvents.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterEvents(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })


    }

    // Location Logic


    override fun onRsvpClick(event: Event) {
        Log.d("EventActivity", "RSVP button clicked for event: ${event.name} (ID: ${event._id})")
        val token = getToken()
        if (token == null) {
            Toast.makeText(this, "You must be logged in to RSVP", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, LoginActivity::class.java))
            return
        }
        viewModel.toggleEventRsvp(token, event._id)
    }

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }
}