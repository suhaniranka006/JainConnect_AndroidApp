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
            // Fallback: User logged in via 'Forgot Password' (No Backend Token)
            // Use Firebase Data directly
            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                // Manually populate views with available data
                tvProfileName.text = "Name: ${firebaseUser.displayName ?: "user"}"
                tvProfileEmail.text = "Email: ${firebaseUser.email ?: "Not Available"}"
                tvProfilePhone.text = "Phone: Not Synced (Re-Login to Sync)"
                tvProfileLocation.text = "Location: Not Synced"
                tvProfileDob.text = "DOB: Not Synced"
                tvProfileGender.text = "Gender: Not Synced"

                Glide.with(this)
                    .load(firebaseUser.photoUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(ivUserProfile)
                
                 // Allow user to trigger sync
                btnGoToEditProfile.text = "Sync Account"
                btnGoToEditProfile.setOnClickListener {
                    showSyncDialog()
                }

                Toast.makeText(this, "Profile partially loaded (Sync Pending)", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show()
            }
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
        tvProfileDob = findViewById(R.id.tvProfileDob)
        tvProfileGender = findViewById(R.id.tvProfileGender)
        btnGoToEditProfile = findViewById(R.id.btnGoToEditProfile)
    }

    private fun showSyncDialog() {
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "Enter Password"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Sync Account")
            .setMessage("Enter your password to sync with the server.")
            .setView(input)
            .setPositiveButton("Sync") { _, _ ->
                val password = input.text.toString()
                val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
                if (email != null && password.isNotEmpty()) {
                    viewModel.syncUser(email, password)
                } else {
                    Toast.makeText(this, "Email or Password missing", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(this) { user ->
            if (user != null) {
                currentUser = user
                populateData(user)
                // Restore Edit Button if fully synced
                btnGoToEditProfile.text = "Edit Profile"
                btnGoToEditProfile.setOnClickListener {
                     currentUser?.let {
                        val intent = Intent(this, EditProfileActivity::class.java)
                        intent.putExtra("USER_DATA", it)
                        editProfileLauncher.launch(intent)
                    }
                }
            } else {
                Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.syncResult.observe(this) { result ->
            when (result) {
                is com.mycompany.jainconnect.data.network.NetworkResult.Success -> {
                    Toast.makeText(this, "Sync Successful!", Toast.LENGTH_SHORT).show()
                    // Save Token
                    val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("jwt_token", result.data!!.token)
                        apply()
                    }
                    // Refresh Data
                    fetchData()
                }
                is com.mycompany.jainconnect.data.network.NetworkResult.Error -> {
                    Toast.makeText(this, "Sync Failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is com.mycompany.jainconnect.data.network.NetworkResult.Loading -> {
                     Toast.makeText(this, "Syncing...", Toast.LENGTH_SHORT).show()
                }
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