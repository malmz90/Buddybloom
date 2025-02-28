package com.example.buddybloom.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.buddybloom.data.model.PlantHistory
import com.example.buddybloom.data.model.Plant
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
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
        private const val USERS = "users"
        private const val PLANTS = "plants"
        private const val PLANT_REF = "plantRef"
        private const val HISTORY = "history"
    }

    /**
     * Saves a dead plant to the user's history collection in Firestore.
     * This function calculates how many days the plant survived before dying and
     * creates a PlantHistory record with this information.
     */
    suspend fun savePlantToHistory(plant: Plant, onFailure: (Exception) -> Unit) {
        auth.currentUser?.uid?.let { uid ->
            try {
                // Calculate how many days the plant lived
                val createdAt = plant.createdAt
                val diedAt = Timestamp.now()

                // Calculate days difference
                val millisPerDay = 24 * 60 * 60 * 1000
                val daysDifference = ((diedAt.seconds - createdAt.seconds) * 1000) / millisPerDay

                val plantHistory = PlantHistory(
                    name = plant.name,
                    streakCount = daysDifference.toInt(),
                    timestamp = diedAt
                )

                db.collection(USERS).document(uid).collection(HISTORY).add(plantHistory).await()
            } catch (error: Exception) {
                onFailure(error)
            }
        } ?: onFailure(firebaseException)
    }


    suspend fun deletePlant(onFailure: (Exception) -> Unit) {
        auth.currentUser?.uid?.let {
            try {
                db.collection(USERS).document(it).collection(PLANTS).document(PLANT_REF).delete()
                    .await()
            } catch (error: Exception) {
                onFailure(error)
            }
        } ?: onFailure(Exception(firebaseException))
    }

    /**
     * Fetches the current plant from Firestore, or null if document doesn't exist (user has no plant).
     */
    //TODO Add retries?
    suspend fun fetchPlant(onFailure: (Exception) -> Unit): Plant? {
        val userId = auth.currentUser?.uid ?: run {
            onFailure(Exception("No authenticated user found"))
            return null
        }
        return try {
                db.collection(USERS).document(userId).collection(PLANTS).document(PLANT_REF).get()
                    .await().toObject<Plant>()
            } catch (error: Exception) {
                onFailure(error)
                null
            }
    }

    /**
     * Overwrites the current plant on Firestore with a new one.
     */
    suspend fun savePlant(plant: Plant, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            onFailure(Exception("No authenticated user found"))
            return
        }
        Log.d("PlantRepo", "Saving plant for user: $userId")
            try {
                val plantDoc =
                    db.collection(USERS).document(userId).collection(PLANTS).document(PLANT_REF)
                plantDoc.set(plant).await()
            } catch (error: Exception) {
                onFailure(error)
            }
    }

    /**
     * Updates only the specified fields of the current plant on Firestore.
     */
    suspend fun updateRemotePlant(plant: Plant, onFailure: (Exception) -> Unit) {
        auth.currentUser?.uid?.let {
            try {
                val updates = mapOf(
                    "waterLevel" to plant.waterLevel,
                    "fertilizerLevel" to plant.fertilizerLevel,
                    "sunLevel" to plant.sunLevel,
                    "infected" to plant.infected,
                    "lastUpdated" to Timestamp.now(),
                    "protectedFromSun" to plant.protectedFromSun
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

    /**
     * Fetches the user's plant history from Firestore as a one-time operation.
     * This retrieves all the plants that have died and been saved to history,
     * ordered from most recent to oldest.
     */
    suspend fun getPlantHistory(
        onSuccess: (List<PlantHistory>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.currentUser?.uid?.let { uid ->
            try {
                val snapshot = db.collection(USERS).document(uid).collection(HISTORY)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val historyList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<PlantHistory>()
                }

                onSuccess(historyList)
            } catch (error: Exception) {
                onFailure(error)
                onSuccess(emptyList())
            }
        } ?: run {
            onFailure(firebaseException)
            onSuccess(emptyList())
        }
    }
}