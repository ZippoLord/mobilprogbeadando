package com.example.mobilprog_beadando.data.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val repo: WeatherRepository) : ViewModel() {

    private val _history = MutableStateFlow<List<MoodEntry>>(emptyList())
    val history = _history

    fun loadHistory() {
        viewModelScope.launch {
            try {
                val result = repo.getAllMood()
                _history.value = result.take(10) // csak 10 elem
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
