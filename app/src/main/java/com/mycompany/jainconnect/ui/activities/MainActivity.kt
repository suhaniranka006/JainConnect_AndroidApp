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
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navigationViewRight: NavigationView // New Right Drawer
    private val viewModel: JainViewModel by viewModels()



    // Network Monitor : Monitor internet connection
    private var networkReceiver: com.mycompany.jainconnect.utils.NetworkChangeReceiver? = null
    //snackbar : show message to user
    private var internetSnackbar: com.google.android.material.snackbar.Snackbar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationViewRight = findViewById(R.id.nav_view_right) // Bind Right Drawer

        setupBottomNavigation()
        setupDrawer()
        setupRightDrawer() // Setup Right Drawer Logic
        
        // Check Permissions immediately on launch
        checkAndRequestPermissions()

        // Load Home Fragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
        
        loadProfileData()
        
        // Subscribe to Topic
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("global_chat")
        
        setupChatBadgeListener()
        observeDeleteResult()
    }

    private fun observeDeleteResult() {
        viewModel.deleteResult.observe(this) { result ->
            when (result) {
                is com.mycompany.jainconnect.data.network.NetworkResult.Success -> {
                    // Backend deletion success, now delete from Firebase
                    deleteFirebaseAccount()
                }
                is com.mycompany.jainconnect.data.network.NetworkResult.Error -> {
                    Toast.makeText(this, "Backend Deletion Failed: ${result.message}", Toast.LENGTH_SHORT).show()
                }
                is com.mycompany.jainconnect.data.network.NetworkResult.Loading -> {
                     // Loading
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permissions when coming back (e.g. from Settings)
        if (!hasPermissions()) {
            checkAndRequestPermissions()
        }

        //check internet connection
        
        // Register Network Receiver
        if (networkReceiver == null) {
            networkReceiver = com.mycompany.jainconnect.utils.NetworkChangeReceiver { isConnected ->
                showNetworkStatus(isConnected)
            }
        }
        val filter = android.content.IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
    }


    //this code runs when activity is paused
    //unregister the receiver
    override fun onPause() {
        super.onPause()
        try {
            if (networkReceiver != null) unregisterReceiver(networkReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    

    //show network status
    private fun showNetworkStatus(isConnected: Boolean) {
        if (!isConnected) {
            if (internetSnackbar == null) {
                internetSnackbar = com.google.android.material.snackbar.Snackbar.make(
                    drawerLayout,
                    "No Internet Connection",
                    com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
                ).setBackgroundTint(ContextCompat.getColor(this, R.color.error_red))
                 .setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            internetSnackbar?.show()
        } else {
            internetSnackbar?.dismiss()
        }
    }



    private fun setupDrawer() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_my_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_members -> startActivity(Intent(this, MembersActivity::class.java))
                R.id.nav_mentors -> Toast.makeText(this, "Mentors Coming Soon", Toast.LENGTH_SHORT).show()
                R.id.nav_rate -> Toast.makeText(this, "Rate Us Coming Soon", Toast.LENGTH_SHORT).show()
                R.id.nav_feedback -> Toast.makeText(this, "Feedback Coming Soon", Toast.LENGTH_SHORT).show()
                R.id.nav_about -> Toast.makeText(this, "About JainConnect", Toast.LENGTH_SHORT).show()
                R.id.nav_help -> startActivity(Intent(this, ContactActivity::class.java))
                R.id.nav_policy -> {
                     // Open Privacy Policy URL
                     val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/jainconnect-privacy/home"))
                     startActivity(intent)
                }
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
                R.id.nav_delete_account -> showDeleteAccountDialog()
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
        val session = com.mycompany.jainconnect.data.local.SessionManager(this)
        session.saveLoginStatus(false)
        session.clearSession()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showDeleteAccountDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                performDeleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDeleteAccount() {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)

        if (token != null) {
            // 1. Delete from Backend first
            viewModel.deleteAccount(token)
            // Result observed in observeDeleteResult()
        } else {
            // No backend token (Force Login state), just delete Firebase
            deleteFirebaseAccount()
        }
    }

    private fun deleteFirebaseAccount() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user != null) {
            user.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account Deleted Successfully", Toast.LENGTH_SHORT).show()
                    performLogout()
                } else {
                    Toast.makeText(this, "Failed to delete Firebase account: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    // Still logout because backend account is gone?
                    // Yes, consistency is better. Force logout.
                    performLogout()
                }
            }
        } else {
            // User already null in Firebase, but backend deletion happened.
            Toast.makeText(this, "Account Deleted Successfully", Toast.LENGTH_SHORT).show()
            performLogout()
        }
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
                    
                    val ivProfile = headerView.findViewById<ImageView>(R.id.ivNavProfile)
                    Glide.with(this)
                        .load(user.profileImage)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(ivProfile)
                }
            }
        } else {
            // Fallback: If no token (Force Login), use Firebase Data
            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                val headerView = navigationView.getHeaderView(0)
                val displayName = firebaseUser.displayName ?: "Jain Connect User"
                headerView.findViewById<TextView>(R.id.tvNavName).text = displayName

                val ivProfile = headerView.findViewById<ImageView>(R.id.ivNavProfile)
                val photoUrl = firebaseUser.photoUrl
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(ivProfile)
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

    // =================================================================================
    //                           MANDATORY PERMISSIONS LOGIC
    // =================================================================================
    private val PERMISSION_REQUEST_CODE = 101

    private fun hasPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = mutableListOf<String>()
            
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                }
            }

            if (deniedPermissions.isNotEmpty()) {
                // Determine if we should show rationale or if user checked "Don't ask again"
                val shouldShowRationale = deniedPermissions.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) }
                
                if (!shouldShowRationale) {
                    // "Don't ask again" was checked -> Redirect to Settings
                    showMandatoryPermissionsDialog(isPermanentDenial = true)
                } else {
                    // Normal denial -> Show blocking dialog to retry
                    showMandatoryPermissionsDialog(isPermanentDenial = false)
                }
            } else {
                // All granted!
                Toast.makeText(this, "Permissions Granted. Welcome!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMandatoryPermissionsDialog(isPermanentDenial: Boolean) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Permissions Required")
        builder.setCancelable(false) // BLOCKING: User cannot click outside

        if (isPermanentDenial) {
            builder.setMessage("This app requires Location and Notification permissions to function correctly. You have denied them permanently. Please enable them manually in Settings.")
            builder.setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        } else {
            builder.setMessage("JainConnect requires Location (for Sunrise/Sunset & Monk finding) and Notifications (for updates). Please grant these permissions to continue.")
            builder.setPositiveButton("Grant") { _, _ ->
                checkAndRequestPermissions()
            }
        }

        builder.setNegativeButton("Exit App") { _, _ ->
            finishAffinity() // Store listing requirement compliant? Ideally shouldn't crash, just close.
        }

        builder.show()
    }
    private fun setupChatBadgeListener() {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val query = firestore.collection("global_chat")
        
        query.addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null) return@addSnapshotListener
            
            val serverCount = snapshots.size()
            updateBadge(serverCount)
        }
    }

    private fun updateBadge(serverCount: Int) {
        val sharedPref = getSharedPreferences("chat_prefs", Context.MODE_PRIVATE)
        val lastReadCount = sharedPref.getInt("last_read_count", 0)
        
        val unreadCount = serverCount - lastReadCount
        
        if (unreadCount > 0) {
            val badge = bottomNavigation.getOrCreateBadge(R.id.nav_community)
            badge.isVisible = true
            badge.number = unreadCount
            badge.backgroundColor = ContextCompat.getColor(this, R.color.error_red)
            badge.badgeTextColor = ContextCompat.getColor(this, android.R.color.white)
        } else {
            bottomNavigation.removeBadge(R.id.nav_community)
        }
    }
    
    fun markChatAsRead(totalMessages: Int) {
         val sharedPref = getSharedPreferences("chat_prefs", Context.MODE_PRIVATE)
         with(sharedPref.edit()) {
             putInt("last_read_count", totalMessages)
             apply()
         }
         // Update UI immediately (remove badge)
         bottomNavigation.removeBadge(R.id.nav_community)
    }

    private fun setupRightDrawer() {
        navigationViewRight.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_volunteer_right -> startActivity(Intent(this, VolunteerActivity::class.java))
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }
    }

    // Called by Profile Image -> Opens LEFT Drawer
    fun openDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // Called by Menu Icon (Right Top) -> Opens RIGHT Drawer
    fun customToggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            drawerLayout.openDrawer(GravityCompat.END)
        }
    }
}