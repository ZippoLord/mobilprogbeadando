package com.example.mobilprog_beadando

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.utils.AppSettings
import com.example.mobilprog_beadando.data.utils.LocationHelper
import com.example.mobilprog_beadando.data.utils.TemperatureFormatter
import com.example.mobilprog_beadando.data.viewModel.WeatherViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirstFragment : Fragment(R.layout.fragment_first) {

    private val weatherViewModel: WeatherViewModel by activityViewModels()
    private val prefs by lazy {
        requireContext().getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
    }

    private lateinit var textCity: TextView
    private lateinit var textTemp: TextView
    private lateinit var textWeather: TextView

    companion object {
        private const val KEY_LAST_MOOD_DAY = "last_mood_day"
        private const val KEY_LAST_MOOD_VALUE = "last_mood_value"
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textCity = view.findViewById(R.id.textCity)
        textTemp = view.findViewById(R.id.textTemp)
        textWeather = view.findViewById(R.id.textWeather)

        val locationHelper = LocationHelper(requireActivity())

        locationHelper.getCityName { city ->
            Log.d("CITY_DEBUG", "Lekért város: $city")
            textCity.text = city
            weatherViewModel.loadWeather(city)
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            weatherViewModel.weather.collectLatest { weather ->
                if (weather != null) {
                    textCity.text = weather.cityName
                    textTemp.text = TemperatureFormatter.format(requireContext(), weather.temperature)
                    textWeather.text = weather.weatherType
                    AppSettings.saveLatestWeather(
                        requireContext(),
                        weather.cityName,
                        weather.weatherType,
                        weather.temperature
                    )
                }
            }
        }

        Log.d("CITY_DEBUG", "Lekért város: '$textCity'")
        view.findViewById<Button>(R.id.emoji1).setOnClickListener { saveMood(1)}
        view.findViewById<Button>(R.id.emoji2).setOnClickListener { saveMood(2) }
        view.findViewById<Button>(R.id.emoji3).setOnClickListener { saveMood(3) }
        view.findViewById<Button>(R.id.emoji4).setOnClickListener { saveMood(4) }
        view.findViewById<Button>(R.id.emoji5).setOnClickListener { saveMood(5) }

        applyMoodStateForToday()

        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                val city = textCity.text.toString()
                if (city.isNotBlank()) {
                    weatherViewModel.loadWeather(city)
                    Log.d("WEATHER_REFRESH", "Időjárás frissítve: $city")
                }
                delay(60_000)
            }
        }
    }

    private fun saveMood(moodValue: Int) {
        if (hasSubmittedMoodToday()) {
            val savedMood = prefs.getInt(KEY_LAST_MOOD_VALUE, moodValue)
            showMoodResult(savedMood)
            Toast.makeText(requireContext(), getString(R.string.home_already_selected_today), Toast.LENGTH_SHORT).show()
            return
        }

        val weather = weatherViewModel.weather.value ?: return

        val date = System.currentTimeMillis()

        val moodEntry = MoodEntry(
            date = date,
            moodValue = moodValue,
            temperature = weather.temperature,
            weatherType = weather.weatherType,
            cityName = weather.cityName
        )

        weatherViewModel.sendMood(moodEntry)
        rememberTodayMood(moodValue)
        MoodWidgetProvider.updateAllWidgets(requireContext())
        showMoodResult(moodValue)
    }

    private fun hasSubmittedMoodToday(): Boolean {
        return prefs.getString(KEY_LAST_MOOD_DAY, null) == getTodayKey()
    }

    private fun rememberTodayMood(moodValue: Int) {
        prefs.edit()
            .putString(KEY_LAST_MOOD_DAY, getTodayKey())
            .putInt(KEY_LAST_MOOD_VALUE, moodValue)
            .apply()
    }

    private fun applyMoodStateForToday() {
        if (hasSubmittedMoodToday()) {
            showMoodResult(prefs.getInt(KEY_LAST_MOOD_VALUE, 3))
            return
        }

        val moodSelector = view?.findViewById<View>(R.id.moodSelector)
        val moodResult = view?.findViewById<View>(R.id.moodResult)
        val moodTitle = view?.findViewById<TextView>(R.id.moodTitle)

        moodSelector?.visibility = View.VISIBLE
        moodResult?.visibility = View.GONE
        moodTitle?.visibility = View.VISIBLE
    }

    private fun getTodayKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun showMoodResult(moodValue: Int) {
        val moodSelector = view?.findViewById<View>(R.id.moodSelector)
        val moodResult = view?.findViewById<View>(R.id.moodResult)
        val moodEmoji = view?.findViewById<TextView>(R.id.moodEmoji)
        val moodText = view?.findViewById<TextView>(R.id.moodText)
        val moodTitle = view?.findViewById<TextView>(R.id.moodTitle)
        val emoji = when (moodValue) {
            1 -> "😢"
            2 -> "☹️"
            3 -> "😐"
            4 -> "🙂"
            5 -> "😄"
            else -> "❓"
        }
        val text = when (moodValue) {
            1 -> getString(R.string.home_mood_result_1)
            2 -> getString(R.string.home_mood_result_2)
            3 -> getString(R.string.home_mood_result_3)
            4 -> getString(R.string.home_mood_result_4)
            5 -> getString(R.string.home_mood_result_5)
            else -> ""
        }

        moodEmoji?.text = emoji
        moodText?.text = text
        moodSelector?.visibility = View.GONE
        moodResult?.visibility = View.VISIBLE
        moodTitle?.visibility = View.GONE
    }

}

