package com.example.jainconnect

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel
    private var selectedImageUri: Uri? = null

    // ✅ FIX: Saare UI elements declare kiye gaye hain
    private lateinit var ivEditProfile: ImageView
    private lateinit var btnChangeImage: Button
    private lateinit var etEditEmail: EditText
    private lateinit var etEditName: EditText
    private lateinit var etEditPhone: EditText
    private lateinit var etEditLocation: EditText
    private lateinit var etEditDob: EditText
    private lateinit var etEditGender: EditText // Assuming you have an EditText for gender
    private lateinit var btnSaveChanges: Button
    private lateinit var editProfileProgress: ProgressBar

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivEditProfile.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        viewModel = ViewModelProvider(this)[JainViewModel::class.java]
        initializeViews() // Views ko initialize karein

        val user = intent.getSerializableExtra("USER_DATA") as? User
        user?.let { populateInitialData(it) }

        btnChangeImage.setOnClickListener { imagePickerLauncher.launch("image/*") }
        btnSaveChanges.setOnClickListener { handleSaveChanges() }

        observeViewModel()
    }

    // ✅ FIX: initializeViews() function add kiya gaya hai
    private fun initializeViews() {
        ivEditProfile = findViewById(R.id.ivEditProfile)
        btnChangeImage = findViewById(R.id.btnChangeImage)
        etEditEmail = findViewById(R.id.etEditEmail)
        etEditName = findViewById(R.id.etEditName)
        etEditPhone = findViewById(R.id.etEditPhone) // Assuming you have these IDs in your XML
        etEditLocation = findViewById(R.id.etEditLocation)
        etEditDob = findViewById(R.id.etEditDob)
        etEditGender = findViewById(R.id.etEditGender)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        editProfileProgress = findViewById(R.id.editProfileProgress)
    }

    private fun populateInitialData(user: User) {
        etEditEmail.setText(user.email)
        etEditName.setText(user.name)
        etEditPhone.setText(user.phone ?: "")
        etEditLocation.setText(user.location ?: "")
        etEditDob.setText(user.dob?.split("T")?.get(0) ?: "")
        etEditGender.setText(user.gender ?: "")

        Glide.with(this).load(user.profileImage).into(ivEditProfile)
    }

    private fun handleSaveChanges() {
        // ✅ FIX: Saare fields se data nikala gaya hai
        val name = etEditName.text.toString().trim()
        val phone = etEditPhone.text.toString().trim()
        val location = etEditLocation.text.toString().trim()
        val dob = etEditDob.text.toString().trim()
        val gender = etEditGender.text.toString().trim()

        val token = getToken()
        if (token == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        editProfileProgress.visibility = View.VISIBLE
        val imageFile = selectedImageUri?.let { getFileFromUri(it) }

        // ✅ FIX: Saare variables ab available hain
        viewModel.updateProfile(token, name, phone, location, dob, gender, imageFile)
    }

    private fun observeViewModel() {
        viewModel.updateResult.observe(this) { response ->
            editProfileProgress.visibility = View.GONE
            if (response != null && response.isSuccessful) {
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ FIX: Helper functions poore kiye gaye hain
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("profile_image_update", ".jpg", cacheDir)
            tempFile.deleteOnExit()
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("EditProfileActivity", "Error creating file from URI", e)
            null
        }
    }

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }
}