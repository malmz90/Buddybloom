package com.example.buddybloom.data.repository

import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.buddybloom.data.model.PlantHistory
import com.example.buddybloom.data.model.Plant
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class PlantRepository {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private var isInfected = false

    companion object {
        const val USERS = "users"
        const val PLANTS = "plants"
        const val PLANT_REF = "plantRef"
        const val HISTORY = "history"
    }

    fun snapshotOfCurrentUserPlant(callback: (Plant?) -> Unit) {
        userId ?: return
        db.collection(USERS).document(userId).collection(PLANTS).document(PLANT_REF)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    callback(null)
                    return@addSnapshotListener
                }
                val plant = snapshot?.toObject(Plant::class.java)
                callback(plant)
            }
    }

    fun getCurrentUserPlant(callback: (Plant?) -> Unit) {
        userId ?: return
        db.collection(USERS).document(userId).collection(PLANTS).document(PLANT_REF).get()
            .addOnSuccessListener { document ->
                val plant = document?.toObject(Plant::class.java)
                callback(plant)
            }.addOnFailureListener {
                //TODO error handling
                callback(null)
            }
    }
    // Needs to update plant if infected alse when its not infected after have been infected
    fun updateInfectedStatus(infected: Boolean) {
        userId ?: return
        val plantRef = db.collection(USERS).document(userId).collection(PLANTS).document(PLANT_REF)

        plantRef.update("infected", infected)
            .addOnSuccessListener {
                Log.d("PlantRepository", "Infected status updated successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("PlantRepository", "Error updating infected status", e)
            }
    }

    /**
     * get user plant and if it not already is infected by bugs it randomly pix a number
     * and if it lower than 10 it will be infected and a bug gif will show on screen,
     * user needs to press bug spray button to get plant healthy again.
     */
        fun startRandomInfection(plant:Plant) {
            userId ?: return
            val plantRef =
                db.collection(USERS).document(userId).collection(PLANTS).document(PLANT_REF)

            plantRef.get().addOnSuccessListener { document ->
                val plant = document.toObject(Plant::class.java)
                if (plant != null && plant.infected) {
                    Log.d("infected", "Plant is already infected, skipping random check")
                    return@addOnSuccessListener // stop random if plant already is infeckted
                }

                val randomValue = (0..50).random()
                Log.d("infected", "Random value: $randomValue")
                if (randomValue <= 10) {
                    plant?.infected = true
                    updateInfectedStatus(true)
                    Log.d("infected", "Plant is infected: ${plant?.infected}")
                } else {
                    Log.d("infected", "Plant is not infected: ${plant?.infected}")
                }
            }
        }

    /**
     * plant gets healhty after been infected by bugs
     */
    fun plantGetFreeFromBugs(plant:Plant) {
        plant.infected = false
        updateInfectedStatus(false)
    }

    fun increaseWaterLevel(plant:Plant,amount: Int) {
        plant.waterLevel = maxOf(0, plant.waterLevel + amount)
        plant.waterLevel += amount
        Log.d("PlantStatus", "Your Plant lost water by $amount!")
    }
    /**
     * Decreases WaterLevel, amount sets in Plantworker
     */
    fun decreaseWaterLevel(plant: Plant, amount: Int) {
        plant.waterLevel = maxOf(0, plant.waterLevel - amount)
        plant.waterLevel -= amount
        Log.d("PlantStatus", "Your Plant lost water by $amount!")
    }
    /**
     * Decreases FertilizeLevel, amount sets in Plantworker
     */
    fun decreaseFertilizer(plant: Plant, amount: Int) {
        plant.fertilizerLevel = maxOf(0, plant.fertilizerLevel - amount)
        plant.fertilizerLevel -= amount
        Log.d("PlantStatus", "Your Plant lost Nutrition by $amount!")
    }

    fun saveUserPlant(plant: Plant, callback: (Boolean) -> Unit) {
        userId ?: return
        // Use if we want to save more than one plant
        //db.collection(USERS).document(userId).collection(PLANTS).add(plant)
        db.collection(USERS).document(userId).collection(PLANTS).document(PLANT_REF).set(plant)
            .addOnSuccessListener {
                Log.d("Firebase", "Plant saved successfully")
                callback(true)
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Error saving plant: ${e.message}")
                callback(false)
            }
    }

    fun savePlantHistory(plant: Plant, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (userId == null) {
            onFailure(Exception("Firebase error: Could not find logged in user's id."))
            return
        }
        val docRef = db.collection(USERS).document(userId).collection(HISTORY).document()
        docRef.set(PlantHistory(name = plant.name))
            .addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {
                onFailure(it)
            }
    }

    fun getPlantHistoryLiveData(): LiveData<List<PlantHistory>> {
        val liveData = MutableLiveData<List<PlantHistory>>()
        userId?.let {
            db.collection(USERS).document(it).collection(HISTORY)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.i("PlantRepository", "Error fetching history from Firebase")
                        return@addSnapshotListener
                    }
                    val historyItems = snapshot?.toObjects<PlantHistory>().orEmpty()
                        .sortedBy { item -> item.timestamp }
                    liveData.postValue(historyItems)
                }
        }
        return liveData
    }
}