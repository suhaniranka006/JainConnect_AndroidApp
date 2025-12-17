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
    private var carpools: List<Carpool>
) : RecyclerView.Adapter<CarpoolAdapter.CarpoolViewHolder>() {

    class CarpoolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDriverName: TextView = itemView.findViewById(R.id.tvDriverName)
        val tvSource: TextView = itemView.findViewById(R.id.tvSource)
        val tvDestination: TextView = itemView.findViewById(R.id.tvDestination)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvSeats: TextView = itemView.findViewById(R.id.tvSeats)
        val tvVehicleType: TextView = itemView.findViewById(R.id.tvVehicleType)
        val btnCall: Button = itemView.findViewById(R.id.btnCallDriver)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarpoolViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carpool, parent, false)
        return CarpoolViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarpoolViewHolder, position: Int) {
        val carpool = carpools[position]
        holder.tvDriverName.text = carpool.driverName ?: "Unknown Driver"
        holder.tvSource.text = carpool.source ?: ""
        holder.tvDestination.text = carpool.destination ?: ""
        holder.tvDate.text = carpool.date ?: ""
        holder.tvTime.text = carpool.time ?: ""
        holder.tvSeats.text = "${carpool.seatsAvailable ?: 0} Seats"
        holder.tvVehicleType.text = carpool.vehicleType ?: "Car"

        holder.btnCall.setOnClickListener {
            val contact = carpool.contactNumber
            if (!contact.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$contact")
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = carpools.size

    fun updateList(newList: List<Carpool>) {
        carpools = newList
        notifyDataSetChanged()
    }
}
