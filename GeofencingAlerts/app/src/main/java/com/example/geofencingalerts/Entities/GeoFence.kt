package com.example.geofencingalerts.Entities

import com.google.android.libraries.maps.model.LatLng

data class GeoFence(
    val lat: Double? = null,
    val lng: Double? = null,
    val radius: Float? = null,
    val geoFenceName: String? = null
)
