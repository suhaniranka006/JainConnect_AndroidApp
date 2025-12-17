package com.mycompany.jainconnect.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AddMaharajActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()
    private var selectedImageFile: File? = null
    private lateinit var ivMaharajImage: ImageView

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                ivMaharajImage.setImageURI(it)
                selectedImageFile = getFileFromUri(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_maharaj)

        // 1. Bind Views (Matches IDs in activity_add_maharaj.xml)
        val etName = findViewById<EditText>(R.id.etMaharajName)
        val etTitle = findViewById<EditText>(R.id.etMaharajTitle) 
        val etCity = findViewById<EditText>(R.id.etMaharajLocation)
        val etDate = findViewById<EditText>(R.id.etMaharajDate)
        val etContact = findViewById<EditText>(R.id.etMaharajContact)
        ivMaharajImage = findViewById(R.id.ivMaharajImage)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitMaharaj)

        // Set today's date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etDate.setText(dateFormat.format(Date()))

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        btnSubmit.setOnClickListener {
            // 2. Get Text values
            val name = etName.text.toString().trim()
            val title = etTitle.text.toString().trim()
            val city = etCity.text.toString().trim()
            val date = etDate.text.toString().trim()
            val contact = etContact.text.toString().trim()

            // 3. Get Auth Token
            val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val token = sharedPref.getString("jwt_token", null)

            // 4. Validate Input
            if (token != null && name.isNotEmpty() && city.isNotEmpty()) {
                // 5. Submit Data
                if (selectedImageFile != null) {
                    viewModel.submitNewMaharajWithImage(token, name, title, city, date, contact, selectedImageFile)
                } else {
                    viewModel.submitNewMaharaj(token, name, title, city, date, contact)
                }
            } else {
                Toast.makeText(this, "Please fill at least Name and City (and login)", Toast.LENGTH_SHORT).show()
            }
        }

        val tvErrorLog = findViewById<android.widget.TextView>(R.id.tvErrorLog)

        // 6. Observe Result
        viewModel.addMaharajResult.observe(this) { result ->
            if (result == "Success") {
                Toast.makeText(this, "Location Sent for Approval!", Toast.LENGTH_LONG).show()
                finish() // Close screen on success
            } else {
                tvErrorLog.visibility = android.view.View.VISIBLE
                tvErrorLog.text = "ERROR:\n$result"
                Toast.makeText(this, "Submission Failed! See details above.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val contentResolver = contentResolver
        val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
        
        try {
            // 1. Decode URI to Bitmap
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // 2. Compress Bitmap to JPEG (Quality 70)
            val outputStream = java.io.FileOutputStream(tempFile)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            outputStream.flush()
            outputStream.close()
            
            val fileSizeInKB = tempFile.length() / 1024
            android.widget.Toast.makeText(this, "Compressed to: ${fileSizeInKB} KB", android.widget.Toast.LENGTH_LONG).show()

            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}