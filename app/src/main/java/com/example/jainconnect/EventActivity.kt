package com.example.jainconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// Import EventAdapter, Event data class, JainViewModel

class EventActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel
    private lateinit var eventAdapter: EventAdapter
    private lateinit var recyclerViewEvents: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_events) // << CRITICAL: Does activity_event.xml exist and is it correct?

        // Breakpoint here
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents) // << CRITICAL: Does R.id.recyclerViewEvents exist in activity_event.xml?
        recyclerViewEvents.layoutManager = LinearLayoutManager(this)

        eventAdapter = EventAdapter(emptyList())
        recyclerViewEvents.adapter = eventAdapter

        viewModel = ViewModelProvider(this).get(JainViewModel::class.java)

        viewModel.eventList.observe(this) { events ->
            eventAdapter.updateData(events ?: emptyList())
        }

        viewModel.fetchEvents()
        // Breakpoint here
    }
}