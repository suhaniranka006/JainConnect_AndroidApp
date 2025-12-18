package com.mycompany.jainconnect.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Tirthyatra
import com.mycompany.jainconnect.databinding.ActivityTirthyatraDetailsBinding
import com.mycompany.jainconnect.ui.adapters.YatraDetailsPagerAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class TirthyatraDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTirthyatraDetailsBinding
    private val viewModel: JainViewModel by viewModels()
    private var tirthyatra: Tirthyatra? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTirthyatraDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get Data
        tirthyatra = intent.getParcelableExtra("YATRA_DATA")

        setupUI()
        setupObservers()
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        val token = getToken()
        if (token.isNotEmpty()) {
            viewModel.fetchUserProfile(token)
        }
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        tirthyatra?.let { yatra ->
            binding.collapsingToolbar.title = yatra.title
            binding.tvTitle.text = yatra.title
            
            // Image
            if (!yatra.imageUrl.isNullOrEmpty()) {
                var url = yatra.imageUrl
                if (!url.startsWith("http")) {
                    // It's a relative path, probably from uploads
                    // Fix backslashes for Windows paths
                    url = url.replace("\\", "/")
                    // Prepend Base URL (Hardcoded or injected preferred, but for quick fix using constant)
                    // TODO: Inject Base URL properly
                    val baseUrl = "https://jainconnect-backened-2.onrender.com/" // Or "http://10.0.2.2:5000/" for local
                    // We should match the Retrofit Base URL
                    url = "$baseUrl$url"
                }

                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_tirthyatra)
                    .into(binding.ivHeaderImage)
            } else {
                binding.ivHeaderImage.setImageResource(R.drawable.ic_tirthyatra)
            }
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val start = yatra.startDate?.let { sdf.format(it) } ?: "??"
            val end = yatra.endDate?.let { sdf.format(it) } ?: "??"
            binding.tvDateRange.text = "$start - $end"

            // Load Members Fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, com.mycompany.jainconnect.ui.fragments.YatraMembersFragment.newInstance(yatra))
                .commit()
            
            // Join Button Logic (Initial, updated by Observer)
            binding.btnJoinYatra.setOnClickListener {
                joinYatra()
            }
        }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { user ->
            currentUserId = user?.id
            checkJoinStatus()
        }

        viewModel.yatraOperationResult.observe(this) { result ->
            if (result == "Joined") {
                Toast.makeText(this, "Joined successfully!", Toast.LENGTH_SHORT).show()
                binding.btnJoinYatra.visibility = View.GONE
            } else if (result == "Deleted") {
                Toast.makeText(this, "Yatra deleted successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish() // Close details and go back
            } else if (result.startsWith("Failed")) {
                 Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_yatra_details, menu)
        
        // Visibility check
        val deleteItem = menu?.findItem(R.id.action_delete)
        if (currentUserId != null && tirthyatra?.creatorId?.id == currentUserId) {
            deleteItem?.isVisible = true
        } else {
            deleteItem?.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showDeleteConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Yatra")
            .setMessage("Are you sure you want to delete this yatra? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val token = getToken()
                tirthyatra?.id?.let { id ->
                    viewModel.deleteYatra(token, id)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Existing checkJoinStatus (updated to invalidate menu)
    private fun checkJoinStatus() {
        // Invalidate menu to trigger onCreateOptionsMenu visibility check
        invalidateOptionsMenu()
        
        tirthyatra?.let { yatra ->
            if (currentUserId != null) {
                // Check if already a participant (participants is List<TirthyatraUser>)
                val isParticipant = yatra.participants.any { it.id == currentUserId }
                
                // Show button ONLY if:
                // 1. Not a participant
                // 2. Yatra is Public (assuming Visibility field, default Private)
                // 3. (Optional) Not creator (creator is always participant usually)
                
                if (!isParticipant && yatra.visibility == "Public") {
                    binding.btnJoinYatra.visibility = View.VISIBLE
                } else {
                    binding.btnJoinYatra.visibility = View.GONE
                }
            }
        }
    }

    private fun joinYatra() {
        val token = getToken()
        if (token.isEmpty()) {
            Toast.makeText(this, "Please Login", Toast.LENGTH_SHORT).show()
            return
        }
        tirthyatra?.id?.let { id ->
            viewModel.joinYatra(token, id)
        }
    }

    private fun getToken(): String {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", "") ?: ""
    }
}
