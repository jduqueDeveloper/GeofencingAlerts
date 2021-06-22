package com.example.geofencingalerts.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    val geoFenceName: String? = null,
    val CellPhone: String? = null,
    val message: String? = null
)