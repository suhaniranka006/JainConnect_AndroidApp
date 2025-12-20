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

class EventDetailActivity : AppCompatActivity() {

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
        if (!event.endDate.isNullOrEmpty()) {
            tvEndDate.text = "End: ${event.endDate}"
            (tvEndDate.parent as View).visibility = View.VISIBLE
        } else {
             (tvEndDate.parent as View).visibility = View.GONE
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

        // Setup RSVP Button (Functionality can be added here later, for now just a UI stub or Toast)
        btnRsvp.text = "Join (${event.rsvpCount})"
        btnRsvp.setOnClickListener {
            Toast.makeText(this, "RSVP Feature coming in detail view!", Toast.LENGTH_SHORT).show()
            // To implement real RSVP here, we'd need ViewModel / Repository access
        }
    }
}
