package com.example.mobilprog_beadando.data.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

object AppSettings {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_TEMPERATURE_UNIT = "temperature_unit"
    private const val KEY_LAST_CITY = "last_city"
    private const val KEY_LAST_WEATHER_TYPE = "last_weather_type"
    private const val KEY_LAST_TEMPERATURE = "last_temperature"

    data class LatestWeatherData(
        val cityName: String,
        val weatherType: String,
        val temperatureInCelsius: Double
    )

    fun getLanguage(context: Context): String {
        return prefs(context).getString(KEY_LANGUAGE, "hu") ?: "hu"
    }

    fun setLanguage(context: Context, languageCode: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, languageCode).apply()
        applyLanguageRuntime(languageCode)
    }

    fun getTemperatureUnit(context: Context): TemperatureUnit {
        val raw = prefs(context).getString(KEY_TEMPERATURE_UNIT, TemperatureUnit.CELSIUS.name)
        return runCatching { TemperatureUnit.valueOf(raw ?: TemperatureUnit.CELSIUS.name) }
            .getOrDefault(TemperatureUnit.CELSIUS)
    }

    fun setTemperatureUnit(context: Context, unit: TemperatureUnit) {
        prefs(context).edit().putString(KEY_TEMPERATURE_UNIT, unit.name).apply()
    }

    fun saveLatestWeather(
        context: Context,
        cityName: String,
        weatherType: String,
        temperatureInCelsius: Double
    ) {
        prefs(context).edit()
            .putString(KEY_LAST_CITY, cityName)
            .putString(KEY_LAST_WEATHER_TYPE, weatherType)
            .putFloat(KEY_LAST_TEMPERATURE, temperatureInCelsius.toFloat())
            .apply()
    }

    fun getLatestWeather(context: Context): LatestWeatherData {
        val sharedPrefs = prefs(context)
        val city = sharedPrefs.getString(KEY_LAST_CITY, "Unknown") ?: "Unknown"
        val weatherType = sharedPrefs.getString(KEY_LAST_WEATHER_TYPE, "Unknown") ?: "Unknown"
        val temperature = sharedPrefs.getFloat(KEY_LAST_TEMPERATURE, 0f).toDouble()

        return LatestWeatherData(
            cityName = city,
            weatherType = weatherType,
            temperatureInCelsius = temperature
        )
    }

    fun applyLocale(context: Context): Context {
        val languageCode = getLanguage(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        applyLanguageRuntime(languageCode)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun applyLanguageRuntime(languageCode: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
