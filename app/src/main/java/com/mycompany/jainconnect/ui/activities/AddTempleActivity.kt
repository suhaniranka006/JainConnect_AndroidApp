package com.mycompany.jainconnect.ui.activities

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel

@AndroidEntryPoint
class AddTempleActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_add_temple)

        // Initialize Views
        ivPreview = findViewById(R.id.ivTempleImagePreview)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)

        val etName = findViewById<EditText>(R.id.etName)
        val etCity = findViewById<EditText>(R.id.etCity)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etContact = findViewById<EditText>(R.id.etContact)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val city = etCity.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val contact = etContact.text.toString().trim()
            val description = etDescription.text.toString().trim()

            val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val token = sharedPref.getString("jwt_token", null)

            if (token != null && name.isNotEmpty() && city.isNotEmpty()) {
                val file = selectedImageUri?.let { getFileFromUri(it) }
                // Note: File is optional in JVM but backend expects it for /with-image endpoint logic if we want image.
                viewModel.submitNewTemple(token, name, city, address, contact, description, file)
            } else {
                Toast.makeText(this, "Please fill required fields (Name, City)", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.addTempleResult.observe(this) { result ->
            if (result == "Success") {
                Toast.makeText(this, "Temple Sent for Approval!", Toast.LENGTH_LONG).show()
                finish()
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
