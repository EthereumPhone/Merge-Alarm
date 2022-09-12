package org.ethereumphone.mergealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.*
import android.os.StrictMode.ThreadPolicy
import androidx.appcompat.app.AppCompatActivity
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.http.HttpService


class MainActivity : AppCompatActivity() {
    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent
    private val web3j: Web3j by lazy { Web3j.build(HttpService("https://cloudflare-eth.com/")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                checkMergeComing()
                mainHandler.postDelayed(this, 1000)
            }
        })


    }

    private fun checkMergeComing() {
        val block: EthBlock.Block = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().block
        println("Total difficulty: "+block.totalDifficulty)
        if (block.totalDifficulty.toLong()>10000) {
            mergeIsComing()
        }
    }

    private fun mergeIsComing() {
        alarmMgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(this, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        alarmMgr?.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1000*5,
            alarmIntent
        )
    }
}