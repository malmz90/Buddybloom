package com.example.buddybloom.ui.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.repository.PlantRepository

class PlantViewModel : ViewModel() {
    private val plantRepository = PlantRepository()
    private val _selectedPlant = MutableLiveData<Plant?>()
    val selectedPlant: LiveData<Plant?> get() = _selectedPlant
    private val _currentPlant = MutableLiveData<Plant?>()
    val currentPlant: LiveData<Plant?> get() = _currentPlant

    init {
        plantRepository.snapshotOfCurrentUserPlant { plant ->
            _currentPlant.value = plant
        }
    }

    //TODO ta bort?
    fun setSelectedPlant(plant: Plant) {
        _selectedPlant.value = plant
    }

    fun getCurrentUserPlant(onPlantFetched: (Plant?) -> Unit) {
        plantRepository.getCurrentUserPlant { plant: Plant? ->
            onPlantFetched(plant)
        }
    }

    fun savePlantForCurrentUser(plant: Plant, onPlantSaved: () -> Unit) {
        plantRepository.saveUserPlant(plant) { saved ->
            if (saved) onPlantSaved()
        }
    }

    fun increaseWaterLevel(amount: Int) {
        _selectedPlant.value?.let { plant ->
            plant.waterLevel = minOf(100, plant.waterLevel + amount)
            _selectedPlant.postValue(plant)
            // savePlantForCurrentUser()
            Log.d("PlantVM", "Water level increased by $amount")
        } ?: Log.e("PlantVM", "No plant selected to update water level")
    }
}