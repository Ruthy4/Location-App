package com.decagon.android.sq007


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var map: GoogleMap
    private var marker1 : Marker? = null
    private var marker2 : Marker? = null
    lateinit var databaseRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        databaseRef = Firebase.database.reference
        databaseRef.addValueEventListener(logListener)

    }

    //gets location access and displays the google map
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getLocationAccess()

        getLocationUpdates()
        startLocationUpdates()
    }


    private val logListener = object : ValueEventListener {

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(applicationContext, "Could not read from database", Toast.LENGTH_LONG).show()
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {

            if (dataSnapshot.exists()) {

                val locationLogging = dataSnapshot.child("femilocation").getValue(LocationModel::class.java)

                val partnerLat =locationLogging?.latitude
                val partnerLong=locationLogging?.longitude

                if (partnerLat !=null  && partnerLong != null) {
                    val partnerLoc = LatLng(partnerLat, partnerLong)

                    marker1 = map.addMarker(MarkerOptions().position(partnerLoc).title("Femi"))
                    marker1?.position = partnerLoc

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(partnerLoc, 20f))


                    Toast.makeText(applicationContext, "Locations accessed from the database", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    //loads the location on the map if permission has ben granted
    private fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            map.isMyLocationEnabled = true

        } else
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
    }

    //set the interval for receiving location updates and connect to a database
    private fun getLocationUpdates() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 20000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation


                    //get a reference to the database
                    databaseRef= Firebase.database.reference


                    val locationLogging = LocationModel(location.latitude, location.longitude)


                    databaseRef.child("userlocation").setValue(locationLogging).addOnSuccessListener {
                        Toast.makeText(applicationContext, "Locations written into the database", Toast.LENGTH_LONG).show()
                        }.addOnFailureListener {
                            Toast.makeText(
                                applicationContext,
                                "Error occured while writing the locations", Toast.LENGTH_LONG).show()
                        }



                    map.clear()
                    //get the user location, add a marker then zoom the map to building level
                    val latLng = LatLng(location.latitude, location.longitude)
                    Log.d("Map", "onLocationResult: ${latLng.latitude} ${latLng.longitude}}")
                    marker1 = map.addMarker(MarkerOptions().position(latLng).title("Ruth"))
                   marker1?.position = latLng

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))
                }
            }
        }
    }

    //checks for permission and register the location call back with the location client
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        //register callback with client
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
//                getLocationAccess()
            } else {
                Toast.makeText(
                    this,
                    "User has not granted location access permission",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }
}

