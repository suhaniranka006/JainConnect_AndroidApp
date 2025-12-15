package com.mycompany.jainconnect.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.fragments.*
import dagger.hilt.android.AndroidEntryPoint
import android.widget.Toast // Needed for error messages

// ⭐ 1. Import Razorpay Listener
import com.razorpay.PaymentResultListener


// ⭐ 2. Implement PaymentResultListener in the Activity class definition
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PaymentResultListener { // <-- ADDED PaymentResultListener

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        setupBottomNavigation()

        // Load Home Fragment by default if savedInstanceState is null (first launch)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
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

    // ⭐ 3. Implement the required Razorpay success handler
    override fun onPaymentSuccess(razorpayPaymentID: String) {
        // Find the Fragment currently in the container
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // Check if the current fragment is a HomeFragment (where the result needs to go)
        if (currentFragment is HomeFragment) {
            // Forward the success result to the HomeFragment's implementation
            currentFragment.onPaymentSuccess(razorpayPaymentID)
        } else {
            // Fallback: If not on HomeFragment, show a general success message
            Toast.makeText(this, "Donation Successful! ID: $razorpayPaymentID", Toast.LENGTH_LONG).show()
        }
    }

    // ⭐ 4. Implement the required Razorpay error handler
    override fun onPaymentError(code: Int, response: String) {
        // Find the Fragment currently in the container
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // Check if the current fragment is a HomeFragment
        if (currentFragment is HomeFragment) {
            // Forward the error result to the HomeFragment's implementation
            currentFragment.onPaymentError(code, response)
        } else {
            // Fallback: If not on HomeFragment, show a general error message
            Toast.makeText(this, "Payment Failed. Error: $response (Code: $code)", Toast.LENGTH_LONG).show()
        }
    }
}