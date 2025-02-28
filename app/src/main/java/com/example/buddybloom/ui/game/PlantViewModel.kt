package com.example.buddybloom.ui.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddybloom.data.GameManager
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.repository.PlantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlantViewModel : ViewModel() {
    private val plantRepository = PlantRepository()

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
            } else {
                updateRemotePlant(plant)
            }
        },
        onAutoSave = { plant ->
            updateRemotePlant(plant)
        }
    )

    fun resetPlantDeathState() {
        _plantJustDied.postValue(false)
    }

    /** Delete plant is firestore */
    private fun deletePlantFromRemote() {
        viewModelScope.launch(Dispatchers.IO) {
            plantRepository.deletePlant() { error ->
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
            plantRepository.savePlantToHistory(plant) { error ->
                _errorMessage.postValue("Failed to save plant history: ${error.message}")
            }
        }
    }

    /**
     * Updates the fields of the current plant on Firestore.
     */
    private fun updateRemotePlant(plant: Plant) {
        viewModelScope.launch(Dispatchers.IO) {
            plantRepository.updateRemotePlant(plant, onFailure = { error ->
                _errorMessage.postValue(error.message)
            })
        }
    }

    init {
        syncPlantFromRemote()
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
            val fetchedPlant = plantRepository.fetchPlant { error ->
                _errorMessage.postValue(error.message)
            }
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
                    plantRepository.updateRemotePlant(it) { error ->
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
            plantRepository.savePlant(plant) { error ->
                _errorMessage.postValue(error.message)
            }
            gameManager.updateLocalPlant(plant)
        }
    }

    fun waterPlant() {
        gameManager.waterPlant()
    }

    fun waterSpray(){
        gameManager.sprayWaterPlant()
    }

    fun fertilizePlant() {
        gameManager.addFertilizer()
    }

    fun sprayOnBugs() {
        gameManager.plantGetFreeFromBugs()

    }
    /**
     * Takes currentPlant and if waterLevel is under 10 a toast will appear for user
     */
    fun isPlantThirsty(): Boolean {
        return _localSessionPlant.value?.let { plant ->
            plant.waterLevel < 30
        } ?: false
    }
}