package com.example.geofencingalerts.dao

import androidx.room.*
import com.example.geofencingalerts.Entities.GeoFenceEntity
import com.example.geofencingalerts.Entities.GeoFence

@Dao
interface GeoFenceEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGeoFenceEntity(geoFenceEntity: GeoFenceEntity)

    @Query("SELECT * FROM GeoFenceEntity")
    fun loadAllGeoFences(): Array<GeoFence>

}