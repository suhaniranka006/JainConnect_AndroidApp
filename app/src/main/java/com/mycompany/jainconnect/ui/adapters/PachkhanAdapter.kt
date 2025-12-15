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
    val description: String
)

class PachkhanAdapter(
    private val pachkhanList: List<PachkhanItem>,
    private val onAudioClick: (PachkhanItem) -> Unit
) : RecyclerView.Adapter<PachkhanAdapter.PachkhanViewHolder>() {

    class PachkhanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvPachkhanName)
        val tvTime: TextView = itemView.findViewById(R.id.tvPachkhanTime)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnAudio: ImageButton = itemView.findViewById(R.id.btnAudio)
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

        holder.btnAudio.setOnClickListener {
            onAudioClick(item)
        }
    }

    override fun getItemCount() = pachkhanList.size
}
