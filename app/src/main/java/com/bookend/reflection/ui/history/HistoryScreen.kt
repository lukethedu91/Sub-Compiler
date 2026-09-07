package com.bookend.reflection.ui.history

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.data.Question
import com.bookend.reflection.data.ReflectionEntry
import com.bookend.reflection.ui.components.ContentMaxWidth
import com.bookend.reflection.ui.components.Pill
import com.bookend.reflection.ui.relativeLabel
import com.bookend.reflection.ui.title
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    entries: List<ReflectionEntry>,
    onBack: () -> Unit,
    onOpen: (LocalDate, DayPart) -> Unit,
    onDelete: (ReflectionEntry) -> Unit,
) {
    val days = entries
        .groupBy { it.dateEpochDay }
        .entries
        .sortedByDescending { it.key }
        .map { LocalDate.ofEpochDay(it.key) to it.value }

    var pendingDelete by remember { mutableStateOf<ReflectionEntry?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("History", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        if (days.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nothing written yet.\nAnswer today's five and it shows up here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            return@Scaffold
        }

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
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(days, key = { it.first.toEpochDay() }) { (date, dayEntries) ->
                    DaySection(
                        date = date,
                        entries = dayEntries.sortedBy { it.part.ordinal },
                        onOpen = onOpen,
                        onDelete = { pendingDelete = it },
                    )
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
                        "${target.date.relativeLabel().lowercase()} will be removed.",
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
}

@Composable
private fun DaySection(
    date: LocalDate,
    entries: List<ReflectionEntry>,
    onOpen: (LocalDate, DayPart) -> Unit,
    onDelete: (ReflectionEntry) -> Unit,
) {
    Column {
        Text(
            text = date.relativeLabel(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(10.dp))
        entries.forEach { entry ->
            EntryCard(
                entry = entry,
                onOpen = { onOpen(date, entry.part) },
                onDelete = { onDelete(entry) },
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun EntryCard(
    entry: ReflectionEntry,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val accent = if (entry.part == DayPart.MORNING) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .clickable { expanded = !expanded }
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.part.title(),
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    modifier = Modifier.weight(1f),
                )
                Pill(text = "${entry.answeredCount}/${Question.entries.size}")
                Spacer(Modifier.size(4.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete ${entry.part.title()} entry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            val shown = if (expanded) {
                Question.entries.filter { entry.answer(it).isNotBlank() }
            } else {
                Question.entries.filter { entry.answer(it).isNotBlank() }.take(2)
            }
            shown.forEach { question ->
                Text(
                    text = question.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = entry.answer(question),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                )
            }

            Spacer(Modifier.height(12.dp))
            Row {
                Text(
                    text = if (expanded) "Show less" else "Show all",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { expanded = !expanded },
                )
                Spacer(Modifier.size(20.dp))
                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    modifier = Modifier.clickable(onClick = onOpen),
                )
            }
        }
    }
}
