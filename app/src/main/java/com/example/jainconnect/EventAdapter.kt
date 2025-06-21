package com.example.jainconnect

// Or your adapter package: com.example.jainconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(private var eventList: List<Event>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEventName: TextView = itemView.findViewById(R.id.tvEventName)
        val tvEventDateTime: TextView = itemView.findViewById(R.id.tvEventDateTime)
        val tvEventLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        val tvEventDescription: TextView = itemView.findViewById(R.id.tvEventDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.tvEventName.text = event.name
        holder.tvEventDateTime.text = "${event.date} - ${event.time}" // Combine date and time
        holder.tvEventLocation.text = event.location

        if (!event.description.isNullOrEmpty()) {
            holder.tvEventDescription.text = event.description
            holder.tvEventDescription.visibility = View.VISIBLE
        } else {
            holder.tvEventDescription.visibility = View.GONE
        }

        // You can add an OnClickListener here if needed
        // holder.itemView.setOnClickListener { /* Handle item click */ }
    }

    override fun getItemCount() = eventList.size

    fun updateData(newEventList: List<Event>) {
        this.eventList = newEventList
        notifyDataSetChanged() // Or use DiffUtil for better performance
    }
}