package com.example.jainconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * MainActivity serves as the home screen of the JainConnect app.
 * Provides navigation to Tithis, Events, and Maharaj Locations.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout for this Activity
        setContentView(R.layout.activity_main)

        // ------------------- Button Bindings -------------------
        val btnTithis: Button = findViewById(R.id.btnGoToTithi)
        val btnEvents: Button = findViewById(R.id.btnGoToEvents)
        val btnMaharaj: Button = findViewById(R.id.btnGoToMaharajLocation)

        // ------------------- Button Click Listeners -------------------

        // Navigate to TithiActivity
        btnTithis.setOnClickListener {
            val intent = Intent(this, TithiActivity::class.java)
            startActivity(intent)
        }

        // Navigate to EventActivity
        btnEvents.setOnClickListener {
            val intent = Intent(this, EventActivity::class.java)
            startActivity(intent)
        }

        // Navigate to MaharajLocationActivity
        btnMaharaj.setOnClickListener {
            val intent = Intent(this, MaharajLocationActivity::class.java)
            startActivity(intent)
        }
    }
}
