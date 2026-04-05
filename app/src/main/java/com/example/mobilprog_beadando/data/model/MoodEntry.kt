package com.example.mobilprog_beadando.data.model

data class MoodEntry(
    val date: Long,
    val moodValue: Int,
    val temperature: Double,
    val weatherType: String,
    val cityName: String
)
