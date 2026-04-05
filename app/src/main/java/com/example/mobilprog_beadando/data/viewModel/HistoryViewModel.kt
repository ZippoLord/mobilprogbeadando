package com.example.mobilprog_beadando.data.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.repository.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel : ViewModel() {

    private val repo = MoodRepository()

    private val _history = MutableStateFlow<List<MoodEntry>>(emptyList())
    val history: StateFlow<List<MoodEntry>> = _history

    private var allItems: List<MoodEntry> = emptyList()
    private var dateQuery: String = ""
    private var moodFilter: Int? = null

    fun loadHistory() {
        viewModelScope.launch {
            try {
                allItems = repo.getAllMood()
                applyFilters()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateDateQuery(query: String) {
        dateQuery = query.trim()
        applyFilters()
    }

    fun updateMoodFilter(filter: Int?) {
        moodFilter = filter
        applyFilters()
    }

    fun deleteEntry(entry: MoodEntry) {
        viewModelScope.launch {
            try {
                val id = entry._id
                if (!id.isNullOrBlank()) {
                    repo.deleteMood(id)
                }
                allItems = allItems.filterNot { it._id == entry._id && it.date == entry.date }
                applyFilters()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyFilters() {
        val filtered = allItems.filter { item ->
            val dateMatches = dateQuery.isBlank() ||
                formatDate(item.date).contains(dateQuery, ignoreCase = true)
            val moodMatches = moodFilter == null || item.moodValue == moodFilter
            dateMatches && moodMatches
        }
        _history.value = filtered
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
