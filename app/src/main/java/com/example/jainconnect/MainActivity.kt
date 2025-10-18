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
        val btnTithis: Button = findViewById(R.id.btnTithi)
        val btnEvents: Button = findViewById(R.id.btnEvents)
        val btnMaharaj: Button = findViewById(R.id.btnMonks)
        val btnProfile: Button = findViewById(R.id.btnMe) // Profile button

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

        // MainActivity.kt

// For example, in a button's click listener:
        val profileButton: Button = findViewById(R.id.btnMe)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
