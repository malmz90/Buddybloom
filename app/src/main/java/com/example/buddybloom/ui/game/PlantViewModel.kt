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

    private val _plantDiedFromOverWatering = MutableLiveData(false)
    val plantDiedFromOverWatering: LiveData<Boolean> get() = _plantDiedFromOverWatering

    private val maxWaterLevel = 100

    //TODO Make sure UI observes these (show a toast?)
    /**
     * Potential error messages from Firestore or GameManager.
     */
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        syncPlantFromRemote()
        fetchOrCreateDailyReport()
    }

    private val gameManager = GameManager(
        scope = viewModelScope,
        onPlantEvent = { plant ->
//-------------------how it was before----------------------------
//            _localSessionPlant.postValue(plant)
//            if (plant == null && _localSessionPlant.value != null) {
//                // The plant was not null before, but now it is - this means it just died
//                _plantJustDied.postValue(true)
//                // Save the dead plant to history before it's deleted
//                _localSessionPlant.value?.let { deadPlant ->
//                    savePlantToHistoryRemote(deadPlant)
//                }
//            }
//            if (plant == null) {
//                deletePlantFromRemote() // Delete plant when it dies
//            }

// -------------- new to kill and save plant when it dies by different cause
            _localSessionPlant.postValue(plant)
            if (plant == null && _localSessionPlant.value != null) {
                val lastWaterLevel = _localSessionPlant.value?.waterLevel ?: 0
                if (lastWaterLevel > maxWaterLevel) {
                    Log.d("PlantViewModel", "Plant died from overwatering!")
                    _plantDiedFromOverWatering.postValue(true)
                    _localSessionPlant.value?.let { deadPlant ->
                        savePlantToHistoryRemote(deadPlant)
                        deletePlantFromRemote() // Delete plant when it dies
                    }
                } else {
                    Log.d("PlantViewModel", "Plant died from lack of water or nutrients!")
                    // The plant was not null before, but now it is - this means it just died
                    _plantJustDied.postValue(true)
                    _localSessionPlant.value?.let { deadPlant ->
                        savePlantToHistoryRemote(deadPlant)
                        deletePlantFromRemote() // Delete plant when it dies
                    }
                }
//                _localSessionPlant.value?.let { deadPlant ->
//                    savePlantToHistoryRemote(deadPlant)
//                    deletePlantFromRemote() // Delete plant when it dies
//                }
            }
        },
        onAutoSave = { plant ->
            updateRemotePlant(plant)
        }
)
    /** Reset death to start gameLoop */
    fun resetPlantDeath() {
        gameManager.resetPlantDeathState()
    }
    fun resetPlantDeathState() {
        _plantJustDied.postValue(false)
    }
    fun resetOverWateringDeathState() {
        _plantDiedFromOverWatering.postValue(false)
    }

    /** Delete plant on Firestore */
    private fun deletePlantFromRemote() {
        viewModelScope.launch(Dispatchers.IO) {
            plantRepository.deletePlant().onSuccess {
                _localSessionPlant.postValue(null) // dead plant removes from memory and don´t follow in to newplant
            }.onFailure { error ->
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
            val newPlant = Plant(
                name = plant.name,
                waterLevel = 100, // Start value for every new plant
                fertilizerLevel = 100,
                sunLevel = 100,
                infected = false,
                createdAt = com.google.firebase.Timestamp.now(),
                lastUpdated = com.google.firebase.Timestamp.now(),
                protectedFromSun = false
            )
            plantRepository.savePlant(newPlant).onSuccess {
                gameManager.updateLocalPlant(newPlant)
                _localSessionPlant.postValue(newPlant) // Updates LiveData with new plant
            }.onFailure { error ->
                _errorMessage.postValue(error.message)
            }
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

    private fun fetchOrCreateDailyReport() {
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