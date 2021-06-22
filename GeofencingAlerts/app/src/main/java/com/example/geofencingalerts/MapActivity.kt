package com.example.geofencingalerts

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.geofencingalerts.Entities.GeoFence
import com.example.geofencingalerts.database.GeoFenceDataBase
import com.example.geofencingalerts.interfaces.ISendSms
import com.example.geofencingalerts.service.IApiSmsRestService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.FusedLocationApi
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MapActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener, ISendSms {

    private var googleApiClient: GoogleApiClient? = null
    private var map: GoogleMap? = null

    private var listGeofences: MutableList<GeoFence>? = null
    private lateinit var geofence: Geofence
    private val GEO_DURATION = (60 * 60 * 1000).toLong()
    lateinit var geofencingClient: GeofencingClient
    private var geoFenceList: List<Geofence>? = null
    private var geoFenceLimits: Circle? = null


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
        getDataBase()
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

    private fun getDataBase() {
        val db = Room.databaseBuilder(
            this,
            GeoFenceDataBase::class.java, "database-geoFence"
        ).allowMainThreadQueries().build()

        listGeofences = db.geoFenceEntityDao().loadAllGeoFences().toMutableList()

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

        listGeofences?.let { createGeofences(it) }
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

    }

    // Check for permission to access Location
    private fun checkPermission(): Boolean {
        Log.d("TAG", "checkPermission()")
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }


    private fun createGeofences(listGeoFences: MutableList<GeoFence>) {

        listGeoFences.forEach {
            //val title = geoFence.latLng?.latitude.toString() + ", " + geoFence.latLng?.longitude.toString()
            val title = it.geoFenceName
            val latLng = LatLng(it.lat!!,it.lng!!)

            // Define marker options
            val markerOptions = MarkerOptions()
                .position(latLng!!)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(title)
            if (map != null) {
                // Remove last geoFenceMarker
                //if (geoFenceMarker != null) geoFenceMarker!!.remove()
                geoFenceMarker = map?.addMarker(markerOptions)
            }

            createGeofence(it)
            drawGeofences(it)
        }

        startGeofenceRequest()

    }
    // Create a Geofence
    private fun createGeofence(geoFence: GeoFence) {
        Log.d("TAG", "createGeofence")
        geofence = Geofence.Builder()
            .setRequestId(geoFence.geoFenceName) // Geofence ID
            .setCircularRegion(
                geoFence.lat!!,
                geoFence.lng!!,
                geoFence.radius!!
            ) // defining fence region
            .setExpirationDuration(GEO_DURATION) // expiring date
            // Transition types that it should look for
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        geoFenceList = listOf(geofence)
        // geoFenceList?.add(geofence)
    }

    private fun drawGeofences(geoFence: GeoFence) {

        Log.d("TAG", "drawGeofence()")
        //if (geoFenceLimits != null) geoFenceLimits!!.remove()
        val circleOptions = CircleOptions()
            .center(geoFenceMarker!!.position)
            .strokeColor(Color.argb(50, 70, 70, 70))
            .fillColor(Color.argb(100, 150, 150, 150))
            .radius(geoFence.radius!!.toDouble())
        geoFenceLimits = map!!.addCircle(circleOptions)
    }

    private fun startGeofenceRequest() {
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
                //Toast.makeText(applicationContext, "se creo la geovalla", Toast.LENGTH_SHORT).show()
                // Geofences added
                // ...
                // drawGeofence()
            }
            addOnFailureListener {
                Toast.makeText(applicationContext, " No se creo la geovalla", Toast.LENGTH_SHORT).show()
                // Failed to add geofences
                // ...

            }
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geoFenceList)
        }.build()
    }

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

    override fun sendSms(cellPhone: String, message: String, context: Context) {

        CoroutineScope(Dispatchers.IO).launch {
            val response = getRetrofit().create(IApiSmsRestService::class.java).postSms(
                Body = message,
                From = "MG64ec7a79a9c635d44093679b51070d85",
                To = cellPhone
            )
            runOnUiThread {
                if (response.isSuccessful) {
                    Toast.makeText(context, "se ha enviado el mensaje", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No se pudo enviar el mensaje", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun getRetrofit(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        return Retrofit.Builder()
            .baseUrl("https://c14d2a2a6629.ngrok.io/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

}