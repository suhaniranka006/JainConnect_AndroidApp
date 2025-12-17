package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Bhojanshala

class BhojanshalaAdapter : RecyclerView.Adapter<BhojanshalaAdapter.BhojanshalaViewHolder>() {

    private var bhojanshalaList = ArrayList<Bhojanshala>()

    fun submitList(list: List<Bhojanshala>) {
        bhojanshalaList.clear()
        bhojanshalaList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BhojanshalaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bhojanshala, parent, false)
        return BhojanshalaViewHolder(view)
    }

    override fun onBindViewHolder(holder: BhojanshalaViewHolder, position: Int) {
        val bhojanshala = bhojanshalaList[position]
        holder.bind(bhojanshala)
    }

    override fun getItemCount(): Int = bhojanshalaList.size

    inner class BhojanshalaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvCity: TextView = itemView.findViewById(R.id.tvCity)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvTimings: TextView = itemView.findViewById(R.id.tvTimings)
        private val tvContact: TextView = itemView.findViewById(R.id.tvContact)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)

        fun bind(bhojanshala: Bhojanshala) {
            tvName.text = bhojanshala.name
            tvCity.text = bhojanshala.city
            tvAddress.text = "Address: ${bhojanshala.address}"
            tvTimings.text = "Timings: ${bhojanshala.timings ?: "N/A"}"
            tvContact.text = bhojanshala.contact?.let { "Contact: $it" } ?: ""
            tvDescription.text = bhojanshala.description ?: ""
        }
    }
}
