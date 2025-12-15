package com.mycompany.jainconnect.ui.activities

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



import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.local.SessionManager
import com.mycompany.jainconnect.data.network.NetworkResult
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel

/**
 * The login screen for the application.
 * Annotated with @AndroidEntryPoint so Hilt can inject dependencies (like ViewModel) into it.
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    // --- UI Views ---
    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var loginProgressBar: ProgressBar
    private lateinit var tvGoToSignUp: TextView

    // --- ViewModel ---
    // 'by viewModels()' is a property delegate that:
    // 1. Asks Hilt to provide an instance of JainViewModel.
    // 2. Scopes the ViewModel to this Activity (it survives configuration changes).
    private val viewModel: JainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Note: No manual ViewModelProvider instantiation needed anymore.
        // Hilt and the 'by viewModels()' delegate handle initialization automatically.

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
            // Navigate to SignUpActivity if user doesn't have an account
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

        // Show loading indicator and initiate API call
        loginProgressBar.visibility = View.VISIBLE
        viewModel.performLogin(email, password)
    }

    // LoginActivity.kt
// ...

    //checks if response if successful
    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    loginProgressBar.visibility = View.VISIBLE
                    btnLogin.isEnabled = false // Prevent double clicks
                }
                is NetworkResult.Success -> {
                    loginProgressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    
                    val response = result.data
                    if (response?.success == true) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        response.token?.let { saveAuthToken(it) }

                        val session = SessionManager(this)
                        session.saveLoginStatus(true)

                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Login Failed: ${response?.message}", Toast.LENGTH_LONG).show()
                    }
                }
                is NetworkResult.Error -> {
                    loginProgressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
// ...


    // On success, save the received token to SharedPreferences.
    // This allows the app to remember the user's logged-in state.
    private fun saveAuthToken(token: String) {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("jwt_token", token)
            apply()
        }
        Log.d("Auth", "Token saved successfully!")
    }
}