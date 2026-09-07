package com.bookend.reflection.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.data.ReminderSettings
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Schedules one alarm per day part. Alarms use [AlarmManager.setAndAllowWhileIdle],
 * which needs no special permission and is accurate enough for a nudge; each
 * firing schedules the next day's.
 */
class ReminderScheduler(private val context: Context) {

    fun apply(settings: ReminderSettings) {
        DayPart.entries.forEach { part ->
            if (settings.remindersEnabled) {
                schedule(part, settings.minuteOfDayFor(part))
            } else {
                cancel(part)
            }
        }
    }

    fun schedule(part: DayPart, minuteOfDay: Int, from: LocalDateTime = LocalDateTime.now()) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val triggerAt = nextOccurrence(minuteOfDay, from)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent(part),
        )
    }

    fun cancel(part: DayPart) {
        context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent(part))
    }

    private fun pendingIntent(part: DayPart): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMIND
            putExtra(Notifications.EXTRA_PART, part.name)
        }
        return PendingIntent.getBroadcast(
            context,
            part.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_REMIND = "com.bookend.reflection.action.REMIND"
    }
}
