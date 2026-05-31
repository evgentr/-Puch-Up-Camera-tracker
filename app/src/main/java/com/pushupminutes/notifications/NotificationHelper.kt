package com.pushupminutes.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pushupminutes.R

class NotificationHelper(private val context: Context) {
    private val channelId = "time_over"

    fun ensureChannel() {
        val channel = NotificationChannel(
            channelId,
            "Time Over",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showTimeOver() {
        ensureChannel()
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_arm)
            .setContentTitle(context.getString(R.string.times_up_title))
            .setContentText(context.getString(R.string.times_up_body))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)
    }
}
