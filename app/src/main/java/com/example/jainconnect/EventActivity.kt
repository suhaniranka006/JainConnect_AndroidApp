package com.example.jainconnect

import android.os.Bundle
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class EventActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel
    private lateinit var eventAdapter: EventAdapter
    private lateinit var recyclerViewEvents: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        // Setup RecyclerView and Adapter
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents)
        recyclerViewEvents.layoutManager = LinearLayoutManager(this)
        eventAdapter = EventAdapter(emptyList())
        recyclerViewEvents.adapter = eventAdapter

        // Observe events and update adapter
        viewModel.eventList.observe(this) { events ->
            eventAdapter.updateData(events)
        }

        // Fetch events first
        viewModel.fetchEvents()

        // State filter setup
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
        // Default to show all events
        stateButton.text = "All"

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

        // All button resets filters
        findViewById<Button>(R.id.buttonAll).setOnClickListener {
            viewModel.filterEvents("")
            stateButton.text = "All"
        }

        // Upcoming button filter
        findViewById<Button>(R.id.buttonUpcoming).setOnClickListener {
            viewModel.filterUpcomingEvents()
            stateButton.text = "All"
        }

        // SearchView filter
        findViewById<SearchView>(R.id.searchView).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterEvents(newText ?: "")
                return true
            }
        })
    }
}
