package com.example.buddybloom.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddybloom.data.model.PlantHistory
import com.example.buddybloom.data.repository.PlantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val plantRepo = PlantRepository()

    private val _historyItems = MutableLiveData<List<PlantHistory>>()
    val historyItems: LiveData<List<PlantHistory>> get() = _historyItems

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage


    /**
     * Loads the plant history from the repository.
     * This function fetches the history data from Firestore and updates the LiveData objects.
     */
    fun loadHistory() {


        viewModelScope.launch(Dispatchers.IO) {
            plantRepo.getPlantHistory(
                onSuccess = { historyList ->
                    _historyItems.postValue(historyList)

                },
                onFailure = { error ->
                    _errorMessage.postValue("Error loading history: ${error.message}")

                }
            )
        }
    }
}