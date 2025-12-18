package com.mycompany.jainconnect.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mycompany.jainconnect.R

class CreateYatraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_yatra)

        val templateTitle = intent.getStringExtra("TEMPLATE_TITLE")
        Toast.makeText(this, "Planning for: $templateTitle", Toast.LENGTH_SHORT).show()
    }
}
