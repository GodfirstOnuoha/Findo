package com.godfirst.findo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.godfirst.findo.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val PERMISSION_REQUEST = 419
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        loadMapWithCurrentLocation()
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            PERMISSION_REQUEST
                        )
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener { location ->
            mMap.clear()
            val myLocation = LatLng(location.latitude, location.longitude)
            mMap.addMarker(MarkerOptions().position(myLocation).title("My Current Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadMapWithCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST
            )
        }

        mMap.setOnMapLongClickListener {
            mMap.addMarker(
                MarkerOptions().position(it).title("New Marker")
                    .snippet("${it.latitude}, ${it.longitude}")
            )
        }
        mMap.setOnPoiClickListener {
            Toast.makeText(
                this,
                "You clicked: ${it.name} with the Location of \n${it.latLng}",
                Toast.LENGTH_LONG
            ).show()
        }
        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.normal_chip -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                }
                R.id.hybrid_chip -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                }
                R.id.satellite_chip -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                }
                R.id.terrain_chip -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadMapWithCurrentLocation() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0.0f,
            locationListener
        )
        val lastKnownLocation =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: return
        val myLocation = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
        mMap.addMarker(MarkerOptions().position(myLocation).title("My Current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
        Toast.makeText(this@MapsActivity, "$myLocation", Toast.LENGTH_SHORT).show()
    }
}