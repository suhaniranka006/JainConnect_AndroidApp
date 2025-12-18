package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Tirthyatra
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MyYatraAdapter(
    private var yatras: List<Tirthyatra>,
    private val onItemClick: (Tirthyatra) -> Unit
) : RecyclerView.Adapter<MyYatraAdapter.YatraViewHolder>() {

    class YatraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvYatraTitle)
        val tvDates: TextView = itemView.findViewById(R.id.tvDates)
        val tvDaysLeft: TextView = itemView.findViewById(R.id.tvDaysLeft)
        val statusStrip: View = itemView.findViewById(R.id.viewStatusStrip)
        val ivYatraImage: android.widget.ImageView = itemView.findViewById(R.id.ivYatraImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YatraViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_yatra, parent, false)
        return YatraViewHolder(view)
    }

    override fun onBindViewHolder(holder: YatraViewHolder, position: Int) {
        val yatra = yatras[position]

        holder.tvTitle.text = yatra.title

        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        val startStr = yatra.startDate?.let { sdf.format(it) } ?: "??"
        val endStr = yatra.endDate?.let { sdf.format(it) } ?: "??"
        holder.tvDates.text = "$startStr - $endStr"

        // Load Image
        // Use placeholder or template image
        if (!yatra.imageUrl.isNullOrEmpty()) {
            var url = yatra.imageUrl!!
            if (!url.startsWith("http")) {
                 url = url.replace("\\", "/")
                 val baseUrl = "https://jainconnect-backened-2.onrender.com/"
                 url = "$baseUrl$url"
            }

            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(url)
                .placeholder(R.drawable.ic_tirthyatra) // Ensure resource exists
                .into(holder.ivYatraImage)
        } else {
            holder.ivYatraImage.setImageResource(R.drawable.ic_tirthyatra)
        }

        yatra.startDate?.let { start ->
            val diff = start.time - System.currentTimeMillis()
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            if (days > 0) {
                holder.tvDaysLeft.text = "$days Days Left"
                holder.tvDaysLeft.visibility = View.VISIBLE
            } else if (days > - (yatra.durationDays)) {
                 holder.tvDaysLeft.text = "Ongoing"
                 holder.tvDaysLeft.visibility = View.VISIBLE
            } else {
                holder.tvDaysLeft.text = "Completed"
                holder.tvDaysLeft.visibility = View.VISIBLE
            }
        } ?: run {
            holder.tvDaysLeft.visibility = View.GONE
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(yatra)
        }
    }

    override fun getItemCount(): Int = yatras.size

    fun updateData(newYatras: List<Tirthyatra>) {
        yatras = newYatras
        notifyDataSetChanged()
    }
}
