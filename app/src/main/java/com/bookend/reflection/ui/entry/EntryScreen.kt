package com.bookend.reflection.ui.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.data.Question
import com.bookend.reflection.data.ReflectionEntry
import com.bookend.reflection.ui.SaveState
import com.bookend.reflection.ui.components.ContentMaxWidth
import com.bookend.reflection.ui.components.ProgressTrack
import com.bookend.reflection.ui.components.SegmentedToggle
import com.bookend.reflection.ui.relativeLabel
import com.bookend.reflection.ui.title
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    date: LocalDate,
    part: DayPart,
    entry: ReflectionEntry?,
    saveState: SaveState,
    onAnswerChange: (Question, String) -> Unit,
    onBack: () -> Unit,
    onSwitchPart: (DayPart) -> Unit,
) {
    val questions = Question.entries
    val answered = entry?.answeredCount ?: 0
    val focusManager = LocalFocusManager.current
    // One requester per question so the keyboard's next key walks the form.
    val requesters = remember { List(questions.size) { FocusRequester() } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = date.relativeLabel(),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    SaveIndicator(saveState)
                    Spacer(Modifier.size(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 56.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item {
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        SegmentedToggle(
                            options = DayPart.entries.map { it.title() },
                            selectedIndex = part.ordinal,
                            onSelect = { index ->
                                val target = DayPart.entries[index]
                                if (target != part) onSwitchPart(target)
                            },
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProgressTrack(
                                progress = answered.toFloat() / questions.size,
                                modifier = Modifier.weight(1f),
                                color = if (part == DayPart.MORNING) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                            )
                            Spacer(Modifier.size(10.dp))
                            Text(
                                text = "$answered of ${questions.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                items(questions, key = { it.name }) { question ->
                    val index = question.ordinal
                    QuestionField(
                        index = index,
                        question = question,
                        part = part,
                        value = entry?.answer(question).orEmpty(),
                        onValueChange = { onAnswerChange(question, it) },
                        focusRequester = requesters[index],
                        imeAction = if (index == questions.lastIndex) {
                            ImeAction.Done
                        } else {
                            ImeAction.Next
                        },
                        onNext = { requesters[index + 1].requestFocus() },
                        onDone = { focusManager.clearFocus() },
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveIndicator(saveState: SaveState) {
    AnimatedVisibility(
        visible = saveState != SaveState.IDLE,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (saveState == SaveState.SAVED) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.size(4.dp))
            }
            Text(
                text = if (saveState == SaveState.SAVED) "Saved" else "Saving",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionField(
    index: Int,
    question: Question,
    part: DayPart,
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    imeAction: ImeAction,
    onNext: () -> Unit,
    onDone: () -> Unit,
) {
    val answered = value.isNotBlank()

    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = question.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (answered) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = question.promptFor(part),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(10.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = "Write a line",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = MaterialTheme.shapes.medium,
            minLines = 2,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() },
                onDone = { onDone() },
            ),
        )
    }
}
