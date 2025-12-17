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

    /**
     * ViewHolder class for Maharaj items.
     * Holds references to the views for each item.
     */
    class MaharajViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMaharajImage: android.widget.ImageView = itemView.findViewById(R.id.ivMaharajImage)
        val tvMaharajName: TextView = itemView.findViewById(R.id.tvMaharajName)

        val tvMaharajCity: TextView = itemView.findViewById(R.id.tvMaharajCity)
        val tvMaharajDate: TextView = itemView.findViewById(R.id.tvMaharajDate)
        val tvMaharajSampraday: TextView = itemView.findViewById(R.id.tvMaharajSampraday)
        val tvMaharajContact: TextView = itemView.findViewById(R.id.tvMaharajContact)
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
        
        // Image Binding (Glide)
        if (!maharaj.image.isNullOrEmpty()) {
            holder.ivMaharajImage.visibility = View.VISIBLE
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(maharaj.image)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background) // Placeholder
                .into(holder.ivMaharajImage)
        } else {
            holder.ivMaharajImage.visibility = View.GONE
        }

        // Optional fields
        holder.tvMaharajCity.apply {
            if (!maharaj.city.isNullOrEmpty()) {
                text = maharaj.city
                visibility = View.VISIBLE
            } else visibility = View.GONE
        }

        holder.tvMaharajDate.apply {
            if (!maharaj.relevantDate.isNullOrEmpty()) {
                text = maharaj.relevantDate
                visibility = View.VISIBLE
            } else visibility = View.GONE
        }

        holder.tvMaharajSampraday.apply {
            if (!maharaj.sampraday.isNullOrEmpty()) {
                text = maharaj.sampraday
                visibility = View.VISIBLE
            } else visibility = View.GONE
        }

        holder.tvMaharajContact.apply {
            if (!maharaj.contactInfo.isNullOrEmpty()) {
                text = maharaj.contactInfo
                visibility = View.VISIBLE
            } else visibility = View.GONE
        }

        // Optional click listener
        holder.itemView.setOnClickListener {
            Log.d(TAG, "Clicked on Maharaj: ${maharaj.name} at position $position")
        }
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