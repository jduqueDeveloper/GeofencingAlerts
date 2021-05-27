package com.example.geofencingalerts

import com.google.android.libraries.maps.model.LatLng

data class GeoFenceListDTO(
    val geoFenceList: MutableList<GeoFence>? = null,
    val alertList: MutableList<AlertList>? = null
)

data class GeoFence(
    val latLng: LatLng? = null,
    val radius: Float? = null,
    val geoFenceName: String? = null
)

data class AlertList(
    val cellphone: String? = null,
    val message: String? = null
)