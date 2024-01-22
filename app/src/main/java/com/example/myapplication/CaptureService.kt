package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class CaptureService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("CaptureService", "onStartCommand")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager =
                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "CaptureService",
                "CaptureService",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(applicationContext, "CaptureService")
                .setContentText("On Capture...")
                .setContentTitle("CaptureService")
                .build()

            startForeground(1, notification)

        }
        val data = intent?.getParcelableExtra<Intent>("data")!!
        val code = intent.getIntExtra("code", 0)
        CaptureManager.initialize(code, data, this)
        CaptureManager.startCapture()
        return START_STICKY
    }
}