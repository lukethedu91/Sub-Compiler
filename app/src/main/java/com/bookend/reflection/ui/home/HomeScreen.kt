package com.bookend.reflection.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.data.Question
import com.bookend.reflection.data.ReflectionEntry
import com.bookend.reflection.ui.HomeUiState
import com.bookend.reflection.ui.longLabel
import com.bookend.reflection.ui.tagline
import com.bookend.reflection.ui.title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpen: (DayPart) -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onResume: () -> Unit,
) {
    LaunchedEffect(Unit) { onResume() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookend") },
                actions = {
                    IconButton(onClick = onHistory) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = state.today.longLabel(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = streakLine(state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )

            Spacer(Modifier.height(24.dp))

            PartCard(
                part = DayPart.MORNING,
                icon = Icons.Outlined.WbSunny,
                entry = state.morning,
                onClick = { onOpen(DayPart.MORNING) },
            )
            Spacer(Modifier.height(16.dp))
            PartCard(
                part = DayPart.EVENING,
                icon = Icons.Outlined.DarkMode,
                entry = state.evening,
                onClick = { onOpen(DayPart.EVENING) },
            )

            Spacer(Modifier.height(28.dp))
            Text(
                text = "Five questions, twice a day",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(10.dp))
            Question.entries.forEach { question ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "·",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 10.dp),
                    )
                    Text(
                        text = question.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun streakLine(state: HomeUiState): String = when {
    state.streak > 1 -> "${state.streak} days in a row"
    state.streak == 1 -> "Streak started today"
    state.totalEntries > 0 -> "Pick the streak back up"
    else -> "Your first entry starts here"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartCard(
    part: DayPart,
    icon: ImageVector,
    entry: ReflectionEntry?,
    onClick: () -> Unit,
) {
    val answered = entry?.answeredCount ?: 0
    val complete = answered == Question.entries.size
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (part == DayPart.MORNING) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = part.title(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = when {
                        complete -> "All five answered"
                        answered > 0 -> "$answered of ${Question.entries.size} answered"
                        else -> part.tagline()
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (complete) {
                Icon(Icons.Filled.Check, contentDescription = "Complete")
            }
        }
    }
}
