package com.example.mobilprog_beadando.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilprog_beadando.R
import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.utils.TemperatureFormatter
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    items: List<MoodEntry>,
    private val onDeleteClick: (MoodEntry) -> Unit
) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val items = items.toMutableList()

    fun submitList(newItems: List<MoodEntry>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMood: TextView = view.findViewById(R.id.textMood)
        val textWeather: TextView = view.findViewById(R.id.textWeather)
        val textDate: TextView = view.findViewById(R.id.textDate)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        val formattedTemperature = TemperatureFormatter.format(holder.itemView.context, item.temperature)
        val context = holder.itemView.context

        holder.textMood.text = context.getString(R.string.history_mood_label, moodLabel(item.moodValue))
        holder.textWeather.text = context.getString(
            R.string.history_weather_line,
            formattedTemperature,
            item.weatherType,
            item.cityName
        )

        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        holder.textDate.text = sdf.format(Date(item.date))
        holder.deleteButton.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount() = items.size

    private fun moodLabel(moodValue: Int): String {
        return when (moodValue) {
            1 -> "😢"
            2 -> "☹️"
            3 -> "😐"
            4 -> "🙂"
            5 -> "😄"
            else -> moodValue.toString()
        }
    }
}
