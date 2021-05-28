package com.example.geofencingalerts

import androidx.room.TypeConverter
import com.google.android.libraries.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.moshi.Moshi

class Converters {

    @TypeConverter
    fun toGeoFence(data: String): GeoFence{
        val geoFence = object : TypeToken<GeoFence>() {}.type
        return Gson().fromJson(data, geoFence)
    }

    @TypeConverter
    fun fromGeoFence(geoFence: GeoFence): String{
        val gson = Gson()
        return gson.toJson(geoFence)
    }

    @TypeConverter
    fun latLngToString(input: LatLng): String? =
        Moshi.Builder().build().adapter(LatLng::class.java).toJson(input)

}