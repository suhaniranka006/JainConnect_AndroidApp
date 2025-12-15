package com.mycompany.jainconnect.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.fragments.*
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import com.razorpay.PaymentResultListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private val viewModel: JainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        setupBottomNavigation()
        setupDrawer()

        // Load Home Fragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
        
        loadProfileData()
    }

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun setupDrawer() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_my_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_members -> Toast.makeText(this, "Members Coming Soon", Toast.LENGTH_SHORT).show()
                R.id.nav_mentors -> Toast.makeText(this, "Mentors Coming Soon", Toast.LENGTH_SHORT).show()
                R.id.nav_rate -> Toast.makeText(this, "Rate Us Coming Soon", Toast.LENGTH_SHORT).show()
                R.id.nav_feedback -> Toast.makeText(this, "Feedback Coming Soon", Toast.LENGTH_SHORT).show()
                R.id.nav_about -> Toast.makeText(this, "About JainConnect", Toast.LENGTH_SHORT).show()
                R.id.nav_help -> startActivity(Intent(this, ContactActivity::class.java))
                R.id.nav_share -> {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Check out JainConnect app! Download it now.")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }
                R.id.nav_logout -> performLogout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        
        // Header logic (removed edit button)
    }

    private fun performLogout() {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadProfileData() {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)
        
        if (token != null) {
            viewModel.fetchUserProfile(token)
            viewModel.userProfile.observe(this) { user ->
                if (user != null) {
                    val headerView = navigationView.getHeaderView(0)
                    headerView.findViewById<TextView>(R.id.tvNavName).text = user.name
                    // Subtitle could be phone or roles if available
                    
                    val ivProfile = headerView.findViewById<ImageView>(R.id.ivNavProfile)
                    Glide.with(this)
                        .load(user.profileImage)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(ivProfile)
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_updates -> UpdatesFragment()
                R.id.nav_reminders -> RemindersFragment()
                R.id.nav_leaderboard -> LeaderboardFragment()
                R.id.nav_community -> CommunityFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Razorpay Success/Failure Callbacks
    override fun onPaymentSuccess(razorpayPaymentID: String) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is HomeFragment) {
            currentFragment.onPaymentSuccess(razorpayPaymentID)
        } else {
            Toast.makeText(this, "Donation Successful! ID: $razorpayPaymentID", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentError(code: Int, response: String) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is HomeFragment) {
            currentFragment.onPaymentError(code, response)
        } else {
            Toast.makeText(this, "Payment Failed. Error: $response", Toast.LENGTH_LONG).show()
        }
    }
}