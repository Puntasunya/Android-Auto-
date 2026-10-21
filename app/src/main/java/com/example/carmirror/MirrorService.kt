package com.example.carmirror

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper

object MirrorHolder {
    @Volatile var projection: MediaProjection? = null
}

class MirrorService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("mirror", "Mirror", NotificationManager.IMPORTANCE_LOW))
        val n = Notification.Builder(this, "mirror")
            .setContentTitle("Car Mirror กำลังทำงาน")
            .setSmallIcon(android.R.drawable.ic_menu_slideshow)
            .build()
        startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

        val code = intent?.getIntExtra("code", 0) ?: 0
        @Suppress("DEPRECATION")
        val data = intent?.getParcelableExtra<Intent>("data")
        if (data != null) {
            val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val p = mpm.getMediaProjection(code, data)
            p.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() { MirrorHolder.projection = null }
            }, Handler(Looper.getMainLooper()))
            MirrorHolder.projection = p
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        MirrorHolder.projection?.stop()
        MirrorHolder.projection = null
        super.onDestroy()
    }
}