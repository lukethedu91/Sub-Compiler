package com.bookend.reflection

import android.app.Application
import com.bookend.reflection.data.BookendDatabase
import com.bookend.reflection.data.EntryRepository
import com.bookend.reflection.data.SettingsRepository
import com.bookend.reflection.reminder.Notifications
import com.bookend.reflection.reminder.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BookendApp : Application() {

    lateinit var entries: EntryRepository
        private set
    lateinit var settings: SettingsRepository
        private set
    lateinit var scheduler: ReminderScheduler
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        entries = EntryRepository(BookendDatabase.get(this).entryDao())
        settings = SettingsRepository(this)
        scheduler = ReminderScheduler(this)

        Notifications.ensureChannel(this)
        appScope.launch {
            scheduler.apply(settings.settings.first())
        }
    }
}
