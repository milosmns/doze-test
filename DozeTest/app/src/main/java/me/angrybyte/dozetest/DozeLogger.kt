package me.angrybyte.dozetest

import android.util.Log
import java.util.*

/**
 * Logs something every [DozeLogger.INTERVAL] milliseconds.
 */
class DozeLogger(name: String) : Thread(name) {

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

    val TAG: String = DozeLogger::class.java.simpleName.toString()
    val INTERVAL: Long = 1000

    var listener: OnCycleFinishedListener? = null

    override fun run() {
        var time: Date
        var delay: Long
        var currentTime: Long
        var message: String

        // loop here until thread is stopped
        while (!isInterrupted) {
            time = Date()

            message = "Still alive at: ${time.toString()}"
            Log.d(TAG, message)
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

}
