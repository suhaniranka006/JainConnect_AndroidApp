package com.mycompany.jainconnect.ui.activities

import android.Manifest // 👈 Naya Import
import android.content.Intent
import android.content.pm.PackageManager // 👈 Naya Import
import android.os.Build // 👈 Naya Import
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log // 👈 Naya Import
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast // 👈 Naya Import
import androidx.activity.result.contract.ActivityResultContracts // 👈 Naya Import
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat // 👈 Naya Import
import com.google.firebase.messaging.FirebaseMessaging // 👈 Naya Import
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.local.SessionManager
import com.mycompany.jainconnect.data.models.User

class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"

    // --- Permission ke liye Launcher ---
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted.")
            // Permission mil gayi, ab aage badhein
        } else {
            Log.w(TAG, "Notification permission denied.")
            // Permission nahi mili, lekin app ko rokna nahi hai
        }
        // Dono cases mein, humein delay ke baad aage badhna hai
    }
    // ------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Layout ko set karein

        Log.d(TAG, "SplashActivity Started!")

        // Initialize Views for Animation
        val cardLogo = findViewById<View>(R.id.cardLogo)
        val tvTagline = findViewById<View>(R.id.tvTagline)

        // 1. Logo Animation (Fade In + Scale Up)
        cardLogo.alpha = 0f
        cardLogo.scaleX = 0.5f
        cardLogo.scaleY = 0.5f
        cardLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1200)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        // 2. Tagline Animation (Slide Up + Fade In)
        tvTagline.alpha = 0f
        tvTagline.translationY = 50f
        tvTagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1000)
            .setStartDelay(500) // Wait for logo to start
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // --- Naya Code: Permission Maangein ---
        askNotificationPermission()
        // -------------------------------------

        // --- Naya Code: Topic Subscribe Karein ---
        subscribeToTithiUpdates()
        // -----------------------------------------

        // 2.5 second ka delay (Increased for animation)
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginAndProceed()
        }, 2500)
    }

    private fun askNotificationPermission() {
        // Yeh sirf Android 13 (API 33) aur usse upar ke liye zaroori hai
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission pehle se hai
                    Log.d(TAG, "Notification permission is already granted.")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // User ne pehle deny kiya tha, usse samjhayein (Abhi ke liye skip)
                    // Hum phir se permission maang rahe hain
                    Log.d(TAG, "Showing rationale and requesting permission again.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Permission pehli baar maang rahe hain
                    Log.d(TAG, "Requesting notification permission for the first time.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        // Android 13 se neeche permission ki zaroorat nahi hai (automatic milti hai)
    }

    private fun subscribeToTithiUpdates() {
        // "tithi_updates" topic ko subscribe karein
        // Isse aapka server sabhi subscribed devices ko message bhej paayega
        FirebaseMessaging.getInstance().subscribeToTopic("tithi_updates")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Successfully subscribed to 'tithi_updates' topic")
                } else {
                    Log.e(TAG, "Failed to subscribe to 'tithi_updates' topic", task.exception)
                }
            }
    }

    private fun checkLoginAndProceed() {
        // 1. SessionManager se check karein
        val session = SessionManager(this)

        // 2. Decide karein kahaan bhejna hai
        if (session.isLoggedIn()) {
            // User IS logged in: Go to MainActivity
            Log.d(TAG, "User is logged in. Navigating to MainActivity.")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // User is NOT logged in: Go to LoginActivity
            Log.d(TAG, "User is NOT logged in. Navigating to LoginActivity.")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // 3. SplashActivity ko finish karein
        finish()
    }
}