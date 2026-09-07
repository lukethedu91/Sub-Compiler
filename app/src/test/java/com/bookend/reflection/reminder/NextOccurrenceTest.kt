package com.bookend.reflection.reminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class NextOccurrenceTest {

    @Test
    fun `a time later today fires today`() {
        val from = LocalDateTime.of(2026, 9, 7, 6, 0)
        assertEquals(
            LocalDateTime.of(2026, 9, 7, 21, 0),
            nextOccurrence(21 * 60, from),
        )
    }

    @Test
    fun `a time already past rolls to tomorrow`() {
        val from = LocalDateTime.of(2026, 9, 7, 22, 30)
        assertEquals(
            LocalDateTime.of(2026, 9, 8, 21, 0),
            nextOccurrence(21 * 60, from),
        )
    }

    @Test
    fun `the exact minute rolls to tomorrow rather than firing twice`() {
        val from = LocalDateTime.of(2026, 9, 7, 7, 0)
        assertEquals(
            LocalDateTime.of(2026, 9, 8, 7, 0),
            nextOccurrence(7 * 60, from),
        )
    }
}
