package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Temple

class TempleAdapter : RecyclerView.Adapter<TempleAdapter.TempleViewHolder>() {

    private var templeList = ArrayList<Temple>()

    fun submitList(list: List<Temple>) {
        templeList.clear()
        templeList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TempleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_temple, parent, false)
        return TempleViewHolder(view)
    }

    override fun onBindViewHolder(holder: TempleViewHolder, position: Int) {
        val temple = templeList[position]
        holder.bind(temple)
    }

    override fun getItemCount(): Int = templeList.size

    inner class TempleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvCity: TextView = itemView.findViewById(R.id.tvCity)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvContact: TextView = itemView.findViewById(R.id.tvContact)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)

        private val ivImage: android.widget.ImageView = itemView.findViewById(R.id.ivTempleImage)

        fun bind(temple: Temple) {
            tvName.text = temple.name
            tvCity.text = temple.city
            tvAddress.text = "Address: ${temple.address ?: "N/A"}"
            tvContact.text = temple.contact?.let { "Contact: $it" } ?: ""
            tvDescription.text = temple.description ?: ""

            if (!temple.image.isNullOrEmpty()) {
                ivImage.visibility = View.VISIBLE
                com.bumptech.glide.Glide.with(itemView.context)
                    .load(temple.image)
                    .placeholder(R.drawable.jainconnect_app_logo)
                    .into(ivImage)
            } else {
                ivImage.visibility = View.GONE
            }
        }
    }
}
