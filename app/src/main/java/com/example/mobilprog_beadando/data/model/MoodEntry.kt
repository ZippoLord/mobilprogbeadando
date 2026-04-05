package com.example.mobilprog_beadando.data.model

data class MoodEntry(
    val _id: String? = null,
    val date: Long,
    val moodValue: Int,
    val temperature: Double,
    val weatherType: String,
    val cityName: String
)
