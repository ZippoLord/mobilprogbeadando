package com.example.mobilprog_beadando

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.viewModel.HistoryViewModel
import com.example.mobilprog_beadando.ui.adapters.HistoryAdapter
import kotlinx.coroutines.flow.collectLatest

class SecondFragment : Fragment(R.layout.fragment_second) {

    private val historyViewModel: HistoryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.historyList)
        val searchDateInput = view.findViewById<EditText>(R.id.searchDateInput)
        val moodFilterSpinner = view.findViewById<Spinner>(R.id.moodFilterSpinner)
        val applyFilterButton = view.findViewById<Button>(R.id.applyFilterButton)
        val clearFilterButton = view.findViewById<Button>(R.id.clearFilterButton)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        val adapter = HistoryAdapter(
            items = emptyList(),
            onDeleteClick = { entry -> showDeleteConfirmation(entry) }
        )
        recycler.adapter = adapter

        moodFilterSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                getString(R.string.history_filter_all_moods),
                getString(R.string.history_mood_1),
                getString(R.string.history_mood_2),
                getString(R.string.history_mood_3),
                getString(R.string.history_mood_4),
                getString(R.string.history_mood_5)
            )
        )

        
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            historyViewModel.history.collectLatest { list ->
                adapter.submitList(list)
            }
        }

        applyFilterButton.setOnClickListener {
            val dateQuery = searchDateInput.text.toString()
            val moodFilter = selectedMoodValue(moodFilterSpinner.selectedItemPosition)

            historyViewModel.updateDateQuery(dateQuery)
            historyViewModel.updateMoodFilter(moodFilter)
        }

        clearFilterButton.setOnClickListener {
            searchDateInput.setText("")
            moodFilterSpinner.setSelection(0)

            historyViewModel.updateDateQuery("")
            historyViewModel.updateMoodFilter(null)
        }

        
        historyViewModel.loadHistory()
    }

    private fun selectedMoodValue(selectedPosition: Int): Int? {
        return when (selectedPosition) {
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 4
            5 -> 5
            else -> null
        }
    }

    private fun showDeleteConfirmation(entry: MoodEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.history_delete_confirm_title))
            .setMessage(getString(R.string.history_delete_confirm_message))
            .setNegativeButton(getString(R.string.history_delete_cancel), null)
            .setPositiveButton(getString(R.string.history_delete_confirm)) { _, _ ->
                historyViewModel.deleteEntry(entry)
            }
            .show()
    }
}
