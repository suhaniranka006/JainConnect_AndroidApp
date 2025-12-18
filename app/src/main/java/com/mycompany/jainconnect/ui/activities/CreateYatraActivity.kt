package com.mycompany.jainconnect.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Tirthyatra
import com.mycompany.jainconnect.data.models.TirthyatraTemplate
import com.mycompany.jainconnect.databinding.ActivityCreateYatraBinding
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CreateYatraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateYatraBinding
    private val viewModel: JainViewModel by viewModels()
    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null
    private var isCustom = false
    private var template: TirthyatraTemplate? = null
    private var uploadedImageUrl: String? = null

    private val pickImage = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        uri?.let {
            try {
                val file = uriToFile(it)
                val token = getToken()
                if (token.isNotEmpty()) {
                    Toast.makeText(this, "Uploading Image...", Toast.LENGTH_SHORT).show()
                    viewModel.uploadYatraImage(token, file)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateYatraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // Get Data
        isCustom = intent.getBooleanExtra("IS_CUSTOM", false)
        template = intent.getParcelableExtra("TEMPLATE_DATA")

        if (!isCustom && template == null) {
            Toast.makeText(this, "Error loading template", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupListeners()
        setupObservers()
    }

    private fun setupUI() {
        if (isCustom) {
             // Custom Mode UI
             binding.collapsingToolbar.title = "Create Your Trip"
             binding.tilTitle.visibility = android.view.View.VISIBLE
             
             // Hide Template specific 'About Place'
             binding.tvDescription.visibility = android.view.View.GONE
             
             // Default Image
             binding.ivHeaderImage.setImageResource(R.drawable.ic_tirthyatra)

             // Allow Image Upload
             binding.ivHeaderImage.setOnClickListener { 
                 pickImage.launch("image/*")
             }
             Toast.makeText(this, "Tap image to change", Toast.LENGTH_SHORT).show()
             
             // Default Values
             binding.etDuration.setText("1")
             binding.etNotes.hint = "Description / Notes"
             
        } else {
             // Template Mode UI
             template?.let { temp ->
                binding.collapsingToolbar.title = temp.title
                
                // Uneditable Description (About Place)
                binding.tvDescription.text = temp.description ?: "No description available."
                binding.tvDescription.visibility = android.view.View.VISIBLE
                
                binding.tilTitle.visibility = android.view.View.GONE
                
                // Prefill Editable Fields
                binding.etDuration.setText(temp.durationDays.toString())
                binding.etNotes.setText("") 
                
                if (!temp.image.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(temp.image)
                        .placeholder(R.drawable.ic_tirthyatra)
                        .into(binding.ivHeaderImage)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.etStartDate.setOnClickListener {
            showDatePicker(isStartDate = true)
        }
        
        binding.etEndDate.setOnClickListener {
            showDatePicker(isStartDate = false)
        }

        binding.btnCreateYatra.setOnClickListener {
            createYatra()
        }
        
        // Listen for Duration Changes to update End Date
        binding.etDuration.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                if (!s.isNullOrEmpty() && selectedStartDate != null) {
                    val days = s.toString().toIntOrNull() ?: 1
                    calculateEndDate(selectedStartDate!!, days)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    
    // ... [DatePicker methods remain same] ...

    private fun createYatra() {
        if (selectedStartDate == null) {
            Toast.makeText(this, "Please select a start date", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validation for Custom Title
        val title = if (isCustom) {
            binding.etTitle.text.toString().trim()
        } else {
            template?.title ?: "My Yatra" // Should not happen if check passed
        }
        
        if (isCustom && title.isEmpty()) {
             binding.tilTitle.error = "Title is required"
             return
        }

        if (selectedEndDate == null) {
             Toast.makeText(this, "Please verify dates", Toast.LENGTH_SHORT).show()
             return
        }

        val token = getToken()
        if (token.isEmpty()) {
            Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }
        
        val duration = binding.etDuration.text.toString().toIntOrNull() ?: 1
        val notes = binding.etNotes.text.toString()

        val newYatraClean = Tirthyatra(
            title = title,
            creatorId = null, 
            startDate = selectedStartDate,
            endDate = selectedEndDate,
            itinerary = if (isCustom) emptyList() else (template?.defaultItinerary ?: emptyList()),
            checklist = if (isCustom) emptyList() else (template?.defaultChecklist?.map { 
                 com.mycompany.jainconnect.data.models.ChecklistItem(it.item) 
            } ?: emptyList()),
            durationDays = duration,
            templateId = if (isCustom) null else template?.id,
            notes = notes,
            imageUrl = if (isCustom) uploadedImageUrl else template?.image
        )

        viewModel.createYatra(token, newYatraClean)
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val defaultDate = if (isStartDate) selectedStartDate ?: Date() else selectedEndDate ?: Date()
        calendar.time = defaultDate
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                val date = selectedCalendar.time
                
                if (isStartDate) {
                    selectedStartDate = date
                    updateStartDateUI()
                } else {
                    // Validate End Date >= Start Date
                    if (selectedStartDate != null && date.before(selectedStartDate)) {
                        Toast.makeText(this, "End Date cannot be before Start Date", Toast.LENGTH_SHORT).show()
                    } else {
                        selectedEndDate = date
                        updateEndDateUI(updateDuration = true)
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Prevent selecting past dates
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun updateStartDateUI() {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        selectedStartDate?.let { start ->
            binding.etStartDate.setText(sdf.format(start))

            // Auto-calculate End Date based on current duration
            val durationStr = binding.etDuration.text.toString()
            val days = durationStr.toIntOrNull() ?: 1
            calculateEndDate(start, days)
        }
    }
    
    private fun calculateEndDate(start: Date, days: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = start
        // Create Yatra logic usually implies Start Day is Day 1. So adding (days - 1).
        // If 1 Day trip: Start = End. 
        val addDays = if (days > 0) days - 1 else 0
        calendar.add(Calendar.DAY_OF_YEAR, addDays)
        selectedEndDate = calendar.time
        updateEndDateUI(updateDuration = false)
    }

    private fun updateEndDateUI(updateDuration: Boolean) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        selectedEndDate?.let { end ->
            binding.etEndDate.setText(sdf.format(end))
            
            if (updateDuration && selectedStartDate != null) {
                val diff = end.time - selectedStartDate!!.time
                val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff).toInt() + 1
                if (days > 0) {
                     // Update Edit Text without triggering watcher loop (if possible, but simplest is to just set it)
                     // To avoid loop, we check if value is different? 
                     // Or just define 'calculating' flag.
                     // But simplest: setText triggers watcher -> watcher calls calculateEndDate -> calculateEndDate sets EndDate.
                     // Loop: EndDate -> Duration -> EndDate.
                     // If math is consistent, it stabilizes. 
                     // Duration Listener updates EndDate. 
                     // EndDate updates Duration.
                     // If I set text, Listener fires.
                     // Let's remove listener temporarily? Or just check focus?
                     if (binding.etDuration.text.toString() != days.toString()) {
                         binding.etDuration.setText(days.toString())
                     }
                }
            }
        }
    }



    private fun setupObservers() {
        viewModel.yatraOperationResult.observe(this) { result ->
            if (result == "Success") {
                Toast.makeText(this, "Yatra created successfully!", Toast.LENGTH_SHORT).show()
                finish() // Go back
            } else {
                Toast.makeText(this, "Info: $result", Toast.LENGTH_SHORT).show()
            }
        }
        
        viewModel.uploadedImageUrl.observe(this) { url ->
            if (url != null) {
                uploadedImageUrl = url
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_tirthyatra)
                    .into(binding.ivHeaderImage)
                Toast.makeText(this, "Image Uploaded!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getToken(): String {
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", "") ?: ""
    }

    private fun uriToFile(uri: android.net.Uri): java.io.File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = java.io.File.createTempFile("upload", ".jpg", cacheDir)
        tempFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
        return tempFile
    }
}
