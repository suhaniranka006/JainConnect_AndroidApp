package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Event

// === STEP 1: Click Listener Interface ===
// Activity/Fragment will implement this to respond to RSVP clicks
interface OnRsvpButtonClickListener {
    fun onRsvpClick(event: Event)
}

class EventAdapter(
    private var eventList: List<Event>,
    private val rsvpClickListener: OnRsvpButtonClickListener
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivEventImage: android.widget.ImageView = itemView.findViewById(R.id.ivEventImage)
        val tvDateDay: TextView = itemView.findViewById(R.id.tvDateDay)
        val tvDateMonth: TextView = itemView.findViewById(R.id.tvDateMonth)
        val tvEventName: TextView = itemView.findViewById(R.id.tvEventName)
        val tvEventTime: TextView = itemView.findViewById(R.id.tvEventTime)
        val tvEventLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        val tvEventDescription: TextView = itemView.findViewById(R.id.tvEventDescription)
        val tvRsvpCount: TextView = itemView.findViewById(R.id.tvRsvpCount)
        val btnRsvp: MaterialButton = itemView.findViewById(R.id.btnRsvp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]

        // Load Image (Banner)
        if (!event.image.isNullOrEmpty()) {
             holder.ivEventImage.visibility = View.VISIBLE
             com.bumptech.glide.Glide.with(holder.itemView.context)
                 .load(event.image)
                 .centerCrop()
                 .placeholder(R.drawable.bg_gradient_header) // Placeholder
                 .into(holder.ivEventImage)
        } else {
             holder.ivEventImage.visibility = View.GONE
        }

        holder.tvEventName.text = event.name
        holder.tvEventTime.text = event.time ?: "--:--"
        holder.tvEventLocation.text = event.location

        // Parse Date for Date Box (Format YYYY-MM-DD)
        try {
            val parts = event.date.split("-", " ", "/")
            if (parts.size >= 3) {
                 var day = parts[2]
                 var month = parts[1]
                 
                 if (parts[0].length == 4) { // YYYY-MM-DD
                     day = parts[2]
                     month = getMonthName(parts[1].toIntOrNull() ?: 1)
                 } else { // DD-MM-YYYY
                     day = parts[0]
                     month = getMonthName(parts[1].toIntOrNull() ?: 1)
                 }
                 
                 holder.tvDateDay.text = day
                 holder.tvDateMonth.text = month
            } else {
                holder.tvDateDay.text = "--"
                holder.tvDateMonth.text = "DATE"
            }
        } catch (e: Exception) {
            holder.tvDateDay.text = "Evt"
            holder.tvDateMonth.text = "DATE"
        }

        if (!event.description.isNullOrEmpty()) {
            holder.tvEventDescription.text = event.description
            holder.tvEventDescription.visibility = View.VISIBLE
        } else {
            holder.tvEventDescription.visibility = View.GONE
        }

        holder.tvRsvpCount.text = "${event.rsvpCount} Going"

        holder.btnRsvp.setOnClickListener {
            rsvpClickListener.onRsvpClick(event)
        }
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "JAN" 2 -> "FEB" 3 -> "MAR" 4 -> "APR" 5 -> "MAY" 6 -> "JUN"
            7 -> "JUL" 8 -> "AUG" 9 -> "SEP" 10 -> "OCT" 11 -> "NOV" 12 -> "DEC"
            else -> "MTH"
        }
    }

    override fun getItemCount(): Int = eventList.size

    fun updateData(newEventList: List<Event>) {
        this.eventList = newEventList
        notifyDataSetChanged()
    }
}