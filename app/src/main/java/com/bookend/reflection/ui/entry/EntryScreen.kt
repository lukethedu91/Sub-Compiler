package com.bookend.reflection.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.data.Question
import com.bookend.reflection.data.ReflectionEntry
import com.bookend.reflection.ui.relativeLabel
import com.bookend.reflection.ui.title
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    date: LocalDate,
    part: DayPart,
    entry: ReflectionEntry?,
    onAnswerChange: (Question, String) -> Unit,
    onBack: () -> Unit,
    onSwitchPart: (DayPart) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("${part.title()} · ${date.relativeLabel()}")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                bottom = 48.dp,
            ),
        ) {
            item {
                Row(modifier = Modifier.padding(bottom = 12.dp)) {
                    DayPart.entries.forEach { option ->
                        FilterChip(
                            selected = option == part,
                            onClick = { if (option != part) onSwitchPart(option) },
                            label = { Text(option.title()) },
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                }
            }

            items(Question.entries, key = { it.name }) { question ->
                QuestionField(
                    question = question,
                    part = part,
                    value = entry?.answer(question).orEmpty(),
                    onValueChange = { onAnswerChange(question, it) },
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Saved as you type.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun QuestionField(
    question: Question,
    part: DayPart,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = question.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = question.promptFor(part),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            minLines = 2,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default,
            ),
        )
    }
}
