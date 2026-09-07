package com.bookend.reflection.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bookend.reflection.R
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.ui.MainActivity

object Notifications {

    const val CHANNEL_ID = "daily_reflection"
    const val EXTRA_PART = "com.bookend.reflection.extra.PART"

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily reflection",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Morning and evening prompts to write your five answers."
        }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    fun show(context: Context, part: DayPart) {
        if (!canPostNotifications(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PART, part.name)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            part.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val (title, text) = when (part) {
            DayPart.MORNING -> "Good morning" to "Five questions to set up your day."
            DayPart.EVENING -> "How was today?" to "Five questions to close out your day."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(part.ordinal, notification)
        } catch (_: SecurityException) {
            // Permission was revoked between the check and the post; nothing to do.
        }
    }

    fun canPostNotifications(context: Context): Boolean =
        android.os.Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
