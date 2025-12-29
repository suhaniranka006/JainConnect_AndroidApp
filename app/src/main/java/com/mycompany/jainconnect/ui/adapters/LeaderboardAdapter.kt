
package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.LeaderboardUser

class LeaderboardAdapter(
    var list: List<LeaderboardUser>
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Calculate rank: since top 3 are handled separately (or if they are in list? logic depends)
        // If we only pass users starting from rank 4, then rank = position + 4
        // If we pass all users but rely on getItemViewType (complex), keeping it simple.
        // Let's assume this adapter receives ONLY users from rank 4 downwards.
        
        val user = list[position]
        holder.tvRank.text = (position + 4).toString()
        holder.tvName.text = user.name
        holder.tvScore.text = user.coins.toString()

        if (!user.profileImage.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.profileImage)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(holder.ivProfile)
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    override fun getItemCount() = list.size
}
