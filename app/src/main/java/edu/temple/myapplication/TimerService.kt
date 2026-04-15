package edu.temple.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.content.edit

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false

    private var timerHandler : Handler? = null

    lateinit var t: TimerThread

    private var paused = false

    private val preferences by lazy {
        getSharedPreferences("timer_pref", Context.MODE_PRIVATE)
    }
    private var currentValue = 0

    inner class TimerBinder : Binder() {

        fun start(startValue: Int){
            if (!isRunning) {
                if(::t.isInitialized) {
                    t.interrupt()
                }
                isRunning = true

                val savedValue = preferences.getInt("paused_value", -1)
                currentValue = if (savedValue != -1) savedValue else startValue

                paused = false
                preferences.edit { remove("paused_value") }

                t = TimerThread(currentValue)
                t.start()
            }
        }

        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        fun stop() {
            if (::t.isInitialized) {
                t.interrupt()
            }
            isRunning = false
            paused = false
            currentValue = 0
            preferences.edit { remove("paused_value") }
        }

        fun pause () {
            if (::t.isInitialized && isRunning) {
                paused = true
                preferences.edit { putInt("paused_value", currentValue) }
                t.interrupt()
                isRunning = false
            }
        }

        fun getSavedValue(): Int {
            return preferences.getInt("paused_value", -1)
        }

        fun isRunning(): Boolean {
            return isRunning
        }

        fun isPaused(): Boolean {
            return paused
        }

    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TimerService status", "Created")
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    inner class TimerThread(private val startValue: Int) : Thread() {

        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 0)  {
                    Log.d("Countdown", i.toString())
                    currentValue = i
                    timerHandler?.sendEmptyMessage(i)
                    while (paused);
                    sleep(1000)
                }
                isRunning = false
                paused = false
                preferences.edit { remove("paused_value") }
            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunning = false
            }
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        if(!paused) {
            preferences.edit { remove("paused_value") }
        }
        if(::t.isInitialized && isRunning) {
            t.interrupt()
            isRunning = false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TimerService status", "Destroyed")
    }
}