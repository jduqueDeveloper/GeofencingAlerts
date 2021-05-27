package com.example.geofencingalerts

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationServices.FusedLocationApi
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {

    private var googleApiClient: GoogleApiClient? = null
    private var map: GoogleMap? = null

    val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 101
    private lateinit var lastLocation: Location
    private var locationMarker: Marker? = null
    private var geoFenceMarker: Marker? = null
    private var locationRequest: LocationRequest? = null

    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private val UPDATE_INTERVAL = 5000
    private val FASTEST_INTERVAL = 900


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        createGoogleApi()
        initGMaps()
    }

    override fun onStart() {
        super.onStart()
        googleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        googleApiClient?.disconnect()
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

    private fun initGMaps(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    // Get last known location
    private fun getLastKnownLocation() {
        Log.d("w", "getLastKnownLocation()")
        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION
                )
            }
            return
        }
        mFusedLocationClient?.lastLocation
            ?.addOnCompleteListener(this,
                OnCompleteListener<Location?> { task ->
                    if (task.isSuccessful && task.result != null) {
                        lastLocation = task.result as Location
                        writeLastLocation()
                        startLocationUpdates()

                    } else {
                        Log.e("TAG", "no location detected")
                        Log.w("TAG", "getLastLocation:exception", task.exception)
                    }
                })
    }

    private fun writeLastLocation() {
        writeActualLocation(lastLocation)

/*

        val estudio = GeoFence(LatLng(6.142839, -75.447151), 3.0f, "estudio")
        val iglesia = GeoFence(LatLng(6.144133, -75.447755), 10.0f, "iglesia")
        val superMercado = GeoFence(LatLng(6.142618, -75.445971), 100.0f, "mercado")
        val morroputo = GeoFence(LatLng(6.143029, -75.447239), 20.0f, "morroputo")

        var geoFenceList : MutableList<GeoFence>
        geoFenceList.add(estudio)

*/

        //markerForGeofences()


/*        markerForGeofence2(morroputo)
        markerForGeofence2(estudio)
        markerForGeofence2(iglesia)
        markerForGeofence2(superMercado)*/

    }

    @SuppressLint("SetTextI18n")
    private fun writeActualLocation(location: Location) {

        markerLocation(LatLng(location.latitude, location.longitude))
    }
    // Start location Updates
    private fun startLocationUpdates() {
        Log.i("TAG", "startLocationUpdates()")
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL.toLong())
            .setFastestInterval(FASTEST_INTERVAL.toLong())

        if (checkPermission()) FusedLocationApi.requestLocationUpdates(
            googleApiClient,
            locationRequest,
            this
        )
    }
    // Create a Location Marker
    private fun  markerLocation(latLng: LatLng) {
        Log.i("TAG", "markerLocation($latLng)")
        val title = latLng.latitude.toString() + ", " + latLng.longitude
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(title)
        // Remove the anterior marker
        if (locationMarker != null) locationMarker!!.remove()
        locationMarker = map?.addMarker(markerOptions)

/*        val zoom = 17f
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        map?.animateCamera(cameraUpdate)*/
    }

    // Check for permission to access Location
    private fun checkPermission(): Boolean {
        Log.d("TAG", "checkPermission()")
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

/*    // Create a marker for the geofence creation
    private fun markerForGeofences(geoFences: GeoFences) {
        //Log.i("TAG", "markerForGeofence($latLng)")
        val title = geoFence.latLng?.latitude.toString() + ", " + geoFence.latLng?.longitude.toString()
        // Define marker options
        val markerOptions = MarkerOptions()
            .position(geoFence.latLng!!)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            .title(title)
        if (map != null) {
            // Remove last geoFenceMarker
            //if (geoFenceMarker != null) geoFenceMarker!!.remove()
            geoFenceMarker = map?.addMarker(markerOptions)
        }

*//*        createGeofence2(geoFence)
        startGeofenceRequest()
        drawGeofences(geoFence)*//*

    }*/


    override fun onMapReady(p0: GoogleMap?) {
        map=p0
        map?.setOnMapClickListener { this }
        map?.setOnMarkerClickListener { true }
    }

    override fun onConnected(p0: Bundle?) {
        Log.i("TAG", "onConnected()")
        getLastKnownLocation()
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.w("TAG", "onConnectionSuspended()")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.w("TAG", "onConnectionFailed()")
    }

    override fun onLocationChanged(p0: Location?) {
        Log.d("TAG", "onLocationChanged [$p0]")
        lastLocation = p0!!
        writeActualLocation(p0)
    }
}