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

    private var isUpdating = false

    init {
        plantRepository.snapshotOfCurrentUserPlant { plant ->
            _currentPlant.value = plant
        }
    }
   fun checkAndUpdateWaterLevel() {
        _currentPlant.value?.let { plant ->
            plant.updateWaterBasedOnTimePassed()
            plantRepository.saveUserPlant(plant) { success ->
                if (success) {
                    Log.d("PlantVM", "Plant saved with updated water level")
                }
            }
        }
    }

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
        _currentPlant.value?.let { plant ->
            plant.waterLevel = minOf(100, plant.waterLevel + amount)
            plant.lastWaterUpdate = System.currentTimeMillis()
            plantRepository.saveUserPlant(plant) { success ->
                if (success) {
                    Log.d("PlantVM", "Water level increased by $amount")
                }
            }
        }
    }
}
    

