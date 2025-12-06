package com.mycompany.jainconnect

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: JainViewModel
    private lateinit var sharedPreferences: SharedPreferences

    // UI Components
    private lateinit var tvGreeting: TextView
    private lateinit var tvCurrentTithi: TextView
    private lateinit var tvSunriseTime: TextView
    private lateinit var tvSunsetTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[JainViewModel::class.java]

        // ✅ FIX IS HERE: Use "auth_prefs" to match LoginActivity
        sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

        tvGreeting = findViewById(R.id.tvGreeting)
        tvCurrentTithi = findViewById(R.id.tvCurrentTithi)
        tvSunriseTime = findViewById(R.id.tvSunriseTime)
        tvSunsetTime = findViewById(R.id.tvSunsetTime)

        setupNavigationButtons()
        observeData()
        loadDashboardData()
    }

    private fun setupNavigationButtons() {
        findViewById<Button>(R.id.btnTithi).setOnClickListener {
            startActivity(Intent(this, TithiActivity::class.java))
        }
        findViewById<Button>(R.id.btnEvents).setOnClickListener {
            startActivity(Intent(this, EventActivity::class.java))
        }
        findViewById<Button>(R.id.btnMonks).setOnClickListener {
            startActivity(Intent(this, MaharajLocationActivity::class.java))
        }
        findViewById<Button>(R.id.btnHorizons).setOnClickListener {
            startActivity(Intent(this, HorizonsActivity::class.java))
        }
        findViewById<Button>(R.id.btnContact).setOnClickListener {
            startActivity(Intent(this, ContactActivity::class.java))
        }
        findViewById<Button>(R.id.btnMe).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadDashboardData() {
        // ✅ The key "jwt_token" is correct, and now the file "auth_prefs" matches
        val token = sharedPreferences.getString("jwt_token", null)

        if (token != null) {
            // Token found, fetch profile
            viewModel.fetchUserProfile(token)
        } else {
            // Token not found (maybe not logged in)
            tvGreeting.text = "JAI JINENDRA!"
        }

        viewModel.fetchTithis()
        viewModel.fetchSunData(26.9124, 75.7873)
    }

    private fun observeData() {
        // --- 1. Update Greeting Name ---
        viewModel.userProfile.observe(this) { user ->
            if (user != null) {
                // Should now show: "JAI JINENDRA SUHANI!"
                tvGreeting.text = "JAI JINENDRA ${user.name.uppercase()}!"
            }
        }

        // --- 2. Update Tithi ---
        viewModel.tithiList.observe(this) { tithiList ->
            if (tithiList.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayDate = sdf.format(Calendar.getInstance().time)
                val todayTithi = tithiList.find { it.date == todayDate }

                if (todayTithi != null) {
                    tvCurrentTithi.text = "Today is ${todayTithi.name.uppercase()}"
                } else {
                    tvCurrentTithi.text = "No Tithi Data for Today"
                }
            }
        }

        // --- 3. Update Sunrise/Sunset ---
        viewModel.horizonList.observe(this) { horizonList ->
            if (horizonList.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                val todayDate = sdf.format(Calendar.getInstance().time)
                val todayHorizon = horizonList.find { it.date == todayDate }

                if (todayHorizon != null) {
                    tvSunriseTime.text = todayHorizon.sunrise
                    tvSunsetTime.text = todayHorizon.sunset
                } else {
                    tvSunriseTime.text = horizonList[0].sunrise
                    tvSunsetTime.text = horizonList[0].sunset
                }
            }
        }
    }
}
