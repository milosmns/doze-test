package me.angrybyte.dozetest

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_main.*
import me.angrybyte.dozetest.DozeLogger.OnCycleFinishedListener

class MainActivity : AppCompatActivity(), OnCycleFinishedListener {

    val TAG = MainActivity::class.java.simpleName
    val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

    val handler = Handler()
    var logger = DozeLogger("Kotlin-LoggerThread")
    var googleDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logger.listener = this
        logger.start()

        checkPlayServices()
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
                Log.i(TAG, "This device is not supported.")
                finish()
            }
            return false
        }
        return true
    }

}
