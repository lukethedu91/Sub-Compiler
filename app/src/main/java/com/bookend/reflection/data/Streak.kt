package com.bookend.reflection.data

import java.time.LocalDate

/**
 * Consecutive days ending today (or yesterday, so a streak survives until the
 * day is actually over) on which at least one question was answered.
 */
fun currentStreak(daysWithEntries: Collection<LocalDate>, today: LocalDate): Int {
    if (daysWithEntries.isEmpty()) return 0
    val days = daysWithEntries.toHashSet()
    var cursor = when {
        days.contains(today) -> today
        days.contains(today.minusDays(1)) -> today.minusDays(1)
        else -> return 0
    }
    var streak = 0
    while (days.contains(cursor)) {
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}
