package me.angrybyte.dozetest

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.angrybyte.dozetest.DozeLogger.OnCycleFinishedListener

class MainActivity : AppCompatActivity(), OnCycleFinishedListener {

    val handler: Handler = Handler()
    var logger: DozeLogger = DozeLogger("Kotlin-LoggerThread")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logger.listener = this
        logger.start()
    }

    override fun onCycleFinished(caller: DozeLogger, message: String) {
        handler.post {
            logView.text = message
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.interrupt()
    }

}
