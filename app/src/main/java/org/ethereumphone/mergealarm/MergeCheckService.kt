package org.ethereumphone.mergealarm

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.http.HttpService
import java.math.BigInteger

class MergeCheckService: Service() {
    val CHANNEL_ID = "MergeCheckService"
    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent
    private val web3j: Web3j by lazy { Web3j.build(HttpService("https://cloudflare-eth.com/")) }
    private val MERGETTD = BigInteger("58750000000000000000000")

    override fun onCreate() {
        super.onCreate()
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent!!.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, FLAG_IMMUTABLE
        )
        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Merge Alarm")
                .setContentText(input) //.setSmallIcon(R.drawable.)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(1, notification)

        val mainHandler = Handler(Looper.getMainLooper())

        // Check every two minutes
        mainHandler.post(object : Runnable {
            override fun run() {
                checkMergeComing()
                mainHandler.postDelayed(this, 1000 * 60 * 2)
            }
        })

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Merge Check Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(serviceChannel)
    }

    private fun checkMergeComing() {
        val count = 15
        val blockHeight = web3j.ethBlockNumber().send().blockNumber

        var arrayList = ArrayList<EthBlock.Block>()

        repeat(count) {
            arrayList.add(web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockHeight.minus(BigInteger.valueOf(it.toLong()))), false).send().block)
        }

        var timeToMerge = BigInteger.ZERO
        for (i in 0 until count) {
            timeToMerge = timeToMerge.plus(MERGETTD.subtract(arrayList.get(i).totalDifficulty).divide(arrayList.get(i).difficulty).multiply(BigInteger.valueOf(13.toLong())))
        }

        timeToMerge = timeToMerge.divide(BigInteger.valueOf(count.toLong()))
        // 3600

        println("Merge date: $timeToMerge")
        if (timeToMerge.toLong()<=3600) {
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
            SystemClock.elapsedRealtime() + 1000*2,
            alarmIntent
        )
    }
}