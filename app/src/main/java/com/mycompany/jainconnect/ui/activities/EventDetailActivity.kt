package com.mycompany.jainconnect.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Event
import com.mycompany.jainconnect.data.models.RsvpResponse // Added Import
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import com.mycompany.jainconnect.data.network.NetworkResult // Corrected Import

@AndroidEntryPoint
class EventDetailActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        // Setup Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Get Event Data
        val event = intent.getSerializableExtra("EXTRA_EVENT") as? Event

        if (event == null) {
            Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews(event)
    }

    private fun initializeViews(event: Event) {
        val ivImage = findViewById<ImageView>(R.id.ivEventImageDetail)
        val tvName = findViewById<TextView>(R.id.tvEventNameDetail)
        val tvStartDate = findViewById<TextView>(R.id.tvEventStartDateDetail)
        val tvEndDate = findViewById<TextView>(R.id.tvEventEndDateDetail)
        val tvTime = findViewById<TextView>(R.id.tvEventTimeDetail)
        val tvLocation = findViewById<TextView>(R.id.tvEventLocationDetail)
        val tvDesc = findViewById<TextView>(R.id.tvEventDescDetail)
        val tvContact = findViewById<TextView>(R.id.tvEventContactDetail)
        val layoutContact = findViewById<View>(R.id.layoutContactDetail)
        val btnRsvp = findViewById<ExtendedFloatingActionButton>(R.id.btnRsvpDetail)

        tvName.text = event.name
        
        // Start Date Binding
        tvStartDate.text = "Start: ${event.startDate ?: event.date ?: "N/A"}"

        // End Date Binding & Visibility
        val layoutEndDate = findViewById<View>(R.id.layoutEndDate)
        if (!event.endDate.isNullOrEmpty()) {
            tvEndDate.text = "End: ${event.endDate}"
            layoutEndDate.visibility = View.VISIBLE
        } else {
            layoutEndDate.visibility = View.GONE
        }

        tvTime.text = event.time ?: "Time N/A"
        tvLocation.text = event.location
        tvDesc.text = event.description ?: "No description available."

        if (!event.contact.isNullOrEmpty()) {
            tvContact.text = event.contact
            layoutContact.visibility = View.VISIBLE
            layoutContact.setOnClickListener {
                 val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                 intent.data = android.net.Uri.parse("tel:${event.contact}")
                 startActivity(intent)
            }
        } else {
            layoutContact.visibility = View.GONE
        }

        // Load Image
        if (!event.image.isNullOrEmpty()) {
            Glide.with(this)
                .load(event.image)
                .placeholder(R.drawable.bg_gradient_header)
                .into(ivImage)
        }

        // Setup RSVP Button
        btnRsvp.text = "Join (${event.rsvpCount})"
        
        // Observe ViewModel Result
        viewModel.rsvpStatus.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    if (response != null) {
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                        // Update Count UI
                        btnRsvp.text = "Join (${response.rsvpCount})"
                        // TODO: Update 'Join' text to 'Joined' if we track state user-side
                    }
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, "Error: ${result.message}", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading -> {
                    // Optional: Show loading indicator
                }
            }
        }

        btnRsvp.setOnClickListener {
            viewModel.toggleEventRsvp(event._id)
        }
    }
}
