package com.example.mobilprog_beadando

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.repository.MoodRepository
import com.example.mobilprog_beadando.data.utils.AppSettings
import com.example.mobilprog_beadando.data.utils.TemperatureFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateWidgetViews(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action != ACTION_SELECT_MOOD) {
            return
        }

        val moodValue = intent.getIntExtra(EXTRA_MOOD_VALUE, -1)
        if (moodValue !in 1..5) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            if (!hasSubmittedMoodToday(context)) {
                val latestWeather = AppSettings.getLatestWeather(context)
                val moodEntry = MoodEntry(
                    date = System.currentTimeMillis(),
                    moodValue = moodValue,
                    temperature = latestWeather.temperatureInCelsius,
                    weatherType = latestWeather.weatherType,
                    cityName = latestWeather.cityName
                )

                runCatching {
                    MoodRepository().sendMoodToServer(moodEntry)
                }.onSuccess {
                    rememberTodayMood(context, moodValue)
                }
            }

            updateAllWidgets(context)
            pendingResult.finish()
        }
    }

    companion object {
        private const val ACTION_SELECT_MOOD = "com.example.mobilprog_beadando.ACTION_SELECT_MOOD"
        private const val EXTRA_MOOD_VALUE = "extra_mood_value"
        private const val PREFS_MOOD = "mood_prefs"
        private const val KEY_LAST_MOOD_DAY = "last_mood_day"
        private const val KEY_LAST_MOOD_VALUE = "last_mood_value"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, MoodWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                updateWidgetViews(context, appWidgetManager, ids)
            }
        }

        private fun updateWidgetViews(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val localizedContext = AppSettings.applyLocale(context)
            val latestWeather = AppSettings.getLatestWeather(context)
            val selectedMood = getTodayMood(context)

            appWidgetIds.forEach { widgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget_mood)
                val hasMoodForToday = selectedMood != null

                views.setTextViewText(R.id.widgetTitle, localizedContext.getString(R.string.widget_title))
                views.setTextViewText(
                    R.id.widgetQuickMoodTitle,
                    localizedContext.getString(R.string.widget_quick_mood_title)
                )
                views.setTextViewText(R.id.widgetCity, latestWeather.cityName)
                views.setTextViewText(
                    R.id.widgetTemperature,
                    localizedContext.getString(
                        R.string.widget_temperature_label,
                        TemperatureFormatter.format(localizedContext, latestWeather.temperatureInCelsius)
                    )
                )
                views.setTextViewText(
                    R.id.widgetWeather,
                    localizedContext.getString(R.string.widget_weather_label, latestWeather.weatherType)
                )
                views.setTextViewText(R.id.widgetMood, moodText(localizedContext, selectedMood))
                views.setViewVisibility(
                    R.id.widgetQuickMoodTitle,
                    if (hasMoodForToday) android.view.View.GONE else android.view.View.VISIBLE
                )
                views.setViewVisibility(
                    R.id.widgetMoodButtonsRow,
                    if (hasMoodForToday) android.view.View.GONE else android.view.View.VISIBLE
                )

                (1..5).forEach { moodValue ->
                    val intent = Intent(context, MoodWidgetProvider::class.java).apply {
                        action = ACTION_SELECT_MOOD
                        putExtra(EXTRA_MOOD_VALUE, moodValue)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        moodValue,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val buttonId = when (moodValue) {
                        1 -> R.id.widgetMood1
                        2 -> R.id.widgetMood2
                        3 -> R.id.widgetMood3
                        4 -> R.id.widgetMood4
                        else -> R.id.widgetMood5
                    }
                    views.setOnClickPendingIntent(buttonId, pendingIntent)
                }

                val openAppIntent = Intent(context, MainActivity::class.java)
                val openPendingIntent = PendingIntent.getActivity(
                    context,
                    100,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widgetRoot, openPendingIntent)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }

        private fun moodText(context: Context, moodValue: Int?): String {
            if (moodValue == null) {
                return context.getString(R.string.widget_today_mood_not_set)
            }
            return when (moodValue) {
                1 -> context.getString(R.string.widget_today_mood_value, context.getString(R.string.history_mood_1))
                2 -> context.getString(R.string.widget_today_mood_value, context.getString(R.string.history_mood_2))
                3 -> context.getString(R.string.widget_today_mood_value, context.getString(R.string.history_mood_3))
                4 -> context.getString(R.string.widget_today_mood_value, context.getString(R.string.history_mood_4))
                5 -> context.getString(R.string.widget_today_mood_value, context.getString(R.string.history_mood_5))
                else -> context.getString(R.string.widget_today_mood_not_set)
            }
        }

        private fun hasSubmittedMoodToday(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_MOOD, Context.MODE_PRIVATE)
            return prefs.getString(KEY_LAST_MOOD_DAY, null) == todayKey()
        }

        private fun getTodayMood(context: Context): Int? {
            val prefs = context.getSharedPreferences(PREFS_MOOD, Context.MODE_PRIVATE)
            return if (prefs.getString(KEY_LAST_MOOD_DAY, null) == todayKey()) {
                prefs.getInt(KEY_LAST_MOOD_VALUE, 0).takeIf { it in 1..5 }
            } else {
                null
            }
        }

        private fun rememberTodayMood(context: Context, moodValue: Int) {
            context.getSharedPreferences(PREFS_MOOD, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LAST_MOOD_DAY, todayKey())
                .putInt(KEY_LAST_MOOD_VALUE, moodValue)
                .apply()
        }

        private fun todayKey(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}
