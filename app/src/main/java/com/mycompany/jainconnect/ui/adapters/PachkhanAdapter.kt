package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R

data class PachkhanItem(
    val name: String,
    val time: String,
    val description: String,
    val iconResId: Int
)

class PachkhanAdapter(
    private val pachkhanList: List<PachkhanItem>,
    private val onCheck: (PachkhanItem) -> Unit
) : RecyclerView.Adapter<PachkhanAdapter.PachkhanViewHolder>() {

    var takenVows: Set<String> = emptySet()

    class PachkhanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvPachkhanName)
        val tvTime: TextView = itemView.findViewById(R.id.tvPachkhanTime)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val ivIcon: android.widget.ImageView = itemView.findViewById(R.id.ivIcon)
        val cbPachkhan: android.widget.CheckBox = itemView.findViewById(R.id.cbPachkhan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PachkhanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pachkhan, parent, false)
        return PachkhanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PachkhanViewHolder, position: Int) {
        val item = pachkhanList[position]
        holder.tvName.text = item.name
        holder.tvTime.text = item.time
        holder.tvDescription.text = item.description
        
        // Load Icon
        holder.ivIcon.setImageResource(item.iconResId)

        // Reset listener to avoid recycling issues
        holder.cbPachkhan.setOnCheckedChangeListener(null)
        
        // Check if taken
        val isTaken = takenVows.contains(item.name)
        holder.cbPachkhan.isChecked = isTaken
        holder.cbPachkhan.isEnabled = !isTaken // Disable if already taken

        holder.cbPachkhan.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onCheck(item)
                holder.cbPachkhan.isEnabled = false // Disable after taking
            }
        }
    }

    override fun getItemCount() = pachkhanList.size
}
