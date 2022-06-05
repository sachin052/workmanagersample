package com.teamb.workmanagersample.common

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class DownloadApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val notificationChannel = NotificationChannel(
            "download_channel",
            "file_download",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}