package com.example.jainconnect

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.jainconnect.Event
import com.example.jainconnect.R
import com.example.jainconnect.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventAdapter(
    private var eventList: List<Event>
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEventName: TextView = itemView.findViewById(R.id.tvEventName)
        val tvEventDateTime: TextView = itemView.findViewById(R.id.tvEventDateTime)
        val tvEventLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        val tvEventDescription: TextView = itemView.findViewById(R.id.tvEventDescription)
        val btnSetReminder: Button = itemView.findViewById(R.id.btnSetReminder)
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

        holder.btnSetReminder.setOnClickListener {
            try {
                scheduleEventReminder(holder.itemView.context, event)
                Toast.makeText(holder.itemView.context, "Reminder set for ${event.name}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(holder.itemView.context, "Failed to set reminder: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        // ❌ Disable and hide the reminder button completely
        holder.btnSetReminder.visibility = View.GONE
    }

    override fun getItemCount(): Int = eventList.size

    fun updateData(newEventList: List<Event>) {
        this.eventList = newEventList
        notifyDataSetChanged()
    }
}

// Helper function outside adapter
fun scheduleEventReminder(context: Context, event: Event) {
    // Parse date safely
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val eventDate = try {
        sdf.parse(event.date)
    } catch (e: Exception) {
        null
    } ?: throw IllegalArgumentException("Invalid date format: ${event.date}")

    // Schedule one day before at test time
    val calendar = Calendar.getInstance().apply {
        time = eventDate
        add(Calendar.DAY_OF_YEAR, -1)
        set(Calendar.HOUR_OF_DAY, 16)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    // Build intent for broadcast
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("title", event.name)
        putExtra("location", event.location)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        event.date.hashCode(), // unique request code
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )


    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExact(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}
