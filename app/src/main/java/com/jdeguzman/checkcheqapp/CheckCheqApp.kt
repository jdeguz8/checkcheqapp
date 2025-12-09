package com.jdeguzman.checkcheqapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for CheckCheq.
 *
 * Annotated with @HiltAndroidApp to bootstrap Hilt dependency injection.
 */
@HiltAndroidApp
class CheckCheqApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel for “new nearby price” notifications
            val channel = NotificationChannel(
                NEARBY_POSTS_CHANNEL_ID,
                "Nearby price alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when new price posts are near your location."
            }

            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NEARBY_POSTS_CHANNEL_ID = "nearby_price_posts"
    }
}
