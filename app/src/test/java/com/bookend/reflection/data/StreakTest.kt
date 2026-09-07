package com.bookend.reflection.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakTest {

    private val today = LocalDate.of(2026, 9, 7)

    @Test
    fun `no entries means no streak`() {
        assertEquals(0, currentStreak(emptyList(), today))
    }

    @Test
    fun `today alone is a streak of one`() {
        assertEquals(1, currentStreak(listOf(today), today))
    }

    @Test
    fun `consecutive days ending today count`() {
        val days = (0L..3L).map { today.minusDays(it) }
        assertEquals(4, currentStreak(days, today))
    }

    @Test
    fun `streak survives a day that is not over yet`() {
        assertEquals(2, currentStreak(listOf(today.minusDays(1), today.minusDays(2)), today))
    }

    @Test
    fun `a two day gap breaks the streak`() {
        assertEquals(0, currentStreak(listOf(today.minusDays(2), today.minusDays(3)), today))
    }

    @Test
    fun `only the run ending today counts`() {
        val days = listOf(today, today.minusDays(1), today.minusDays(5))
        assertEquals(2, currentStreak(days, today))
    }
}
