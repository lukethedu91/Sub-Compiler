package com.bookend.reflection.ui

import com.bookend.reflection.data.DayPart
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dayFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
private val shortDayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

fun LocalDate.longLabel(): String = format(dayFormatter)

fun LocalDate.relativeLabel(today: LocalDate = LocalDate.now()): String = when (this) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> format(shortDayFormatter)
}

fun LocalTime.label(): String = format(timeFormatter)

fun DayPart.title(): String = when (this) {
    DayPart.MORNING -> "Morning"
    DayPart.EVENING -> "Evening"
}

fun DayPart.tagline(): String = when (this) {
    DayPart.MORNING -> "Set up the day ahead"
    DayPart.EVENING -> "Look back on the day"
}
