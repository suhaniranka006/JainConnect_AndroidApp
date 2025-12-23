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

    // Callback for save
    private var onSaveClickListener: ((Event) -> Unit)? = null
    private var savedIds: Set<String> = emptySet()

    fun setOnSaveClickListener(listener: (Event) -> Unit) {
        onSaveClickListener = listener
    }
    
    fun updateSavedIds(newSavedIds: Set<String>) {
        this.savedIds = newSavedIds
        notifyDataSetChanged()
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivEventImage: android.widget.ImageView = itemView.findViewById(R.id.ivEventImage)
        val tvEventName: TextView = itemView.findViewById(R.id.tvEventName)
        val tvEventTime: TextView = itemView.findViewById(R.id.tvEventTime)
        val tvEventLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        val tvEventDateLinear: TextView? = itemView.findViewById(R.id.tvEventDateLinear) // Nullable as it might not be in all layouts
        
        // Old views (Keep ref if layout uses them, otherwise can remove)
        val tvDateDay: TextView? = itemView.findViewById(R.id.tvDateDay)
        val tvDateMonth: TextView? = itemView.findViewById(R.id.tvDateMonth)
        
        val tvEventDescription: TextView = itemView.findViewById(R.id.tvEventDescription)
        val tvRsvpCount: TextView = itemView.findViewById(R.id.tvRsvpCount)
        val btnRsvp: MaterialButton = itemView.findViewById(R.id.btnRsvp)
        val btnSave: android.widget.ImageView = itemView.findViewById(R.id.btnSave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]

        // Load Image (Square)
        if (!event.image.isNullOrEmpty()) {
             holder.ivEventImage.visibility = View.VISIBLE
             com.bumptech.glide.Glide.with(holder.itemView.context)
                 .load(event.image)
                 .centerCrop() // Back to centerCrop for square thumbnail
                 .placeholder(R.drawable.bg_gradient_header)
                 .into(holder.ivEventImage)
        } else {
             holder.ivEventImage.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.tvEventName.text = event.name
        holder.tvEventTime.text = event.time ?: "--:--"
        holder.tvEventLocation.text = event.location

        // Bind Date (Start Date only as per request)
        if (holder.tvEventDateLinear != null) {
            val dateText = event.startDate ?: event.date ?: "Date N/A"
            holder.tvEventDateLinear.text = dateText
        }
        
        // Hide/Show Description (Usually hidden in compact visual, but if layout has it gone, it's fine)
        holder.tvEventDescription.visibility = View.GONE 

        holder.tvRsvpCount.text = "${event.rsvpCount} Going"

        holder.btnRsvp.setOnClickListener {
            rsvpClickListener.onRsvpClick(event)
        }
        
        // Save Button State
        val isSaved = savedIds.contains(event._id)
        if (isSaved) {
            holder.btnSave.setImageResource(R.drawable.ic_bookmark_filled)
            holder.btnSave.setColorFilter(null)
        } else {
            holder.btnSave.setImageResource(R.drawable.ic_bookmark_border)
            holder.btnSave.setColorFilter(null)
        }
        
        holder.btnSave.setOnClickListener {
            onSaveClickListener?.invoke(event)
            
            // Optimistic Update
            val isCurrentlySaved = savedIds.contains(event._id)
            if (isCurrentlySaved) {
                savedIds = savedIds - event._id
                holder.btnSave.setImageResource(R.drawable.ic_bookmark_border)
            } else {
                savedIds = savedIds + event._id
                holder.btnSave.setImageResource(R.drawable.ic_bookmark_filled)
            }
        }

        // Open Detail Screen on Item Click
        holder.itemView.setOnClickListener {
            val intent = android.content.Intent(holder.itemView.context, com.mycompany.jainconnect.ui.activities.EventDetailActivity::class.java)
            intent.putExtra("EXTRA_EVENT", event)
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "JAN"
            2 -> "FEB"
            3 -> "MAR"
            4 -> "APR"
            5 -> "MAY"
            6 -> "JUN"
            7 -> "JUL"
            8 -> "AUG"
            9 -> "SEP"
            10 -> "OCT"
            11 -> "NOV"
            12 -> "DEC"
            else -> "MTH"
        }
    }

    override fun getItemCount(): Int = eventList.size

    fun updateData(newEventList: List<Event>) {
        this.eventList = newEventList
        notifyDataSetChanged()
    }
}