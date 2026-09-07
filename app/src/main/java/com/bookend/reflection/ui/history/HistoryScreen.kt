package com.bookend.reflection.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.data.Question
import com.bookend.reflection.data.ReflectionEntry
import com.bookend.reflection.ui.components.ContentMaxWidth
import com.bookend.reflection.ui.relativeLabel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

/** The journal's index: every day that has something in it, newest first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    entries: List<ReflectionEntry>,
    onBack: () -> Unit,
    onOpenDay: (LocalDate) -> Unit,
) {
    val days = entries
        .groupBy { it.dateEpochDay }
        .entries
        .sortedByDescending { it.key }
        .map { LocalDate.ofEpochDay(it.key) to it.value }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Journal", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onOpenDay(LocalDate.now()) }) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = "Open today",
                        )
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(days, key = { _, day -> day.first.toEpochDay() }) { index, day ->
                    val (date, dayEntries) = day
                    // A quiet month heading each time the month changes.
                    val previousMonth = days.getOrNull(index - 1)?.let { YearMonth.from(it.first) }
                    if (previousMonth != YearMonth.from(date)) {
                        Text(
                            text = date.format(monthFormatter),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
                        )
                    }
                    DayRow(
                        date = date,
                        entries = dayEntries,
                        onClick = { onOpenDay(date) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayRow(
    date: LocalDate,
    entries: List<ReflectionEntry>,
    onClick: () -> Unit,
) {
    val morning = entries.firstOrNull { it.part == DayPart.MORNING }
    val evening = entries.firstOrNull { it.part == DayPart.EVENING }
    val preview = (evening ?: morning)?.let { entry ->
        Question.entries.firstOrNull { entry.answer(it).isNotBlank() }
            ?.let { entry.answer(it) }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date.relativeLabel(),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (preview != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            PartDot(
                filled = morning != null && !morning.isBlank,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(6.dp))
            PartDot(
                filled = evening != null && !evening.isBlank,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun PartDot(filled: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(
                if (filled) color else MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
    )
}
