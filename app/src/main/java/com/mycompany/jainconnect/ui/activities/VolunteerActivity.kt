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
    }
}
