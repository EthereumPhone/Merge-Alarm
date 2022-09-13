package org.ethereumphone.mergealarm

import android.content.Intent
import android.os.*
import android.os.StrictMode.ThreadPolicy
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val context = this

        findViewById<Button>(R.id.startButton).setOnClickListener {
            val serviceIntent = Intent(context, MergeCheckService::class.java)
            serviceIntent.putExtra("inputExtra", "run")
            context.startForegroundService(serviceIntent)
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            val serviceIntent = Intent(context, MergeCheckService::class.java)
            stopService(serviceIntent)
        }
    }
}