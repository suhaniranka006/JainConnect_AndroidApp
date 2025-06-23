package com.example.jainconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jainconnect.R

class EventAdapter(
    private var eventList: List<Event>
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEventName: TextView = itemView.findViewById(R.id.tvEventName)
        val tvEventDateTime: TextView = itemView.findViewById(R.id.tvEventDateTime)
        val tvEventLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        val tvEventDescription: TextView = itemView.findViewById(R.id.tvEventDescription)
        // ❌ No reminder button anymore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.tvEventName.text = event.name
        holder.tvEventDateTime.text = event.date
        holder.tvEventLocation.text = event.location

        if (!event.description.isNullOrEmpty()) {
            holder.tvEventDescription.text = event.description
            holder.tvEventDescription.visibility = View.VISIBLE
        } else {
            holder.tvEventDescription.visibility = View.GONE
        }

        // ❌ No reminder button, so nothing to hide
    }

    override fun getItemCount(): Int = eventList.size

    fun updateData(newEventList: List<Event>) {
        this.eventList = newEventList
        notifyDataSetChanged()
    }
}
