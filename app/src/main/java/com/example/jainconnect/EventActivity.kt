package com.example.jainconnect

import android.os.Bundle
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * EventActivity displays a list of events in a RecyclerView.
 * Supports search, state-based filtering, upcoming events, and reset all filters.
 */
class EventActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel         // ViewModel instance
    private lateinit var eventAdapter: EventAdapter      // RecyclerView adapter
    private lateinit var recyclerViewEvents: RecyclerView // RecyclerView UI component

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        // -------------------- ViewModel Setup --------------------
        viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        // -------------------- RecyclerView Setup --------------------
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents)
        recyclerViewEvents.layoutManager = LinearLayoutManager(this) // Vertical list
        eventAdapter = EventAdapter(emptyList())                    // Initially empty
        recyclerViewEvents.adapter = eventAdapter

        // -------------------- Observe LiveData --------------------
        viewModel.eventList.observe(this) { events ->
            eventAdapter.updateData(events) // Update RecyclerView when data changes
        }

        // -------------------- Fetch Initial Data --------------------
        viewModel.fetchEvents() // Fetch events from backend via ViewModel

        // -------------------- State Filter --------------------
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
        stateButton.text = "All States" // Default

        stateButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select State")
            val options = listOf("All") + indianStates
            builder.setItems(options.toTypedArray()) { _, which ->
                val choice = options[which]
                if (choice == "All") {
                    viewModel.filterEvents("")   // Show all events
                    stateButton.text = "All"
                } else {
                    viewModel.filterEventsByState(choice) // Filter by selected state
                    stateButton.text = choice
                }
            }
            builder.show()
        }

        // -------------------- All / Upcoming Filters --------------------
        findViewById<Button>(R.id.buttonAll).setOnClickListener {
            viewModel.filterEvents("") // Reset all filters
            stateButton.text = "All"
        }

        findViewById<Button>(R.id.buttonUpcoming).setOnClickListener {
            viewModel.filterUpcomingEvents() // Filter only upcoming events
            stateButton.text = "All"
        }

        // -------------------- SearchView Filter --------------------
        findViewById<SearchView>(R.id.searchView).setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterEvents(newText ?: "") // Filter events dynamically
                return true
            }
        })
    }
}
