package com.snipertech.leftoversaver.view.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.ActivityMapsBinding
import com.snipertech.leftoversaver.util.Constants.REGISTERED_LOCATION
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var mLocationPermissionsGranted = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var location: String

    companion object {
        private const val TAG = "MapActivity"
        private const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1234
        private const val DEFAULT_ZOOM = 15f
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        getLocationPermission()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (mLocationPermissionsGranted) {
            getDeviceLocation()

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false

            init()
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ic_gps -> {
                Log.d(TAG, "onClick: clicked gps icon")
                getDeviceLocation()
            }
            R.id.confirm_location -> {
                Log.d(TAG, "onClick: location sent to register")
                confirmLocation()
            }
        }
    }


    //get the required permissions
    private fun getLocationPermission() {
        Log.d(
            TAG,
            "getLocationPermission: getting location permissions"
        )
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    this.applicationContext,
                    COURSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mLocationPermissionsGranted = true
                initMap()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    //returns the current location for the device
    private fun getDeviceLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            if (mLocationPermissionsGranted) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener {
                        val gcd = Geocoder(applicationContext, Locale.ENGLISH)
                        if (it != null) {
                            val addresses: List<Address> = gcd.getFromLocation(
                                it.latitude, it.longitude, 1
                            )

                            if (addresses.isNotEmpty()) {
                                Toast.makeText(
                                    applicationContext,
                                    addresses[0].locality + " ",
                                    Toast.LENGTH_LONG
                                ).show()

                                location = addresses[0].locality

                                moveCamera(
                                    LatLng(
                                        it.latitude,
                                        it.longitude
                                    ), addresses[0].locality
                                )
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Please make sure you have location turned on",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        } catch (e: SecurityException) {
            Log.e(
                TAG,
                "getDeviceLocation: SecurityException: " + e.message!!
            )
        }
    }

    private fun init() {
        Log.d(TAG, "init: initializing")
        searchLocation()
        binding.icGps.setOnClickListener(this)
        binding.confirmLocation.setOnClickListener(this)
        getLocationOnTouch()
        hideSoftKeyboard()
    }

    // confirm the location
    private fun confirmLocation() {
        val intent = Intent()
        intent.putExtra(REGISTERED_LOCATION, location)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


    // get location from search
    private fun searchLocation() {
        // Initialize the SDK
        if (!Places.isInitialized()) {
            Places.initialize(
                applicationContext,
                "KEY HERE",
                Locale.US
            )
        }
        // Create a new Places client instance
        val placesClient = Places.createClient(this)

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment: AutocompleteSupportFragment? =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment?

        autocompleteFragment?.setTypeFilter(TypeFilter.ADDRESS)

        autocompleteFragment?.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                //Get info about the selected place.
                mMap.clear()
                moveCamera(place.latLng, place.name.toString())
            }

            override fun onError(status: Status) {
                //Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }

    //On touch map
    private fun getLocationOnTouch() {
        mMap.setOnMapClickListener {
            val gcd = Geocoder(applicationContext, Locale.ENGLISH)
            val addresses: List<Address> = gcd.getFromLocation(
                it.latitude, it.longitude, 1
            )

            if (addresses.isNotEmpty()) {
                Toast.makeText(
                    applicationContext,
                    addresses[0].locality + " ",
                    Toast.LENGTH_LONG
                ).show()

                if (addresses[0].locality != null) {
                    location = addresses[0].locality
                }
            }
            mMap.clear()
            moveCamera(it, addresses[0].locality)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mLocationPermissionsGranted = false
        Log.d(TAG, "onRequestPermissionsResult: called.")
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                mLocationPermissionsGranted = true
                Log.d(
                    TAG,
                    "onRequestPermissionsResult: permission granted"
                )
                //initialize our map
                initMap()
            } else {
                mLocationPermissionsGranted = false
                Log.d(
                    TAG,
                    "onRequestPermissionsResult: permission failed"
                )
                return
            }
        }
    }

    private fun initMap() {
        Log.d(TAG, "initMap: initializing map")
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }


    private fun hideSoftKeyboard() {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }


    //move camera to the desired location
    private fun moveCamera(
        latLng: LatLng?,
        title: String?
    ) {

        Log.d(
            TAG,
            "moveCamera: moving the camera to: lat: "
                    + latLng?.latitude + ", lng: " + latLng?.longitude
        )
        val options = MarkerOptions()
            .position(latLng!!)
            .title(title)
        mMap.addMarker(options)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
        hideSoftKeyboard()
    }
}