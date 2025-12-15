package com.mycompany.jainconnect.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.User
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()

    private lateinit var ivUserProfile: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvProfilePhone: TextView
    private lateinit var tvProfileLocation: TextView
    private lateinit var tvProfileDob: TextView
    private lateinit var tvProfileGender: TextView
    private lateinit var btnGoToEditProfile: Button

    private var currentUser: User? = null

    // Launcher for handling results from EditProfileActivity
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            fetchData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        observeViewModel()
        
        btnGoToEditProfile.setOnClickListener {
            currentUser?.let {
                val intent = Intent(this, EditProfileActivity::class.java)
                intent.putExtra("USER_DATA", it)
                editProfileLauncher.launch(intent)
            } ?: run {
                Toast.makeText(this, "Profile data not loaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    private fun fetchData() {
        val token = getToken()
        if (token != null) {
            viewModel.fetchUserProfile(token)
        } else {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        ivUserProfile = findViewById(R.id.ivUserProfile)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileEmail = findViewById(R.id.tvProfileEmail)
        tvProfilePhone = findViewById(R.id.tvProfilePhone)
        tvProfileLocation = findViewById(R.id.tvProfileLocation)
        tvProfileDob = findViewById(R.id.tvProfileDob)
        tvProfileGender = findViewById(R.id.tvProfileGender)
        btnGoToEditProfile = findViewById(R.id.btnGoToEditProfile)
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(this) { user ->
            if (user != null) {
                currentUser = user
                populateData(user)
            } else {
                Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateData(user: User) {
        tvProfileName.text = "Name: ${user.name}"
        tvProfileEmail.text = "Email: ${user.email}"
        tvProfilePhone.text = "Phone: ${user.phone ?: "Not Provided"}"
        tvProfileLocation.text = "Location: ${user.location ?: "Not Provided"}"
        tvProfileDob.text = "DOB: ${user.dob?.split("T")?.get(0) ?: "Not Provided"}"
        tvProfileGender.text = "Gender: ${user.gender ?: "Not Provided"}"

        Glide.with(this)
            .load(user.profileImage)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(ivUserProfile)
    }

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }
}