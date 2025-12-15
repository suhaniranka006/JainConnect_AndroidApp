package com.mycompany.jainconnect.ui.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.PachkhanAdapter
import com.mycompany.jainconnect.ui.adapters.PachkhanItem
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class PachkhanActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PachkhanAdapter
    private lateinit var tvDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pachkhan)

        // Setup Header
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        tvDate = findViewById(R.id.tvCurrentDatePachkhan)
        
        val sdfDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        tvDate.text = sdfDate.format(Calendar.getInstance().time)

        recyclerView = findViewById(R.id.recyclerViewPachkhan)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe Horizon Data (Sunrise/Sunset) to calculate timings
        viewModel.horizonList.observe(this) { horizonList ->
            if (horizonList.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                val todayDateStr = sdf.format(Calendar.getInstance().time)
                val todayHorizon = horizonList.find { it.date == todayDateStr }

                if (todayHorizon != null) {
                    setupList(todayHorizon.sunrise, todayHorizon.sunset)
                } else {
                    // Fallback
                    setupList("06:30 AM", "06:30 PM")
                }
            } else {
                 setupList("06:30 AM", "06:30 PM")
            }
        }

        // Trigger fetch
        viewModel.fetchSunData(26.9124, 75.7873) // Jaipur defaults
    }

    private fun setupList(sunrise: String, sunset: String) {
        // Logic to parse time would go here. For now using basic offsets representation.
        // Ideally we parse "06:45 AM" -> Date object -> Add minutes -> Format back.
        // Doing simplified version for MVP.

        val list = listOf(
            PachkhanItem("Navkarsi", "Sunrise + 48 mins", "Do not eat/drink until 48 mins after sunrise."),
            PachkhanItem("Porsi", "Sunrise + 3 hrs", "Do not eat/drink until 3 hours after sunrise."),
            PachkhanItem("Sadh-Porsi", "Sunrise + 4.5 hrs", "Do not eat/drink until 4.5 hours after sunrise."),
            PachkhanItem("Purimaddh", "Sunrise + 6 hrs", "Do not eat/drink until 6 hours after sunrise."),
            PachkhanItem("Avaddh", "Sunrise + 9 hrs", "Do not eat/drink until 9 hours after sunrise."),
            PachkhanItem("Chauvihar", "Sunset ($sunset)", "No food or water after sunset."),
            PachkhanItem("Tivihar", "Sunset ($sunset)", "No food after sunset, water allowed.")
        )

        adapter = PachkhanAdapter(list) { item ->
            Toast.makeText(this, "Playing ${item.name} audio...", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
    }
}
