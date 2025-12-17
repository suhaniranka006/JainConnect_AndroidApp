package com.mycompany.jainconnect.ui.activities

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel

@AndroidEntryPoint
class AddBhojanshalaActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bhojanshala)

        val etName = findViewById<EditText>(R.id.etName)
        val etCity = findViewById<EditText>(R.id.etCity)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etTimings = findViewById<EditText>(R.id.etTimings)
        val etContact = findViewById<EditText>(R.id.etContact)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val city = etCity.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val timings = etTimings.text.toString().trim()
            val contact = etContact.text.toString().trim()
            val description = etDescription.text.toString().trim()

            val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val token = sharedPref.getString("jwt_token", null)

            if (token != null && name.isNotEmpty() && city.isNotEmpty() && address.isNotEmpty()) {
                viewModel.submitNewBhojanshala(token, name, city, address, timings, contact, description)
            } else {
                Toast.makeText(this, "Please fill required fields (Name, City, Address)", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.addBhojanshalaResult.observe(this) { result ->
            if (result == "Success") {
                Toast.makeText(this, "Bhojanshala Sent for Approval!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
