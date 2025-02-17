package com.example.buddybloom.ui.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.repository.PlantRepository
import com.google.firebase.auth.FirebaseAuth

class PlantViewModel : ViewModel() {
    private val plantRepository = PlantRepository()
    private val _selectedPlant = MutableLiveData<Plant?>()
    val selectedPlant: LiveData<Plant?> get() = _selectedPlant

    fun setSelectedPlant(plant: Plant) {
        _selectedPlant.value = plant
    }

    fun getCurrentUserPlant() {
        plantRepository.getCurrentUserPlant { plant ->
            if (plant != null) {
                _selectedPlant.postValue(plant)
                Log.d("PlantVM", "Plant loaded from Firebase")
            } else {
                Log.d("PlantVM", "No plant found")
            }
        }
    }

    fun savePlantForCurrentUser() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            _selectedPlant.value?.let { plant ->
                plantRepository.saveUserPlant(userId, plant) { success ->
                    if (success) {
                        Log.d("PlantVM", "Plant saved for user $userId")
                    } else {
                        Log.d("PlantVM", "Failed to save plant")
                    }
                }
            }
        } else {
            Log.d("PlantVM", "No user logged in")
        }
    }

    fun increaseWaterLevel(amount: Int) {
        _selectedPlant.value?.let { plant ->
            plant.waterLevel = minOf(100, plant.waterLevel + amount)
            _selectedPlant.postValue(plant)
            savePlantForCurrentUser()
            Log.d("PlantVM", "Water level increased by $amount")
        } ?: Log.e("PlantVM", "No plant selected to update water level")
    }
}
    

