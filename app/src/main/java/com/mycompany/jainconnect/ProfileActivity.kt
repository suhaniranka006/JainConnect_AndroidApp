package com.mycompany.jainconnect
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
// import android.widget.LinearLayout // <-- OLD, INCORRECT IMPORT
import androidx.constraintlayout.widget.ConstraintLayout // <-- NEW, CORRECT IMPORT
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
// import com.mycompany.jainconnect.databinding.ActivityProfileBinding // This line was in your file but not used, so it's safe to keep or remove

class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel
    private lateinit var progressBar: ProgressBar

    // FIX: Changed from LinearLayout to ConstraintLayout
    private lateinit var profileContent: ConstraintLayout

    private lateinit var ivUserProfile: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvProfilePhone: TextView
    private lateinit var tvProfileLocation: TextView
    private lateinit var tvProfileDob: TextView
    private lateinit var tvProfileGender: TextView
    private lateinit var btnGoToEditProfile: Button

    private var currentUser: User? = null

    // EditProfileActivity se result handle karne ke liye
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Agar profile update hua hai, toh data dobara fetch karein
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            fetchData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        viewModel = ViewModelProvider(this)[JainViewModel::class.java]
        initializeViews()
        observeViewModel()

        btnGoToEditProfile.setOnClickListener {
            currentUser?.let {
                val intent = Intent(this, EditProfileActivity::class.java)
                intent.putExtra("USER_DATA", it) // User object ko agli screen par bhejein
                editProfileLauncher.launch(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume() // <-- CORRECTED
        fetchData()
    }

    private fun fetchData() {
        val token = getToken()
        if (token != null) {
            progressBar.visibility = View.VISIBLE
            profileContent.visibility = View.GONE
            viewModel.fetchUserProfile(token)
        } else {
            // Handle case where user is not logged in
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show()
            // Optional: Redirect to LoginActivity
        }
    }

    private fun initializeViews() {
        // ... findViewById for all views ...
        progressBar = findViewById(R.id.profileProgressBar)

        // This line will now work correctly
        profileContent = findViewById(R.id.profileContent)

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
            progressBar.visibility = View.GONE
            if (user != null) {
                currentUser = user
                profileContent.visibility = View.VISIBLE
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
        tvProfileDob.text = "DOB: ${user.dob?.split("T")?.get(0) ?: "Not Provided"}" // Date format theek karein
        tvProfileGender.text = "Gender: ${user.gender ?: "Not Provided"}"

        Glide.with(this)
            .load(user.profileImage)
            .placeholder(R.drawable.ic_launcher_foreground) // Placeholder image
            .into(ivUserProfile)
    }

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }
}
