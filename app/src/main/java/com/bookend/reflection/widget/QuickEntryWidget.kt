package com.bookend.reflection.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.bookend.reflection.BookendApp
import com.bookend.reflection.R
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.data.Question
import com.bookend.reflection.data.ReflectionEntry
import com.bookend.reflection.reminder.Notifications
import com.bookend.reflection.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Home-screen widget showing the next question you have not answered, with a
 * tap target for each half of the day. Widgets cannot take text input, so the
 * job here is to make the first tap land on the right question.
 */
class QuickEntryWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val views = buildViews(appContext, loadState(appContext))
                appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, views) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private data class State(
        val date: LocalDate,
        val morning: ReflectionEntry?,
        val evening: ReflectionEntry?,
        val focus: DayPart,
    ) {
        fun entryFor(part: DayPart) = if (part == DayPart.MORNING) morning else evening
    }

    private suspend fun loadState(context: Context): State {
        val entries = (context.applicationContext as BookendApp).entries
        val today = LocalDate.now()
        return State(
            date = today,
            morning = entries.find(today, DayPart.MORNING),
            evening = entries.find(today, DayPart.EVENING),
            focus = suggestedPart(),
        )
    }

    private fun buildViews(context: Context, state: State): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_quick_entry)

        views.setTextViewText(R.id.widget_date, state.date.format(DATE_FORMAT))
        views.setTextViewText(R.id.widget_prompt, promptFor(state))
        views.setTextViewText(R.id.widget_morning, buttonLabel(DayPart.MORNING, state.morning))
        views.setTextViewText(R.id.widget_evening, buttonLabel(DayPart.EVENING, state.evening))

        views.setOnClickPendingIntent(R.id.widget_root, openIntent(context, state.focus))
        views.setOnClickPendingIntent(R.id.widget_morning, openIntent(context, DayPart.MORNING))
        views.setOnClickPendingIntent(R.id.widget_evening, openIntent(context, DayPart.EVENING))

        return views
    }

    /** The first unanswered question of the half of the day you are in. */
    private fun promptFor(state: State): String {
        val entry = state.entryFor(state.focus)
        val next = Question.entries.firstOrNull { entry?.answer(it).isNullOrBlank() }
            ?: return "All five answered. Nice."
        return next.promptFor(state.focus)
    }

    private fun buttonLabel(part: DayPart, entry: ReflectionEntry?): String {
        val answered = entry?.answeredCount ?: 0
        val name = if (part == DayPart.MORNING) "Morning" else "Evening"
        return if (answered == Question.entries.size) "$name ✓" else "$name $answered/${Question.entries.size}"
    }

    private fun openIntent(context: Context, part: DayPart): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Notifications.EXTRA_PART, part.name)
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_BASE + part.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val REQUEST_BASE = 100
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

        /** Mornings run until mid-afternoon; after that the evening is the live half. */
        fun suggestedPart(now: LocalTime = LocalTime.now()): DayPart =
            if (now.hour < 15) DayPart.MORNING else DayPart.EVENING

        /** Redraws every placed widget; safe to call when none are placed. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(
                ComponentName(context, QuickEntryWidget::class.java),
            )
            if (ids.isEmpty()) return
            context.sendBroadcast(
                Intent(context, QuickEntryWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                },
            )
        }
    }
}
