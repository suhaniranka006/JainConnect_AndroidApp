package com.example.jainconnect

import android.util.Log // Import Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Assuming your Tithi data class is defined elsewhere, e.g.:
// data class Tithi(val id: String, val name: String, val date: String, val details: String? = null)

class TithiAdapter(private var tithiList: List<Tithi>) :
    RecyclerView.Adapter<TithiAdapter.TithiViewHolder>() {

    private val TAG = "TithiAdapter_Debug" // Tag for logs

    /**
     * ViewHolder class for Tithi items.
     * Holds references to the views for each item in the RecyclerView.
     */
    class TithiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTithiName: TextView = itemView.findViewById(R.id.tvTithiName)
        val tvTithiDate: TextView = itemView.findViewById(R.id.tvTithiDate)
        val tvTithiDetails: TextView = itemView.findViewById(R.id.tvTithiDescription)
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TithiViewHolder {
        Log.d(TAG, "onCreateViewHolder called") // Log
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tithi, parent, false) // Inflates your item_tithi.xml
        return TithiViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     */
    override fun onBindViewHolder(holder: TithiViewHolder, position: Int) {
        if (tithiList.isEmpty()) {
            Log.w(TAG, "onBindViewHolder called but tithiList is empty! Position: $position")
            return // Should not happen if getItemCount is correct and > 0
        }
        if (position >= tithiList.size) {
            Log.e(TAG, "onBindViewHolder called with invalid position: $position, size: ${tithiList.size}")
            return // Defensive coding
        }

        val tithi = tithiList[position] // Get the data model for this position
        Log.d(TAG, "onBindViewHolder called for position: $position, Tithi: ${tithi.name}, Date: ${tithi.date}") // Log

        holder.tvTithiName.text = tithi.name
        holder.tvTithiDate.text = tithi.date

        // Handle optional details field
        if (!tithi.details.isNullOrEmpty()) {
            holder.tvTithiDetails.text = tithi.details
            holder.tvTithiDetails.visibility = View.VISIBLE
            Log.d(TAG, "Details for ${tithi.name}: VISIBLE, Content: ${tithi.details}") // Log
        } else {
            holder.tvTithiDetails.visibility = View.GONE
            Log.d(TAG, "Details for ${tithi.name}: GONE") // Log
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    override fun getItemCount(): Int {
        val count = tithiList.size
        Log.d(TAG, "getItemCount called, returning: $count") // Log
        return count
    }

    /**
     * Updates the list of Tithis in the adapter and notifies the RecyclerView to refresh.
     * @param newTithiList The new list of Tithis to display.
     */
    fun updateData(newTithiList: List<Tithi>) {
        Log.d(TAG, "updateData called. New list size: ${newTithiList.size}. Current list size: ${this.tithiList.size}") // Log
        this.tithiList = newTithiList
        notifyDataSetChanged() // For better performance on large lists, consider DiffUtil
        Log.d(TAG, "notifyDataSetChanged() called. Current list size after update: ${this.tithiList.size}") // Log
    }
}