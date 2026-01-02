package com.mycompany.jainconnect.ui.activities

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.mycompany.jainconnect.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolunteerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Add Event
        findViewById<android.view.View>(R.id.cardAddEvent).setOnClickListener {
            startActivity(android.content.Intent(this, AddEventActivity::class.java))
        }

        // Add Monk
        findViewById<android.view.View>(R.id.cardAddMonk).setOnClickListener {
            startActivity(android.content.Intent(this, AddMaharajActivity::class.java))
        }

        // Add Temple
        findViewById<android.view.View>(R.id.cardAddTemple).setOnClickListener {
            startActivity(android.content.Intent(this, AddTempleActivity::class.java))
        }

        // Add Bhojanshala
        findViewById<android.view.View>(R.id.cardAddBhojanshala).setOnClickListener {
            startActivity(android.content.Intent(this, AddBhojanshalaActivity::class.java))
        }
    }
}
