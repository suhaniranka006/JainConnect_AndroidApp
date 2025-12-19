package com.mycompany.jainconnect.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.User
import com.mycompany.jainconnect.ui.activities.EditProfileActivity
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

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
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            fetchData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        observeViewModel()

        btnGoToEditProfile.setOnClickListener {
            currentUser?.let {
                val intent = Intent(requireContext(), EditProfileActivity::class.java)
                intent.putExtra("USER_DATA", it)
                editProfileLauncher.launch(intent)
            } ?: run {
                Toast.makeText(context, "Profile data not loaded", Toast.LENGTH_SHORT).show()
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
            // Fallback: User logged in via 'Forgot Password' or no Token
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

                // Toast.makeText(context, "Profile partially loaded (Sync Pending)", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initializeViews(view: View) {
        ivUserProfile = view.findViewById(R.id.ivUserProfile)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail)
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone)
        tvProfileLocation = view.findViewById(R.id.tvProfileLocation)
        tvProfileDob = view.findViewById(R.id.tvProfileDob)
        tvProfileGender = view.findViewById(R.id.tvProfileGender)
        btnGoToEditProfile = view.findViewById(R.id.btnGoToEditProfile)
    }

    private fun showSyncDialog() {
        val input = android.widget.EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "Enter Password"

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Sync Account")
            .setMessage("Enter your password to sync with the server.")
            .setView(input)
            .setPositiveButton("Sync") { _, _ ->
                val password = input.text.toString()
                val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
                if (email != null && password.isNotEmpty()) {
                    viewModel.syncUser(email, password)
                } else {
                    Toast.makeText(context, "Email or Password missing", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUser = user
                populateData(user)
                // Restore Edit Button if fully synced
                btnGoToEditProfile.text = "Edit Profile"
                btnGoToEditProfile.setOnClickListener {
                    currentUser?.let {
                        val intent = Intent(requireContext(), EditProfileActivity::class.java)
                        intent.putExtra("USER_DATA", it)
                        editProfileLauncher.launch(intent)
                    }
                }
            }
        }

        viewModel.syncResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is com.mycompany.jainconnect.data.network.NetworkResult.Success -> {
                    Toast.makeText(context, "Sync Successful!", Toast.LENGTH_SHORT).show()
                    // Save Token
                    val sharedPref = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("jwt_token", result.data!!.token)
                        apply()
                    }
                    // Refresh Data
                    fetchData()
                }
                is com.mycompany.jainconnect.data.network.NetworkResult.Error -> {
                    Toast.makeText(context, "Sync Failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is com.mycompany.jainconnect.data.network.NetworkResult.Loading -> {
                     Toast.makeText(context, "Syncing...", Toast.LENGTH_SHORT).show()
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
        val sharedPref = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }
}
