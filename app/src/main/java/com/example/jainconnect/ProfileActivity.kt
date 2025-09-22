package com.example.jainconnect


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jainconnect.databinding.ActivityProfileBinding // Is line ko import karein

class ProfileActivity : AppCompatActivity() {

    // View Binding ke liye ek variable banayein
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding ko initialize karein
        binding = ActivityProfileBinding.inflate(layoutInflater)
        // Apne layout ko set karein
        setContentView(binding.root)

        // User data ko load karne ke liye ek function call karein
        loadUserProfileData()

        // Buttons ke click listeners set karein
        setupClickListeners()
    }

    /**
     * Is function mein hum profile ke UI elements (jaise naam, email) par data set karte hain.
     */
    private fun loadUserProfileData() {
        // View Binding ka use karke સીધા IDs se views ko access karein
        binding.tvProfileName.text = "Kavisha Jain"
        binding.tvProfileEmail.text = "kavisha.jain@example.com"

        // Aap yahan profile picture bhi set kar sakti hain
        // For example:
        binding.ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
    }

    /**
     * Is function mein hum buttons aur doosre interactive views ke liye click events handle karte hain.
     */
    private fun setupClickListeners() {
        // Edit Profile button ka click listener
        binding.btnEditProfile.setOnClickListener {
            // Jab user is button par click karega, to yeh message dikhega
            Toast.makeText(this, "Edit Profile Clicked!", Toast.LENGTH_SHORT).show()
            // Yahan aap Edit Profile screen par jaane ka code likh sakti hain
        }

        // Logout button ka click listener
        binding.btnLogout.setOnClickListener {
            // LoginActivity ko kholne ke liye ek naya Intent banayein
            val intent = Intent(this, LoginActivity::class.java).apply {
                // Yeh flags pichhli saari activities ko app ki history se हटा denge.
                // Isse user 'Back' button dabakar wapas Profile screen par nahi aa paayega.
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            // Nayi activity shuru karein
              startActivity(intent)

            // Maujooda ProfileActivity ko foran band kar dein
            finish()
        }

        // Profile picture par click karne par action add kar sakte hain
        binding.ivProfilePicture.setOnClickListener {
            Toast.makeText(this, "Profile picture clicked. You can open image viewer.", Toast.LENGTH_SHORT).show()
        }
    }
}
