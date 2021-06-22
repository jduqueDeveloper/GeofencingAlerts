package com.example.geofencingalerts.Entities

import androidx.room.*

@Entity
data class GeoFenceEntity(
    @PrimaryKey(autoGenerate = true)
    val uid: Int= 0,
    val lat: Double? = null,
    val lng: Double? = null,
    val radius: Float? = null,
    val geoFenceName: String? = null
)