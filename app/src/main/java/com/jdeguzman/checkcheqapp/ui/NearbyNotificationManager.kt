package com.jdeguzman.checkcheqapp.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jdeguzman.checkcheqapp.CheckCheqApp
import com.jdeguzman.checkcheqapp.MainActivity
import com.jdeguzman.checkcheqapp.R
import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlin.math.absoluteValue

/**
 * Helper for showing local notifications when a new nearby post is detected.
 */
object NearbyNotificationManager {

    /**
     * Show a simple notification for a new nearby [post].
     *
     * For now this just opens [MainActivity] when tapped.
     */
    fun showNewNearbyPost(context: Context, post: PricePost) {
        // When user taps the notification, open the app (MainActivity)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // TODO later: pass post.id as extra so we can deep-link to details
        }

        val flags =
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else
                        0

        val pendingIntent = PendingIntent.getActivity(
            context,
            post.id.hashCode().absoluteValue, // requestCode per post
            intent,
            flags
        )

        val contentText =
            "${post.storeName} — ${post.itemName} • $${"%.2f".format(post.price)}"

        val notification = NotificationCompat.Builder(
            context,
            CheckCheqApp.NEARBY_POSTS_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.checkcheq_logo) // or your launcher icon
            .setContentTitle("New price near you")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context)
            .notify(post.id.toInt(), notification)
    }
}
