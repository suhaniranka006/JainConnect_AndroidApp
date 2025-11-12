package com.example.jainconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

// === STEP 1: Click Listener Interface ===
// Activity/Fragment will implement this to respond to RSVP clicks
interface OnRsvpButtonClickListener {
    fun onRsvpClick(event: Event)
}

class EventAdapter(
    private var eventList: List<Event>,
    // === STEP 2: Pass the listener to the adapter's constructor ===
    private val rsvpClickListener: OnRsvpButtonClickListener
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    // === STEP 3: ViewHolder for Event items ===
    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEventName: TextView = itemView.findViewById(R.id.tvEventName)
        val tvEventDateTime: TextView = itemView.findViewById(R.id.tvEventDateTime)
        val tvEventTime: TextView = itemView.findViewById(R.id.tvEventTime)
        val tvEventLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        val tvEventDescription: TextView = itemView.findViewById(R.id.tvEventDescription)
        val tvRsvpCount: TextView = itemView.findViewById(R.id.tvRsvpCount)
        val btnRsvp: MaterialButton = itemView.findViewById(R.id.btnRsvp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false) // Match your item_event.xml
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]

        // === STEP 4: Bind event data ===
        holder.tvEventName.text = event.name
        holder.tvEventDateTime.text = event.date
        holder.tvEventTime.text = event.time ?: "Time not available"
        holder.tvEventLocation.text = event.location

        // Handle optional description
        if (!event.description.isNullOrEmpty()) {
            holder.tvEventDescription.text = event.description
            holder.tvEventDescription.visibility = View.VISIBLE
        } else {
            holder.tvEventDescription.visibility = View.GONE
        }

        // RSVP count: assumes Event.kt has rsvpCount property
        holder.tvRsvpCount.text = "${event.rsvpCount} people are going"

        // RSVP button
        holder.btnRsvp.setOnClickListener {
            rsvpClickListener.onRsvpClick(event)
        }
    }

    override fun getItemCount(): Int = eventList.size

    // === STEP 5: Update the adapter data ===
    fun updateData(newEventList: List<Event>) {
        this.eventList = newEventList
        notifyDataSetChanged()
        // For smoother updates, consider using DiffUtil
    }
}
