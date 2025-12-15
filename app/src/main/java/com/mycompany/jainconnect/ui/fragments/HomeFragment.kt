package com.mycompany.jainconnect.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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

@AndroidEntryPoint
class HomeFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

        initializeViews(view)
        setupNavigationButtons(view)
        
        // Ensure shimmer starts
        shimmerDashboard.startShimmer()
        
        observeData()
        loadDashboardData()
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
        val sdfDate = SimpleDateFormat("EEEE, dd-MM-yy", Locale.getDefault())
        tvDate.text = sdfDate.format(Calendar.getInstance().time)

        // Set Location (Placeholder)
        tvLocationName.text = "Jaipur"
    }

    private fun setupNavigationButtons(view: View) {
        // --- Header Buttons ---
        view.findViewById<View>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }
        view.findViewById<View>(R.id.btnHelp).setOnClickListener {
            startActivity(Intent(requireContext(), ContactActivity::class.java))
        }
        view.findViewById<View>(R.id.btnDonate).setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Donation Feature Coming Soon", android.widget.Toast.LENGTH_SHORT).show()
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

        // --- New Features (Placeholders) ---
        val newFeatures = mapOf(
            R.id.btnTemples to "Temples",
            R.id.btnBhojanshalas to "Bhojanshalas",
            R.id.btnBusiness to "Business Directory",
            R.id.btnCarpooling to "Carpooling",
            R.id.btnTirthyatra to "Tirthyatra Planner",
            R.id.btnNews to "News Portal"
        )
        
        newFeatures.forEach { (id, name) ->
            view.findViewById<View>(id)?.setOnClickListener {
                android.widget.Toast.makeText(requireContext(), "$name Coming Soon!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        // --- Quick Actions ---
         view.findViewById<View>(R.id.cardQuickNotifications)?.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Notifications", android.widget.Toast.LENGTH_SHORT).show()
        }
         view.findViewById<View>(R.id.cardQuickPachkhan)?.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Pachkhan", android.widget.Toast.LENGTH_SHORT).show()
        }
         view.findViewById<View>(R.id.cardQuickMonks)?.setOnClickListener {
             startActivity(Intent(requireContext(), MaharajLocationActivity::class.java))
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
        viewModel.fetchSunData(26.9124, 75.7873)
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
}
