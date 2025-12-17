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
import androidx.appcompat.widget.SwitchCompat // For Switch

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
            view?.findViewById<SwitchCompat>(R.id.switchLocation)?.isChecked = false
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
        setupLocationToggle(view) 
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
    }

    private fun setupNavigationButtons(view: View) {
        // --- Header Buttons ---
        view.findViewById<View>(R.id.btnProfile).setOnClickListener {
            // Updated to Open Drawer
            if (activity is MainActivity) {
                (activity as MainActivity).openDrawer()
            }
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

        // --- Explore Grid Buttons ---
        view.findViewById<View>(R.id.btnTithi).setOnClickListener {
            startActivity(Intent(requireContext(), TithiActivity::class.java))
        }
        view.findViewById<View>(R.id.btnEvents).setOnClickListener {
            startActivity(Intent(requireContext(), EventActivity::class.java))
        }
        view.findViewById<View>(R.id.btnMonks).setOnClickListener {
            startActivity(Intent(requireContext(), MaharajLocationActivity::class.java))
        }
        view.findViewById<View>(R.id.btnHorizons).setOnClickListener {
            startActivity(Intent(requireContext(), HorizonsActivity::class.java))
        }
        view.findViewById<View>(R.id.btnBhojanshalas).setOnClickListener {
            startActivity(Intent(requireContext(), BhojanshalaActivity::class.java))
        }
        view.findViewById<View>(R.id.btnTemples).setOnClickListener {
            startActivity(Intent(requireContext(), TempleActivity::class.java))
        }
        view.findViewById<View>(R.id.btnCarpooling).setOnClickListener {
            startActivity(Intent(requireContext(), CarpoolActivity::class.java))
        }

        // --- New Features (Placeholders) ---
        val newFeatures = mapOf(
            R.id.btnBusiness to "Business Directory",
            R.id.btnTirthyatra to "Tirthyatra Planner",
            R.id.btnNews to "News Portal"
        )

        newFeatures.forEach { (id, name) ->
            view.findViewById<View>(id)?.setOnClickListener {
                Toast.makeText(requireContext(), "$name Coming Soon!", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Quick Actions ---
        view.findViewById<View>(R.id.cardQuickNotifications)?.setOnClickListener {
            Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.cardQuickPachkhan)?.setOnClickListener {
            startActivity(Intent(requireContext(), PachkhanActivity::class.java))
        }
        view.findViewById<View>(R.id.cardQuickMonks)?.setOnClickListener {
            startActivity(Intent(requireContext(), MaharajLocationActivity::class.java))
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

    // --- Location Logic ---

    private fun setupLocationToggle(view: View) {
        val switchLocation = view.findViewById<SwitchCompat>(R.id.switchLocation)
        val tvToggleLabel = view.findViewById<TextView>(R.id.tvToggleLocationLabel)

        // Helper to update colors
        fun updateSwitchColor(isChecked: Boolean) {
            if (isChecked) {
                val green = android.graphics.Color.parseColor("#4CAF50")
                val lightGreen = android.graphics.Color.parseColor("#A5D6A7")
                switchLocation.thumbTintList = android.content.res.ColorStateList.valueOf(green)
                switchLocation.trackTintList = android.content.res.ColorStateList.valueOf(lightGreen)
            } else {
                val gray = android.graphics.Color.GRAY
                val lightGray = android.graphics.Color.LTGRAY
                switchLocation.thumbTintList = android.content.res.ColorStateList.valueOf(gray)
                switchLocation.trackTintList = android.content.res.ColorStateList.valueOf(lightGray)
            }
        }

        // Load saved state
        // Load state: If permission is granted, force ON. Else rely on pref.
        val hasPermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val isLocationOn = hasPermission || sharedPreferences.getBoolean("is_location_on", false)
        
        switchLocation.isChecked = isLocationOn
        updateSwitchColor(isLocationOn)

        if (isLocationOn) {
            tvToggleLabel.visibility = View.VISIBLE
            tvToggleLabel.text = "Locating..."
            tvToggleLabel.setTextColor(android.graphics.Color.BLACK)
            // Save state as true if permission exists
            if(hasPermission) sharedPreferences.edit().putBoolean("is_location_on", true).apply()
            checkAndRequestLocation()
        } else {
            tvToggleLabel.visibility = View.GONE
            clearLocationData()
        }

        switchLocation.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchColor(isChecked)
            if (isChecked) {
                // User turned ON
                tvToggleLabel.visibility = View.VISIBLE
                tvToggleLabel.text = "Locating..."
                tvToggleLabel.setTextColor(android.graphics.Color.BLACK)
                sharedPreferences.edit().putBoolean("is_location_on", true).apply()
                checkAndRequestLocation()
            } else {
                // User turned OFF
                tvToggleLabel.visibility = View.GONE
                sharedPreferences.edit().putBoolean("is_location_on", false).apply()
                clearLocationData()
            }
        }
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
        
        // Update Toggle Label (Under Switch)
        val tvToggleLabel = view?.findViewById<TextView>(R.id.tvToggleLocationLabel)
        tvToggleLabel?.text = cityName

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