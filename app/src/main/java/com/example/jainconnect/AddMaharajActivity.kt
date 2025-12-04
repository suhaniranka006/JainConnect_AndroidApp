package com.example.jainconnect

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class AddMaharajActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_maharaj)

        viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        // 1. Bind Views (Matches IDs in activity_add_maharaj.xml)
        val etName = findViewById<EditText>(R.id.etMaharajName)
        val etTitle = findViewById<EditText>(R.id.etMaharajTitle) // "Title" in XML maps to "Sampraday" logic
        val etCity = findViewById<EditText>(R.id.etMaharajLocation)       // Updated ID for City
        val etDate = findViewById<EditText>(R.id.etMaharajDate)
        val etContact = findViewById<EditText>(R.id.etMaharajContact)

        val btnSubmit = findViewById<Button>(R.id.btnSubmitMaharaj)

        btnSubmit.setOnClickListener {
            // 2. Get Text values
            val name = etName.text.toString().trim()
            val title = etTitle.text.toString().trim()
            val city = etCity.text.toString().trim()
            val date = etDate.text.toString().trim()
            val contact = etContact.text.toString().trim()

            // 3. Get Auth Token
            val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val token = sharedPref.getString("jwt_token", null)

            // 4. Validate Input
            if (token != null && name.isNotEmpty() && city.isNotEmpty()) {
                // 5. Submit Data
                // Order: token, name, sampraday, city, date, contact, desc
                viewModel.submitNewMaharaj(token, name, title , city, date, contact)
            } else {
                Toast.makeText(this, "Please fill at least Name and City", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. Observe Result
        viewModel.addMaharajResult.observe(this) { result ->
            if (result == "Success") {
                Toast.makeText(this, "Location Sent for Approval!", Toast.LENGTH_LONG).show()
                finish() // Close screen on success
            } else {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            }
        }
    }
}