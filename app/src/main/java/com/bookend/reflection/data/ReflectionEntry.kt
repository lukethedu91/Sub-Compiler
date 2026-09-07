package com.bookend.reflection.data

import androidx.room.Entity
import java.time.LocalDate

/**
 * One set of answers for one half of one day. A day can hold a morning entry
 * (what you expect) and an evening entry (what actually happened).
 */
@Entity(tableName = "entries", primaryKeys = ["dateEpochDay", "part"])
data class ReflectionEntry(
    val dateEpochDay: Long,
    val part: DayPart,
    val high: String = "",
    val low: String = "",
    val grateful: String = "",
    val learned: String = "",
    val workOn: String = "",
    val updatedAt: Long = 0L,
) {
    val date: LocalDate get() = LocalDate.ofEpochDay(dateEpochDay)

    val answers: List<String> get() = listOf(high, low, grateful, learned, workOn)

    /** How many of the five questions have something in them. */
    val answeredCount: Int get() = answers.count { it.isNotBlank() }

    val isBlank: Boolean get() = answeredCount == 0

    fun withAnswer(question: Question, text: String): ReflectionEntry = when (question) {
        Question.HIGH -> copy(high = text)
        Question.LOW -> copy(low = text)
        Question.GRATEFUL -> copy(grateful = text)
        Question.LEARNED -> copy(learned = text)
        Question.WORK_ON -> copy(workOn = text)
    }

    fun answer(question: Question): String = when (question) {
        Question.HIGH -> high
        Question.LOW -> low
        Question.GRATEFUL -> grateful
        Question.LEARNED -> learned
        Question.WORK_ON -> workOn
    }

    companion object {
        fun empty(date: LocalDate, part: DayPart) =
            ReflectionEntry(dateEpochDay = date.toEpochDay(), part = part)
    }
}
