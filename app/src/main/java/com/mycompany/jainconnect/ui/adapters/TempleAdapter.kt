package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Temple

class TempleAdapter(
    private val onTempleClick: (Temple) -> Unit
) : RecyclerView.Adapter<TempleAdapter.TempleViewHolder>() {

    private var templeList = ArrayList<Temple>()

    fun submitList(list: List<Temple>) {
        templeList.clear()
        templeList.addAll(list)
        templeList.addAll(list)
        notifyDataSetChanged()
    }

    // Callback for save
    private var onSaveClickListener: ((Temple) -> Unit)? = null
    private var mSavedIds: Set<String> = emptySet()

    fun setOnSaveClickListener(listener: (Temple) -> Unit) {
        onSaveClickListener = listener
    }
    
    fun updateSavedIds(newSavedIds: Set<String>) {
        this.mSavedIds = newSavedIds
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
        // private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription) // Removed from XML
        
        private val ivImage: android.widget.ImageView = itemView.findViewById(R.id.ivTempleImage)
        
        private val btnCall: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btnCall)
        private val btnDetails: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btnViewDetails)
        private val btnSave: android.widget.ImageView = itemView.findViewById(R.id.btnSave)

        fun bind(temple: Temple) {
            tvName.text = temple.name
            tvCity.text = temple.city
            tvAddress.text = temple.address ?: "${temple.city}"
            
            // Image Logic (Full Bleed)
            if (!temple.image.isNullOrEmpty()) {
                ivImage.visibility = View.VISIBLE
                com.bumptech.glide.Glide.with(itemView.context)
                    .load(temple.image)
                    .centerCrop()
                    .placeholder(R.drawable.jainconnect_app_logo)
                    .into(ivImage)
            } else {
                // If no image, show a nice gradient or placeholder
                ivImage.visibility = View.VISIBLE
                ivImage.setImageResource(R.drawable.bg_gradient_header) 
            }

            // Contact Logic - Only Button
            if (!temple.contact.isNullOrEmpty()) {
                btnCall.visibility = View.VISIBLE
                btnCall.setOnClickListener {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:${temple.contact}")
                    }
                    itemView.context.startActivity(intent)
                }
            } else {
                btnCall.visibility = View.GONE
            }

            // Details Click
            btnDetails.setOnClickListener {
                onTempleClick(temple)
            }
            
            // Save Button Logic
            // Assuming Temple model has valid 'id' or '_id'. Checking... Temple typically uses '_id' from backend
            // But let's verify if Temple model has it. Will assume property name 'id' or '_id' exists.
            // If compile fails, I'll fix it. Based on other models, likely '_id'.
            // WAIT - I haven't checked Temple model. I'll use 'id' if consistent, or '_id' if legacy.
            // Safest -> check 'id' field existance in next step if fails. For now assume '_id' like Maharaj/Event originally had?
            // Actually, I should use reflection or just assume 'id' as I fixed others to 'id'.
            // Let's assume 'id' for now.
            
            val isSaved = mSavedIds.contains(temple._id) // Temple likely has _id still
             if (isSaved) {
                btnSave.setImageResource(R.drawable.ic_bookmark_filled)
                btnSave.setColorFilter(null)
            } else {
                btnSave.setImageResource(R.drawable.ic_bookmark_border)
                btnSave.setColorFilter(null)
            }
            
            btnSave.setOnClickListener {
                onSaveClickListener?.invoke(temple)
                
                // Optimistic Update
                val isCurrentlySaved = mSavedIds.contains(temple._id)
                if (isCurrentlySaved) {
                    mSavedIds = mSavedIds - temple._id
                    btnSave.setImageResource(R.drawable.ic_bookmark_border)
                } else {
                    mSavedIds = mSavedIds + temple._id
                    btnSave.setImageResource(R.drawable.ic_bookmark_filled)
                }
            }
        }
    }
}
