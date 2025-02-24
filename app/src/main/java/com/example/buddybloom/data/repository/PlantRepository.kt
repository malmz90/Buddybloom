package com.example.buddybloom.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.buddybloom.data.model.PlantHistory
import com.example.buddybloom.data.model.Plant
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await

class PlantRepository {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private val firebaseException = Exception("Firebase authentication error.")

    companion object {
        const val USERS = "users"
        const val PLANTS = "plants"
        const val PLANT_REF = "plantRef"
        const val HISTORY = "history"
    }

    //TODO Add retries?
    suspend fun fetchPlant(onFailure: (Exception) -> Unit): Plant? {
        return auth.currentUser?.uid?.let {
            try {
                db.collection(USERS).document(it).collection(PLANTS).document(PLANT_REF).get()
                    .await().toObject<Plant>()
            } catch (error: Exception) {
                onFailure(error)
                null
            }
        } ?: run {
            onFailure(firebaseException)
            null
        }
    }

    suspend fun savePlant(plant: Plant, onFailure: (Exception) -> Unit) {
        auth.currentUser?.uid?.let {
            try {
                val plantDoc =
                    db.collection(USERS).document(it).collection(PLANTS).document(PLANT_REF)
                plantDoc.set(plant).await()
            } catch (error: Exception) {
                onFailure(error)
            }
        } ?: onFailure(Exception(firebaseException))
    }

    suspend fun updateRemotePlant(plant: Plant, onFailure: (Exception) -> Unit) {
        auth.currentUser?.uid?.let {
            try {
                val updates = mapOf(
                    "waterLevel" to plant.waterLevel,
                    "fertilizerLevel" to plant.fertilizerLevel,
                    "sunLevel" to plant.sunLevel,
                    "lastUpdated" to Timestamp.now()
                )
                val plantDoc =
                    db.collection(USERS).document(it).collection(PLANTS).document(PLANT_REF)
                if (plantDoc.get().await().exists()) {
                    plantDoc.update(updates).await()
                }

            } catch (error: Exception) {
                onFailure(error)
            }
        } ?: onFailure(firebaseException)
    }

    suspend fun deletePlantFromRemote(onFailure: (Exception) -> Unit) {
        auth.currentUser?.uid?.let {
            try {
                db.collection(USERS).document(it).collection(PLANTS).document(PLANT_REF).delete()
                    .await()
            } catch (error: Exception) {
                onFailure(error)
            }
        } ?: onFailure(Exception(firebaseException))
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