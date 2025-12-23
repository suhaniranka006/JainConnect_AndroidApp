package com.mycompany.jainconnect.ui.activities

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope 
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.location.Geocoder
import java.util.Locale


@AndroidEntryPoint
class AddEventActivity : AppCompatActivity() {



    private val viewModel: JainViewModel by viewModels()

    private lateinit var ivPreview: android.widget.ImageView
    private var selectedImageUri: android.net.Uri? = null
    
    // Image Picker Launcher
    private val pickImageLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            // Safe Preview with Glide
            com.bumptech.glide.Glide.with(this)
                .load(it)
                .centerCrop()
                .into(ivPreview)
        }
    }



    // Location - Auto-detected for Distance
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        // Initialize Views
        ivPreview = findViewById(R.id.ivEventImagePreview)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        
        val etTitle = findViewById<EditText>(R.id.etEventTitle)
        val etCity = findViewById<EditText>(R.id.etEventCity)
        val etStartDate = findViewById<EditText>(R.id.etStartDate)
        val etEndDate = findViewById<EditText>(R.id.etEndDate)
        val etTime = findViewById<EditText>(R.id.etEventTime) 
        val etDesc = findViewById<EditText>(R.id.etEventDesc)
        val etContact = findViewById<EditText>(R.id.etEventContact)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitEvent)
        
        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            // Get Data
            val title = etTitle.text.toString().trim()
            val city = etCity.text.toString().trim()
            val startDate = etStartDate.text.toString().trim()
            val endDate = etEndDate.text.toString().trim()
            val date = startDate // Legacy fallback
            val time = etTime.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val contact = etContact.text.toString().trim()

            // Get Token
            val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val token = sharedPref.getString("jwt_token", null)

            // Validate
            if (token != null && title.isNotEmpty() && city.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty() && time.isNotEmpty() && contact.isNotEmpty()) {
                
                // Show Loading
                val progressDialog = android.app.ProgressDialog(this)
                progressDialog.setMessage("Submitting Event...")
                progressDialog.setCancelable(false)
                progressDialog.show()



                // Auto-Geocode City for Distance Feature
                lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(this@AddEventActivity, Locale.getDefault())
                        // Attempt to get location from city name
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocationName(city, 1)
                        
                        if (!addresses.isNullOrEmpty()) {
                            currentLatitude = addresses[0].latitude
                            currentLongitude = addresses[0].longitude
                        }
                    } catch (e: Exception) {
                         e.printStackTrace()
                    } finally {
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            progressDialog.dismiss()
                            submitToViewModel(token, title, city, date, startDate, endDate, time, desc, contact)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields (dates, contact, etc.)", Toast.LENGTH_SHORT).show()
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
    
    private fun submitToViewModel(
        token: String, title: String, city: String, date: String, 
        startDate: String, endDate: String, time: String, desc: String, contact: String
    ) {
        val file = selectedImageUri?.let { getFileFromUri(it) }
        viewModel.submitNewEvent(token, title, city, date, startDate, endDate, time, desc, contact, file, currentLatitude, currentLongitude)
    }
    
    // Helper to get File from URI
    private fun getFileFromUri(uri: android.net.Uri): java.io.File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = java.io.File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                java.io.FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}