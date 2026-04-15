package edu.temple.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private var timerService: TimerService.TimerBinder? = null
    private var isBound = false
    private lateinit var textView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private val defaultValue = 20

    private val handler = Handler(Looper.getMainLooper()) { msg ->
        textView.text = msg.what.toString()

        if (msg.what == 0) {
            startButton.text = "Start"
        }
        true
    }

    private val conn = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            timerService = p1 as TimerService.TimerBinder
            timerService?.setHandler(handler)
            isBound = true

            if (timerService?.isRunning() == true) {
                startButton.text = "Pause"
            } else {
                startButton.text = "Start"
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            timerService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)

        textView.text = defaultValue.toString()

        startButton.setOnClickListener {
            if(isBound) {
                if (timerService?.isRunning() == true) {
                    timerService?.pause()
                    startButton.text = "Start"
                } else {
                    timerService?.start(defaultValue)
                    startButton.text = "Pause"
                }
            }
        }

        stopButton.setOnClickListener {
            if (isBound) {
                timerService?.stop()
                textView.text = defaultValue.toString()
                startButton.text = "Start"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, TimerService::class.java),
            conn,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        if(isBound) {
            unbindService(conn)
        }
        isBound = false
    }
}