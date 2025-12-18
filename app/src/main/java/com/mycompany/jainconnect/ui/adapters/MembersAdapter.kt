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
    private val members: List<TirthyatraUser>
) : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

    class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDetails: TextView = view.findViewById(R.id.tvDetails)
        val ivProfile: ImageView = view.findViewById(R.id.ivProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_yatra_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.tvName.text = member.name
        
        // Calculate Age and Format Details
        val age = calculateAge(member.dob)
        val gender = member.gender?.capitalize(Locale.getDefault()) ?: "Unknown"
        val ageText = if (age != null) "$age years" else "Age N/A"
        
        holder.tvDetails.text = "$gender, $ageText"

        Glide.with(holder.itemView.context)
            .load(member.profileImage)
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(holder.ivProfile)
    }

    override fun getItemCount() = members.size

    private fun calculateAge(dobString: String?): Int? {
        if (dobString.isNullOrEmpty()) return null
        return try {
            // Expected format from backend defaults usually ISO "yyyy-MM-dd..."
            // But User model says dob is simple string or Date object serialized?
            // Usually JSON returns ISO 8601 string.
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            // Handle ISO format with time if present
            val date = if (dobString.contains("T")) {
                 SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dobString)
            } else {
                 sdf.parse(dobString)
            }

            if (date != null) {
                val dob = Calendar.getInstance()
                dob.time = date
                val today = Calendar.getInstance()
                var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                age
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
