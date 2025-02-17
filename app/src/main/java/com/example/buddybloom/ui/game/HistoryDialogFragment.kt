package com.example.buddybloom.ui.game

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.model.PlantHistory
import com.example.buddybloom.data.repository.PlantRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class HistoryDialogFragment : DialogFragment(R.layout.dialog_history) {
    private lateinit var placeHolderTextView: MaterialTextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: HistoryViewModel
    private lateinit var closeButton: MaterialButton

    init {
        createMockData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        placeHolderTextView = view.findViewById(R.id.tv_empty_history)
        closeButton = view.findViewById(R.id.btn_history_close)
        recyclerView = view.findViewById(R.id.rv_history)

        val adapter = HistoryAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        viewModel.historyItems.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                recyclerView.visibility = View.GONE
                placeHolderTextView.visibility = View.VISIBLE
            } else {
                placeHolderTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.update(it)
            }
        }
        closeButton.setOnClickListener { dismiss() }
    }

    private fun createMockData() {
        val plantRepository = PlantRepository()
        plantRepository.savePlantHistory(Plant("Hillibiskus", streakDays = 5), {}, {})
        plantRepository.savePlantHistory(Plant("Elefantianus", streakDays = 112), {}, {})
        plantRepository.savePlantHistory(Plant("Zebraskopus", streakDays = 19), {}, {})
    }

}