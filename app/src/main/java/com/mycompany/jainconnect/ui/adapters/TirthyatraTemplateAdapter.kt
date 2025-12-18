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
import com.mycompany.jainconnect.data.models.TirthyatraTemplate

class TirthyatraTemplateAdapter(
    private var templates: List<TirthyatraTemplate>,
    private val onPlanClick: (TirthyatraTemplate) -> Unit
) : RecyclerView.Adapter<TirthyatraTemplateAdapter.TemplateViewHolder>() {

    class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivTemplateImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTemplateTitle)
        val tvDays: TextView = itemView.findViewById(R.id.tvTemplateDays)
        val tvDesc: TextView = itemView.findViewById(R.id.tvTemplateDesc)
        val tvTrending: TextView = itemView.findViewById(R.id.tvTrendingBadge)
        val btnPlan: MaterialButton = itemView.findViewById(R.id.btnPlanTrip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tirthyatra_template, parent, false)
        return TemplateViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val template = templates[position]

        holder.tvTitle.text = template.title
        holder.tvDays.text = "${template.durationDays} Days"
        holder.tvDesc.text = template.description ?: "No description available"
        
        if (template.isPopular) {
            holder.tvTrending.visibility = View.VISIBLE
        } else {
            holder.tvTrending.visibility = View.GONE
        }

        // Load image (placeholder if null)
        // Assuming you have Glide or similar. If not, basic resource setting or skip.
        // For now using a default drawable if URL is empty effectively by Glide fallback
         if (!template.image.isNullOrEmpty()) {
             Glide.with(holder.itemView.context)
                 .load(template.image)
                 .placeholder(R.drawable.ic_tirthyatra) // Use placeholder while loading
                 .error(R.drawable.ic_tirthyatra) // Use placeholder on error
                 .centerCrop()
                 .into(holder.ivImage)
         } else {
             holder.ivImage.setImageResource(R.drawable.ic_tirthyatra)
         }


        holder.btnPlan.setOnClickListener {
            onPlanClick(template)
        }
    }

    override fun getItemCount(): Int = templates.size

    fun updateData(newTemplates: List<TirthyatraTemplate>) {
        templates = newTemplates
        notifyDataSetChanged()
    }
}
