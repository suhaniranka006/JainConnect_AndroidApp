package com.mycompany.jainconnect.ui.activities

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Event
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel

@AndroidEntryPoint
class AddEventActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        // viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        // Initialize Views
        val etTitle = findViewById<EditText>(R.id.etEventTitle)
        val etCity = findViewById<EditText>(R.id.etEventCity)
        val etDate = findViewById<EditText>(R.id.etEventDate)
        val etTime = findViewById<EditText>(R.id.etEventTime) // ✅ New
        val etDesc = findViewById<EditText>(R.id.etEventDesc)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitEvent)

        btnSubmit.setOnClickListener {
            // Get Data
            val title = etTitle.text.toString().trim()
            val city = etCity.text.toString().trim()
            val date = etDate.text.toString().trim()
            val time = etTime.text.toString().trim() // ✅ New
            val desc = etDesc.text.toString().trim()

            // Get Token
            val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val token = sharedPref.getString("jwt_token", null)

            // Validate
            if (token != null && title.isNotEmpty() && city.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()) {
                // Submit to ViewModel
                viewModel.submitNewEvent(token, title, city, date, time, desc)
            } else {
                Toast.makeText(this, "Please fill all fields (Title, City, Date, Time)", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe Result
        viewModel.addEventResult.observe(this) { result ->
            if (result == "Success") {
                Toast.makeText(this, "Event Sent to Admin for Approval!", Toast.LENGTH_LONG).show()
                finish() // Close screen
            } else {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            }
        }
    }
}