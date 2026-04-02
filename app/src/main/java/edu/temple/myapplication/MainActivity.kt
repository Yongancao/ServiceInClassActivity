package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import java.util.logging.Handler

class MainActivity : AppCompatActivity() {

    lateinit var timerBinder : TimerService.TimerBinder
    var isConnected = false

    val timerHandler = android.os.Handler(Looper.getMainLooper()) {
        findViewById<TextView>(R.id.textView).text = it.what.toString()
        true
    }

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerBinder = service as TimerService.TimerBinder
            timerBinder.setHandler(timerHandler)
            isConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val timerTextView = findViewById<TextView>(R.id.textView)
        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        bindService(Intent(this, TimerService::class.java), serviceConnection, BIND_AUTO_CREATE)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (isConnected) {
                if (!timerBinder.isRunning && !timerBinder.paused) {
                    // Start timer first time
                    timerBinder.start(10)
                    startButton.text = "Pause"
                } else if (timerBinder.isRunning) {
                    // Pause timer
                    timerBinder.pause()
                    startButton.text = "Resume"
                } else if (timerBinder.paused) {
                    // Resume timer
                    timerBinder.pause()
                    startButton.text = "Pause"
                }
            }

        }
        
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if (isConnected) {
                timerBinder.stop()
            }

        }
    }
}