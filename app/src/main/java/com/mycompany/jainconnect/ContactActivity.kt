package com.mycompany.jainconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contact)

        // Window Insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 2. Initialize Views
        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etMessage = findViewById<TextInputEditText>(R.id.etMessage)
        val btnSubmit = findViewById<MaterialButton>(R.id.btnSubmit)
        val btnDirectEmail = findViewById<LinearLayout>(R.id.btnDirectEmail)

        // 3. Handle Submit Button Click (Form Data)
        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val message = etMessage.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Send Email Intent with form data
            sendEmail(name, email, message)
        }

        // 4. Handle Direct Email Icon Click (Empty Body)
        btnDirectEmail.setOnClickListener {
            // Isme hum name, email aur message empty bhej rahe hain
            // Lekin receiver (aapka email) same rahega
            sendEmail("", "", "")
        }



        val btnWhatsApp = findViewById<LinearLayout>(R.id.btnWhatsApp)
        val btnPhone = findViewById<LinearLayout>(R.id.btnPhone)
        val btnLinkedIn = findViewById<LinearLayout>(R.id.btnLinkedIn)
        val btnInstagram = findViewById<LinearLayout>(R.id.btnInstagram)



// 3. WhatsApp Click
        btnWhatsApp.setOnClickListener {
            val phoneNumber = "+917851976119" // Replace with country code + number (e.g. 91 for India)
            val url = "https://api.whatsapp.com/send?phone=$phoneNumber"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }

// 4. Phone Call Click
        btnPhone.setOnClickListener {
            val phoneNumber = "7851976119" // Replace with your number
            val intent = Intent(Intent.ACTION_DIAL) // ACTION_DIAL is safer than ACTION_CALL (no permission needed)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        }

// 5. LinkedIn Click
        btnLinkedIn.setOnClickListener {
            val profileUrl = "https://www.linkedin.com/in/suhani-ranka-a146a4253/" // Replace URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(profileUrl))
            startActivity(intent)
        }

// 6. Instagram Click
        btnInstagram.setOnClickListener {
            val profileUrl = "http://instagram.com/suhani_jain_006/" // Replace URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(profileUrl))
            startActivity(intent)
        }
    }

    private fun sendEmail(name: String, userEmail: String, message: String) {
        // ✅ CHANGE: Yahan par aapka email fix kar diya hai
        val recipient = "suhaniranka964@gmail.com"

        // Subject aur Body set karna
        val subject = if(name.isNotEmpty()) "Inquiry from JainConnect: $name" else "Inquiry from JainConnect App"

        // Agar form bhara hai to details dikhengi, nahi to blank body
        val body = if(name.isNotEmpty()) {
            "Name: $name\nUser Email: $userEmail\n\nMessage:\n$message"
        } else {
            "" // User khud type karega
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient)) // Receiver set ho gaya
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(Intent.createChooser(intent, "Send email using..."))
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}
