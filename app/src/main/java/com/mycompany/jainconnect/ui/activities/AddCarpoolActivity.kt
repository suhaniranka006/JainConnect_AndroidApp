package com.mycompany.jainconnect.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddCarpoolActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_carpool)

        val etDriver = findViewById<TextInputEditText>(R.id.etDriverName)
        val etSource = findViewById<TextInputEditText>(R.id.etSource)
        val etDest = findViewById<TextInputEditText>(R.id.etDestination)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val etTime = findViewById<TextInputEditText>(R.id.etTime)
        val etSeats = findViewById<TextInputEditText>(R.id.etSeats)
        val etVehicle = findViewById<TextInputEditText>(R.id.etVehicleType)
        val etContact = findViewById<TextInputEditText>(R.id.etContact)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitRide)

        // Observe Result
        viewModel.addCarpoolResult.observe(this) { result ->
            if (result == "Success") {
                Toast.makeText(this, "Ride Published Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                btnSubmit.isEnabled = true
            }
        }

        btnSubmit.setOnClickListener {
            val driver = etDriver.text.toString()
            val source = etSource.text.toString()
            val dest = etDest.text.toString()
            val date = etDate.text.toString()
            val time = etTime.text.toString()
            val seatsStr = etSeats.text.toString()
            val vehicle = etVehicle.text.toString()
            val contact = etContact.text.toString()

            if (driver.isEmpty() || source.isEmpty() || dest.isEmpty() || date.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simple primitive validation
            val seats = seatsStr.toIntOrNull() ?: 1

            btnSubmit.isEnabled = false
            viewModel.submitNewCarpool(driver, source, dest, date, time, vehicle, seats, contact)
        }
    }
}
