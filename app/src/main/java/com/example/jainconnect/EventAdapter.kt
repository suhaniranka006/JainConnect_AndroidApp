package com.example.jainconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter class for displaying Event items in a RecyclerView.
 * Each item displays name, date/time, location, and optional description.
 */
class EventAdapter(
    private var eventList: List<Event> // List of events to display
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    /**
     * ViewHolder class for Event items.
     * Holds references to the views for each item in the RecyclerView.
     */
    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEventName: TextView = itemView.findViewById(R.id.tvEventName)
        val tvEventDateTime: TextView = itemView.findViewById(R.id.tvEventDateTime)
        val tvEventLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        val tvEventDescription: TextView = itemView.findViewById(R.id.tvEventDescription)
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false) // Inflate item_event.xml
        return EventViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     */
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position] // Get the data for this position

        // Bind event data to views
        holder.tvEventName.text = event.name
        holder.tvEventDateTime.text = event.date
        holder.tvEventLocation.text = event.location

        // Handle optional description
        if (!event.description.isNullOrEmpty()) {
            holder.tvEventDescription.text = event.description
            holder.tvEventDescription.visibility = View.VISIBLE
        } else {
            holder.tvEventDescription.visibility = View.GONE
        }
    }

    /**
     * Returns the total number of items in the adapter.
     */
    override fun getItemCount(): Int = eventList.size

    /**
     * Updates the list of events in the adapter and notifies RecyclerView to refresh.
     * @param newEventList The new list of events to display.
     */
    fun updateData(newEventList: List<Event>) {
        this.eventList = newEventList
        notifyDataSetChanged() // Consider DiffUtil for better performance
    }
}
