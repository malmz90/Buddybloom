package com.example.buddybloom.ui.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddybloom.data.GameManager
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.model.WeatherReport
import com.example.buddybloom.data.repository.PlantRepository
import com.example.buddybloom.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlantViewModel : ViewModel() {
    private val plantRepository = PlantRepository()
    private val weatherRepository = WeatherRepository()

    private val _localSessionPlant = MutableLiveData<Plant?>()
    val localSessionPlant: LiveData<Plant?> get() = _localSessionPlant

    private val _plantJustDied = MutableLiveData(false)
    val plantJustDied: LiveData<Boolean> get() = _plantJustDied

    //TODO Make sure UI observes these (show a toast?)
    /**
     * Potential error messages from Firestore or GameManager.
     */
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        syncPlantFromRemote()
    }

    private val gameManager = GameManager(
        scope = viewModelScope,
        onPlantEvent = { plant ->
            _localSessionPlant.postValue(plant)
            if (plant == null && _localSessionPlant.value != null) {
                // The plant was not null before, but now it is - this means it just died
                _plantJustDied.postValue(true)
                // Save the dead plant to history before it's deleted
                _localSessionPlant.value?.let { deadPlant ->
                    savePlantToHistoryRemote(deadPlant)
                }
            }
            if (plant == null) {
                deletePlantFromRemote() // Delete plant when it dies
            }
        },
        onAutoSave = { plant ->
            updateRemotePlant(plant)
        }
    )

    fun resetPlantDeathState() {
        _plantJustDied.postValue(false)
    }

    fun resetErrorMessage() {
        _errorMessage.postValue(null)
    }

    /** Delete plant on Firestore */
    private fun deletePlantFromRemote() {
        viewModelScope.launch(Dispatchers.IO) {
            plantRepository.deletePlant().onFailure { error ->
                _errorMessage.postValue(error.message)
            }
        }
    }

    /**
     * Saves the dead plant to history by calling the repository method.
     * This function is called when a plant dies
     */
    private fun savePlantToHistoryRemote(plant: Plant) {
        viewModelScope.launch(Dispatchers.IO) {
            plantRepository.savePlantToHistory(plant).onFailure { error ->
                _errorMessage.postValue(error.message)
            }
        }
    }

    /**
     * Updates the fields of the current plant on Firestore.
     */
    private fun updateRemotePlant(plant: Plant) {
        viewModelScope.launch(Dispatchers.IO) {
            plantRepository.updatePlant(plant).onFailure { error ->
                _errorMessage.postValue(error.message)
            }
        }
    }


    fun refreshPlant() {
        syncPlantFromRemote()
    }

    /**
     * Fetches the current plant from the remote and checks how many hours have passed since last update.
     * Passes game time for each missing hour and then updates the local game session, live data and on remote.
     */
    //TODO Move as much as possible of this into the game engine?
    private fun syncPlantFromRemote() {
        viewModelScope.launch(Dispatchers.IO) {
            //Get the plant currently stored on Firebase
            val fetchedPlant = plantRepository.fetchPlant().onFailure { error ->
                _errorMessage.postValue(error.message)
            }.getOrNull()
            //Update the (local) session data
            gameManager.updateLocalPlant(fetchedPlant)
            fetchedPlant?.let {
                val hoursSinceLastUpdate =
                    ((System.currentTimeMillis() - (it.lastUpdated.seconds * 1000)) / (1000 * 60 * 60)).toInt()
                Log.i("GameEngine", "Hours since last update: $hoursSinceLastUpdate")
                //Calculate game events since last session
                if (hoursSinceLastUpdate > 0) {
                    gameManager.runGameLoop(hoursSinceLastUpdate)
                    //Update the plant on Firestore again
                    plantRepository.updatePlant(it).onFailure { error ->
                        _errorMessage.postValue(error.message)
                    }
                }
            }
        }
    }

    /**
     * Overwrites the plant stored on remote with a new plant.
     */
    fun savePlantToRemote(plant: Plant) {
        viewModelScope.launch(Dispatchers.IO) {
            plantRepository.savePlant(plant).onFailure { error ->
                _errorMessage.postValue(error.message)
            }
            gameManager.updateLocalPlant(plant)
        }
    }

    fun waterPlant() {
        gameManager.waterPlant()
    }

    fun waterSpray() {
        gameManager.sprayWaterPlant()
    }

    fun fertilizePlant() {
        gameManager.addFertilizer()
    }

    fun sprayOnBugs() {
        gameManager.plantGetFreeFromBugs()
    }

    fun toggleBlinds() {
        gameManager.toggleBlinds()
    }

    /**
     * Takes currentPlant and if waterLevel is under 10 a toast will appear for user
     */
    fun isPlantThirsty(): Boolean {
        return _localSessionPlant.value?.let { plant ->
            plant.waterLevel < 30
        } ?: false
    }

    //--------------------------------------WEATHER STUFF-------------------------------------------

    private val _currentWeatherReport = MutableLiveData<WeatherReport.Daily>()
    val currentWeatherReport: LiveData<WeatherReport.Daily> get() = _currentWeatherReport

    fun fetchOrCreateDailyReport() {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.fetchOrCreateDailyReport()
                .onSuccess { report ->
                    _currentWeatherReport.postValue(report)
                    gameManager.updateLocalDailyWeather(report)
                }.onFailure { error ->
                    _errorMessage.postValue(error.message)
                }
        }
    }
}


//TODO Move this logic into the game engine
/**
 * Checks difficulty on current Plant and increasing water level by different of difficulty
 */
//    fun checkDifficultyWaterSpray() {
//        _currentPlant.value?.let { plant ->
//            if (plant.difficulty == "Easy") {
//                increaseWaterLevel(2)
//                Log.d("PlantVM", "Water level increased by 2")
//            } else if (plant.difficulty == "Medium") {
//                increaseWaterLevel(5)
//                Log.d("PlantVM", "Water level increased by 5")
//            } else if (plant.difficulty == "Hard") {
//                increaseWaterLevel(8)
//                Log.d("PlantVM", "Water level increased by 8")
//            } else {
//                Log.d("PlantVM", "No plant Found")
//            }
//        }
//    }

//TODO Move this logic into the game engine
/**
 * Checks difficulty on current Plant and decrease FertilizerLevel level by different of difficulty
 */
//    fun checkDifficultyFertilizeDecrease() {
//        _currentPlant.value?.let { plant ->
//            if (plant.difficulty == "Easy") {
//                plantRepository.decreaseFertilizer(plant, 1)
//                Log.d("PlantVM", "Nutrition decreases by amount 1")
//            } else if (plant.difficulty == "Medium") {
//                plantRepository.decreaseFertilizer(plant, 2)
//                Log.d("PlantVM", "Nutrition decreases by amount 2")
//            } else if (plant.difficulty == "Hard") {
//                plantRepository.decreaseFertilizer(plant, 5)
//                Log.d("PlantVM", "Nutrition decreases by amount 5")
//            } else {
//                Log.d("PlantVM", "No plant Found")
//            }
//        }
//    }
