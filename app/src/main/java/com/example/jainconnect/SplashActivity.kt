package com.example.jainconnect

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log // <-- Import bilkul sahi hai
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 2000 // 2 seconds
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: Splash screen shuru hui.")

        // 1. Layout ko set karna (Taaki woh dikhe)
        setContentView(R.layout.activity_splash)
        Log.d(TAG, "onCreate: Custom layout (activity_splash.xml) set ho gaya.")

        // 2. Handler (delay) ka istemaal
        Handler(Looper.getMainLooper()).postDelayed({

            Log.d(TAG, "Handler: 2 second poore hue. checkLoginAndProceed() call kar raha hoon.")
            checkLoginAndProceed()

        }, SPLASH_DELAY)
    }







    private fun checkLoginAndProceed() {


        //shared preference code
        // 3. Wahi purana logic
        val session = SessionManager(this)
        val isLoggedIn = session.isLoggedIn() // <-- Status check karein

        Log.d(TAG, "checkLoginAndProceed: User ka login status hai: $isLoggedIn")

        if (isLoggedIn) {
            // User IS logged in: Go to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // User is NOT logged in: Go to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // 4. SplashActivity ko finish karein
        finish()
        Log.d(TAG, "checkLoginAndProceed: Activity finish ho gayi.")
    }
}