package com.example.geofencingalerts

// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions


internal class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    GoogleMap.OnMapClickListener,
    GoogleMap.OnMarkerClickListener,
    LocationListener {

    private lateinit var mMap: GoogleMap
    lateinit var geofencingClient: GeofencingClient
    private lateinit var geofence: Geofence
    private var geoFenceList: ArrayList<Geofence>? = null
    private var googleApiClient: GoogleApiClient? = null
    private lateinit var lastLocation: Location
    private lateinit var textLat: TextView
    private lateinit var textLong: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_maps)

        createGoogleApi()


        geofence = Geofence.Builder()
            .setRequestId("1234") // Geofence ID
            .setCircularRegion(6.201524, -75.501660, 2000.56f ) // defining fence region
            .setExpirationDuration(8000000000000000000) // expiring date
            // Transition types that it should look for
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()


        var request =
            GeofencingRequest.Builder() // Notification to trigger when the Geofence is created
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence) // add a Geofence
                .build()

        geoFenceList?.add(geofence)
        geofencingClient = LocationServices.getGeofencingClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        geofencingClient.addGeofences(request, geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences added
                // ...
            }
            addOnFailureListener {
                // Failed to add geofences
                // ...
            }
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
/*        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)*/
    }
    private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest? {
        Log.d("TAG", "createGeofenceRequest")
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }
    private fun createGoogleApi() {
        Log.d("w", "createGoogleApi()")
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        }
    }

    // Get last known location
    private fun getLastKnownLocation() {
        Log.d("w", "getLastKnownLocation()")
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            Log.i(
                "TAG", "LasKnown location. " +
                        "Long: " + lastLocation.longitude +
                        " | Lat: " + lastLocation.latitude
            )
            writeLastLocation()
            startLocationUpdates()
        } else askPermission()
    }

    private var locationRequest: LocationRequest? = null

    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private val UPDATE_INTERVAL = 1000
    private val FASTEST_INTERVAL = 900

    // Start location Updates
    private fun startLocationUpdates() {
        Log.i("TAG", "startLocationUpdates()")
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL.toLong())
            .setFastestInterval(FASTEST_INTERVAL.toLong())

        if (checkPermission()) LocationServices.FusedLocationApi.requestLocationUpdates(
            googleApiClient,
            locationRequest,
            this
        )
    }

    override fun onLocationChanged(location: Location) {
        Log.d("TAG", "onLocationChanged [$location]")
        lastLocation = location
        writeActualLocation(location)
    }


    private fun writeLastLocation() {
        writeActualLocation(lastLocation)
    }

    // Check for permission to access Location
    private fun checkPermission(): Boolean {
        Log.d("TAG", "checkPermission()")
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    // Asks for permission
    private fun askPermission() {
        Log.d("TAG", "askPermission()")
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            200
        )
    }

    // Verify user's response of the permission requested
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        Log.d("TAG", "onRequestPermissionsResult()")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            200 -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission granted
                    getLastKnownLocation()
                } else {
                    // Permission denied
                    permissionsDenied()
                }
            }
        }
    }

    // App cannot work without the permissions
    private fun permissionsDenied() {
        Log.w("TAG", "permissionsDenied()")
    }


    override fun onStart() {
        super.onStart()

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient?.disconnect()
    }

    // GoogleApiClient.ConnectionCallbacks connected
    override fun onConnected(@Nullable bundle: Bundle?) {
        Log.i("TAG", "onConnected()")
        getLastKnownLocation()
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    override fun onConnectionSuspended(i: Int) {
        Log.w("TAG", "onConnectionSuspended()")
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.w("TAG", "onConnectionFailed()")
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

/*    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geoFenceList)
        }.build()
    }*/


    override fun onMapClick(latLng: LatLng) {
        Log.d("TAG", "onMapClick($latLng)")
        markerForGeofence(latLng)
    }

    private fun writeActualLocation(location: Location) {
        // ...
        textLat.setText("Lat: " + location.latitude)
        textLong.setText("Long: " + location.longitude)
        markerLocation(LatLng(location.latitude, location.longitude))
    }

    private var locationMarker: Marker? = null

    // Create a Location Marker
    private fun markerLocation(latLng: LatLng) {
        Log.i("TAG", "markerLocation($latLng)")
        val title = latLng.latitude.toString() + ", " + latLng.longitude
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(title)
        // Remove the anterior marker
        if (locationMarker != null) locationMarker!!.remove()
        locationMarker = mMap.addMarker(markerOptions)
        val zoom = 14f
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        mMap.animateCamera(cameraUpdate)
    }

    private var geoFenceMarker: Marker? = null

    // Create a marker for the geofence creation
    private fun markerForGeofence(latLng: LatLng) {
        Log.i("TAG", "markerForGeofence($latLng)")
        val title = latLng.latitude.toString() + ", " + latLng.longitude
        // Define marker options
        val markerOptions = MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            .title(title)
        if (mMap != null) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null) geoFenceMarker!!.remove()
            geoFenceMarker = mMap.addMarker(markerOptions)
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
        mMap.setOnMapClickListener { this }
        mMap.setOnMarkerClickListener { true }
        // Add a marker in Sydney and move the camera
        val ubicacionAbuelaOfelia = LatLng(6.201524,  -75.501660)
        mMap.addMarker(
            MarkerOptions()
            .position(ubicacionAbuelaOfelia)
            .title("Abuela en santa elena"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacionAbuelaOfelia))
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        Log.d("TAG", "onMarkerClickListener: " + p0?.position)
        return false;
    }

}