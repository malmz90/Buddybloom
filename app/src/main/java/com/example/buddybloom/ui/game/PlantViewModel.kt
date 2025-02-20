package com.example.buddybloom.ui.game

import android.util.Log
import android.widget.Toast
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

    /**
     * Checks difficulty on current Plant and increasing water level by different of difficulty
     */
    fun checkDifficultyWaterSpray(){
        _currentPlant.value?.let{plant->
            if (plant.difficulty == "Easy"){
                increaseWaterLevel(2)
                Log.d("PlantVM", "Water level increased by 2")
            }else if(plant.difficulty == "Medium"){
                increaseWaterLevel(5)
                Log.d("PlantVM", "Water level increased by 5")
            }else if(plant.difficulty == "Hard"){
                increaseWaterLevel(8)
                Log.d("PlantVM", "Water level increased by 8")
            }else {
                Log.d("PlantVM", "No plant Found")
            }
        }

    }
    fun checkDifficultyFertilizeDecrease(){
        _currentPlant.value?.let{plant->
            if (plant.difficulty == "Easy"){
                plantRepository.decreaseFertilizer(plant,1)
                Log.d("PlantVM", "Nutrition decreases by amount 1")
            }else if(plant.difficulty == "Medium"){
                plantRepository.decreaseFertilizer(plant,2)
                Log.d("PlantVM",  "Nutrition decreases by amount 2")
            }else if(plant.difficulty == "Hard"){
                plantRepository.decreaseFertilizer(plant,5)
                Log.d("PlantVM", "Nutrition decreases by amount 5")
            }else {
                Log.d("PlantVM", "No plant Found")
            }
        }
    }
    fun increaseFertilizeLevel(amount: Int) {
        _currentPlant.value?.let { plant ->
            plant.fertilizerLevel = minOf(100, plant.waterLevel + amount)
            plant.fertilizerLevel += amount
            Log.d("PlantStatus", "Your Plant increased Nutrition by $amount!")
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