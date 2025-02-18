package com.example.buddybloom.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.buddybloom.data.model.PlantHistory
import com.example.buddybloom.data.repository.PlantRepository

class HistoryViewModel : ViewModel() {
    private val plantRepo = PlantRepository()

    private var _historyItems = plantRepo.getPlantHistoryLiveData()
    val historyItems: LiveData<List<PlantHistory>> get() = _historyItems
}