package com.bookend.reflection.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.ui.day.DayScreen
import com.bookend.reflection.ui.entry.EntryScreen
import com.bookend.reflection.ui.history.HistoryScreen
import com.bookend.reflection.ui.home.HomeScreen
import com.bookend.reflection.ui.settings.SettingsScreen
import java.time.LocalDate

@Composable
fun BookendRoot(
    viewModel: AppViewModel,
    launchPart: DayPart?,
    onLaunchPartHandled: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
) {
    // A plain stack: back goes where you came from, which matters once you can
    // reach the editor from home, from a day page, or from the journal index.
    var stack by remember { mutableStateOf(listOf<Screen>(Screen.Home)) }
    val screen = stack.last()

    val home by viewModel.home.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()

    fun leaving(current: Screen) {
        if (current is Screen.Entry) viewModel.closeEntry()
    }

    fun push(destination: Screen) {
        leaving(screen)
        if (destination is Screen.Entry) viewModel.openEntry(destination.date, destination.part)
        stack = stack + destination
    }

    /** Replaces the top of the stack; used to page from one day to the next. */
    fun replace(destination: Screen) {
        leaving(screen)
        if (destination is Screen.Entry) viewModel.openEntry(destination.date, destination.part)
        stack = stack.dropLast(1) + destination
    }

    fun pop() {
        leaving(screen)
        stack = if (stack.size > 1) stack.dropLast(1) else stack
    }

    // A reminder or widget tap always lands on that half of today.
    LaunchedEffect(launchPart) {
        val part = launchPart ?: return@LaunchedEffect
        viewModel.refreshToday()
        val today = LocalDate.now()
        leaving(screen)
        viewModel.openEntry(today, part)
        stack = listOf(Screen.Home, Screen.Entry(today, part))
        onLaunchPartHandled()
    }

    BackHandler(enabled = stack.size > 1) { pop() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        AnimatedContent(
            targetState = stack,
            transitionSpec = {
                val forward = targetState.size >= initialState.size
                val shift = if (forward) 1 else -1
                (
                    slideInHorizontally(tween(260)) { width -> shift * width / 6 } +
                        fadeIn(tween(220))
                    ) togetherWith (
                    slideOutHorizontally(tween(260)) { width -> -shift * width / 8 } +
                        fadeOut(tween(160))
                    )
            },
            label = "screen",
        ) { currentStack ->
            when (val current = currentStack.last()) {
                Screen.Home -> HomeScreen(
                    state = home,
                    onOpen = { part -> push(Screen.Entry(home.today, part)) },
                    onOpenDay = { date -> push(Screen.Day(date)) },
                    onHistory = { push(Screen.History) },
                    onSettings = { push(Screen.Settings) },
                    onResume = viewModel::refreshToday,
                )

                is Screen.Day -> DayScreen(
                    date = current.date,
                    entries = entries.filter { it.dateEpochDay == current.date.toEpochDay() },
                    onBack = ::pop,
                    onGoToDate = { date -> replace(Screen.Day(date)) },
                    onEdit = { part -> push(Screen.Entry(current.date, part)) },
                    onDelete = viewModel::deleteEntry,
                )

                is Screen.Entry -> EntryScreen(
                    date = current.date,
                    part = current.part,
                    entry = draft,
                    saveState = saveState,
                    onAnswerChange = viewModel::updateAnswer,
                    onBack = ::pop,
                    onSwitchPart = { part -> replace(Screen.Entry(current.date, part)) },
                    onOpenDay = { push(Screen.Day(current.date)) },
                )

                Screen.History -> HistoryScreen(
                    entries = entries,
                    onBack = ::pop,
                    onOpenDay = { date -> push(Screen.Day(date)) },
                )

                Screen.Settings -> SettingsScreen(
                    settings = settings,
                    onBack = ::pop,
                    onToggleReminders = { enabled ->
                        viewModel.setRemindersEnabled(enabled)
                        if (enabled) onRequestNotificationPermission()
                    },
                    onSetTime = viewModel::setReminderTime,
                )
            }
        }
    }
}
