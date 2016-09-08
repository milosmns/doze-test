package me.angrybyte.dozetest

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import me.angrybyte.dozetest.DozeLogger.OnCycleFinishedListener

class MainActivity : AppCompatActivity(), OnCycleFinishedListener, OnClickListener {

    val TAG = MainActivity::class.java.simpleName!!
    val TOPIC_EVENTS = "default_events"
    val KEY_FCM_ID = "FCM ID"
    val KEY_FCM_TOKEN = "FCM TOKEN"
    val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

    val handler = Handler()
    var logger = DozeLogger("Kotlin-LoggerThread", this, this)
    var googleDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkPlayServices()) {
            return
        }

        subscribe.setOnClickListener(this)
        unsubscribe.setOnClickListener(this)
        copyId.setOnClickListener(this)
        copyToken.setOnClickListener(this)

        logger.start()
    }

    override fun onCycleFinished(caller: DozeLogger, message: String) {
        handler.post {
            logView.text = message
        }
    }

    override fun onResume() {
        super.onResume()
        checkPlayServices()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.subscribe -> {
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_EVENTS)
                Toast.makeText(this, getString(R.string.subscribe_success, TOPIC_EVENTS), Toast.LENGTH_LONG).show()
            }
            R.id.unsubscribe -> {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_EVENTS)
                Toast.makeText(this, getString(R.string.unsubscribe_success, TOPIC_EVENTS), Toast.LENGTH_LONG).show()
            }
            R.id.copyId -> {
                val id = FirebaseInstanceId.getInstance().id
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(KEY_FCM_ID, id)
                clipboard.primaryClip = clip
                Toast.makeText(this, getString(R.string.copy_id_success, id), Toast.LENGTH_LONG).show()
            }
            R.id.copyToken -> {
                val token = FirebaseInstanceId.getInstance().token
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(KEY_FCM_TOKEN, token)
                clipboard.primaryClip = clip
                Toast.makeText(this, getString(R.string.copy_token_success, token), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.interrupt()
        closeGoogleDialog()
    }

    fun closeGoogleDialog() {
        googleDialog?.dismiss()
        googleDialog = null
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that
     * allows users to download the APK from the Google Play Store or enable it in the device's system settings.
     */
    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                googleDialog = apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                googleDialog?.show()
            } else {
                Log.e(TAG, "This device is not supported.")
                finish()
            }
            return false
        }
        return true
    }

}
