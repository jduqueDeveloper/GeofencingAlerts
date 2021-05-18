package com.example.geofencingalerts

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.FusedLocationApi
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMapClickListener,
    GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    private var textLat: TextView? = null
    private var textLong: TextView? = null
    private var map: GoogleMap? = null

    private var googleApiClient: GoogleApiClient? = null

    private lateinit var lastLocation: Location
    private var locationRequest: LocationRequest? = null

    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private val UPDATE_INTERVAL = 5000
    private val FASTEST_INTERVAL = 900

    private var locationMarker: Marker? = null
    private var geoFenceMarker: Marker? = null

    private val GEO_DURATION = (60 * 60 * 1000).toLong()
    private val GEOFENCE_REQ_ID = "My Geofence"
    private val GEOFENCE_RADIUS = 20.0f // in meters

    // Draw Geofence circle on GoogleMap
    private var geoFenceLimits: Circle? = null

    val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 101

    lateinit var geofencingClient: GeofencingClient

    private lateinit var geofence: Geofence
    private var geoFenceList: List<Geofence>? = null
    // geoFences
    private val morroputo = GeoFence(LatLng(6.143029, -75.447239), 20.0f, "morroputo")
    private val estudio = GeoFence(LatLng(6.142839, -75.447151), 3.0f, "estudio")
    private val iglesia = GeoFence(LatLng(6.144133, -75.447755), 10.0f, "iglesia")
    private val superMercado = GeoFence(LatLng(6.142618, -75.445971), 100.0f, "mercado")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textLat = findViewById(R.id.txt_latitud)
        textLong = findViewById(R.id.txt_longitud)


        // initialize GoogleMaps

        createGoogleApi()
        initGMaps()
        geofencingClient = LocationServices.getGeofencingClient(this)

    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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

    override fun onMapReady(p0: GoogleMap?) {
        map = p0
        map?.setOnMapClickListener { this }
        map?.setOnMarkerClickListener { true }
    }

    override fun onMapClick(p0: LatLng?) {
        //markerForGeofence(p0!!)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    // Start Geofence creation process
    private fun startGeofence() {
        Log.i("TAG", "startGeofence()")
        createGeofence(LatLng(6.143029, -75.447239),GEOFENCE_RADIUS)
        val geoFencingRequest = getGeofencingRequest()
/*        if (geoFenceMarker != null) {
            createGeofence(geoFenceMarker!!.position, GEOFENCE_RADIUS)
            //getGeofencingRequest()*/
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            geofencingClient.addGeofences(geoFencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    // Geofences added
                    // ...
                    drawGeofence()
                }
                addOnFailureListener {
                    // Failed to add geofences
                    // ...

                }
            }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }

    private fun getGeofencingRequest2(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geoFenceList)
        }.build()
    }

    // Create a Geofence
    private fun createGeofence(latLng: LatLng, radius: Float) {
        Log.d("TAG", "createGeofence")
        geofence = Geofence.Builder()
            .setRequestId("1234") // Geofence ID
            .setCircularRegion(latLng.latitude, latLng.longitude, radius ) // defining fence region
            .setExpirationDuration(GEO_DURATION) // expiring date
            // Transition types that it should look for
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        //geoFenceList?.add(geofence)
    }
    // Create a Geofence
    private fun createGeofence2(geoFence: GeoFence) {
        Log.d("TAG", "createGeofence")
        geofence = Geofence.Builder()
            .setRequestId(geoFence._id) // Geofence ID
            .setCircularRegion(
                geoFence.latLng!!.latitude,
                geoFence.latLng.longitude,
                geoFence.radius!!
            ) // defining fence region
            .setExpirationDuration(GEO_DURATION) // expiring date
            // Transition types that it should look for
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        geoFenceList = listOf(geofence)
       // geoFenceList?.add(geofence)

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


        markerForGeofence2(morroputo)
        markerForGeofence2(estudio)
        markerForGeofence2(iglesia)
        markerForGeofence2(superMercado)

    }


    @SuppressLint("SetTextI18n")
    private fun writeActualLocation(location: Location) {
        // ...
        textLat?.text = "Lat: " + location.latitude
        textLong?.text = "Long: " + location.longitude
        markerLocation(LatLng(location.latitude, location.longitude))
    }

    override fun onLocationChanged(p0: Location?) {
        Log.d("TAG", "onLocationChanged [$p0]")
        //Toast.makeText(this, "location changed", Toast.LENGTH_SHORT).show()
        lastLocation = p0!!
        writeActualLocation(p0)

    }


    // Check for permission to access Location
    private fun checkPermission(): Boolean {
        Log.d("TAG", "checkPermission()")
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
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


    // Create a Location Marker
    private fun markerLocation(latLng: LatLng) {
        Log.i("TAG", "markerLocation($latLng)")
        val title = latLng.latitude.toString() + ", " + latLng.longitude
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(title)
        // Remove the anterior marker
        if (locationMarker != null) locationMarker!!.remove()
        locationMarker = map?.addMarker(markerOptions)

        val zoom = 20f
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        map?.animateCamera(cameraUpdate)
    }

    // Create a marker for the geofence creation
    private fun markerForGeofence2(geoFence: GeoFence) {
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

        createGeofence2(geoFence)
        startGeofenceRequest()
        drawGeofences(geoFence)

    }


    private fun startGeofenceRequest() {
        val geoFencingRequest = getGeofencingRequest2()
/*        if (geoFenceMarker != null) {
            createGeofence(geoFenceMarker!!.position, GEOFENCE_RADIUS)
            //getGeofencingRequest()*/
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        geofencingClient.addGeofences(geoFencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences added
                // ...
                // drawGeofence()
            }
            addOnFailureListener {
                // Failed to add geofences
                // ...

            }
        }
    }

    private fun drawGeofences(geoFence:GeoFence) {

        Log.d("TAG", "drawGeofence()")
        //if (geoFenceLimits != null) geoFenceLimits!!.remove()
        val circleOptions = CircleOptions()
            .center(geoFenceMarker!!.position)
            .strokeColor(Color.argb(50, 70, 70, 70))
            .fillColor(Color.argb(100, 150, 150, 150))
            .radius(geoFence.radius!!.toDouble())
        geoFenceLimits = map!!.addCircle(circleOptions)
    }

    private fun drawGeofence() {
        Log.d("TAG", "drawGeofence()")
        if (geoFenceLimits != null) geoFenceLimits!!.remove()
        val circleOptions = CircleOptions()
            .center(geoFenceMarker!!.position)
            .strokeColor(Color.argb(50, 70, 70, 70))
            .fillColor(Color.argb(100, 150, 150, 150))
            .radius(GEOFENCE_RADIUS.toDouble())
        geoFenceLimits = map!!.addCircle(circleOptions)
    }

    override fun onResult(p0: Status) {
        Log.i("TAG", "onResult: " + p0);
        if ( p0.isSuccess) {
            drawGeofence()
        } else {
            // inform about fail
            drawGeofence()
        }
    }

}