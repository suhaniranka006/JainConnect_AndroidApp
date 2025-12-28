package com.mycompany.jainconnect.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.NotificationAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // UI Setup
        val rvNotifications = findViewById<RecyclerView>(R.id.recyclerViewNotifications)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        rvNotifications.layoutManager = LinearLayoutManager(this)
        
        adapter = NotificationAdapter(emptyList()) { notification ->
            // On Click
            // 1. Mark as read
            val token = getPreferences(Context.MODE_PRIVATE).getString("jwt_token", null) ?: return@NotificationAdapter
            if (!notification.isRead) {
                viewModel.markNotificationRead(token, notification.id)
            }
            
            // 2. Navigation Logic based on type
            // For now just Toast, or maybe open CarpoolActivity
            if (notification.type == "CARPOOL_REQUEST" || notification.type == "CARPOOL_UPDATE") {
                // Navigate to Carpool Activity? Or just show details?
                // For simplified V1, let's keep it here.
            }
        }
        rvNotifications.adapter = adapter

        // Observe
        viewModel.notificationList.observe(this) { list ->
            progressBar.visibility = View.GONE
            if (list.isNullOrEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                adapter.updateList(emptyList())
            } else {
                tvEmptyState.visibility = View.GONE
                adapter.updateList(list)
            }
        }

        // Fetch Data
        val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        
        if (token != null) {
            progressBar.visibility = View.VISIBLE
            viewModel.fetchNotifications(token)
        } else {
            Toast.makeText(this, "Please login to see notifications", Toast.LENGTH_SHORT).show()
        }
        
        // Refresh Listener
        // Refresh Listener
        val swipeRefresh = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            if (token != null) {
                viewModel.fetchNotifications(token)
            }
            swipeRefresh.isRefreshing = false
        }
    }
}
