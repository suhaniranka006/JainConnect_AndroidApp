package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Event

class RemindersAdapter(
    private var events: List<Event>
) : RecyclerView.Adapter<RemindersAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvEventDate)
        // val tvStatus: TextView = itemView.findViewById(R.id.tvReminderStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder_event, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val event = events[position]
        holder.tvTitle.text = event.name
        holder.tvDate.text = "${event.date} | ${event.time ?: "All Day"}"
    }

    override fun getItemCount() = events.size

    fun updateList(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}
