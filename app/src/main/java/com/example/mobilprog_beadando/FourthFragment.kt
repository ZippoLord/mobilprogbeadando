package com.example.mobilprog_beadando

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mobilprog_beadando.data.utils.AppSettings
import com.example.mobilprog_beadando.data.utils.TemperatureUnit

class FourthFragment : Fragment(R.layout.fragment_fourth) {

	private val requestLocationPermission = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { granted ->
		if (!granted) {
			view?.findViewById<Switch>(R.id.locationSwitch)?.isChecked = false
			Toast.makeText(requireContext(), getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val locationSwitch = view.findViewById<Switch>(R.id.locationSwitch)
		val languageSpinner = view.findViewById<Spinner>(R.id.languageSpinner)
		val temperatureSpinner = view.findViewById<Spinner>(R.id.temperatureSpinner)
		val saveButton = view.findViewById<Button>(R.id.saveSettingsButton)

		val languageItems = listOf(
			getString(R.string.language_hu),
			getString(R.string.language_en)
		)
		val languageCodes = listOf("hu", "en")
		languageSpinner.adapter = ArrayAdapter(
			requireContext(),
			android.R.layout.simple_spinner_dropdown_item,
			languageItems
		)

		val temperatureItems = listOf(
			getString(R.string.temperature_celsius),
			getString(R.string.temperature_fahrenheit)
		)
		temperatureSpinner.adapter = ArrayAdapter(
			requireContext(),
			android.R.layout.simple_spinner_dropdown_item,
			temperatureItems
		)

		locationSwitch.isChecked = hasLocationPermission()

		val currentLanguage = AppSettings.getLanguage(requireContext())
		languageSpinner.setSelection(languageCodes.indexOf(currentLanguage).coerceAtLeast(0))

		val currentUnit = AppSettings.getTemperatureUnit(requireContext())
		temperatureSpinner.setSelection(if (currentUnit == TemperatureUnit.CELSIUS) 0 else 1)

		locationSwitch.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked && !hasLocationPermission()) {
				requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
			}

			if (!isChecked && hasLocationPermission()) {
				Toast.makeText(
					requireContext(),
					getString(R.string.location_disable_in_settings),
					Toast.LENGTH_LONG
				).show()
				openAppSettings()
				locationSwitch.isChecked = true
			}
		}

		saveButton.setOnClickListener {
			val selectedLanguage = languageCodes[languageSpinner.selectedItemPosition]
			val selectedUnit = if (temperatureSpinner.selectedItemPosition == 0) {
				TemperatureUnit.CELSIUS
			} else {
				TemperatureUnit.FAHRENHEIT
			}

			AppSettings.setLanguage(requireContext(), selectedLanguage)
			AppSettings.setTemperatureUnit(requireContext(), selectedUnit)
			MoodWidgetProvider.updateAllWidgets(requireContext())

			Toast.makeText(requireContext(), getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
			requireActivity().recreate()
		}
	}

	private fun hasLocationPermission(): Boolean {
		val fine = ContextCompat.checkSelfPermission(
			requireContext(),
			Manifest.permission.ACCESS_FINE_LOCATION
		) == PackageManager.PERMISSION_GRANTED

		val coarse = ContextCompat.checkSelfPermission(
			requireContext(),
			Manifest.permission.ACCESS_COARSE_LOCATION
		) == PackageManager.PERMISSION_GRANTED

		return fine || coarse
	}

	private fun openAppSettings() {
		val intent = Intent(
			Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
			Uri.parse("package:${requireContext().packageName}")
		)
		startActivity(intent)
	}
}