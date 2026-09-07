package com.bookend.reflection.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.data.Question
import com.bookend.reflection.data.ReflectionEntry
import com.bookend.reflection.ui.DayMark
import com.bookend.reflection.ui.HomeUiState
import com.bookend.reflection.ui.components.ContentMaxWidth
import com.bookend.reflection.ui.components.Pill
import com.bookend.reflection.ui.components.ProgressTrack
import com.bookend.reflection.ui.greeting
import com.bookend.reflection.ui.longLabel
import com.bookend.reflection.ui.title
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpen: (DayPart) -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onResume: () -> Unit,
) {
    LaunchedEffect(Unit) { onResume() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onHistory) {
                    Icon(Icons.Outlined.History, contentDescription = "History")
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            }

            Text(
                text = greeting(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.today.longLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (state.streak > 0) {
                    Pill(
                        text = "${state.streak} day streak",
                        container = MaterialTheme.colorScheme.primaryContainer,
                        content = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Side by side once there is room for two readable columns.
            SideBySideWhenWide(spacing = 14.dp) {
                PartCard(
                    part = DayPart.MORNING,
                    icon = Icons.Outlined.WbSunny,
                    entry = state.morning,
                    onClick = { onOpen(DayPart.MORNING) },
                )
                PartCard(
                    part = DayPart.EVENING,
                    icon = Icons.Outlined.DarkMode,
                    entry = state.evening,
                    onClick = { onOpen(DayPart.EVENING) },
                )
            }

            if (state.week.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "This week",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                WeekStrip(days = state.week, onClick = onHistory)
            }

            if (state.totalEntries == 0) {
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "Five questions, morning and evening. Answer what you can — " +
                        "a single line counts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartCard(
    part: DayPart,
    icon: ImageVector,
    entry: ReflectionEntry?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val answered = entry?.answeredCount ?: 0
    val total = Question.entries.size
    val complete = answered == total
    val accent = if (part == DayPart.MORNING) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val container = if (part == DayPart.MORNING) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val onContainer = if (part == DayPart.MORNING) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = onContainer,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.size(10.dp))
                Text(
                    text = part.title(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = when {
                    complete -> "All five answered"
                    answered > 0 -> nextQuestionLabel(part, entry)
                    else -> firstPrompt(part)
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
            )

            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressTrack(
                    progress = answered.toFloat() / total,
                    color = accent,
                    trackColor = onContainer.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    text = "$answered/$total",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

private fun firstPrompt(part: DayPart): String = Question.entries.first().promptFor(part)

private fun nextQuestionLabel(part: DayPart, entry: ReflectionEntry?): String {
    val next = Question.entries.firstOrNull { entry?.answer(it).isNullOrBlank() }
        ?: return "All five answered"
    return next.promptFor(part)
}

@Composable
private fun WeekStrip(days: List<DayMark>, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        days.forEach { day ->
            val filled = MaterialTheme.colorScheme.primary
            val half = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            val empty = MaterialTheme.colorScheme.surfaceContainerHigh
            val dotColor by animateColorAsState(
                targetValue = when (day.partsWritten) {
                    0 -> empty
                    1 -> half
                    else -> filled
                },
                animationSpec = tween(250),
                label = "day-dot",
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day.date.dayOfWeek
                        .getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
            }
        }
    }
}

/**
 * Stacks its two children on a phone and places them side by side once the
 * available width can carry two comfortable columns.
 */
@Composable
private fun SideBySideWhenWide(
    spacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit,
) {
    Layout(content = content) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val wide = constraints.maxWidth >= (360.dp.roundToPx() * 2 + gap)

        if (wide) {
            val childWidth = (constraints.maxWidth - gap) / 2
            val placeables = measurables.map {
                it.measure(constraints.copy(minWidth = childWidth, maxWidth = childWidth))
            }
            val height = placeables.maxOfOrNull { it.height } ?: 0
            layout(constraints.maxWidth, height) {
                var x = 0
                placeables.forEach { placeable ->
                    placeable.placeRelative(x, 0)
                    x += placeable.width + gap
                }
            }
        } else {
            val placeables = measurables.map { it.measure(constraints) }
            val height = placeables.sumOf { it.height } +
                gap * (placeables.size - 1).coerceAtLeast(0)
            layout(constraints.maxWidth, height) {
                var y = 0
                placeables.forEach { placeable ->
                    placeable.placeRelative(0, y)
                    y += placeable.height + gap
                }
            }
        }
    }
}
