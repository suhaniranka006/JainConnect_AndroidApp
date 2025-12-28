package com.mycompany.jainconnect.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        // Observe Results
        viewModel.addCarpoolResult.observe(this) { result ->
            if (result == "Success") {
                Toast.makeText(this, "Ride Published Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                btnSubmit.isEnabled = true
            }
        }
        
        viewModel.rideActionResult.observe(this) { result ->
            if (result == "Updated") {
                 Toast.makeText(this, "Ride Updated Successfully!", Toast.LENGTH_SHORT).show()
                 finish()
            } else if (result.startsWith("Update Failed") || result.startsWith("Update Error")) {
                 Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                 btnSubmit.isEnabled = true
            }
        }
        
        // Check Edit Mode
        val isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        val rideData = intent.getSerializableExtra("RIDE_DATA") as? com.mycompany.jainconnect.data.models.Carpool
        
        if (isEditMode && rideData != null) {
            btnSubmit.text = "Update Ride"
            etDriver.setText(rideData.driverName)
            etSource.setText(rideData.source)
            etDest.setText(rideData.destination)
            etDate.setText(rideData.date)
            etTime.setText(rideData.time)
            etSeats.setText((rideData.seatsAvailable ?: 1).toString())
            etVehicle.setText(rideData.vehicleType)
            etContact.setText(rideData.contactNumber)
            // Safety check for ladies only
            findViewById<android.widget.CheckBox>(R.id.cbLadiesOnly).isChecked = rideData.isLadiesOnly ?: false
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
            val isLadiesOnly = findViewById<android.widget.CheckBox>(R.id.cbLadiesOnly).isChecked
            
            if (driver.isEmpty() || source.isEmpty() || dest.isEmpty() || date.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val seats = seatsStr.toIntOrNull() ?: 1
            
            // Get Token
            val sharedPref = getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
            val token = sharedPref.getString("jwt_token", null)
            
            if (token != null) {
                btnSubmit.isEnabled = false // Prevent double taps
                
                // --- GEOCODING (Background) ---
                lifecycleScope.launch(Dispatchers.IO) {
                    var sLat: Double? = null
                    var sLng: Double? = null
                    var dLat: Double? = null
                    var dLng: Double? = null
                    
                    try {
                        if (android.location.Geocoder.isPresent()) {
                            val geocoder = android.location.Geocoder(this@AddCarpoolActivity, java.util.Locale.getDefault())
                            
                            // Source Coords
                            try {
                                val sList = geocoder.getFromLocationName(source, 1)
                                if (!sList.isNullOrEmpty()) {
                                    sLat = sList[0].latitude
                                    sLng = sList[0].longitude
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                            
                            // Dest Coords
                            try {
                                val dList = geocoder.getFromLocationName(dest, 1)
                                if (!dList.isNullOrEmpty()) {
                                    dLat = dList[0].latitude
                                    dLng = dList[0].longitude
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    // --- SUBMIT TO VM (Main Thread) ---
                    withContext(Dispatchers.Main) {
                         // Check Geocoding Fail
                         if (sLat == null && driver.isNotEmpty()) { // Just a sanity check to show toast once
                             Toast.makeText(this@AddCarpoolActivity, "Warning: Could not fetch GPS for City. Sort may fail.", Toast.LENGTH_LONG).show()
                         }

                         if (isEditMode && rideData != null) {
                             val rideId = rideData.id ?: rideData._id ?: ""
                             if (rideId.isNotEmpty()) {
                                viewModel.updateRide(
                                    token, rideId, rideData, 
                                    driver, source, dest, date, time, vehicle, seats, contact, isLadiesOnly,
                                    sourceLat = sLat, sourceLng = sLng, destLat = dLat, destLng = dLng
                                )
                             }
                         } else {
                            viewModel.submitNewCarpool(
                                token, driver, source, dest, date, time, vehicle, seats, contact, isLadiesOnly,
                                sourceLat = sLat, sourceLng = sLng, destLat = dLat, destLng = dLng
                            )
                         }
                    }
                }
            } else {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
