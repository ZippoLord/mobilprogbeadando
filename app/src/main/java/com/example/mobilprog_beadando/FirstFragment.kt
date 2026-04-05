package com.example.mobilprog_beadando

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.utils.LocationHelper
import com.example.mobilprog_beadando.data.viewModel.WeatherViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FirstFragment : Fragment(R.layout.fragment_first) {

    private val weatherViewModel: WeatherViewModel by activityViewModels()

    private lateinit var textCity: TextView
    private lateinit var textTemp: TextView
    private lateinit var textWeather: TextView


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


        // API válasz figyelése
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            weatherViewModel.weather.collectLatest { weather ->
                if (weather != null) {
                    textCity.text = weather.cityName
                    textTemp.text = "${weather.temperature} °C"
                    textWeather.text = weather.weatherType
                }
            }
        }

        Log.d("CITY_DEBUG", "Lekért város: '$textCity'")
        view.findViewById<Button>(R.id.emoji1).setOnClickListener { saveMood(1)}
        view.findViewById<Button>(R.id.emoji2).setOnClickListener { saveMood(2) }
        view.findViewById<Button>(R.id.emoji3).setOnClickListener { saveMood(3) }
        view.findViewById<Button>(R.id.emoji4).setOnClickListener { saveMood(4) }
        view.findViewById<Button>(R.id.emoji5).setOnClickListener { saveMood(5) }

        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                val city = textCity.text.toString()
                if (city.isNotBlank()) {
                    weatherViewModel.loadWeather(city)
                    Log.d("WEATHER_REFRESH", "Időjárás frissítve: $city")
                }
                delay(60_000) // percenként frissít
            }
        }
    }

    private fun saveMood(moodValue: Int) {
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

        // UI váltás
        showMoodResult(moodValue)
    }

    private fun showMoodResult(moodValue: Int) {
        val moodSelector = view?.findViewById<View>(R.id.moodSelector)
        val moodResult = view?.findViewById<View>(R.id.moodResult)
        val moodEmoji = view?.findViewById<TextView>(R.id.moodEmoji)
        val moodText = view?.findViewById<TextView>(R.id.moodText)
        val moodTitle = view?.findViewById<TextView>(R.id.moodTitle)

        // 1–5 értékhez emoji
        val emoji = when (moodValue) {
            1 -> "😢"
            2 -> "☹️"
            3 -> "😐"
            4 -> "🙂"
            5 -> "😄"
            else -> "❓"
        }

        // Szöveg
        val text = when (moodValue) {
            1 -> "Ma szomorú vagy"
            2 -> "Ma rossz kedved van"
            3 -> "Ma átlagos napod van"
            4 -> "Ma jó kedved van"
            5 -> "Ma nagyon boldog vagy"
            else -> ""
        }

        moodEmoji?.text = emoji
        moodText?.text = text

        // Váltás
        moodSelector?.visibility = View.GONE
        moodResult?.visibility = View.VISIBLE
        moodTitle?.visibility = View.GONE
    }

}

