package com.example.geofencingalerts


data class GeoFenceListDTO(
    val geoFenceList: MutableList<GeoFence>? = null,
    val alertList: MutableList<AlertList>? = null
)


data class AlertList(
    val cellphone: String? = null,
    val message: String? = null
)