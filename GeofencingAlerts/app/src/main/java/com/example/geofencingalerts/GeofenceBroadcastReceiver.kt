package com.example.geofencingalerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.room.Room
import com.example.geofencingalerts.Entities.AlertEntity
import com.example.geofencingalerts.database.GeoFenceDataBase
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver(){
    private var listAlerts: MutableList<AlertEntity>? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e("TAG", errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = getGeofenceTrasitionDetails(
                geofenceTransition,
                triggeringGeofences
            )

            // Send notification and log the transition details.
            Toast.makeText(context,geofenceTransitionDetails
                , Toast.LENGTH_SHORT).show()
            createNotification(geofenceTransitionDetails,context)
            Log.i("TAG", geofenceTransitionDetails)
        } else {
            // Log the error.
         /*   Log.e("TAG", getString(R.string.geofence_transition_invalid_type,
                geofenceTransition))*/
        }
    }

    private fun getGeofenceTrasitionDetails(
        geoFenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        // get the ID of each geofence triggered
        val triggeringGeofencesList: ArrayList<String?> = ArrayList()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesList.add(geofence.requestId)
        }
        var status: String? = null
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) status =
            "Entering " else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) status =
            "Exiting "
        return status + TextUtils.join(", ", triggeringGeofencesList)
    }

    private fun createNotification(geofenceTransitionDetails: String, context: Context?) {
        Log.i("TAG", "sendNotification: $geofenceTransitionDetails")
        val db = Room.databaseBuilder(
            context!!,
            GeoFenceDataBase::class.java, "database-geoFence"
        ).allowMainThreadQueries().build()

        listAlerts = db.alertEntityDao().loadAllAlertEntitys().toMutableList()

        val alert = listAlerts!!.find { alertEntity ->
            alertEntity.geoFenceName!!.contains(
                geofenceTransitionDetails.subSequence(
                    9,
                    geofenceTransitionDetails.length
                )
            )
        }
        val phone = alert!!.CellPhone
        val message = alert.message
        var iSendSms = MapActivity()

        iSendSms.sendSms(phone!!,message!!,context)

    }


}