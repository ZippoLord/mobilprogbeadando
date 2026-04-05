package com.example.mobilprog_beadando.data.viewModel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.model.Weather
import com.example.mobilprog_beadando.data.model.WeatherResponse
import com.example.mobilprog_beadando.data.repository.MoodRepository
import com.example.mobilprog_beadando.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repo = WeatherRepository()
    private val MoodRepo = MoodRepository()

    private val _weather = MutableStateFlow<Weather?>(null)
    val weather: StateFlow<Weather?> = _weather

    fun loadWeather(city: String) {
        Log.d("WEATHER_DEBUG", "loadWeather meghívva: $city")

        viewModelScope.launch {
            try {
                val result = repo.getWeather(city)
                _weather.value = result
                Log.d("WEATHER_DEBUG", "API válasz: $result")
            } catch (e: Exception) {
                Log.e("WEATHER_DEBUG", "API hiba: ${e.message}", e)
            }
        }
    }

    fun sendMood(entry: MoodEntry) {
        viewModelScope.launch {
            try {
                MoodRepo.sendMoodToServer(entry)
                Log.d("MOOD_API", "Sikeresen elküldve a backendnek")
            } catch (e: Exception) {
                Log.e("MOOD_API", "Hiba történt: ${e.message}")
            }
        }
    }



}
