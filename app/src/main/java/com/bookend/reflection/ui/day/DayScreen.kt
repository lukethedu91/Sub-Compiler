package com.bookend.reflection.ui.day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.data.Question
import com.bookend.reflection.data.ReflectionEntry
import com.bookend.reflection.ui.components.ContentMaxWidth
import com.bookend.reflection.ui.longLabel
import com.bookend.reflection.ui.relativeLabel
import com.bookend.reflection.ui.title
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(
    date: LocalDate,
    entries: List<ReflectionEntry>,
    onBack: () -> Unit,
    onGoToDate: (LocalDate) -> Unit,
    onEdit: (DayPart) -> Unit,
    onDelete: (ReflectionEntry) -> Unit,
) {
    val today = LocalDate.now()
    val morning = entries.firstOrNull { it.part == DayPart.MORNING }
    val evening = entries.firstOrNull { it.part == DayPart.EVENING }
    var pendingDelete by remember { mutableStateOf<ReflectionEntry?>(null) }
    var showCalendar by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = date.relativeLabel(today),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCalendar = true }) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = "Jump to a date",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    DayNavigator(
                        date = date,
                        canGoForward = date.isBefore(today),
                        onPrevious = { onGoToDate(date.minusDays(1)) },
                        onNext = { onGoToDate(date.plusDays(1)) },
                    )
                }

                item {
                    PartSection(
                        part = DayPart.MORNING,
                        icon = Icons.Outlined.WbSunny,
                        entry = morning,
                        onEdit = { onEdit(DayPart.MORNING) },
                        onDelete = { morning?.let { pendingDelete = it } },
                    )
                }

                item {
                    PartSection(
                        part = DayPart.EVENING,
                        icon = Icons.Outlined.DarkMode,
                        entry = evening,
                        onEdit = { onEdit(DayPart.EVENING) },
                        onDelete = { evening?.let { pendingDelete = it } },
                    )
                }

                if (date != today) {
                    item {
                        TextButton(onClick = { onGoToDate(today) }) { Text("Back to today") }
                    }
                }
            }
        }
    }

    val target = pendingDelete
    if (target != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this entry?") },
            text = {
                Text(
                    "The ${target.part.title().lowercase()} answers for " +
                        "${date.longLabel()} will be removed.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(target)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }

    if (showCalendar) {
        DateJumpDialog(
            initial = date,
            latest = today,
            onDismiss = { showCalendar = false },
            onPick = { picked ->
                showCalendar = false
                onGoToDate(picked)
            },
        )
    }
}

@Composable
private fun DayNavigator(
    date: LocalDate,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous day",
            )
        }
        Text(
            text = date.longLabel(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
        )
        IconButton(onClick = onNext, enabled = canGoForward) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next day",
            )
        }
    }
}

@Composable
private fun PartSection(
    part: DayPart,
    icon: ImageVector,
    entry: ReflectionEntry?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val accent = if (part == DayPart.MORNING) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val written = entry != null && !entry.isBlank

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = part.title(),
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    modifier = Modifier.weight(1f),
                )
                if (written) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete ${part.title()} entry",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (!written) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Nothing written for this ${part.title().lowercase()}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                FilledTonalButton(onClick = onEdit) { Text("Write it now") }
                return@Column
            }

            Question.entries.forEach { question ->
                val answer = entry?.answer(question).orEmpty()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = question.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = answer.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (answer.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }

            Spacer(Modifier.height(18.dp))
            FilledTonalButton(onClick = onEdit) { Text("Edit") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateJumpDialog(
    initial: LocalDate,
    latest: LocalDate,
    onDismiss: () -> Unit,
    onPick: (LocalDate) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli(),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { DatePicker(state = state, title = null) },
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        val picked = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        // The journal has no future pages to turn to.
                        onPick(if (picked.isAfter(latest)) latest else picked)
                    } else {
                        onDismiss()
                    }
                },
            ) { Text("Go") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
