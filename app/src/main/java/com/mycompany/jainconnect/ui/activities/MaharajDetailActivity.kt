package com.mycompany.jainconnect.ui.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Maharaj

class MaharajDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maharaj_detail)

        // Toolbar setup
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "" // Collapsing Toolbar handles title usually, or hide it
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Get Event Object
        val maharaj = intent.getSerializableExtra("EXTRA_MAHARAJ") as? Maharaj

        if (maharaj != null) {
            setupViews(maharaj)
        }
    }

    private fun setupViews(maharaj: Maharaj) {
        val ivImage: ImageView = findViewById(R.id.ivMaharajImageDetail)
        val tvName: TextView = findViewById(R.id.tvMaharajNameDetail)
        val tvSampraday: TextView = findViewById(R.id.tvMaharajSampradayDetail)
        val tvCity: TextView = findViewById(R.id.tvMaharajCityDetail)
        val tvArrival: TextView = findViewById(R.id.tvMaharajArrivalDetail)
        val tvVihar: TextView = findViewById(R.id.tvMaharajViharDetail)
        val tvDescription: TextView = findViewById(R.id.tvMaharajDescriptionDetail)
        val tvContact: TextView = findViewById(R.id.tvMaharajContactDetail)

        tvName.text = maharaj.name
        tvSampraday.text = maharaj.sampraday ?: "Gach / Community"
        tvCity.text = maharaj.city ?: "Unknown Location"
        
        // Date Logic (Prefer Arrival Date, Fallback to old 'date')
        tvArrival.text = maharaj.arrivalDate ?: maharaj.relevantDate ?: "--"
        tvVihar.text = maharaj.viharDate ?: "--"
        tvDescription.text = maharaj.description ?: "No description provided."
        
        val layoutContact: android.view.View = findViewById(R.id.layoutContactDetail)
        tvContact.text = maharaj.contactInfo ?: "No contact info available."
        
        layoutContact.setOnClickListener {
            maharaj.contactInfo?.let { phone ->
                if (phone.isNotBlank()) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                    intent.data = android.net.Uri.parse("tel:$phone")
                    startActivity(intent)
                }
            }
        }

        if (!maharaj.image.isNullOrEmpty()) {
            var imageUrl = maharaj.image!!
            if (!imageUrl.startsWith("http")) {
                val cleanPath = imageUrl.replace("\\", "/")
                imageUrl = "https://jainconnect-backened-2.onrender.com/$cleanPath"
            }

            Glide.with(this)
                 .load(imageUrl)
                 .placeholder(R.drawable.bg_gradient_header)
                 .into(ivImage)
        }
    }
}
