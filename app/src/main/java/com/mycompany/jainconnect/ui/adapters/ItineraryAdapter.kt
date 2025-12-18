package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.ItineraryDay

class ItineraryAdapter(private val days: List<ItineraryDay>) :
    RecyclerView.Adapter<ItineraryAdapter.DayViewHolder>() {

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDayHeader: TextView = itemView.findViewById(R.id.tvDayHeader)
        val tvActivities: TextView = itemView.findViewById(R.id.tvActivities)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_itinerary_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.tvDayHeader.text = "Day ${day.day} ${day.title?.let { "- $it" } ?: ""}"
        
        val activityText = StringBuilder()
        day.activities.forEach { act ->
            activityText.append("• ${act.name} (${act.time ?: ""})\n")
        }
        holder.tvActivities.text = activityText.toString().trim()
    }

    override fun getItemCount(): Int = days.size
}
