package com.example.mobilprog_beadando

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.repository.MoodRepository
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.util.Locale

class ThirdFragment : Fragment(R.layout.fragment_third) {

	private val moodRepository = MoodRepository()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val weeklyAverageValue = view.findViewById<TextView>(R.id.weeklyAverageValue)
		val bestWeatherValue = view.findViewById<TextView>(R.id.bestWeatherValue)
		val temperatureAverageValue = view.findViewById<TextView>(R.id.temperatureAverageValue)
		val moodDistributionChart = view.findViewById<BarChart>(R.id.moodDistributionChart)
		val temperatureMoodChart = view.findViewById<LineChart>(R.id.temperatureMoodChart)
		val statsInfo = view.findViewById<TextView>(R.id.statsInfo)

		viewLifecycleOwner.lifecycleScope.launch {
			try {
				val entries = moodRepository.getAllMood()
				if (entries.isEmpty()) {
					statsInfo.text = getString(R.string.stats_no_entries)
					return@launch
				}

				statsInfo.text = getString(R.string.stats_loaded)
				renderWeeklyAverage(entries, weeklyAverageValue)
				renderMoodDistribution(entries, moodDistributionChart)
				renderBestWeatherForGoodMood(entries, bestWeatherValue)
				renderMoodByTemperature(entries, temperatureAverageValue, temperatureMoodChart)
			} catch (e: Exception) {
				statsInfo.text = getString(R.string.stats_load_failed)
			}
		}
	}

	private fun renderWeeklyAverage(entries: List<MoodEntry>, textView: TextView) {
		val now = System.currentTimeMillis()
		val sevenDaysAgo = now - 7L * 24L * 60L * 60L * 1000L
		val weeklyItems = entries.filter { it.date >= sevenDaysAgo }

		if (weeklyItems.isEmpty()) {
			textView.text = getString(R.string.stats_no_last_7_days)
			return
		}

		val avg = weeklyItems.map { it.moodValue }.average()
		textView.text = getString(R.string.stats_weekly_avg_value, avg)
	}

	private fun renderMoodDistribution(entries: List<MoodEntry>, chart: BarChart) {
		val counts = (1..5).associateWith { mood -> entries.count { it.moodValue == mood } }
		val barEntries = (1..5).map { mood ->
			BarEntry(mood.toFloat(), (counts[mood] ?: 0).toFloat())
		}

		val set = BarDataSet(barEntries, getString(R.string.stats_distribution_dataset))
		set.color = Color.parseColor("#0EA5E9")
		set.valueTextColor = Color.parseColor("#0F172A")
		set.valueTextSize = 12f

		chart.data = BarData(set).apply { barWidth = 0.55f }
		chart.description.isEnabled = false
		chart.legend.isEnabled = false
		chart.setFitBars(true)
		chart.axisRight.isEnabled = false
		chart.axisLeft.axisMinimum = 0f
		chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
		chart.xAxis.granularity = 1f
		chart.xAxis.valueFormatter = object : ValueFormatter() {
			override fun getFormattedValue(value: Float): String {
				return when (value.toInt()) {
					1 -> "😢"
					2 -> "☹️"
					3 -> "😐"
					4 -> "🙂"
					5 -> "😄"
					else -> ""
				}
			}
		}
		chart.animateY(600)
		chart.invalidate()
	}

	private fun renderBestWeatherForGoodMood(entries: List<MoodEntry>, textView: TextView) {
		val goodMoodEntries = entries.filter { it.moodValue >= 4 }
		if (goodMoodEntries.isEmpty()) {
			textView.text = getString(R.string.stats_no_good_mood_data)
			return
		}

		val best = goodMoodEntries
			.groupingBy { it.weatherType }
			.eachCount()
			.maxByOrNull { it.value }

		if (best == null) {
			textView.text = getString(R.string.stats_no_evaluable_data)
			return
		}

		textView.text = getString(R.string.stats_best_weather_value, best.key, best.value)
	}

	private fun renderMoodByTemperature(
		entries: List<MoodEntry>,
		textView: TextView,
		chart: LineChart
	) {
		val buckets = linkedMapOf(
			getString(R.string.stats_bucket_0_5) to mutableListOf<Int>(),
			getString(R.string.stats_bucket_6_15) to mutableListOf<Int>(),
			getString(R.string.stats_bucket_16_25) to mutableListOf<Int>(),
			getString(R.string.stats_bucket_26_plus) to mutableListOf<Int>()
		)

		val bucket0_5 = getString(R.string.stats_bucket_0_5)
		val bucket6_15 = getString(R.string.stats_bucket_6_15)
		val bucket16_25 = getString(R.string.stats_bucket_16_25)
		val bucket26Plus = getString(R.string.stats_bucket_26_plus)

		entries.forEach { item ->
			when {
				item.temperature <= 5 -> buckets[bucket0_5]?.add(item.moodValue)
				item.temperature <= 15 -> buckets[bucket6_15]?.add(item.moodValue)
				item.temperature <= 25 -> buckets[bucket16_25]?.add(item.moodValue)
				else -> buckets[bucket26Plus]?.add(item.moodValue)
			}
		}

		val averages = buckets.mapValues { (_, moods) ->
			if (moods.isEmpty()) 0.0 else moods.average()
		}

		val summary = averages.entries
			.filter { it.value > 0.0 }
			.joinToString(" | ") { (label, value) ->
				getString(R.string.stats_bucket_summary_item, label, value)
			}

		textView.text = if (summary.isBlank()) getString(R.string.stats_no_temp_data) else summary

		val labels = buckets.keys.toList()
		val points = labels.mapIndexed { index, label ->
			Entry(index.toFloat(), averages[label]!!.toFloat())
		}

		val lineSet = LineDataSet(points, getString(R.string.stats_avg_mood_dataset))
		lineSet.color = Color.parseColor("#16A34A")
		lineSet.valueTextColor = Color.parseColor("#0F172A")
		lineSet.valueTextSize = 11f
		lineSet.circleRadius = 4f
		lineSet.setCircleColor(Color.parseColor("#16A34A"))
		lineSet.lineWidth = 2.2f

		chart.data = LineData(lineSet)
		chart.description.isEnabled = false
		chart.legend.isEnabled = false
		chart.axisRight.isEnabled = false
		chart.axisLeft.axisMinimum = 0f
		chart.axisLeft.axisMaximum = 5.2f
		chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
		chart.xAxis.granularity = 1f
		chart.xAxis.labelCount = labels.size
		chart.xAxis.valueFormatter = object : ValueFormatter() {
			override fun getFormattedValue(value: Float): String {
				val index = value.toInt()
				return if (index in labels.indices) labels[index] else ""
			}
		}
		chart.animateX(600)
		chart.invalidate()
	}
}