package me.angrybyte.dozetest

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v7.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles Firebase Cloud Messages/Notifications in foregrounded apps, receives data payload, it can send upstream messages, etc.
 * Basically everything that a simple background FCM receiver couldn't do.
 */
class FirebaseCloudReceiver : FirebaseMessagingService() {

    val TAG = FirebaseCloudReceiver::class.java.simpleName!!
    val NOTIFICATION_INFO = 0xFC0001
    val NOTIFICATION_INFO_REQUEST = 0xFC0002

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Received message from ${remoteMessage.from}")

        // check if message contains a data payload
        if (remoteMessage.data.size > 0) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        // check if message contains a notification payload
        val body = remoteMessage.notification?.body
        Log.d(TAG, "Message notification body: $body");

        body?.let {
            sendNotification(it)
        }
    }

    /**
     * Creates and shows a simple notification containing the received FCM message.
     * @param messageBody FCM message body received
     */
    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_INFO_REQUEST, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_social_notifications_paused)
                .setContentTitle(getString(R.string.notification_push_title))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_INFO, notificationBuilder.build())
    }

}
