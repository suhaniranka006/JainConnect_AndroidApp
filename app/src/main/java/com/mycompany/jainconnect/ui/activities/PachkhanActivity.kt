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
                    // Try to find tomorrow's horizon for next sunrise
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    val tomorrowDateStr = sdf.format(cal.time)
                    val tomorrowHorizon = horizonList.find { it.date == tomorrowDateStr }
                    
                    val nextSunrise = tomorrowHorizon?.sunrise ?: "06:30 AM" // Fallback
                    
                    setupList(todayHorizon.sunrise, todayHorizon.sunset, nextSunrise)
                } else {
                    setupList("06:30 AM", "06:30 PM", "06:30 AM")
                }
            } else {
                 setupList("06:30 AM", "06:30 PM", "06:30 AM")
            }
        }

        // Trigger fetch
        viewModel.fetchSunData(26.9124, 75.7873) // Jaipur defaults
    }

    private fun setupList(sunriseStr: String, sunsetStr: String, nextSunriseStr: String) {
        // API sends "HH:mm" (24-hour), so input must match that.
        val sdfInput = SimpleDateFormat("HH:mm", Locale.getDefault()) 
        val sdfOutput = SimpleDateFormat("hh:mm a", Locale.getDefault())

        fun calculateTime(baseTime: String, minutesToAdd: Int): String {
            try {
                val date = sdfInput.parse(baseTime) ?: return "N/A"
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.add(Calendar.MINUTE, minutesToAdd)
                return sdfOutput.format(calendar.time)
            } catch (e: Exception) {
                return "N/A"
            }
        }

        val navkarsiTime = calculateTime(sunriseStr, 48)
        val porsiTime = calculateTime(sunriseStr, 180) // 3 hours
        val sadhPorsiTime = calculateTime(sunriseStr, 270) // 4.5 hours
        
        // Use ic_sunny for morning, ic_sunset (or similar) for evening
        val sunIcon = R.drawable.ic_sunrise 
        val nightIcon = R.drawable.ic_sunset

        val list = listOf(
            PachkhanItem("Navkarsi", "End Time: $navkarsiTime", "Do not eat/drink until 48 mins after sunrise.", sunIcon),
            PachkhanItem("Porsi", "End Time: $porsiTime", "Do not eat/drink until 3 hours after sunrise.", sunIcon),
            PachkhanItem("Sadh-Porsi", "End Time: $sadhPorsiTime", "Do not eat/drink until 4.5 hours after sunrise.", sunIcon),
            PachkhanItem("Chauvihar", "Sunset: $sunsetStr | Sunrise: $nextSunriseStr", "No food or water from Sunset to next Sunrise.", nightIcon),
            PachkhanItem("Tivihar", "Sunset: $sunsetStr | Sunrise: $nextSunriseStr", "No food from Sunset to next Sunrise. Water allowed.", nightIcon),
            
            // General Vows (Audio Disabled)
            PachkhanItem("Ekasam", "Valid for Today", "Eat only once in a day sitting in one place.", R.drawable.ic_pachkhan, false),
            PachkhanItem("Biasan", "Valid for Today", "Eat only twice in a day sitting in one place.", R.drawable.ic_pachkhan, false),
            PachkhanItem("Upvas", "Valid for Today", "Fasting for the whole day (No food).", R.drawable.ic_pachkhan, false),
            PachkhanItem("Ayambil", "Valid for Today", "One meal, no spices/milk/sugar/ghee/oil.", R.drawable.ic_pachkhan, false),
            
            // Tyags (14 Niyam) (Audio Disabled)
            PachkhanItem("Hari ka Tyag", "Valid for Today", "Avoid green vegetables/fruits today.", R.drawable.ic_pachkhan, false),
            PachkhanItem("Jamikand ka Tyag", "Valid for Today", "Avoid root vegetables today.", R.drawable.ic_pachkhan, false),
            PachkhanItem("Mukhvas ka Tyag", "Valid for Today", "Avoid mouth fresheners today.", R.drawable.ic_pachkhan, false),
            PachkhanItem("Vigay ka Tyag", "Valid for Today", "Avoid 6 Vigayas (Milk, Curd, Ghee, Oil, Sugar, Jaggery).", R.drawable.ic_pachkhan, false),
            PachkhanItem("Mahavigay ka Tyag", "Valid for Today", "Avoid Butter, Honey, Alcohol, Meat.", R.drawable.ic_pachkhan, false)
        )

        adapter = PachkhanAdapter(list) { item ->
            Toast.makeText(this, "Playing ${item.name} audio...", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
    }
}
