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

    private lateinit var ivPreview: android.widget.ImageView
    private var selectedImageUri: android.net.Uri? = null
    
    // Image Picker Launcher
    private val pickImageLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            ivPreview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        // Initialize Views
        ivPreview = findViewById(R.id.ivEventImagePreview)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        
        val etTitle = findViewById<EditText>(R.id.etEventTitle)
        val etCity = findViewById<EditText>(R.id.etEventCity)
        // val etDate = findViewById<EditText>(R.id.etEventDate) // Removed
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
                val file = selectedImageUri?.let { getFileFromUri(it) }
                
                // Submit to ViewModel
                viewModel.submitNewEvent(token, title, city, date, startDate, endDate, time, desc, contact, file)
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
    
    // Helper to get File from URI
    private fun getFileFromUri(uri: android.net.Uri): java.io.File? {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = java.io.File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            java.io.FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}