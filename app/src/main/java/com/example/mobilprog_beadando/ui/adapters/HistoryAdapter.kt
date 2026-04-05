package com.example.mobilprog_beadando.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilprog_beadando.R
import com.example.mobilprog_beadando.data.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private val items: List<MoodEntry>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMood: TextView = view.findViewById(R.id.textMood)
        val textWeather: TextView = view.findViewById(R.id.textWeather)
        val textDate: TextView = view.findViewById(R.id.textDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]

        holder.textMood.text = "Hangulat: ${item.moodValue}"
        holder.textWeather.text = "${item.temperature}°C, ${item.weatherType}"

        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        holder.textDate.text = sdf.format(Date(item.date))
    }

    override fun getItemCount() = items.size
}
