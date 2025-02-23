package com.example.buddybloom.ui.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddybloom.data.Game
import com.example.buddybloom.data.model.LocalGameState
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.repository.PlantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlantViewModel : ViewModel() {
    private val plantRepository = PlantRepository()
    private val game = Game(LocalGameState)

    private val _selectedPlant = MutableLiveData<Plant?>()
    val selectedPlant: LiveData<Plant?> get() = _selectedPlant

    private val _currentPlant = MutableLiveData<Plant?>()
    val currentPlant: LiveData<Plant?> get() = _currentPlant

    private val _testPlant = MutableLiveData<Plant?>()
    val testPlant: LiveData<Plant?> get() = _testPlant

    //TODO Make sure UI listens to these (show a toast?)
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        startGameLoop()
        startAutoSave()
        syncPlantFromRemote()
//        plantRepository.snapshotOfCurrentUserPlant { plant ->
//            _currentPlant.value = plant
//        }
    }

    private fun startAutoSave() {
        viewModelScope.launch {
            while (true) {
                delay(Game.AUTO_SAVE_TIMER)
                withContext(Dispatchers.IO) {
                    game.state.plant?.let {
                        updatePlantOnRemote(it)
                    }
                }
            }
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (true) {
                delay(Game.GAME_LOOP_TIMER)
                game.runGameLoop()
                updatePlantLiveData()
            }
        }
    }

    private fun syncPlantFromRemote() {
        viewModelScope.launch(Dispatchers.IO) {
            val fetchedPlant = plantRepository.fetchPlant { error ->
                _errorMessage.postValue(error.message)
            }
            withContext(Dispatchers.Main) {
                game.setPlant(fetchedPlant)
                game.state.plant?.let {
                    val hoursSinceLastUpdate =
                        ((System.currentTimeMillis() - (it.lastUpdated.seconds * 1000)) / (1000 * 60 * 60)).toInt()
                    if (hoursSinceLastUpdate > 0) {
                        game.runGameLoop(hoursSinceLastUpdate)
                    }
                }
                updatePlantLiveData()
            }
        }
    }

    fun savePlantToRemote(plant: Plant) {
        viewModelScope.launch(Dispatchers.IO) {
            plantRepository.savePlant(plant) { error ->
                _errorMessage.postValue(error.message)
            }
        }
    }

    private suspend fun updatePlantOnRemote(plant: Plant) {
        plantRepository.updatePlant(plant) { error ->
            _errorMessage.postValue(error.message)
        }
    }

    fun setUserId(newId: String?) {
        game.setUserId(newId)
    }

    fun setPlant(newPlant: Plant?) {
        game.setPlant(newPlant)
        updatePlantLiveData()
    }

    fun waterPlant() {
        game.waterPlant()
        updatePlantLiveData()
    }


    fun addFertilizer() {
        game.addFertilizer()
        updatePlantLiveData()
    }


    private fun updatePlantLiveData() {
        _testPlant.postValue(game.state.plant)
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

    /**
     * Takes currentPlant and if waterLevel is under 10 a toast will appear for user
     */
    fun isPlantThirsty(): Boolean {
        return _currentPlant.value?.let { plant ->
            plant.waterLevel < 10
        } ?: false
    }

    /**
     * Checks difficulty on current Plant and increasing water level by different of difficulty
     */
    fun checkDifficultyWaterSpray() {
        _currentPlant.value?.let { plant ->
            if (plant.difficulty == "Easy") {
                increaseWaterLevel(2)
                Log.d("PlantVM", "Water level increased by 2")
            } else if (plant.difficulty == "Medium") {
                increaseWaterLevel(5)
                Log.d("PlantVM", "Water level increased by 5")
            } else if (plant.difficulty == "Hard") {
                increaseWaterLevel(8)
                Log.d("PlantVM", "Water level increased by 8")
            } else {
                Log.d("PlantVM", "No plant Found")
            }
        }

    }

    /**
     * Checks difficulty on current Plant and decrease FertilizerLevel level by different of difficulty
     */
    fun checkDifficultyFertilizeDecrease() {
        _currentPlant.value?.let { plant ->
            if (plant.difficulty == "Easy") {
                plantRepository.decreaseFertilizer(plant, 1)
                Log.d("PlantVM", "Nutrition decreases by amount 1")
            } else if (plant.difficulty == "Medium") {
                plantRepository.decreaseFertilizer(plant, 2)
                Log.d("PlantVM", "Nutrition decreases by amount 2")
            } else if (plant.difficulty == "Hard") {
                plantRepository.decreaseFertilizer(plant, 5)
                Log.d("PlantVM", "Nutrition decreases by amount 5")
            } else {
                Log.d("PlantVM", "No plant Found")
            }
        }
    }

    /**
     * Takes userplant and increases fertilizerLevel, amount sets in StartPagePlantFragment
     * and drives when user presses the fertilize button
     */
    fun increaseFertilizeLevel(amount: Int) {
        _currentPlant.value?.let { plant ->
            plant.fertilizerLevel = minOf(100, plant.waterLevel + amount)
            plant.fertilizerLevel += amount
            Log.d("PlantStatus", "Your Plant increased Nutrition by $amount!")
        }
    }

    /**
     * Takes userplant and increases WaterLevel, amount sets in StartPagePlantFragment
     * and drives when user presses the Water button
     */
    fun increaseWaterLevel(amount: Int) {
        _selectedPlant.value?.let { plant ->
            plant.waterLevel = minOf(100, plant.waterLevel + amount)
            _selectedPlant.postValue(plant)
            // savePlantForCurrentUser()
            Log.d("PlantVM", "Water level increased by $amount")
        } ?: Log.e("PlantVM", "No plant selected to update water level")
    }


}