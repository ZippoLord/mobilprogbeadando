package com.example.mobilprog_beadando.data.repository


import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.network.RetrofitClient

class MoodRepository {

    suspend fun sendMoodToServer(entry: MoodEntry) {
        RetrofitClient.api.sendMood(entry)
    }

    suspend fun getAllMood(): List<MoodEntry> {
        return RetrofitClient.api.getAllMood()
    }

    suspend fun deleteMood(id: String) {
        RetrofitClient.api.deleteMood(id)
    }
}
