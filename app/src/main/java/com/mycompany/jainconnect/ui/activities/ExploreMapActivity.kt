package com.mycompany.jainconnect.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@AndroidEntryPoint
class ExploreMapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private val viewModel: JainViewModel by viewModels()
    private var myLocationOverlay: MyLocationNewOverlay? = null

    // Permission Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            setupLocationOverlay()
        } else {
            Toast.makeText(this, "Location permission denied. Cannot show your location.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Important for OSMDroid configuration
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContentView(R.layout.activity_explore_map)

        mapView = findViewById(R.id.mapView)
        setupMap()

        // Request Permissions
        checkPermissions()

        // Fetch Data
        viewModel.fetchTemples()
        viewModel.fetchBhojanshalas()

        // Observe Data
        observeData()

        val fabMyLocation = findViewById<View>(R.id.fabMyLocation)
        fabMyLocation.setOnClickListener {
            val location = myLocationOverlay?.myLocation
            if (location != null) {
                mapView.controller.animateTo(location)
                mapView.controller.setZoom(15.0)
            } else {
                Toast.makeText(this, "Waiting for location...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        
        // Default View (India Center approx)
        val startPoint = GeoPoint(20.5937, 78.9629)
        mapView.controller.setZoom(5.0)
        mapView.controller.setCenter(startPoint)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        } else {
            setupLocationOverlay()
        }
    }

    private fun setupLocationOverlay() {
        if (myLocationOverlay == null) {
            myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
            myLocationOverlay?.enableMyLocation()
            mapView.overlays.add(myLocationOverlay)
            
            // Auto zoom to location once found
            myLocationOverlay?.runOnFirstFix {
                 runOnUiThread {
                     val loc = myLocationOverlay?.myLocation
                     if (loc != null) {
                         mapView.controller.animateTo(loc)
                         mapView.controller.setZoom(15.0)
                     }
                 }
            }
        }
    }

    private fun observeData() {
        viewModel.templeList.observe(this) { temples ->
            // Use IO Dispatcher for Geocoding (Network operation)
            lifecycleScope.launch(Dispatchers.IO) {
                val markers = mutableListOf<Marker>()
                
                temples.forEach { temple ->
                    val addressToSearch = if (!temple.address.isNullOrEmpty()) {
                        "${temple.address}, ${temple.city}"
                    } else {
                        temple.city
                    }
                    
                    val point = getCoordinatesFromAddress(addressToSearch)
                    
                    if (point != null) {
                        val marker = Marker(mapView)
                        marker.position = point
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = temple.name
                        marker.snippet = temple.address ?: temple.city
                        marker.subDescription = "Tap for details"
                        
                        marker.setOnMarkerClickListener { m, _ ->
                            m.showInfoWindow()
                            true
                        }
                        markers.add(marker)
                    }
                }

                // Switch to Main thread to update UI
                withContext(Dispatchers.Main) {
                    markers.forEach { mapView.overlays.add(it) }
                    mapView.invalidate()
                    if (markers.isNotEmpty()) {
                         // Optional: Center map on first result if location not found
                         // mapView.controller.animateTo(markers[0].position)
                    }
                }
            }
        }

        viewModel.bhojanshalaList.observe(this) { bhojanshalas ->
             lifecycleScope.launch(Dispatchers.IO) {
                val markers = mutableListOf<Marker>()
                
                bhojanshalas.forEach { place ->
                     val addressToSearch = "${place.address}, ${place.city}"
                     val point = getCoordinatesFromAddress(addressToSearch)

                     if (point != null) {
                         val marker = Marker(mapView)
                         marker.position = point
                         marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                         marker.title = place.name
                         marker.snippet = place.address
                         
                         marker.setOnMarkerClickListener { m, _ ->
                            m.showInfoWindow()
                            true
                        }
                        markers.add(marker)
                     }
                }
                
                withContext(Dispatchers.Main) {
                    markers.forEach { mapView.overlays.add(it) }
                    mapView.invalidate()
                }
            }
        }
    }

    private fun getCoordinatesFromAddress(address: String): GeoPoint? {
        return try {
            val geocoder = android.location.Geocoder(this, java.util.Locale.getDefault())
            // Deprecated in API 33 but still works broadly, handled for simplicity
            @Suppress("DEPRECATION")
            val locationList = geocoder.getFromLocationName(address, 1)
            
            if (!locationList.isNullOrEmpty()) {
                val location = locationList[0]
                GeoPoint(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
