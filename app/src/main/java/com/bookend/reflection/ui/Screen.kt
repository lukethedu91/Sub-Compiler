package com.bookend.reflection.ui

import com.bookend.reflection.data.DayPart
import java.time.LocalDate

sealed interface Screen {
    data object Home : Screen
    data class Entry(val date: LocalDate, val part: DayPart) : Screen
    data object History : Screen
    data object Settings : Screen
}
