package com.example.geofencingalerts

import com.google.android.libraries.maps.model.LatLng

data class GeoFence(
    val latLng: LatLng? = null,
    val radius: Float? = null,
    val geoFenceName: String? = null
)
