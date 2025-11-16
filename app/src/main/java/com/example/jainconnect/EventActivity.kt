package com.example.jainconnect

import android.content.Context
import android.os.Bundle
import android.text.Editable // <-- NAYA IMPORT
import android.text.TextWatcher // <-- NAYA IMPORT
import android.util.Log
import android.widget.Button
import android.widget.EditText // <-- NAYA IMPORT (SearchView ki jagah)
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
// import androidx.appcompat.widget.SearchView // <-- Puraana import (ab zaroorat nahi)
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

    // === YEH CHANGE HUA HAI: SearchView ki jagah EditText ===
    private lateinit var etSearchEvents: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Default (duplicate) action bar ko hide karein
        supportActionBar?.hide()

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

        // State filter (Pehle jaisa hi hai)
        val indianStates = listOf(
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa",
            "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
            "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland",
            "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
            "Uttar Pradesh", "Uttarakhand", "West Bengal",
            "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu",
            "Delhi", "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
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

        // All / Upcoming filters (Pehle jaisa hi hai)
        findViewById<Button>(R.id.buttonAll).setOnClickListener {
            viewModel.filterEvents("")
            stateButton.text = "All"
        }
        findViewById<Button>(R.id.buttonUpcoming).setOnClickListener {
            viewModel.filterUpcomingEvents()
            stateButton.text = "All"
        }


        // === YEH POORA SECTION UPDATE HUA HAI ===
        // Humne 'findViewById<SearchView>' ko 'findViewById<EditText>' se badal diya hai
        // Aur ID ko 'searchView' se 'etSearchEvents' kar diya hai
        etSearchEvents = findViewById(R.id.etSearchEvents)

        // Humne 'setOnQueryTextListener' ko 'addTextChangedListener' se badal diya hai
        etSearchEvents.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Kuch nahi karna
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Jaise hi user type karega, filterEvents call ho jaayega
                viewModel.filterEvents(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Kuch nahi karna
            }
        })
        // ===========================================
    }

    // RSVP button click callback (Pehle jaisa hi hai, bilkul sahi)
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

        // 3. Token is available, call ViewModel function
        viewModel.toggleEventRsvp(token, event._id)
    }

    /** SharedPreferences se saved JWT token nikaalta hai. */
    private fun getToken(): String? {
        // "auth_prefs" and "jwt_token" must match your LoginActivity saving logic
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }
}