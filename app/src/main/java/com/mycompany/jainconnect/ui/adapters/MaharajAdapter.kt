package com.mycompany.jainconnect.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Maharaj

/**
 * Adapter class for displaying Maharaj items in a RecyclerView.
 * Each item displays name, current Sthan, city, relevant date,
 * sampraday, and contact info (all optional except name and sthan).
 */
class MaharajAdapter(private var maharajList: List<Maharaj>) :
    RecyclerView.Adapter<MaharajAdapter.MaharajViewHolder>() {

    private val TAG = "MaharajAdapter"

    // Callback for save
    private var onSaveClickListener: ((Maharaj) -> Unit)? = null
    private var mSavedIds: Set<String> = emptySet()

    fun setOnSaveClickListener(listener: (Maharaj) -> Unit) {
        onSaveClickListener = listener
    }
    
    fun updateSavedIds(newSavedIds: Set<String>) {
        this.mSavedIds = newSavedIds
        notifyDataSetChanged()
    }

    // Location for Distance Calculation
    private var userLocation: android.location.Location? = null

    fun setUserLocation(location: android.location.Location) {
        userLocation = location
        notifyDataSetChanged()
    }

    /**
     * ViewHolder class for Maharaj items.
     * Holds references to the views for each item.
     */
    class MaharajViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMaharajImage: android.widget.ImageView = itemView.findViewById(R.id.ivMaharajImage)
        val tvMaharajName: TextView = itemView.findViewById(R.id.tvMaharajName)

        val tvMaharajCity: TextView = itemView.findViewById(R.id.tvMaharajCity)
        val layoutDistanceContainer: View = itemView.findViewById(R.id.layoutDistanceContainer) // Added container
        val tvMaharajDistance: TextView = itemView.findViewById(R.id.tvMaharajDistance)
        val tvMaharajDate: TextView = itemView.findViewById(R.id.tvMaharajDate)
        val tvMaharajSampraday: TextView = itemView.findViewById(R.id.tvMaharajSampraday)
        
        val layoutMaharajContact: View = itemView.findViewById(R.id.layoutMaharajContact)
        val tvMaharajContact: TextView = itemView.findViewById(R.id.tvMaharajContact)
        val btnViewDetails: View = itemView.findViewById(R.id.btnViewDetails)
        val btnSave: android.widget.ImageView = itemView.findViewById(R.id.btnSave)
    }

    /**
     * Called when RecyclerView needs a new ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaharajViewHolder {
        Log.d(TAG, "onCreateViewHolder: Creating new view holder")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_maharaj, parent, false)
        return MaharajViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     */
    override fun onBindViewHolder(holder: MaharajViewHolder, position: Int) {
        val maharaj = maharajList[position]

        // Required fields
        holder.tvMaharajName.text = maharaj.name
        
        // Image Binding (Square)
        if (!maharaj.image.isNullOrEmpty()) {
            holder.ivMaharajImage.visibility = View.VISIBLE
            
            var imageUrl = maharaj.image!!
            if (!imageUrl.startsWith("http")) {
                // Fix backslashes for Windows paths and prepend Base URL
                val cleanPath = imageUrl.replace("\\", "/")
                imageUrl = "https://jainconnect-backened-2.onrender.com/$cleanPath"
            }
            
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivMaharajImage)
        } else {
            holder.ivMaharajImage.visibility = View.VISIBLE
            holder.ivMaharajImage.setImageResource(R.drawable.ic_launcher_background)
        }

        // Optional fields
        holder.tvMaharajCity.text = maharaj.city ?: "City"

        // --- DISTANCE LOGIC ---
        if (userLocation != null && maharaj.latitude != null && maharaj.longitude != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                userLocation!!.latitude, userLocation!!.longitude,
                maharaj.latitude!!, maharaj.longitude!!,
                results
            )
            val distanceInKm = results[0] / 1000
            holder.layoutDistanceContainer.visibility = View.VISIBLE
            holder.tvMaharajDistance.text = String.format("%.1f km", distanceInKm)
        } else {
            holder.layoutDistanceContainer.visibility = View.GONE
        }
        // ---------------------
        
        holder.tvMaharajDate.text = maharaj.arrivalDate ?: maharaj.relevantDate ?: "Date N/A"
        
        holder.tvMaharajSampraday.apply {
            if (!maharaj.sampraday.isNullOrEmpty()) {
                text = maharaj.sampraday
                visibility = View.VISIBLE
            } else visibility = View.GONE
        }

        // Contact Binding (Layout Visibility & Click)
        if (!maharaj.contactInfo.isNullOrEmpty()) {
            holder.tvMaharajContact.text = maharaj.contactInfo
            holder.layoutMaharajContact.visibility = View.VISIBLE
            
            holder.layoutMaharajContact.setOnClickListener {
                val phone = maharaj.contactInfo
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                intent.data = android.net.Uri.parse("tel:$phone")
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.layoutMaharajContact.visibility = View.GONE
        }
        
        // Save Button State
        val isSaved = this.mSavedIds.contains(maharaj.id)
        if (isSaved) {
            holder.btnSave.setImageResource(R.drawable.ic_bookmark_filled)
            holder.btnSave.setColorFilter(null)
        } else {
            holder.btnSave.setImageResource(R.drawable.ic_bookmark_border)
            holder.btnSave.setColorFilter(null)
        }
        
        holder.btnSave.setOnClickListener {
            onSaveClickListener?.invoke(maharaj)
            
            // Optimistic Update
            val isCurrentlySaved = mSavedIds.contains(maharaj.id)
            if (isCurrentlySaved) {
                mSavedIds = mSavedIds - (maharaj.id ?: "")
                holder.btnSave.setImageResource(R.drawable.ic_bookmark_border)
            } else {
                mSavedIds = mSavedIds + (maharaj.id ?: "")
                holder.btnSave.setImageResource(R.drawable.ic_bookmark_filled)
            }
        }

        // Click Listener -> Detail Activity (ONLY ON BUTTON)
        holder.btnViewDetails.setOnClickListener {
            val intent = android.content.Intent(holder.itemView.context, com.mycompany.jainconnect.ui.activities.MaharajDetailActivity::class.java)
            intent.putExtra("EXTRA_MAHARAJ", maharaj)
            holder.itemView.context.startActivity(intent)
        }
        
        // Remove item click listener if it was set
        holder.itemView.setOnClickListener(null)
    }

    /**
     * Returns total number of Maharaj items.
     */
    override fun getItemCount(): Int {
        val count = maharajList.size
        Log.d(TAG, "getItemCount: List size is $count")
        return count
    }

    /**
     * Updates the list of Maharajs and refreshes RecyclerView.
     */
    fun updateData(newMaharajList: List<Maharaj>) {
        Log.d(TAG, "updateData: New list size = ${newMaharajList.size}")
        this.maharajList = newMaharajList
        notifyDataSetChanged() // Consider DiffUtil for performance with large lists
    }
}