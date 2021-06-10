package com.example.geofencingalerts

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = arrayOf(GeoFenceEntity::class, AlertEntity::class),version = 1)
abstract class GeoFenceDataBase :RoomDatabase() {
    abstract fun geoFenceEntityDao(): GeoFenceEntityDao
    abstract fun alertEntityDao(): AlertEntityDAO

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: GeoFenceDataBase? = null

        fun getDatabase(context: Context): GeoFenceDataBase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,

                    GeoFenceDataBase::class.java,
                    "geo_fences_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}