package me.angrybyte.dozetest

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Logs something every [DozeLogger.INTERVAL] milliseconds.
 */
class DozeLogger(name: String, val context: Context, var listener: OnCycleFinishedListener?) : Thread(name) {

    val TAG = DozeLogger::class.java.simpleName!!
    val INTERVAL = 1000L

    /**
     * The basic listener interface for when the thread's loop cycle finishes.
     */
    interface OnCycleFinishedListener {
        /**
         * Notifies the listener that thread's loop cycle completed.
         *
         * @param caller Thread instance
         * @param message Last log message
         */
        fun onCycleFinished(caller: DozeLogger, message: String)
    }

    override fun run() {
        var time: Date
        var delay: Long
        var currentTime: Long
        var message: String
        var pingOk: Boolean

        // loop here until thread is stopped
        while (!isInterrupted) {
            time = Date()

            message = "Still alive at: ${time.toString()}"
            Log.d(TAG, message)

            pingOk = pingGoogle()
            if (!pingOk) {
                message = "Ooops, lost Google..."
                Log.d(TAG, message)
            }

            currentTime = System.currentTimeMillis()

            try {
                sleep(INTERVAL)
            } catch (ex: InterruptedException) {
                Log.d(TAG, TAG + " interrupted at: ${time.toString()}!")
                break
            }

            delay = System.currentTimeMillis() - currentTime - INTERVAL
            if (delay > INTERVAL) {
                // slightly bigger delay, something's working..
                message = "I'm late by $delay ms at: ${time.toString()}"
                Log.d(TAG, message)
            }

            listener?.onCycleFinished(this, message)
        }

        listener = null
    }

    /**
     * Checks the system connection to see if there are any connected and available networks.
     *
     * @return `True` if there are connected or connecting networks, `false` otherwise
     */
    private fun hasInternet(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return manager.activeNetworkInfo != null && manager.activeNetworkInfo.isConnectedOrConnecting
    }


    /**
     * Pings [Google](https://www.google.com) website.
     *
     * @return `True` if Google was reached, `false` if not
     */
    private fun pingGoogle(): Boolean {
        if (!hasInternet()) {
            return false
        }

        var conn: HttpURLConnection? = null
        try {
            conn = URL("https://www.google.com/").openConnection() as HttpURLConnection
            conn.readTimeout = INTERVAL.toInt() // ms
            conn.connectTimeout = INTERVAL.toInt() // ms
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect()
            return conn.responseCode == HttpURLConnection.HTTP_OK
        } catch (ignored: Exception) {
            return false
        } finally {
            conn?.disconnect()
        }

    }

}
