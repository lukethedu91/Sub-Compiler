package com.bookend.reflection.reminder

import java.time.LocalDateTime
import java.time.LocalTime

/** The next time today or tomorrow that [minuteOfDay] comes around. */
fun nextOccurrence(minuteOfDay: Int, from: LocalDateTime): LocalDateTime {
    val time = LocalTime.ofSecondOfDay(minuteOfDay.coerceIn(0, 24 * 60 - 1) * 60L)
    val todayAt = from.toLocalDate().atTime(time)
    return if (todayAt.isAfter(from)) todayAt else todayAt.plusDays(1)
}
