package com.bookend.reflection.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bookend.reflection.BookendApp
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.data.EntryRepository
import com.bookend.reflection.data.Question
import com.bookend.reflection.data.ReflectionEntry
import com.bookend.reflection.data.ReminderSettings
import com.bookend.reflection.data.SettingsRepository
import com.bookend.reflection.data.currentStreak
import com.bookend.reflection.reminder.ReminderScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val AUTOSAVE_DELAY_MS = 700L
private const val SAVED_BADGE_MS = 1_600L

enum class SaveState { IDLE, EDITING, SAVED }

/** One day in the week strip: how many of its two halves were written in. */
data class DayMark(
    val date: LocalDate,
    val partsWritten: Int,
)

data class HomeUiState(
    val today: LocalDate = LocalDate.now(),
    val morning: ReflectionEntry? = null,
    val evening: ReflectionEntry? = null,
    val streak: Int = 0,
    val totalEntries: Int = 0,
    val week: List<DayMark> = emptyList(),
) {
    fun entryFor(part: DayPart): ReflectionEntry? =
        if (part == DayPart.MORNING) morning else evening
}

/** One view model for the whole app: the surface is small and the state is shared. */
class AppViewModel(
    private val entriesRepo: EntryRepository,
    private val settingsRepo: SettingsRepository,
    private val scheduler: ReminderScheduler,
) : ViewModel() {

    val entries: StateFlow<List<ReflectionEntry>> = entriesRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val settings: StateFlow<ReminderSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderSettings())

    private val today = MutableStateFlow(LocalDate.now())

    val home: StateFlow<HomeUiState> = combine(entries, today) { all, day ->
        val epochDay = day.toEpochDay()
        val byDay = all.groupBy { it.dateEpochDay }
        HomeUiState(
            today = day,
            morning = all.firstOrNull { it.dateEpochDay == epochDay && it.part == DayPart.MORNING },
            evening = all.firstOrNull { it.dateEpochDay == epochDay && it.part == DayPart.EVENING },
            streak = currentStreak(all.map { it.date }.toSet(), day),
            totalEntries = all.size,
            week = (6 downTo 0).map { back ->
                val date = day.minusDays(back.toLong())
                DayMark(
                    date = date,
                    partsWritten = byDay[date.toEpochDay()].orEmpty().count { !it.isBlank },
                )
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val _draft = MutableStateFlow<ReflectionEntry?>(null)

    /** The entry currently open for editing, kept in memory and saved as you type. */
    val draft: StateFlow<ReflectionEntry?> = _draft.asStateFlow()

    private val _saveState = MutableStateFlow(SaveState.IDLE)

    /** Drives the quiet "saving / saved" line under the questions. */
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private var autosaveJob: Job? = null

    /** Call before showing today's screens so a session left open overnight rolls over. */
    fun refreshToday() {
        today.value = LocalDate.now()
    }

    fun openEntry(date: LocalDate, part: DayPart) {
        autosaveJob?.cancel()
        _saveState.value = SaveState.IDLE
        _draft.value = ReflectionEntry.empty(date, part)
        viewModelScope.launch {
            val existing = entriesRepo.find(date, part) ?: return@launch
            val current = _draft.value ?: return@launch
            // Only adopt the stored answers if nothing was typed while it loaded.
            if (current.dateEpochDay == existing.dateEpochDay &&
                current.part == existing.part &&
                current.isBlank
            ) {
                _draft.value = existing
            }
        }
    }

    fun updateAnswer(question: Question, text: String) {
        val current = _draft.value ?: return
        _draft.value = current.withAnswer(question, text)
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            _saveState.value = SaveState.EDITING
            delay(AUTOSAVE_DELAY_MS)
            _draft.value?.let { entriesRepo.save(it) }
            _saveState.value = SaveState.SAVED
            delay(SAVED_BADGE_MS)
            _saveState.value = SaveState.IDLE
        }
    }

    /** Flushes any pending edit; used when leaving the entry screen. */
    fun closeEntry() {
        autosaveJob?.cancel()
        _saveState.value = SaveState.IDLE
        val pending = _draft.value
        _draft.value = null
        if (pending != null) {
            viewModelScope.launch { entriesRepo.save(pending) }
        }
    }

    fun deleteEntry(entry: ReflectionEntry) {
        viewModelScope.launch { entriesRepo.delete(entry) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setRemindersEnabled(enabled)
            scheduler.apply(settingsRepo.settings.first())
        }
    }

    fun setReminderTime(part: DayPart, hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepo.setReminderTime(part, hour * 60 + minute)
            scheduler.apply(settingsRepo.settings.first())
        }
    }

    /** Re-books alarms after the notification permission is granted. */
    fun rescheduleReminders() {
        viewModelScope.launch { scheduler.apply(settingsRepo.settings.first()) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookendApp
                AppViewModel(app.entries, app.settings, app.scheduler)
            }
        }
    }
}
