package com.bookend.reflection.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    var screen: Screen by remember { mutableStateOf<Screen>(Screen.Home) }
    val home by viewModel.home.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()

    fun open(date: LocalDate, part: DayPart) {
        viewModel.openEntry(date, part)
        screen = Screen.Entry(date, part)
    }

    fun goHome() {
        if (screen is Screen.Entry) viewModel.closeEntry()
        screen = Screen.Home
    }

    // A reminder tap always lands on that half of today.
    LaunchedEffect(launchPart) {
        val part = launchPart ?: return@LaunchedEffect
        viewModel.refreshToday()
        open(LocalDate.now(), part)
        onLaunchPartHandled()
    }

    BackHandler(enabled = screen !is Screen.Home) { goHome() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen",
        ) { current ->
            when (current) {
                Screen.Home -> HomeScreen(
                    state = home,
                    onOpen = { part -> open(home.today, part) },
                    onHistory = { screen = Screen.History },
                    onSettings = { screen = Screen.Settings },
                    onResume = viewModel::refreshToday,
                )

                is Screen.Entry -> EntryScreen(
                    date = current.date,
                    part = current.part,
                    entry = draft,
                    onAnswerChange = viewModel::updateAnswer,
                    onBack = ::goHome,
                    onSwitchPart = { part ->
                        viewModel.closeEntry()
                        open(current.date, part)
                    },
                )

                Screen.History -> HistoryScreen(
                    entries = entries,
                    onBack = ::goHome,
                    onOpen = { date, part -> open(date, part) },
                    onDelete = viewModel::deleteEntry,
                )

                Screen.Settings -> SettingsScreen(
                    settings = settings,
                    onBack = ::goHome,
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
