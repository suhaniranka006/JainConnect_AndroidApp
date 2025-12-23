package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Bhojanshala

class BhojanshalaAdapter : RecyclerView.Adapter<BhojanshalaAdapter.BhojanshalaViewHolder>() {

    private var bhojanshalaList = ArrayList<Bhojanshala>()

    fun submitList(list: List<Bhojanshala>) {
        bhojanshalaList.clear()
        bhojanshalaList.addAll(list)
        notifyDataSetChanged()
    }

    // Callback for save
    private var onSaveClickListener: ((Bhojanshala) -> Unit)? = null
    private var mSavedIds: Set<String> = emptySet()

    fun setOnSaveClickListener(listener: (Bhojanshala) -> Unit) {
        onSaveClickListener = listener
    }
    
    fun updateSavedIds(newSavedIds: Set<String>) {
        this.mSavedIds = newSavedIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BhojanshalaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bhojanshala, parent, false)
        return BhojanshalaViewHolder(view)
    }

    override fun onBindViewHolder(holder: BhojanshalaViewHolder, position: Int) {
        holder.bind(bhojanshalaList[position])
    }

    override fun getItemCount(): Int = bhojanshalaList.size

    inner class BhojanshalaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvCity: TextView = itemView.findViewById(R.id.tvCity)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivBhojanshalaImage)
        private val btnCall: MaterialButton = itemView.findViewById(R.id.btnCall)
        private val btnViewDetails: MaterialButton = itemView.findViewById(R.id.btnViewDetails)
        private val btnSave: ImageView = itemView.findViewById(R.id.btnSave)

        fun bind(bhojanshala: Bhojanshala) {
            tvName.text = bhojanshala.name
            tvCity.text = bhojanshala.city
            tvAddress.text = bhojanshala.address ?: "Address not available"

            if (!bhojanshala.image.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(bhojanshala.image)
                    .placeholder(R.drawable.jainconnect_app_logo)
                    .into(ivImage)
            } else {
                ivImage.setImageResource(R.drawable.jainconnect_app_logo)
            }

            if (!bhojanshala.contact.isNullOrEmpty()) {
                btnCall.visibility = View.VISIBLE
                btnCall.setOnClickListener {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                        intent.data = android.net.Uri.parse("tel:${bhojanshala.contact}")
                        itemView.context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                btnCall.visibility = View.GONE
            }

            btnViewDetails.setOnClickListener {
                val intent = android.content.Intent(itemView.context, com.mycompany.jainconnect.ui.activities.BhojanshalaDetailActivity::class.java)
                intent.putExtra("bhojanshala_data", bhojanshala)
                itemView.context.startActivity(intent)
            }

            // Save Logic
            val isSaved = mSavedIds.contains(bhojanshala._id) // Assuming _id
             if (isSaved) {
                btnSave.setImageResource(R.drawable.ic_bookmark_filled)
                btnSave.setColorFilter(null)
            } else {
                btnSave.setImageResource(R.drawable.ic_bookmark_border)
                btnSave.setColorFilter(null)
            }
            
            btnSave.setOnClickListener {
                onSaveClickListener?.invoke(bhojanshala)

                // Optimistic Update
                val isCurrentlySaved = mSavedIds.contains(bhojanshala._id)
                if (isCurrentlySaved) {
                    mSavedIds = mSavedIds - bhojanshala._id
                    btnSave.setImageResource(R.drawable.ic_bookmark_border)
                } else {
                    mSavedIds = mSavedIds + bhojanshala._id
                    btnSave.setImageResource(R.drawable.ic_bookmark_filled)
                }
            }
        }
    }
}
