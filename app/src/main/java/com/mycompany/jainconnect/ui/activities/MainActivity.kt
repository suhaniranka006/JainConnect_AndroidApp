package com.mycompany.jainconnect.ui.activities

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

import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Tithi
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel

/**
 * The main screen of the app.
 * Annotated with @AndroidEntryPoint to allow Hilt to inject dependencies.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences

    // UI Components
    private lateinit var tvGreeting: TextView
    private lateinit var tvTithiName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvLocationName: TextView
    private lateinit var tvSunriseTime: TextView
    private lateinit var tvSunsetTime: TextView

    // Data Loading Status Flags
    private var isUserLoaded = false
    private var isTithiLoaded = false
    private var isSunLoaded = false

    private lateinit var shimmerDashboard: com.facebook.shimmer.ShimmerFrameLayout
    private lateinit var cardGreeting: com.google.android.material.card.MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ FIX IS HERE: Use "auth_prefs" to match LoginActivity
        sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

        shimmerDashboard = findViewById(R.id.shimmerDashboard)
        cardGreeting = findViewById(R.id.cardGreeting)
        shimmerDashboard.startShimmer()

        tvGreeting = findViewById(R.id.tvGreeting)
        tvTithiName = findViewById(R.id.tvTithiName)
        tvDate = findViewById(R.id.tvDate)
        tvLocationName = findViewById(R.id.tvLocationName)
        tvSunriseTime = findViewById(R.id.tvSunriseTime)
        tvSunsetTime = findViewById(R.id.tvSunsetTime)

        // Set Date immediately
        val sdfDate = SimpleDateFormat("EEEE, dd-MM-yy", Locale.getDefault())
        tvDate.text = sdfDate.format(Calendar.getInstance().time)

        // Set Location (Placeholder for now, matching coords)
        tvLocationName.text = "Jaipur"

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
            tvGreeting.text = "Jai Jinendra!"
            isUserLoaded = true // Mark as loaded since we don't need to wait
            checkDataLoaded()
        }

        viewModel.fetchTithis()
        viewModel.fetchSunData(26.9124, 75.7873)
    }

    private fun checkDataLoaded() {
        if (isUserLoaded && isTithiLoaded && isSunLoaded) {
            shimmerDashboard.stopShimmer()
            shimmerDashboard.visibility = android.view.View.GONE
            cardGreeting.visibility = android.view.View.VISIBLE
        }
    }

    private fun observeData() {
        // --- 1. Update Greeting Name ---
        viewModel.userProfile.observe(this) { user ->
            isUserLoaded = true
            checkDataLoaded()
            if (user != null) {
                // Should now show: "Jai Jinendra Suhani"
                // Using capitalize for First Letter Capitalization to match sketch style roughly
                tvGreeting.text = "Jai Jinendra ${user.name}!" 
            }
        }

        // --- 2. Update Tithi ---
        viewModel.tithiList.observe(this) { tithiList ->
            isTithiLoaded = true
            checkDataLoaded()
            if (tithiList.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayDate = sdf.format(Calendar.getInstance().time)
                val todayTithi = tithiList.find { it.date == todayDate }

                if (todayTithi != null) {
                    tvTithiName.text = todayTithi.name
                } else {
                    tvTithiName.text = "No Data"
                }
            }
        }

        // --- 3. Update Sunrise/Sunset ---
        viewModel.horizonList.observe(this) { horizonList ->
            isSunLoaded = true
            checkDataLoaded()
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