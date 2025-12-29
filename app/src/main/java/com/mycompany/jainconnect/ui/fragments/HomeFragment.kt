package com.mycompany.jainconnect.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.activities.*
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ⭐ Razorpay and Dialog Imports
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener

// Location Imports
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.mycompany.jainconnect.databinding.FragmentHomeBinding // Ensure binding is imported if used, otherwise direct IDs

// ⭐ CLASS DEFINITION: Implements PaymentResultListener AND the custom AmountDialogListener
@AndroidEntryPoint
class HomeFragment : Fragment(), PaymentResultListener, AmountDialogFragment.AmountDialogListener {

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

    private lateinit var shimmerDashboard: ShimmerFrameLayout
    private lateinit var cardGreeting: MaterialCardView

    // Location Permission Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupLocationListener()
        } else {
            // Permission denied
            // view?.findViewById<SwitchCompat>(R.id.switchLocation)?.isChecked = false // Removed
            Toast.makeText(context, "Location permission required.", Toast.LENGTH_SHORT).show()
        }
    }

    // Live Date Receiver
    private val timeReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateDateDisplay()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ... (existing code remains same, handled by caller logic usually, but here we just need to ensure super call matches)
        
        sharedPreferences = requireActivity().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        Checkout.preload(requireContext())

        initializeViews(view)
        setupNavigationButtons(view)
        initializeViews(view)
        setupNavigationButtons(view)
        setupLocationHeader(view) 
        shimmerDashboard.startShimmer() 
        shimmerDashboard.startShimmer()
        observeData()
        loadDashboardData()
    }
    
    override fun onResume() {
        super.onResume()
        // Register receiver for time updates
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK) // Updates every minute
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
        }
        requireContext().registerReceiver(timeReceiver, filter)
        // Also update immediately
        updateDateDisplay()
    }

    override fun onPause() {
        super.onPause()
        try {
            requireContext().unregisterReceiver(timeReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }

    private fun updateDateDisplay() {
        if (::tvDate.isInitialized) {
            val sdfDate = SimpleDateFormat("EEEE, dd-MM-yy", Locale.getDefault())
            tvDate.text = sdfDate.format(Calendar.getInstance().time)
        }
        
        // Also refresh greeting based on time of day (Good Morning/Evening etc if we implemented that later)
    }

    private fun initializeViews(view: View) {
        shimmerDashboard = view.findViewById(R.id.shimmerDashboard)
        cardGreeting = view.findViewById(R.id.cardGreeting)

        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvTithiName = view.findViewById(R.id.tvTithiName)
        tvDate = view.findViewById(R.id.tvDate)
        tvLocationName = view.findViewById(R.id.tvLocationName)
        tvSunriseTime = view.findViewById(R.id.tvSunriseTime)
        tvSunsetTime = view.findViewById(R.id.tvSunsetTime)

        // Set Date immediately
        updateDateDisplay()

        // Default: No Data (Location Logic will decide)
        tvLocationName.text = "Location Off"

        // LOAD CACHE IMMEDIATELY
        loadFromCache()
    }

    private fun loadFromCache() {
        val cachedName = sharedPreferences.getString("cached_user_name", null)
        if (!cachedName.isNullOrEmpty()) {
            tvGreeting.text = "Jai Jinendra $cachedName!"
            isUserLoaded = true
        } else if (sharedPreferences.getString("jwt_token", null) == null) {
            // If no token (Guest), treat user as loaded
             isUserLoaded = true
        }

        val cachedTithi = sharedPreferences.getString("cached_tithi_name", null)
        if (!cachedTithi.isNullOrEmpty()) {
            tvTithiName.text = cachedTithi
            isTithiLoaded = true
        }

        val cachedSunrise = sharedPreferences.getString("cached_sunrise", null)
        val cachedSunset = sharedPreferences.getString("cached_sunset", null)
        if (!cachedSunrise.isNullOrEmpty() && !cachedSunset.isNullOrEmpty()) {
            tvSunriseTime.text = cachedSunrise
            tvSunsetTime.text = cachedSunset
            isSunLoaded = true
        }
        
        checkDataLoaded()
    }

    private fun setupNavigationButtons(view: View) {
        // --- Header Buttons ---
        
        // Profile Image -> Open Drawer
        view.findViewById<View>(R.id.ivProfile).setOnClickListener {
             if (activity is MainActivity) (activity as MainActivity).openDrawer()
        }

        // Menu Icon -> Open Drawer
        view.findViewById<View>(R.id.btnMenu).setOnClickListener {
             if (activity is MainActivity) (activity as MainActivity).openDrawer()
        }

        view.findViewById<View>(R.id.btnHelp).setOnClickListener {
            startActivity(Intent(requireContext(), ContactActivity::class.java))
        }

        // ⭐ DONATE BUTTON: Opens the custom AmountDialogFragment
        view.findViewById<View>(R.id.btnDonate).setOnClickListener {
            val dialog = AmountDialogFragment()
            // Use childFragmentManager to ensure the result listener works correctly
            dialog.show(childFragmentManager, "AmountDialog")
        }

        // --- Explore Grid Buttons with Entry Animation ---
        val btnTithi = view.findViewById<View>(R.id.btnTithi)
        val btnEvents = view.findViewById<View>(R.id.btnEvents)
        val btnMonks = view.findViewById<View>(R.id.btnMonks)
        val btnHorizons = view.findViewById<View>(R.id.btnHorizons)
        val btnBhojan = view.findViewById<View>(R.id.btnBhojanshalas)
        val btnTemples = view.findViewById<View>(R.id.btnTemples)
        val btnCarpool = view.findViewById<View>(R.id.btnCarpooling)
        val btnLegacy = view.findViewById<View>(R.id.btnLegacy)

        // Staggered Entry Animation
        startEntryAnimation(btnTithi, 100L)
        startEntryAnimation(btnEvents, 200L)
        startEntryAnimation(btnMonks, 300L)
        startEntryAnimation(btnHorizons, 400L)
        startEntryAnimation(btnBhojan, 500L)
        startEntryAnimation(btnTemples, 600L)
        startEntryAnimation(btnCarpool, 700L)
        startEntryAnimation(btnLegacy, 800L)

        btnTithi.setOnClickListener { startActivity(Intent(requireContext(), TithiActivity::class.java)) }
        btnEvents.setOnClickListener { startActivity(Intent(requireContext(), EventActivity::class.java)) }
        btnMonks.setOnClickListener { startActivity(Intent(requireContext(), MaharajLocationActivity::class.java)) }
        btnHorizons.setOnClickListener { startActivity(Intent(requireContext(), HorizonsActivity::class.java)) }
        btnBhojan.setOnClickListener { startActivity(Intent(requireContext(), BhojanshalaActivity::class.java)) }
        btnTemples.setOnClickListener { startActivity(Intent(requireContext(), TempleActivity::class.java)) }
        btnCarpool.setOnClickListener { startActivity(Intent(requireContext(), CarpoolActivity::class.java)) }
        btnLegacy.setOnClickListener { startActivity(Intent(requireContext(), LegacyActivity::class.java)) }
        view.findViewById<View>(R.id.btnTirthyatra).setOnClickListener {
            startActivity(Intent(requireContext(), TirthyatraActivity::class.java))
        }
        view.findViewById<View>(R.id.btnTirthyatra).setOnClickListener {
            startActivity(Intent(requireContext(), TirthyatraActivity::class.java))
        }
        // Removed btnExploreMap from here as it is now in Quick Actions

        // --- New Features (Placeholders) ---
        val newFeatures = mapOf(
            R.id.btnNews to "News Portal"
        )

        newFeatures.forEach { (id, name) ->
            view.findViewById<View>(id)?.setOnClickListener {
                Toast.makeText(requireContext(), "$name Coming Soon!", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Quick Actions with Animation ---
        val cardExploreMapQuick = view.findViewById<View>(R.id.cardQuickExploreMap)
        val cardNotif = view.findViewById<View>(R.id.cardQuickNotifications)
        val cardSaved = view.findViewById<View>(R.id.cardQuickSaved) // New
        val cardPachkhan = view.findViewById<View>(R.id.cardQuickPachkhan)
        val cardMonksQuick = view.findViewById<View>(R.id.cardQuickMonks)

        // Apply Subtle Floating Animation
        startFloatingAnimation(cardExploreMapQuick, 0L)
        startFloatingAnimation(cardNotif, 200L) 
        startFloatingAnimation(cardSaved, 300L) // New
        startFloatingAnimation(cardPachkhan, 400L)
        startFloatingAnimation(cardMonksQuick, 600L)

        cardExploreMapQuick?.setOnClickListener {
            startActivity(Intent(requireContext(), ExploreMapActivity::class.java))
        }
        cardNotif?.setOnClickListener {
             startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }
        cardSaved?.setOnClickListener {
             startActivity(Intent(requireContext(), SavedActivity::class.java))
        }
        cardPachkhan?.setOnClickListener {
            startActivity(Intent(requireContext(), PachkhanActivity::class.java))
        }
        cardMonksQuick?.setOnClickListener {
            startActivity(Intent(requireContext(), MaharajLocationActivity::class.java))
        }


    }

    // Helper for entry fade-in/slide-up animation
    private fun startEntryAnimation(view: View?, delay: Long) {
        view?.alpha = 0f
        view?.translationY = 50f
        view?.animate()
            ?.alpha(1f)
            ?.translationY(0f)
            ?.setDuration(600)
            ?.setStartDelay(delay)
            ?.setInterpolator(android.view.animation.DecelerateInterpolator())
            ?.start()
    }

    // Helper for subtle floating animation
    private fun startFloatingAnimation(view: View?, delay: Long) {
        view?.let {
            val animator = android.animation.ObjectAnimator.ofFloat(it, "translationY", 0f, -10f, 0f)
            animator.duration = 2000 // 2 seconds per cycle
            animator.repeatCount = android.animation.ObjectAnimator.INFINITE
            animator.repeatMode = android.animation.ObjectAnimator.REVERSE
            animator.startDelay = delay
            animator.start()
        }
    }


    // ⭐ AMOUNT DIALOG LISTENER: Receives the custom amount from the DialogFragment
    override fun onAmountEntered(amountInPaise: Int) {
        // This function is called by AmountDialogFragment when the user hits 'Proceed to Donate'
        if (amountInPaise > 0) {
            startPayment(amountInPaise)
        } else {
            Toast.makeText(requireContext(), "Donation amount cannot be zero.", Toast.LENGTH_SHORT).show()
        }
    }


    // ⭐ PAYMENT LAUNCHER
    private fun startPayment(amount: Int) {
        val checkout = Checkout()
        // Using your TEST key as requested for development purposes
        checkout.setKeyID("rzp_test_RNVxYRUof2vEci")

        try {
            val options = org.json.JSONObject()
            options.put("name", "JainConnect App Donation")
            options.put("description", "Support the App Development")
            options.put("currency", "INR")
            options.put("amount", amount) // Amount is in smallest currency unit (paise)
            options.put("theme.color", "#FF9800")

            val prefill = org.json.JSONObject()
            // Using placeholder prefill data for better test detection
            prefill.put("email", "test@razorpay.com")
            prefill.put("contact", "9999999999")
            options.put("prefill", prefill)

            // Launch the Razorpay checkout interface
            checkout.open(requireActivity(), options)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error in Payment: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    private fun loadDashboardData() {
        val token = sharedPreferences.getString("jwt_token", null)

        if (token != null) {
            viewModel.fetchUserProfile(token)
        } else {
            tvGreeting.text = "Jai Jinendra!"
            isUserLoaded = true
            checkDataLoaded()
        }

        viewModel.fetchTithis()
        
        // Only fetch sun data if enabled
        val isLocationOn = sharedPreferences.getBoolean("is_location_on", false)
        if (isLocationOn) {
            // We usually wait for GPS, but if we have saved coords we could use them.
            // For now, let the toggle setup trigger the fresh fetch.
            // Or use default just to init if needed, but user asked for "No Data".
            // So we do nothing here, the setupLocationToggle calls checkAndRequestLocation if ON.
        }
    }

    private fun checkDataLoaded() {
        if (isUserLoaded && isTithiLoaded && isSunLoaded) {
            shimmerDashboard.stopShimmer()
            shimmerDashboard.visibility = View.GONE
            cardGreeting.visibility = View.VISIBLE
        }
    }

    private fun observeData() {
        // --- 1. Update Greeting Name ---
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            isUserLoaded = true
            checkDataLoaded()
            if (user != null) {
                tvGreeting.text = "Jai Jinendra ${user.name}!"
                sharedPreferences.edit().putString("cached_user_name", user.name).apply()
            }
        }

        // --- 2. Update Tithi ---
        viewModel.tithiList.observe(viewLifecycleOwner) { tithiList ->
            isTithiLoaded = true
            checkDataLoaded()
            if (tithiList.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayDate = sdf.format(Calendar.getInstance().time)
                val todayTithi = tithiList.find { it.date == todayDate }

                if (todayTithi != null) {
                    tvTithiName.text = todayTithi.name
                    sharedPreferences.edit().putString("cached_tithi_name", todayTithi.name).apply()
                } else {
                    tvTithiName.text = "No Data"
                }
            }
        }

        // --- 3. Update Sunrise/Sunset ---
        viewModel.horizonList.observe(viewLifecycleOwner) { horizonList ->
            isSunLoaded = true
            checkDataLoaded()
            if (horizonList.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                val todayDate = sdf.format(Calendar.getInstance().time)
                val todayHorizon = horizonList.find { it.date == todayDate }

                if (todayHorizon != null) {
                    tvSunriseTime.text = todayHorizon.sunrise
                    tvSunsetTime.text = todayHorizon.sunset
                    sharedPreferences.edit()
                        .putString("cached_sunrise", todayHorizon.sunrise)
                        .putString("cached_sunset", todayHorizon.sunset)
                        .apply()
                } else {
                    tvSunriseTime.text = horizonList[0].sunrise
                    tvSunsetTime.text = horizonList[0].sunset
                }
            }
        }
    }

    // ⭐ PAYMENT RESULT LISTENERS
    override fun onPaymentSuccess(razorpayPaymentID: String) {
        Toast.makeText(requireContext(), "Donation Successful! ID: $razorpayPaymentID", Toast.LENGTH_LONG).show()
    }

    override fun onPaymentError(code: Int, response: String) {
        Toast.makeText(requireContext(), "Donation Failed. Error Code: $code", Toast.LENGTH_LONG).show()
    }

    // --- Location Logic (Updated for New Header) ---

    private fun setupLocationHeader(view: View) {
        val layoutLocation = view.findViewById<View>(R.id.layoutLocationData)
        val tvLocationHeader = view.findViewById<TextView>(R.id.tvHeaderLocationName) // In Header

        // Load saved state
        val hasPermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val isLocationOn = hasPermission || sharedPreferences.getBoolean("is_location_on", false)
        
        if (isLocationOn) {
            tvLocationHeader.text = "Locating..."
            if(hasPermission) sharedPreferences.edit().putBoolean("is_location_on", true).apply()
            checkAndRequestLocation()
        } else {
            tvLocationHeader.text = "Location Off"
            clearLocationData()
        }

        layoutLocation.setOnClickListener {
             showLocationDialog()
        }
    }

    private fun showLocationDialog() {
        val isCurrentlyOn = sharedPreferences.getBoolean("is_location_on", false)
        val title = if (isCurrentlyOn) "Turn Location OFF?" else "Turn Location ON?"
        val message = if (isCurrentlyOn) "Do you want to stop getting location updates?" else "Enable location to see sunrise/sunset and city info?"

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(if (isCurrentlyOn) "Turn Off" else "Enable") { _, _ ->
                if (isCurrentlyOn) {
                    // Turn Off
                    sharedPreferences.edit().putBoolean("is_location_on", false).apply()
                    clearLocationData()
                    Toast.makeText(requireContext(), "Location Disabled", Toast.LENGTH_SHORT).show()
                } else {
                    // Turn On
                    sharedPreferences.edit().putBoolean("is_location_on", true).apply()
                    checkAndRequestLocation()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    // ... existing permission methods ...

    private fun clearLocationData() {
        if(::tvLocationName.isInitialized) tvLocationName.text = "Location Off"
        if(::tvSunriseTime.isInitialized) tvSunriseTime.text = "--:--"
        if(::tvSunsetTime.isInitialized) tvSunsetTime.text = "--:--"
    }
    
    // ... existing permission methods ...

    private fun checkAndRequestLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission missing (though Mandatory logic should have caught this), ask again
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            setupLocationListener()
        }
    }

    private fun setupLocationListener() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                fetchCityName(location.latitude, location.longitude)
            } else {
                Toast.makeText(context, "Unable to fetch location. Using default.", Toast.LENGTH_SHORT).show()
                updateUIWithLocation("Jaipur", 26.9124, 75.7873)
            }
        }
    }

    private fun fetchCityName(lat: Double, long: Double) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            // Deprecated in API 33 but still works for now.
            val addresses = geocoder.getFromLocation(lat, long, 1)
            if (!addresses.isNullOrEmpty()) {
                val city = addresses[0].locality ?: addresses[0].subAdminArea ?: "Unknown"
                updateUIWithLocation(city, lat, long)
            } else {
                updateUIWithLocation("Unknown Loc", lat, long)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateUIWithLocation("Error", lat, long)
        }
    }

    private fun updateUIWithLocation(cityName: String, lat: Double, long: Double) {
        // Update Main Location Text (Card)
        if(::tvLocationName.isInitialized) {
             tvLocationName.text = cityName
        }

        // Update Header Location Text
        val tvHeader = view?.findViewById<TextView>(R.id.tvHeaderLocationName)
        tvHeader?.text = cityName

        // Calculate Sun Times using new Coords
        val sunTimes = calculateSunTimes(lat, long)
        if(::tvSunriseTime.isInitialized && ::tvSunsetTime.isInitialized) {
            tvSunriseTime.text = sunTimes.first
            tvSunsetTime.text = sunTimes.second
        }
        
        viewModel.fetchSunData(lat, long)
    }

    // Reuse existing calculation logic but parametrized
    private fun calculateSunTimes(lat: Double, long: Double): Pair<String, String> {
        // Simplified logic for demo (Real logic needs SunCalc algo or API)
        // For now, let's just shift time slightly based on Longitude difference from Jaipur (75.78)
        // 1 degree diff approx 4 mins.
        val baseLong = 75.7873
        val diffDeg = baseLong - long
        val diffMins = (diffDeg * 4).toInt()

        // Base Jaipur Times (approx default)
        var sunriseMins = 7 * 60 + 5 // 07:05
        var sunsetMins = 17 * 60 + 35 // 17:35

        // Adjust
        sunriseMins += diffMins
        sunsetMins += diffMins

        return Pair(formatTime(sunriseMins), formatTime(sunsetMins))
    }
    
    private fun formatTime(totalMins: Int): String {
        val hrs = totalMins / 60
        val mins = totalMins % 60
        return String.format("%02d:%02d", hrs, mins)
    }

}