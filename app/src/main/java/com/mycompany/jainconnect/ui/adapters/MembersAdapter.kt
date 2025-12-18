package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.TirthyatraUser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MembersAdapter(
    private val members: List<TirthyatraUser>,
    private val onItemClick: ((TirthyatraUser) -> Unit)? = null
) : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
        val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_yatra_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.tvName.text = member.name
        
        // Calculate Age using Utility
        val age = com.mycompany.jainconnect.utils.AgeUtils.calculateAge(member.dob)
        val gender = member.gender?.capitalize(Locale.getDefault()) ?: "Unknown"
        val ageText = if (age != "N/A") "$age years" else "Age N/A"
        
        holder.tvDetails.text = "$gender, $ageText"

        Glide.with(holder.itemView.context)
            .load(member.profileImage)
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(holder.ivProfile)
            
        holder.itemView.setOnClickListener {
             onItemClick?.invoke(member)
        }
    }

    override fun getItemCount() = members.size
}
