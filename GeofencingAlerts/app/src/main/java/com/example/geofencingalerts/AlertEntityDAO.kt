package com.example.geofencingalerts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlertEntityDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlertEntity(alertEntity: AlertEntity)

    @Query("SELECT * FROM AlertEntity")
    fun loadAllAlertEntitys(): Array<AlertEntity>

}