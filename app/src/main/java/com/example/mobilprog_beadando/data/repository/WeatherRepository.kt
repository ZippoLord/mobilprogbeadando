package com.example.mobilprog_beadando.data.repository

import android.util.Log
import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.model.Weather
import com.example.mobilprog_beadando.data.network.RetrofitClient
import com.example.mobilprog_beadando.data.network.RetrofitClient.api

class WeatherRepository {

    suspend fun getWeather(city: String): Weather {
        val response = api.getWeather(city)

        return Weather(
            cityName = response.cityName,
            temperature = response.temperature,
            weatherType = response.weatherType
        )
    }
    suspend fun getAllMood(): List<MoodEntry> { return api.getAllMood() }
}
