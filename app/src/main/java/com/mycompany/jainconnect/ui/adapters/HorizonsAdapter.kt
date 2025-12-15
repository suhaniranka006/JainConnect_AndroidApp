package com.mycompany.jainconnect.ui.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.HorizonItem

class HorizonsAdapter : RecyclerView.Adapter<HorizonsAdapter.HorizonViewHolder>() {

    private var items = listOf<HorizonItem>()

    fun submitList(list: List<HorizonItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_horizon_card, parent, false)
        return HorizonViewHolder(view)
    }

    override fun onBindViewHolder(holder: HorizonViewHolder, position: Int) {
        val item = items[position]
        holder.tvDate.text = item.date
        holder.tvSunrise.text = "☀ Rise: ${item.sunrise}"
        holder.tvSunset.text = "☾ Set: ${item.sunset}"
    }

    override fun getItemCount() = items.size

    class HorizonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvSunrise: TextView = itemView.findViewById(R.id.tvSunrise)
        val tvSunset: TextView = itemView.findViewById(R.id.tvSunset)
    }
}