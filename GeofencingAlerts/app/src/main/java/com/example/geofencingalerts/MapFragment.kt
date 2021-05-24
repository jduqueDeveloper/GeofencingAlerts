package com.example.geofencingalerts


import android.Manifest
import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MapFragment : Fragment(), OnMapReadyCallback,
GoogleMap.OnMapClickListener,
GoogleMap.OnMarkerClickListener, LocationListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var map: GoogleMap? = null
    private var googleApiClient: GoogleApiClient? = null
    private lateinit var lastLocation: Location
    private var locationMarker: Marker? = null
    val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 101
    private var locationRequest: LocationRequest? = null
    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private val UPDATE_INTERVAL = 5000
    private val FASTEST_INTERVAL = 900


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        createGoogleApi()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
    private fun createGoogleApi() {
        Log.d("w", "createGoogleApi()")
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(requireContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun onMapClick(p0: LatLng?) {
        Log.d("TAG", "onMapClick [$p0]")
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }



    // Check for permission to access Location
    private fun checkPermission(): Boolean {
        Log.d("TAG", "checkPermission()")
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
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
    // Start location Updates
    private fun startLocationUpdates() {
        Log.i("TAG", "startLocationUpdates()")
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL.toLong())
            .setFastestInterval(FASTEST_INTERVAL.toLong())

/*        if (checkPermission()) FusedLocationApi.requestLocationUpdates(
            googleApiClient,
            locationRequest,
            requireActivity()
        )*/
    }

    // Get last known location
    private fun getLastKnownLocation() {
        Log.d("w", "getLastKnownLocation()")
        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
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
            ?.addOnCompleteListener(requireActivity(),
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

        markerForGeofence2(morroputo)
        markerForGeofence2(estudio)
        markerForGeofence2(iglesia)
        markerForGeofence2(superMercado)*/

    }


    @SuppressLint("SetTextI18n")
    private fun writeActualLocation(location: Location) {
        // ...
/*        textLat?.text = "Lat: " + location.latitude
        textLong?.text = "Long: " + location.longitude*/
        markerLocation(LatLng(location.latitude, location.longitude))
    }
    // App cannot work without the permissions
    private fun permissionsDenied() {
        Log.w("TAG", "permissionsDenied()")
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
    override fun onMapReady(p0: GoogleMap?) {
        map = p0
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

    override fun onLocationChanged(location: Location) {
        Log.d("TAG", "onLocationChanged [$location]")
        //Toast.makeText(this, "location changed", Toast.LENGTH_SHORT).show()
        lastLocation = location!!
        markerLocation(LatLng(location.latitude, location.longitude))
    }

    override fun onResult(p0: Status) {
        Log.i("TAG", "onResult: " + p0);
        if ( p0.isSuccess) {
            //drawGeofence()
        } else {
            // inform about fail
           // drawGeofence()
        }
    }
}