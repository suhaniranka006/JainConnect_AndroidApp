package com.mycompany.jainconnect.ui.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.TirthyatraPagerAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TirthyatraActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tirthyatra)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
        
        val btnAdd = findViewById<ImageView>(R.id.btnAddYatra)
        btnAdd.setOnClickListener {
            // Navigate to Create Yatra Screen (To be implemented)
            Toast.makeText(this, "Create Yatra coming soon", Toast.LENGTH_SHORT).show()
        }

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val adapter = TirthyatraPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Explore"
                1 -> "My Yatras"
                2 -> "Community"
                else -> "Explore"
            }
        }.attach()
    }

    private fun observeViewModel() {
        // Observe data if needed here
    }
}
