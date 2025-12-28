package com.mycompany.jainconnect.ui.adapters

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private var notifications: List<Notification>,
    private val onItemClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val unreadDot: View = itemView.findViewById(R.id.viewUnreadDot)
        val ivIcon: android.widget.ImageView = itemView.findViewById(R.id.ivIcon)

        fun bind(notification: Notification) {
            tvTitle.text = notification.title
            tvMessage.text = notification.message

            // Format Time
            try {
                // Assuming server sends ISO format roughly: "2025-10-14T10:00:00.000Z"
                // Or whatever format Notification.createdAt is.
                // SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                // long time = iso.parse(notification.createdAt).getTime();
                // For now just showing raw or "Just now" if parsing fails, need robust parsing usually
                // But let's assume we can just show "New" for now or implement simple parsing
                 tvTime.text = "New" // Placeholder, will fix if needed
            } catch (e: Exception) {
                tvTime.text = ""
            }
            
            if (notification.isRead) {
                unreadDot.visibility = View.GONE
                itemView.alpha = 0.6f
            } else {
                unreadDot.visibility = View.VISIBLE
                itemView.alpha = 1.0f
            }

            itemView.setOnClickListener { onItemClick(notification) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size

    fun updateList(newList: List<Notification>) {
        notifications = newList
        notifyDataSetChanged()
    }
}
