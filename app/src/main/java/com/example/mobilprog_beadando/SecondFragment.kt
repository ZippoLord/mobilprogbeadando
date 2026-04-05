package com.example.mobilprog_beadando

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilprog_beadando.data.viewModel.HistoryViewModel
import com.example.mobilprog_beadando.ui.adapters.HistoryAdapter
import kotlinx.coroutines.flow.collectLatest

class SecondFragment : Fragment(R.layout.fragment_second) {

    private val historyViewModel: HistoryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.historyList)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // figyeljük a history flow-t
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            historyViewModel.history.collectLatest { list ->
                recycler.adapter = HistoryAdapter(list)
            }
        }

        // indítsuk el a lekérést
        historyViewModel.loadHistory()
    }
}
