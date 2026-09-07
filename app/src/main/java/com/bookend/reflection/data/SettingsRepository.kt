package com.bookend.reflection.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

data class ReminderSettings(
    val remindersEnabled: Boolean = true,
    val morningMinuteOfDay: Int = 7 * 60,
    val eveningMinuteOfDay: Int = 21 * 60,
) {
    val morningTime: LocalTime get() = LocalTime.ofSecondOfDay(morningMinuteOfDay * 60L)
    val eveningTime: LocalTime get() = LocalTime.ofSecondOfDay(eveningMinuteOfDay * 60L)

    fun minuteOfDayFor(part: DayPart): Int = when (part) {
        DayPart.MORNING -> morningMinuteOfDay
        DayPart.EVENING -> eveningMinuteOfDay
    }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    val settings: Flow<ReminderSettings> = context.dataStore.data.map { prefs ->
        ReminderSettings(
            remindersEnabled = prefs[KEY_ENABLED] ?: true,
            morningMinuteOfDay = prefs[KEY_MORNING] ?: (7 * 60),
            eveningMinuteOfDay = prefs[KEY_EVENING] ?: (21 * 60),
        )
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ENABLED] = enabled }
    }

    suspend fun setReminderTime(part: DayPart, minuteOfDay: Int) {
        val key = if (part == DayPart.MORNING) KEY_MORNING else KEY_EVENING
        context.dataStore.edit { it[key] = minuteOfDay.coerceIn(0, 24 * 60 - 1) }
    }

    private companion object {
        val KEY_ENABLED = booleanPreferencesKey("reminders_enabled")
        val KEY_MORNING = intPreferencesKey("morning_minute_of_day")
        val KEY_EVENING = intPreferencesKey("evening_minute_of_day")
    }
}
