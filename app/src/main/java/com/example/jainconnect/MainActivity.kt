package com.example.jainconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //used to restore activity,s state after is has beed destroyed and recreated
        setContentView(R.layout.activity_main)

        // ------------------- Button Bindings -------------------
        val btnTithis: Button = findViewById(R.id.btnTithi)
        val btnEvents: Button = findViewById(R.id.btnEvents)
        val btnMaharaj: Button = findViewById(R.id.btnMonks)
        val btnProfile: Button = findViewById(R.id.btnMe) // Profile button
        val btnHorizon: Button = findViewById(R.id.btnHorizons)
        val btnContact: Button = findViewById(R.id.btnContact)

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


        btnHorizon.setOnClickListener {
            startActivity(Intent(this, HorizonsActivity::class.java))
        }

        btnContact.setOnClickListener {
            startActivity(Intent(this, ContactActivity::class.java))
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
