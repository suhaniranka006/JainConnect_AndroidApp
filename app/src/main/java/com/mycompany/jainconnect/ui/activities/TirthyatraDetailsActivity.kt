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

        // Observe single yatra details
        viewModel.yatraDetails.observe(this) { yatra ->
            if (yatra != null) {
                this.tirthyatra = yatra
                // Update UI elements that might change
                binding.collapsingToolbar.title = yatra.title
                
                // Update header image if changed (optional, but good practice)
                if (!yatra.imageUrl.isNullOrEmpty()) {
                     var url = yatra.imageUrl
                     if (!url.startsWith("http")) {
                         val baseUrl = "https://jainconnect-backened-2.onrender.com/"
                         url = url.replace("\\", "/")
                         url = "$baseUrl$url"
                     }
                     Glide.with(this).load(url).placeholder(R.drawable.ic_tirthyatra).into(binding.ivHeaderImage)
                }
                
                // Refresh members fragment
                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (fragment is com.mycompany.jainconnect.ui.fragments.YatraMembersFragment) {
                    fragment.updateData(yatra)
                }
                
                checkJoinStatus()
            }
        }

        viewModel.yatraOperationResult.observe(this) { result ->
            if (result == "Joined") {
                Toast.makeText(this, "Request Sents / Joined successfully!", Toast.LENGTH_SHORT).show()
                refreshYatraDetails()
            } else if (result == "Deleted") {
                Toast.makeText(this, "Yatra deleted successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else if (result == "Request Cancelled" || result.startsWith("Companionship")) {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                refreshYatraDetails()
            } else if (result.startsWith("Failed")) {
                 Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshYatraDetails() {
         val token = getToken()
         tirthyatra?.id?.let { id ->
             viewModel.loadYatraDetails(token, id)
         }
    }

    // Existing checkJoinStatus (updated to invalidate menu)
    private fun checkJoinStatus() {
        invalidateOptionsMenu()
        
        tirthyatra?.let { yatra ->
            if (currentUserId != null) {
                val isCreator = yatra.creatorId?.id == currentUserId
                val isParticipant = yatra.participants.any { it.id == currentUserId }
                // Pending check: pendingRequests is List<JoinRequest>. Check if ANY request has userId == currentUserId
                // Note: JoinRequest.userId is TirthyatraUser object.
                val isPending = yatra.pendingRequests.any { it.userId?.id == currentUserId }

                if (isCreator) {
                    // Creator View
                    binding.llActions.visibility = View.VISIBLE
                    binding.btnJoinYatra.visibility = View.GONE
                    
                    binding.btnCompanionship.visibility = View.VISIBLE
                    if (yatra.visibility == "Public") {
                        binding.btnCompanionship.text = "Disable Companionship"
                        binding.btnCompanionship.setBackgroundColor(getColor(android.R.color.darker_gray))
                    } else {
                        binding.btnCompanionship.text = "I want Companionship"
                        binding.btnCompanionship.setBackgroundColor(getColor(R.color.saffron))
                    }
                    
                    if (yatra.pendingRequests.isNotEmpty()) {
                        binding.btnManageRequests.visibility = View.VISIBLE
                        binding.btnManageRequests.text = "Pending Requests (${yatra.pendingRequests.size})"
                    } else {
                        binding.btnManageRequests.visibility = View.GONE
                    }
                    
                    // Click Listeners
                    binding.btnCompanionship.setOnClickListener {
                        val enable = yatra.visibility != "Public"
                        if (enable) {
                            // Show Dialog to collect details
                            showCompanionshipDetailsDialog(yatra.id!!)
                        } else {
                            viewModel.toggleCompanionship(getToken(), yatra.id!!, false)
                        }
                    }
                    
                    binding.btnManageRequests.setOnClickListener {
                        showManageRequestsDialog()
                    }

                } else {
                    // Public / Participant View
                    binding.llActions.visibility = View.GONE // Hide admin actions
                    
                    if (isParticipant) {
                        binding.btnJoinYatra.visibility = View.GONE
                    } else if (isPending) {
                        binding.btnJoinYatra.visibility = View.VISIBLE
                        binding.btnJoinYatra.text = "Cancel Request"
                        binding.btnJoinYatra.setBackgroundColor(getColor(android.R.color.holo_red_light))
                        binding.btnJoinYatra.setOnClickListener {
                            viewModel.cancelRequest(getToken(), yatra.id!!)
                        }
                    } else if (yatra.visibility == "Public") {
                         binding.btnJoinYatra.visibility = View.VISIBLE
                         binding.btnJoinYatra.text = "Wanna Join?"
                         binding.btnJoinYatra.setBackgroundColor(getColor(R.color.saffron))
                         binding.btnJoinYatra.setOnClickListener {
                             showJoinDialog()
                         }
                    } else {
                        binding.btnJoinYatra.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showManageRequestsDialog() {
        if (tirthyatra?.pendingRequests.isNullOrEmpty()) {
            Toast.makeText(this, "No pending requests", Toast.LENGTH_SHORT).show()
            return
        }

        val requests = tirthyatra?.pendingRequests!!
        
        // Optimization: If only one request, show details/actions directly
        if (requests.size == 1) {
            showRequestDetailsDialog(requests[0])
            return
        }

        val items = requests.map { req -> 
             "${req.userId?.name ?: "Unknown"} \nMessage: ${req.message ?: "No message"}" 
        }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Pending Requests (Tap to Manage)")
            .setItems(items) { _, which ->
                showRequestDetailsDialog(requests[which])
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showRequestDetailsDialog(request: com.mycompany.jainconnect.data.models.JoinRequest) {
        val user = request.userId
        // Prefer manual details if available, else user object
        // Prefer manual details if available, else user object fallback
        val nameToDisplay = request.name ?: user?.name ?: "Unknown User"
        // request.age might be "25" OR null. user.dob might be ISO Date String.
        // Use AgeUtils to handle both cases efficiently.
        val rawAgeSource = request.age ?: user?.dob
        val ageToDisplay = com.mycompany.jainconnect.utils.AgeUtils.calculateAge(rawAgeSource)
        
        val genderToDisplay = request.gender ?: user?.gender ?: "N/A"
        
        val reqContact = request.contactNumber ?: user?.phone ?: "N/A"
        val peopleCount = request.peopleCount // Default 1 if null? Model has default.
        
        val message = "Name: $nameToDisplay\n" +
                      "Age: $ageToDisplay\n" +
                      "Gender: $genderToDisplay\n" +
                      "Contact: $reqContact\n" +
                      "People: $peopleCount\n\n" +
                      "Message:\n${request.message ?: "No message"}"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Request Details")
            .setMessage(message)
            .setPositiveButton("Accept") { _, _ ->
                user?.id?.let { userId ->
                    tirthyatra?.id?.let { yatraId ->
                        viewModel.manageMember(getToken(), yatraId, userId, "approve")
                    }
                }
            }
            .setNegativeButton("Reject") { _, _ ->
                 user?.id?.let { userId ->
                    tirthyatra?.id?.let { yatraId ->
                        viewModel.manageMember(getToken(), yatraId, userId, "reject")
                    }
                }
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showJoinDialog() {

        // Fallback to code layout if XML doesn't exist yet, but better to create XML.
        // Let's build layout dynamically for speed or create XML next.
        // Valid approach: Create AlertDialog with View.
        
        // I will create a simple LinearLayout programmatically here to avoid blocking on XML creation step immediately.
        val context = this
        val layout = android.widget.LinearLayout(context)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val etMessage = android.widget.EditText(context)
        etMessage.hint = "Message for Creator (e.g. I am alone...)"
        layout.addView(etMessage)
        
        val etContact = android.widget.EditText(context)
        etContact.hint = "Contact Number"
        etContact.inputType = android.text.InputType.TYPE_CLASS_PHONE
        // Prefill contact if available? 
        // We don't have current user phone in 'userProfile' LiveData details easily here unless we store it.
        // User profile IS in 'viewModel.userProfile'.
        viewModel.userProfile.value?.let { 
            // etContact.setText(it.phone) // Assuming User model has phone accessible
            // User model in Android 'User' might need check.
        }
        layout.addView(etContact)
        
        val etPeopleCount = android.widget.EditText(context)
        etPeopleCount.hint = "Number of People (e.g., 2)"
        etPeopleCount.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        etPeopleCount.setText("1")
        layout.addView(etPeopleCount)

        val etName = android.widget.EditText(context)
        etName.hint = "My Name"
         viewModel.userProfile.value?.let { etName.setText(it.name) }
        layout.addView(etName)
        
        val etAge = android.widget.EditText(context)
        etAge.hint = "My Age"
        etAge.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(etAge)
        
        val etGender = android.widget.EditText(context)
        etGender.hint = "My Gender"
         viewModel.userProfile.value?.let { etGender.setText(it.gender) }
        layout.addView(etGender)

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Join Request")
            .setView(layout)
            .setPositiveButton("Send") { _, _ ->
                val message = etMessage.text.toString()
                val contact = etContact.text.toString()
                val countStr = etPeopleCount.text.toString()
                val name = etName.text.toString()
                val age = etAge.text.toString()
                val gender = etGender.text.toString()
                
                if (contact.isEmpty() || name.isEmpty() || age.isEmpty() || gender.isEmpty()) {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val peopleCount = if (countStr.isNotEmpty()) countStr.toIntOrNull() ?: 1 else 1
                viewModel.joinYatra(getToken(), tirthyatra?.id!!, message, contact, peopleCount, name, age, gender)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    // override fun onResume() ... to fetch details?
    override fun onResume() {
        super.onResume()
        refreshYatraDetails()
    }

    private fun joinYatra() {
        // Deprecated, using specific listeners in checkJoinStatus
    }

    private fun showCompanionshipDetailsDialog(yatraId: String) {
        val context = this
        val layout = android.widget.LinearLayout(context)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val etName = android.widget.EditText(context)
        etName.hint = "Your Name"
        viewModel.userProfile.value?.let { etName.setText(it.name) } // Prefill
        layout.addView(etName)
        
        val etAge = android.widget.EditText(context)
        etAge.hint = "Age"
        etAge.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(etAge)
        
        val etGender = android.widget.EditText(context)
        etGender.hint = "Gender (Male/Female)"
        viewModel.userProfile.value?.let { etGender.setText(it.gender) } // Prefill
        layout.addView(etGender)

        val etContact = android.widget.EditText(context)
        etContact.hint = "Contact Number"
        etContact.inputType = android.text.InputType.TYPE_CLASS_PHONE
        // Try to prefill from user profile phone/mobileNumber if available? 
        // viewModel.userProfile.value?.phone ...
        layout.addView(etContact)

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Companionship Details")
            .setMessage("Share details for potential companions:")
            .setView(layout)
            .setPositiveButton("Enable") { _, _ ->
                val name = etName.text.toString()
                val age = etAge.text.toString()
                val gender = etGender.text.toString()
                val contact = etContact.text.toString()
                
                if (name.isEmpty() || age.isEmpty() || gender.isEmpty() || contact.isEmpty()) {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                viewModel.toggleCompanionship(getToken(), yatraId, true, name, age, gender, contact)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getToken(): String {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", "") ?: ""
    }
}
