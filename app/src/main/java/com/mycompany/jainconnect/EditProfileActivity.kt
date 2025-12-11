package com.mycompany.jainconnect

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button // Yeh import zaroori hai
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.bumptech.glide.Glide
import java.io.File
import kotlin.jvm.java


@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    // UI Elements
    private lateinit var ivEditProfile: ImageView
    private lateinit var btnChangeImage: TextView
    private lateinit var etEditEmail: EditText
    private lateinit var etEditName: EditText
    private lateinit var etEditPhone: EditText
    private lateinit var etEditLocation: EditText
    private lateinit var etEditDob: EditText
    private lateinit var etEditGender: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var editProfileProgress: ProgressBar

    // --- NEW LOGOUT BUTTON ---
    private lateinit var btnLogout: Button

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivEditProfile.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        // 1. Saare views ko initialize karein
        initializeViews()

        // 2. Data populate karein
        val user = intent.getSerializableExtra("USER_DATA") as? User
        user?.let { populateInitialData(it) }

        // 3. Click Listeners set karein
        btnChangeImage.setOnClickListener { imagePickerLauncher.launch("image/*") }
        btnSaveChanges.setOnClickListener { handleSaveChanges() }

        // --- NEW LOGOUT CLICK LISTENER ---
        btnLogout.setOnClickListener {
            handleLogout()
        }
        // ---------------------------------

        // 4. ViewModel ko observe karein
        observeViewModel()
    }

    private fun initializeViews() {
        ivEditProfile = findViewById(R.id.ivEditProfile)
        btnChangeImage = findViewById(R.id.btnChangeImage)
        etEditEmail = findViewById(R.id.etEditEmail)
        etEditName = findViewById(R.id.etEditName)
        etEditPhone = findViewById(R.id.etEditPhone)
        etEditLocation = findViewById(R.id.etEditLocation)
        etEditDob = findViewById(R.id.etEditDob)
        etEditGender = findViewById(R.id.etEditGender)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        editProfileProgress = findViewById(R.id.editProfileProgress)

        // --- LOGOUT BUTTON KO INITIALIZE KAREIN ---
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun populateInitialData(user: User) {
        // ... (Aapka pehle ka code, bilkul sahi hai)
        etEditEmail.setText(user.email)
        etEditName.setText(user.name)
        etEditPhone.setText(user.phone ?: "")
        etEditLocation.setText(user.location ?: "")
        etEditDob.setText(user.dob?.split("T")?.get(0) ?: "")
        etEditGender.setText(user.gender ?: "")
        Glide.with(this).load(user.profileImage).into(ivEditProfile)
    }

    private fun handleSaveChanges() {

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

        viewModel.updateProfile(token, name, phone, location, dob, gender, imageFile)
    }

    // --- NEW LOGOUT FUNCTION ---
    private fun handleLogout() {
        // 1. SessionManager se session clear karein
        val session = SessionManager(this) // 'this' context use karein
        session.clearSession() // Yeh 'isLoggedIn' ko false kar dega

        // 2. User ko waapis LoginActivity bhej dein
        val intent = Intent(this, LoginActivity::class.java)

        // Saari pichli activities ko clear karein
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // Iss EditProfileActivity ko band kar dein
        finish()
    }
    // -------------------------

    private fun observeViewModel() {
        // ... (Aapka pehle ka code, bilkul sahi hai)
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

    private fun getFileFromUri(uri: Uri): File? {
        // ... (Aapka pehle ka code, bilkul sahi hai)
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
        // ... (Aapka pehle ka code, bilkul sahi hai)
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }
}
