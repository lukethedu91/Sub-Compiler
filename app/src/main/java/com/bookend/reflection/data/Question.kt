package com.bookend.reflection.data

/**
 * The five questions, asked in both directions: forward-looking in the
 * morning, backward-looking in the evening.
 */
enum class Question(
    val label: String,
    val morningPrompt: String,
    val eveningPrompt: String,
) {
    HIGH(
        label = "High",
        morningPrompt = "What are you most looking forward to today?",
        eveningPrompt = "What was the high point of your day?",
    ),
    LOW(
        label = "Low",
        morningPrompt = "What do you expect to be the hardest part of today?",
        eveningPrompt = "What was the low point of your day?",
    ),
    GRATEFUL(
        label = "Grateful for",
        morningPrompt = "What are you grateful for as today starts?",
        eveningPrompt = "What are you grateful for from today?",
    ),
    LEARNED(
        label = "Learned",
        morningPrompt = "What do you want to learn today?",
        eveningPrompt = "What did you learn today?",
    ),
    WORK_ON(
        label = "To work on",
        morningPrompt = "What is one thing you want to do better today?",
        eveningPrompt = "What is one thing to work on tomorrow?",
    );

    fun promptFor(part: DayPart): String = when (part) {
        DayPart.MORNING -> morningPrompt
        DayPart.EVENING -> eveningPrompt
    }
}
