import android.R
import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.geofencingalerts.MainActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent


class GeofenceTrasitionService : IntentService(TAG) {
    override fun onHandleIntent(intent: Intent?) {
        // Retrieve the Geofencing intent
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // Handling errors
        if (geofencingEvent.hasError()) {
            val errorMsg = getErrorString(geofencingEvent.errorCode)
            Log.e(TAG, errorMsg)
            return
        }

        // Retrieve GeofenceTrasition
        val geoFenceTransition = geofencingEvent.geofenceTransition
        // Check if the transition type
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            // Get the geofence that were triggered
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            // Create a detail message with Geofences received
            val geofenceTransitionDetails =
                getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences)
            // Send notification details as a String
            sendNotification(geofenceTransitionDetails)
        }
    }

    // Create a detail message with Geofences received
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

    // Send a notification
    private fun sendNotification(msg: String) {
        Log.i(TAG, "sendNotification: $msg")

/*        // Intent to start the main Activity
        val notificationIntent: Intent = MainActivity.makeNotificationIntent(
            applicationContext, msg
        )
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        val notificationPendingIntent: PendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // Creating and sending Notification
        val notificatioMng =
            getSystemService<Any>(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificatioMng.notify(
            GEOFENCE_NOTIFICATION_ID,
            createNotification(msg, notificationPendingIntent)
        )*/
    }

    // Create a notification
    private fun createNotification(
        msg: String,
        notificationPendingIntent: PendingIntent
    ): Notification {
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this)
        notificationBuilder
            .setSmallIcon(R.drawable.ic_dialog_alert)
            .setColor(Color.RED)
            .setContentTitle(msg)
            .setContentText("Geofence Notification!")
            .setContentIntent(notificationPendingIntent)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND)
            .setAutoCancel(true)
        return notificationBuilder.build()
    }

    companion object {
        private val TAG = GeofenceTrasitionService::class.java.simpleName
        const val GEOFENCE_NOTIFICATION_ID = 0

        // Handle errors
        private fun getErrorString(errorCode: Int): String {
            return when (errorCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "GeoFence not available"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Too many GeoFences"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Too many pending intents"
                else -> "Unknown error."
            }
        }
    }
}