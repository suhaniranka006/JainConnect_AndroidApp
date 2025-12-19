package com.mycompany.jainconnect.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Tithi

/**
 * Adapter class for displaying Tithi items in a RecyclerView.
 * Each item displays a name, date, and optional details.
 */
class TithiAdapter(private var tithiList: List<Tithi>) :
    RecyclerView.Adapter<TithiAdapter.TithiViewHolder>() {

    private val TAG = "TithiAdapter_Debug"

    class TithiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTithiName: TextView = itemView.findViewById(R.id.tvTithiName)
        val tvTithiDetails: TextView = itemView.findViewById(R.id.tvTithiDescription)
        val tvDateDay: TextView = itemView.findViewById(R.id.tvDateDay)
        val tvDateMonth: TextView = itemView.findViewById(R.id.tvDateMonth)
        // val tvSunrise: TextView = itemView.findViewById(R.id.tvSunrise) // Data missing
        // val tvSunset: TextView = itemView.findViewById(R.id.tvSunset)   // Data missing
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TithiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tithi, parent, false)
        return TithiViewHolder(view)
    }

    override fun onBindViewHolder(holder: TithiViewHolder, position: Int) {
        val tithi = tithiList[position]

        holder.tvTithiName.text = tithi.name
        
        // Handle Description
        if (!tithi.details.isNullOrEmpty()) {
            holder.tvTithiDetails.text = tithi.details
            holder.tvTithiDetails.visibility = View.VISIBLE
        } else {
            holder.tvTithiDetails.visibility = View.GONE
        }

        // Parse Date for Date Box (Assuming format "YYYY-MM-DD" or "DD-MM-YYYY")
        try {
            // Attempt to parse standard formats
            val parts = tithi.date.split("-", " ", "/")
            if (parts.size >= 3) {
                 // Try to guess Day vs Year. Usually Day is 1-31.
                 // Heuristic: If part[0] is > 31, it's Year (YYYY-MM-DD). Day is last.
                 // If part[0] <= 31, it's Day (DD-MM-YYYY). Day is first.
                 
                 var day = parts[2]
                 var month = parts[1] // Month is usually middle
                 
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
                // Fallback
                holder.tvDateDay.text = "--"
                holder.tvDateMonth.text = "DATE"
            }
        } catch (e: Exception) {
            holder.tvDateDay.text = "Tithi"
            holder.tvDateMonth.text = "DATE"
        }
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "JAN" 2 -> "FEB" 3 -> "MAR" 4 -> "APR" 5 -> "MAY" 6 -> "JUN"
            7 -> "JUL" 8 -> "AUG" 9 -> "SEP" 10 -> "OCT" 11 -> "NOV" 12 -> "DEC"
            else -> "MTH"
        }
    }

    override fun getItemCount(): Int = tithiList.size

    fun updateData(newTithiList: List<Tithi>) {
        this.tithiList = newTithiList
        notifyDataSetChanged()
    }
}