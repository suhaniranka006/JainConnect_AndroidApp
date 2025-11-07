package com.example.jainconnect

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.util.Calendar

class SignUpActivity : AppCompatActivity() {

    // --- UI Views ---
    private lateinit var ivProfileImage: ImageButton
    private lateinit var btnSelectImage: TextView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPhone: EditText
    private lateinit var etLocation: EditText
    private lateinit var etDob: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var btnSignUp: Button
    private lateinit var progressBar: ProgressBar

    // --- ViewModel and Data ---
    private lateinit var viewModel: JainViewModel
    private var selectedImageUri: Uri? = null

    // --- Activity Result Launcher for Image Picking ---
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivProfileImage.setImageURI(it)
            Log.d("SignUpActivity", "Image selected: $it")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        initializeViews()
        viewModel = ViewModelProvider(this)[JainViewModel::class.java]
        setClickListeners()
        observeSignupResult()
    }

    /**
     * Saare UI elements ko unki ID se find karta hai.
     */
    private fun initializeViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etPhone = findViewById(R.id.etPhone)
        etLocation = findViewById(R.id.etLocation)
        etDob = findViewById(R.id.etDob)
        rgGender = findViewById(R.id.rgGender)
        btnSignUp = findViewById(R.id.btnSignUp)
        progressBar = findViewById(R.id.progressBar)
    }

    /**
     * Saare buttons ke click events ko handle karta hai.
     */
    private fun setClickListeners() {
        ivProfileImage.setOnClickListener {
            imagePickerLauncher.launch("image/*") // Gallery kholta hai
        }

        btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*") // Gallery kholta hai
        }

        etDob.setOnClickListener {
            showDatePickerDialog()
        }

        btnSignUp.setOnClickListener {
            handleSignUp()
        }
    }

    /**
     * ViewModel se aane waale signup ke result ko observe (sunta) karta hai.
     */
    private fun observeSignupResult() {
        viewModel.signupResult.observe(this) { response ->
            progressBar.visibility = View.GONE

            if (response == null) {
                Toast.makeText(this, "Error: Check your network connection", Toast.LENGTH_LONG).show()
                return@observe
            }

            if (response.isSuccessful && response.body()?.success == true) {
                // Signup successful hua
                val token = response.body()?.token
                if (token != null) {
                    saveAuthToken(token) // Token save kar lete hain
                }




                //shared prefernce

                // 1. Session ko 'logged in' set karein
                val session = SessionManager(this)
                session.saveLoginStatus(true)
                // ---------------------

                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

                // MainActivity par jaayein
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                // --- CHANGES HERE ---
                // 2. Iss activity ko finish karein
                finish()
                // ---------------------

            } else {
                // Signup fail hua
                val errorMsg = response.errorBody()?.string() ?: "Signup Failed"
                Log.e("SignUpActivity", "API Error: $errorMsg")
                Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
            }
        }
    }


    /**
     * "Sign Up" button dabane par yeh function chalta hai.
     */
    private fun handleSignUp() {
        val TAG = "SignUpDebug"
        Log.d(TAG, "===== handleSignUp function started =====")

        // 1. Get all data from UI
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val dob = etDob.text.toString().trim()
        val selectedGenderId = rgGender.checkedRadioButtonId
        val gender = if (selectedGenderId != -1) findViewById<RadioButton>(selectedGenderId).text.toString() else ""

        // 2. Validation
        Log.d(TAG, "--- Starting validation ---")
        if (name.isEmpty()) {
            etName.error = "Name is required"; return
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"; return
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"; return
        }
        if (gender.isEmpty()) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show(); return
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show(); return
        }
        Log.d(TAG, "✅ Validation PASSED.")

        progressBar.visibility = View.VISIBLE

        // 3. Image URI ko File object me convert karein
        val imageFile = getFileFromUri(selectedImageUri!!)
        if (imageFile == null) {
            Log.e(TAG, "ERROR: Could not create a File from the image URI.")
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. ViewModel ko call karein (saare data ke saath)
        Log.d(TAG, "--- Calling viewModel.performSignup() with all data ---")
        viewModel.performSignup(name, email, password, phone, location, dob, gender, imageFile)
    }


    /**
     * Date picker dialog dikhata hai.
     */
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val formattedDate = "$year-${month + 1}-$dayOfMonth"
                etDob.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    /**
     * Yeh ek helper function hai jo gallery se select ki gayi image ke URI ko
     * ek temporary File object me convert karta hai, taaki hum use API par bhej sakein.
     */
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("profile_image", ".jpg", cacheDir)
            tempFile.deleteOnExit()
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("SignUpActivity", "Error creating file from URI", e)
            null
        }
    }

    /**
     * Login/Signup ke baad mile token ko SharedPreferences me save karta hai.
     */
    private fun saveAuthToken(token: String) {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("jwt_token", token)
            apply()
        }
        Log.d("Auth", "Token saved successfully!")
    }
}