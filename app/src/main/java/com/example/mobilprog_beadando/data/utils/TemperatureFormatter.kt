package com.example.mobilprog_beadando.data.utils

import android.content.Context
import java.util.Locale

object TemperatureFormatter {

    fun format(context: Context, temperatureInCelsius: Double): String {
        val locale = Locale(AppSettings.getLanguage(context))
        return when (AppSettings.getTemperatureUnit(context)) {
            TemperatureUnit.CELSIUS -> {
                String.format(locale, "%.1f °C", temperatureInCelsius)
            }
            TemperatureUnit.FAHRENHEIT -> {
                val fahrenheit = celsiusToFahrenheit(temperatureInCelsius)
                String.format(locale, "%.1f °F", fahrenheit)
            }
        }
    }

    private fun celsiusToFahrenheit(celsius: Double): Double {
        return celsius * 9.0 / 5.0 + 32.0
    }
}
