package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.CarpoolRequestItem

class CarpoolRequestAdapter(
    private var requests: List<CarpoolRequestItem>,
    private val onActionClick: (CarpoolRequestItem, String) -> Unit // action: "approve" or "reject"
) : RecyclerView.Adapter<CarpoolRequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvRequesterName)
        val tvSeats: TextView = itemView.findViewById(R.id.tvSeatsRequested)
        val tvDetails: TextView = itemView.findViewById(R.id.tvRequesterDetails)
        val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carpool_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.tvName.text = request.name
        holder.tvSeats.text = "${request.seats} Seats"
        holder.tvDetails.text = "${request.gender} • ${request.contact}"

        holder.btnApprove.setOnClickListener {
            onActionClick(request, "approve")
        }

        holder.btnReject.setOnClickListener {
            onActionClick(request, "reject")
        }
    }

    override fun getItemCount() = requests.size

    fun updateList(newList: List<CarpoolRequestItem>) {
        requests = newList
        notifyDataSetChanged()
    }
}
