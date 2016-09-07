package me.angrybyte.dozetest

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Handles creation, rotation, and updating of registration tokens. This is required for sending to specific devices or for creating device groups.
 */
class FirebaseIdService : FirebaseInstanceIdService() {

    val TAG = FirebaseIdService::class.java.simpleName;

    override fun onTokenRefresh() {
        super.onTokenRefresh()
        val refreshedToken = FirebaseInstanceId.getInstance().token

        // we want to send messages to this application instance and manage this apps subscriptions on the server side
        // so now send the Instance ID token to the app server
        refreshedToken?.let {
            sendRegistrationToServer(refreshedToken)
        }
    }

    private fun sendRegistrationToServer(refreshedToken: String) {
        Log.d(TAG, "Refreshed token: $refreshedToken - preparing to send to server")
    }

}
