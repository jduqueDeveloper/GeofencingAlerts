package com.example.geofencingalerts

import androidx.room.*

@Dao
interface GeoFenceEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGeoFenceEntity(geoFenceEntity: GeoFenceEntity)

}