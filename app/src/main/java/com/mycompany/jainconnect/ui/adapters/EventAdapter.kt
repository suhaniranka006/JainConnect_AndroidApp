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
    // Location
    private var userLocation: android.location.Location? = null

    fun setUserLocation(location: android.location.Location) {
        userLocation = location
        notifyDataSetChanged()
    }
    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivEventImage: android.widget.ImageView = itemView.findViewById(R.id.ivEventImage)
        val tvEventName: TextView = itemView.findViewById(R.id.tvEventName)
        val tvEventTime: TextView = itemView.findViewById(R.id.tvEventTime)
        val tvEventLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        val tvEventDateLinear: TextView? = itemView.findViewById(R.id.tvEventDateLinear)
        
        // Old views
        val tvDateDay: TextView? = itemView.findViewById(R.id.tvDateDay)
        val tvDateMonth: TextView? = itemView.findViewById(R.id.tvDateMonth)
        
        val tvEventDescription: TextView = itemView.findViewById(R.id.tvEventDescription)
        val tvRsvpCount: TextView = itemView.findViewById(R.id.tvRsvpCount)
        val btnRsvp: MaterialButton = itemView.findViewById(R.id.btnRsvp)
        val btnSave: android.widget.ImageView = itemView.findViewById(R.id.btnSave)
        val tvPostDate: TextView? = itemView.findViewById(R.id.tvPostDate)
        val tvDistance: TextView? = itemView.findViewById(R.id.tvDistance)
 
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
             
             // Handl full URL vs Relative Path
             val imageUrl = if (event.image.startsWith("http")) {
                 event.image
             } else {
                 // Replace backslashes (Windows) with forward slashes
                 val cleanPath = event.image.replace("\\", "/")
                 "https://jainconnect-backened-2.onrender.com/$cleanPath"
             }

             com.bumptech.glide.Glide.with(holder.itemView.context)
                 .load(imageUrl)
                 .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade()) // Smooth Fade In
                 .centerCrop()
                 .placeholder(R.drawable.bg_gradient_header) // Or a dedicated shimmer drawable
                 .error(R.drawable.ic_launcher_background)
                 .into(holder.ivEventImage)
        } else {
             holder.ivEventImage.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.tvEventName.text = event.name
        holder.tvEventTime.text = event.time ?: "--:--"
        holder.tvEventLocation.text = event.location

        // --- DISTANCE LOGIC ---
        if (userLocation != null && event.latitude != null && event.longitude != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                userLocation!!.latitude, userLocation!!.longitude,
                event.latitude, event.longitude,
                results
            )
            val distanceInKm = results[0] / 1000
            holder.tvDistance?.visibility = View.VISIBLE
            holder.tvDistance?.text = String.format("%.1f km", distanceInKm)
        } else {
            holder.tvDistance?.visibility = View.GONE
        }
        // ---------------------

        // Bind Date
        if (holder.tvEventDateLinear != null) {
            val dateText = event.startDate ?: event.date ?: "Date N/A"
            holder.tvEventDateLinear.text = dateText
        }
        
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

        // Bind Post Date
        holder.tvPostDate?.let { textView ->
            if (!event.createdAt.isNullOrEmpty()) {
                textView.visibility = View.VISIBLE
                try {
                    // Parse ISO 8601 (2024-12-23T10:00:00.000Z)
                    val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    
                    val date = inputFormat.parse(event.createdAt)
                    
                    if (date != null) {
                        val outputFormat = java.text.SimpleDateFormat("d MMM, hh:mm a", java.util.Locale.getDefault())
                        val formattedDate = outputFormat.format(date)
                        textView.text = "Posted on $formattedDate"
                    } else {
                        textView.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    textView.visibility = View.GONE
                }
            } else {
                textView.visibility = View.GONE
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