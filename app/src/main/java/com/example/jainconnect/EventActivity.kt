package com.example.jainconnect

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * EventActivity displays a list of events in a RecyclerView.
 * Supports search, state-based filtering, upcoming events, and reset all filters.
 * Implements OnRsvpButtonClickListener to handle "I'm Going" clicks.
 */
class EventActivity : AppCompatActivity(), OnRsvpButtonClickListener {

    private lateinit var viewModel: JainViewModel
    private lateinit var eventAdapter: EventAdapter
    private lateinit var recyclerViewEvents: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        // ViewModel Setup
        viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        // RecyclerView Setup
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents)
        recyclerViewEvents.layoutManager = LinearLayoutManager(this)

        // Adapter with RSVP click listener
        eventAdapter = EventAdapter(emptyList(), this)
        recyclerViewEvents.adapter = eventAdapter

        // Observe event list
        viewModel.eventList.observe(this) { events ->
            eventAdapter.updateData(events)
        }

        // RSVP result observer
        viewModel.rsvpResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "RSVP updated!", Toast.LENGTH_SHORT).show()
                viewModel.fetchEvents() // Refresh data to show updated count
            } else {
                Toast.makeText(this, "Failed to update RSVP. Try again.", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch initial data
        viewModel.fetchEvents()

        // State filter
        val indianStates = listOf(
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
            "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
            "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
            "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
            "Uttar Pradesh", "Uttarakhand", "West Bengal",
            "Andaman and Nicobar Islands", "Chandigarh",
            "Dadra and Nagar Haveli and Daman and Diu", "Delhi",
            "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
        )

        val stateButton = findViewById<Button>(R.id.buttonStateFilter)
        stateButton.text = "All States"

        stateButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select State")
            val options = listOf("All") + indianStates
            builder.setItems(options.toTypedArray()) { _, which ->
                val choice = options[which]
                if (choice == "All") {
                    viewModel.filterEvents("")
                    stateButton.text = "All"
                } else {
                    viewModel.filterEventsByState(choice)
                    stateButton.text = choice
                }
            }
            builder.show()
        }

        // All / Upcoming filters
        findViewById<Button>(R.id.buttonAll).setOnClickListener {
            viewModel.filterEvents("")
            stateButton.text = "All"
        }

        findViewById<Button>(R.id.buttonUpcoming).setOnClickListener {
            viewModel.filterUpcomingEvents()
            stateButton.text = "All"
        }

        // SearchView filter
        findViewById<SearchView>(R.id.searchView).setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterEvents(newText ?: "")
                return true
            }
        })
    }

    // === UPDATED RSVP button click callback ===
    override fun onRsvpClick(event: Event) {
        Log.d("EventActivity", "RSVP button clicked for event: ${event.name} (ID: ${event._id})")

        // 1. Fetch token from SharedPreferences
        val token = getToken()

        // 2. If no token, show error and optionally redirect to login
        if (token == null) {
            Toast.makeText(this, "You must be logged in to RSVP", Toast.LENGTH_SHORT).show()
            // Optional: Redirect to login screen
            // startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        // 3. Token is available, call ViewModel function (implement this in your ViewModel)
        viewModel.toggleEventRsvp(token, event._id)
    }

    /** SharedPreferences se saved JWT token nikaalta hai. */
    private fun getToken(): String? {
        // "auth_prefs" and "jwt_token" must match your LoginActivity saving logic
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }
}
