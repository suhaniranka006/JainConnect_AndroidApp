package com.mycompany.jainconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider



class LoginActivity : AppCompatActivity() {

    // --- UI Views ---
    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var loginProgressBar: ProgressBar
    private lateinit var tvGoToSignUp: TextView

    // --- ViewModel ---
    private lateinit var viewModel: JainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // ViewModel ko initialize karein
        viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        initializeViews()
        setClickListeners()
        observeLoginResult()
    }

    private fun initializeViews() {
        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        loginProgressBar = findViewById(R.id.loginProgressBar)
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp)
    }

    private fun setClickListeners() {
        btnLogin.setOnClickListener {
            handleLogin()



        }

        tvGoToSignUp.setOnClickListener {
            // Agar user ke paas account nahi hai, toh SignUpActivity par bhejein
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleLogin() {
        val email = etLoginEmail.text.toString().trim()
        val password = etLoginPassword.text.toString().trim()

        // Validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etLoginEmail.error = "Please enter a valid email"
            return
        }
        if (password.isEmpty()) {
            etLoginPassword.error = "Password cannot be empty"
            return
        }

        // Loading indicator dikhayein aur API call karein
        loginProgressBar.visibility = View.VISIBLE
        viewModel.performLogin(email, password)
    }

    // LoginActivity.kt
// ...

    //checks if response if successful
    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { response ->
            loginProgressBar.visibility = View.GONE

            // ... (Network error handling) ...

            if (response!!.isSuccessful && response.body()?.success == true) {
                // Login successful hua
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                // Token save karein
                response?.body()?.token?.let { saveAuthToken(it) }



                //shared prefernce
                // --- YAHAN PAR SESSION SAVE KAREIN ---
                val session = SessionManager(this)
                session.saveLoginStatus(true)
                // ------------------------------------

                // MainActivity par jaayein
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            } else {
                // Login fail hua
                // ...
            }
        }
    }
// ...


    //on success , it saves the received token to shredprefrernces , this is how app will remeber the user is logged in
    private fun saveAuthToken(token: String) {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("jwt_token", token)
            apply()
        }
        Log.d("Auth", "Token saved successfully!")
    }
}
