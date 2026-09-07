package com.bookend.reflection.data

/** The two ends of a day that Bookend prompts you at. */
enum class DayPart {
    MORNING,
    EVENING;

    companion object {
        fun fromNameOrNull(value: String?): DayPart? =
            entries.firstOrNull { it.name == value }
    }
}
