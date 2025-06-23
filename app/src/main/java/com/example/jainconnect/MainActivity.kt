package com.example.jainconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button // Or whatever UI elements you are using
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Assuming you have activity_main.xml



        val btnTithis: Button = findViewById(R.id.btnGoToTithi) // Replace with your actual Button ID
        val btnEvents: Button = findViewById(R.id.btnGoToEvents) // Replace with your actual Button ID
        val btnMaharaj: Button = findViewById(R.id.btnGoToMaharajLocation) // Replace with your actual Button ID

        btnTithis.setOnClickListener {
            val intent = Intent(this, TithiActivity::class.java)
            startActivity(intent)
        }

        btnEvents.setOnClickListener {
            // THIS IS LIKELY WHERE THE ISSUE FOR EVENTS IS
            val intent = Intent(this, EventActivity::class.java) // Ensure it's EventActivity::class.java
            startActivity(intent)
        }

        btnMaharaj.setOnClickListener {
            // THIS IS LIKELY WHERE THE ISSUE FOR MAHARAJ LOCATION IS
            val intent = Intent(this, MaharajLocationActivity::class.java) // Should be MaharajLocationActivity::class.java, NOT TithiActivity
            startActivity(intent)
        }
    }
}

