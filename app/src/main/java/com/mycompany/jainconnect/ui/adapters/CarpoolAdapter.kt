package com.mycompany.jainconnect.ui.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Carpool

class CarpoolAdapter(
    private var carpools: List<Carpool>,
    private var currentUserId: String,
    private val onCallClick: (String) -> Unit,
    private val onEditClick: (Carpool) -> Unit,
    private val onDeleteClick: (Carpool) -> Unit
) : RecyclerView.Adapter<CarpoolAdapter.CarpoolViewHolder>() {

    class CarpoolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDriverName: TextView = itemView.findViewById(R.id.tvDriverName)
        val tvSource: TextView = itemView.findViewById(R.id.tvSource)
        val tvDestination: TextView = itemView.findViewById(R.id.tvDestination)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvSeats: TextView = itemView.findViewById(R.id.tvSeats)
        val tvVehicleType: TextView = itemView.findViewById(R.id.tvVehicleType)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val btnCall: Button = itemView.findViewById(R.id.btnCallDriver)
        val chipLadiesOnly: com.google.android.material.chip.Chip = itemView.findViewById(R.id.chipLadiesOnlyItem)
        
        // Actions
        val layoutMyActions: View = itemView.findViewById(R.id.layoutMyRideActions)
        val btnEdit: Button = itemView.findViewById(R.id.btnEditRide)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeleteRide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarpoolViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carpool, parent, false)
        return CarpoolViewHolder(view)
    }

    private var isMyRidesTab: Boolean = false

    fun setMyRidesTab(enable: Boolean) {
        isMyRidesTab = enable
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CarpoolViewHolder, position: Int) {
        val carpool = carpools[position]
        holder.tvDriverName.text = carpool.driverName ?: "Unknown Driver"
        holder.tvSource.text = carpool.source ?: ""
        holder.tvDestination.text = carpool.destination ?: ""
        holder.tvDate.text = carpool.date ?: ""
        holder.tvTime.text = carpool.time ?: ""
        holder.tvSeats.text = "${carpool.seatsAvailable ?: 0} Seats"
        
        // Show Vehicles Type + Distance (Debug/Feature)
        var vText = carpool.vehicleType ?: "Car"
        if (carpool.distanceFromUser != null) {
            val km = (carpool.distanceFromUser!! / 1000).toInt()
            vText += " • ${km}km"
        }
        holder.tvVehicleType.text = vText

        if (carpool.isLadiesOnly == true) {
            holder.chipLadiesOnly.visibility = View.VISIBLE
        } else {
            holder.chipLadiesOnly.visibility = View.GONE
        }

        // --- Status Logic ---
        var displayStatus = carpool.status ?: "Open"
        var isPast = false
        
        try {
             val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
             val rideDate = sdfDate.parse(carpool.date ?: "")
             val today = java.util.Calendar.getInstance()
             today.set(java.util.Calendar.HOUR_OF_DAY, 0)
             today.set(java.util.Calendar.MINUTE, 0)
             today.set(java.util.Calendar.SECOND, 0)
             today.set(java.util.Calendar.MILLISECOND, 0)
             
             if (rideDate != null && rideDate.before(today.time)) {
                 isPast = true
             }
        } catch (e: Exception) {}

        if (displayStatus == "Open" && isPast) displayStatus = "Expired"

        // Alpha & Status Tag
        when (displayStatus) {
            "Open" -> {
                holder.tvStatus.visibility = View.GONE
                holder.itemView.alpha = 1.0f
            }
            "Full" -> {
                holder.tvStatus.visibility = View.VISIBLE
                holder.tvStatus.text = "Full"
                holder.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.DKGRAY)
                holder.itemView.alpha = 0.6f
            }
            "Completed", "Expired" -> {
                 holder.tvStatus.visibility = View.VISIBLE
                 holder.tvStatus.text = "Completed"
                 holder.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY)
                 holder.itemView.alpha = 0.5f
            }
            "Cancelled" -> {
                holder.tvStatus.visibility = View.VISIBLE
                holder.tvStatus.text = "Cancelled"
                holder.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.RED)
                holder.itemView.alpha = 0.5f
            }
        }

        // --- Visibility Constants ---
        holder.layoutMyActions.visibility = View.GONE
        holder.btnCall.visibility = View.GONE

        // --- Logic: My Ride vs Others ---
        val isMyRide = !currentUserId.isNullOrEmpty() && carpool.userId == currentUserId

        if (isMyRide) {
            // My Ride -> Check Tab Mode
            if (isMyRidesTab) {
                // Show Edit/Delete Actions ONLY in My Rides Tab
                holder.layoutMyActions.visibility = View.VISIBLE
                holder.btnEdit.setOnClickListener { onEditClick(carpool) }
                holder.btnDelete.setOnClickListener { onDeleteClick(carpool) }
            } else {
                // Nearby Tab: Hide Actions for my own ride (just show info)
                holder.layoutMyActions.visibility = View.GONE
            }
        } else {
            // Other's Ride -> Show Call (if Open)
            if (displayStatus == "Open") {
                holder.btnCall.visibility = View.VISIBLE
                holder.btnCall.setOnClickListener {
                     carpool.contactNumber?.let { contact ->
                         onCallClick(contact)
                     }
                }
            }
        }
    }

    override fun getItemCount() = carpools.size

    fun updateList(newList: List<Carpool>) {
        carpools = newList
        notifyDataSetChanged()
    }

    fun updateUserId(newId: String) {
        currentUserId = newId
        notifyDataSetChanged()
    }
}
