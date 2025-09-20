package com.example.jainconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ------------------- Button Bindings -------------------
        val btnTithis: Button = findViewById(R.id.btnGoToTithi)
        val btnEvents: Button = findViewById(R.id.btnGoToEvents)
        val btnMaharaj: Button = findViewById(R.id.btnGoToMaharajLocation)
        val btnProfile: Button = findViewById(R.id.btnGoToProfile) // Profile button

        // ------------------- Button Click Listeners -------------------
        btnTithis.setOnClickListener {
            startActivity(Intent(this, TithiActivity::class.java))
        }

        btnEvents.setOnClickListener {
            startActivity(Intent(this, EventActivity::class.java))
        }

        btnMaharaj.setOnClickListener {
            startActivity(Intent(this, MaharajLocationActivity::class.java))
        }

        // Navigate to ProfileActivity
        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}
