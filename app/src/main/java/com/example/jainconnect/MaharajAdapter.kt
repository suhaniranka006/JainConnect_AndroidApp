package com.example.jainconnect // Or your actual package: com.example.jainconnect.adapter

import android.util.Log // For logging, if you uncomment the Log.d lines
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Ensure your Maharaj data class is correctly imported if it's in a different package
// For example: import com.example.jainconnect.model.Maharaj

class MaharajAdapter(private var maharajList: List<Maharaj>) :
    RecyclerView.Adapter<MaharajAdapter.MaharajViewHolder>() {

    private val TAG = "MaharajAdapter" // Log TAG for debugging this adapter

    /**
     * ViewHolder class for Maharaj items.
     * Holds references to the views for each item in the RecyclerView.
     */
    class MaharajViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMaharajName: TextView = itemView.findViewById(R.id.tvMaharajName)
        val tvMaharajSthan: TextView = itemView.findViewById(R.id.tvMaharajSthan)
        val tvMaharajCity: TextView = itemView.findViewById(R.id.tvMaharajCity) // For City
        val tvMaharajDate: TextView = itemView.findViewById(R.id.tvMaharajDate) // For Date
        val tvMaharajSampraday: TextView = itemView.findViewById(R.id.tvMaharajSampraday)
        val tvMaharajContact: TextView = itemView.findViewById(R.id.tvMaharajContact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaharajViewHolder {
        Log.d(TAG, "onCreateViewHolder: Creating new view holder")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_maharaj, parent, false) // Ensure this is item_maharaj.xml
        return MaharajViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaharajViewHolder, position: Int) {
        val maharaj = maharajList[position]
        Log.d(TAG, "onBindViewHolder: Binding data for position $position - Name: ${maharaj.name}")

        holder.tvMaharajName.text = maharaj.name
        holder.tvMaharajSthan.text = maharaj.currentSthan // Make sure 'currentSthan' is the correct property name

        // Handle City
        // Make sure 'maharaj.city' is the correct property from your Maharaj data class
        if (!maharaj.city.isNullOrEmpty()) {
            holder.tvMaharajCity.text = maharaj.city
            holder.tvMaharajCity.visibility = View.VISIBLE
        } else {
            holder.tvMaharajCity.visibility = View.GONE // Or set text to "N/A" and keep VISIBLE
            // holder.tvMaharajCity.text = "City: N/A"
        }

        // Handle Date
        // Make sure 'maharaj.relevantDate' is the correct property from your Maharaj data class
        if (!maharaj.relevantDate.isNullOrEmpty()) {
            holder.tvMaharajDate.text = maharaj.relevantDate
            holder.tvMaharajDate.visibility = View.VISIBLE
        } else {
            holder.tvMaharajDate.visibility = View.GONE // Or set text to "N/A" and keep VISIBLE
            // holder.tvMaharajDate.text = "Date: N/A"
        }

        // Handle optional Sampraday field
        // Make sure 'maharaj.sampraday' is the correct property
        if (!maharaj.sampraday.isNullOrEmpty()) {
            holder.tvMaharajSampraday.text = maharaj.sampraday
            holder.tvMaharajSampraday.visibility = View.VISIBLE
        } else {
            holder.tvMaharajSampraday.visibility = View.GONE
        }

        // Handle optional Contact Info field
        // Make sure 'maharaj.contactInfo' is the correct property
        if (!maharaj.contactInfo.isNullOrEmpty()) {
            holder.tvMaharajContact.text = maharaj.contactInfo
            holder.tvMaharajContact.visibility = View.VISIBLE
        } else {
            holder.tvMaharajContact.visibility = View.GONE
        }

        // Optional: Add an OnClickListener to the item view if needed
        holder.itemView.setOnClickListener {
            Log.d(TAG, "Clicked on Maharaj: ${maharaj.name} at position $position")
            // Example: Handle item click, e.g., open details for this Maharaj
            // val clickedMaharaj = maharajList[position]
            // val context = holder.itemView.context
            // Implement navigation or action here
        }
    }

    override fun getItemCount(): Int {
        val count = maharajList.size
        Log.d(TAG, "getItemCount: List size is $count")
        return count
    }

    /**
     * Updates the list of Maharajs in the adapter and notifies the RecyclerView to refresh.
     * @param newMaharajList The new list of Maharajs to display.
     */
    fun updateData(newMaharajList: List<Maharaj>) {
        Log.d(TAG, "updateData: New data received with ${newMaharajList.size} items.")
        this.maharajList = newMaharajList
        notifyDataSetChanged() // This tells the RecyclerView to redraw the entire list.
        // For better performance with large or frequently changing lists,
        // consider using DiffUtil.
    }
}