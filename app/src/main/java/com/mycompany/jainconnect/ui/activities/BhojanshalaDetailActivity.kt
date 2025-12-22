package com.mycompany.jainconnect.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Bhojanshala

class BhojanshalaDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bhojanshala_detail)

        val bhojanshala = intent.getParcelableExtra<Bhojanshala>("bhojanshala_data")

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }

        if (bhojanshala != null) {
            setupViews(bhojanshala)
        }
    }

    private fun setupViews(bhojanshala: Bhojanshala) {
        val ivImage = findViewById<ImageView>(R.id.ivBhojanshalaImageDetail)
        val tvName = findViewById<TextView>(R.id.tvBhojanshalaNameDetail)
        val tvCity = findViewById<TextView>(R.id.tvBhojanshalaCityDetail)
        val tvAddress = findViewById<TextView>(R.id.tvBhojanshalaAddressDetail)
        val tvOpening = findViewById<TextView>(R.id.tvOpeningTime)
        val tvClosing = findViewById<TextView>(R.id.tvClosingTime)
        val tvContact = findViewById<TextView>(R.id.tvBhojanshalaContactDetail)
        val tvDesc = findViewById<TextView>(R.id.tvBhojanshalaDescDetail)
        val layoutContact = findViewById<LinearLayout>(R.id.layoutContactDetail)

        tvName.text = bhojanshala.name
        tvCity.text = bhojanshala.city
        tvAddress.text = bhojanshala.address
        
        // Timings Logic
        if (!bhojanshala.openingTime.isNullOrEmpty() || !bhojanshala.closingTime.isNullOrEmpty()) {
            tvOpening.text = "Opening: ${bhojanshala.openingTime ?: "N/A"}"
            tvClosing.text = "Closing: ${bhojanshala.closingTime ?: "N/A"}"
        } else {
            // Backward Compatibility: Split timings string
            val timings = bhojanshala.timings
            if (!timings.isNullOrEmpty() && timings.contains("-")) {
                val parts = timings.split("-")
                if (parts.size >= 2) {
                    tvOpening.text = "Opening: " + parts[0].trim()
                    tvClosing.text = "Closing: " + parts[1].trim()
                } else {
                    tvOpening.text = "Opening: $timings"
                    tvClosing.text = "Closing: N/A"
                }
            } else {
                 tvOpening.text = "Opening: ${timings ?: "N/A"}"
                 tvClosing.text = "Closing: N/A"
            }
        }

        tvDesc.text = bhojanshala.description ?: "No description available."

        if (!bhojanshala.contact.isNullOrEmpty()) {
            tvContact.text = bhojanshala.contact
            layoutContact.visibility = View.VISIBLE
            layoutContact.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${bhojanshala.contact}")
                startActivity(intent)
            }
        } else {
            layoutContact.visibility = View.GONE
        }

        if (!bhojanshala.image.isNullOrEmpty()) {
            Glide.with(this)
                .load(bhojanshala.image)
                .placeholder(R.drawable.jainconnect_app_logo)
                .into(ivImage)
        } else {
            ivImage.setImageResource(R.drawable.jainconnect_app_logo)
        }
    }
}
