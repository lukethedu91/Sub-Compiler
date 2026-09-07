package com.bookend.reflection.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bookend.reflection.data.SettingsRepository
import com.bookend.reflection.widget.QuickEntryWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Alarms do not survive reboots, time changes or app updates: re-book them. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = SettingsRepository(appContext).settings.first()
                ReminderScheduler(appContext).apply(settings)
                QuickEntryWidget.refresh(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
