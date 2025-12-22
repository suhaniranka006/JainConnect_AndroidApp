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
import com.mycompany.jainconnect.data.models.Temple

class TempleDetailActivity : AppCompatActivity() {

    private lateinit var temple: Temple

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temple_detail)

        // Receive Data
        temple = intent.getSerializableExtra("TEMPLE_DATA") as? Temple ?: run {
            finish()
            return
        }

        setupToolbar()
        setupViews()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupViews() {
        // Image
        val ivImage = findViewById<ImageView>(R.id.ivTempleImageDetail)
        if (!temple.image.isNullOrEmpty()) {
            Glide.with(this)
                .load(temple.image)
                .placeholder(R.drawable.jainconnect_app_logo)
                .into(ivImage)
        } else {
            // keep default or set gradient
             ivImage.setImageResource(R.drawable.jainconnect_app_logo) // or gradient header
        }

        // Texts
        findViewById<TextView>(R.id.tvTempleNameDetail).text = temple.name
        findViewById<TextView>(R.id.tvTempleCityDetail).text = temple.city
        
        // Address
        val tvAddress = findViewById<TextView>(R.id.tvTempleAddressDetail)
        if (!temple.address.isNullOrEmpty()) {
            tvAddress.text = temple.address
        } else {
            tvAddress.text = "${temple.city} (Address not available)"
        }

        // Description
        val tvDesc = findViewById<TextView>(R.id.tvTempleDescDetail)
        tvDesc.text = temple.description?.takeIf { it.isNotEmpty() } ?: "No description available."

        // Contact Logic
        val layoutContact = findViewById<LinearLayout>(R.id.layoutContactDetail)
        val tvContact = findViewById<TextView>(R.id.tvTempleContactDetail)

        if (!temple.contact.isNullOrEmpty()) {
            layoutContact.visibility = View.VISIBLE
            tvContact.text = temple.contact
            layoutContact.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${temple.contact}")
                }
                startActivity(intent)
            }
        } else {
            layoutContact.visibility = View.GONE
        }
    }
}
