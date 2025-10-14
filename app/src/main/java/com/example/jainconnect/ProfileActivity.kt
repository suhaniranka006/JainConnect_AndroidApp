package com.example.jainconnect
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.jainconnect.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    // Tag for Logcat
    private val TAG = "ProfileActivity"

    // Modern Activity Result API to get data back from EditProfileActivity
    private val editProfileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Data sent back from EditProfileActivity
                val data = result.data
                val updatedName = data?.getStringExtra("name") ?: ""
                val updatedEmail = data?.getStringExtra("email") ?: ""
                val updatedImageUrl = data?.getStringExtra("profileImageUrl") ?: ""

                // Update UI
                binding.tvProfileName.text = updatedName
                binding.tvProfileEmail.text = updatedEmail

                if (updatedImageUrl.isNotEmpty()) {
                    // Glide can be used if image is from URL
                    Glide.with(this)
                        .load(updatedImageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(binding.ivProfilePicture)
                }

                Log.d(TAG, "Profile updated: $updatedName, $updatedEmail, $updatedImageUrl")
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "Edit profile canceled or no data returned")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load default user data
        loadUserProfileData()

        // Set up buttons
        setupClickListeners()
    }

    private fun loadUserProfileData() {
        binding.tvProfileName.text = "Kavisha Jain"
        binding.tvProfileEmail.text = "kavisha.jain@example.com"
        binding.ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            // Open EditProfileActivity for result
            val intent = Intent(this, EditProfileActivity::class.java)

            // Optional: pass current data so user can see it in Edit screen
            intent.putExtra("name", binding.tvProfileName.text.toString())
            intent.putExtra("email", binding.tvProfileEmail.text.toString())

            editProfileLauncher.launch(intent)
        }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        binding.ivProfilePicture.setOnClickListener {
            Toast.makeText(this, "Profile picture clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
