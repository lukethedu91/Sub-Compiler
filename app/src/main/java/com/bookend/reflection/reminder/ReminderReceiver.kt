package com.bookend.reflection.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.data.SettingsRepository
import com.bookend.reflection.widget.QuickEntryWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Posts the prompt, then books the same prompt for tomorrow. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val part = DayPart.fromNameOrNull(intent.getStringExtra(Notifications.EXTRA_PART)) ?: return
        val appContext = context.applicationContext
        val pendingResult = goAsync()

        Notifications.ensureChannel(appContext)
        Notifications.show(appContext, part)
        QuickEntryWidget.refresh(appContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = SettingsRepository(appContext).settings.first()
                if (settings.remindersEnabled) {
                    ReminderScheduler(appContext).schedule(part, settings.minuteOfDayFor(part))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
