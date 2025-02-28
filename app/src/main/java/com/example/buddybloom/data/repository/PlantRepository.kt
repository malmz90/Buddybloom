package com.example.buddybloom.data.repository

import android.util.Log
import com.example.buddybloom.data.model.PlantHistory
import com.example.buddybloom.data.model.Plant
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PlantRepository {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId get() = auth.currentUser?.uid
    private val userIdException = Exception("Firebase authentication error.")

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
    suspend fun savePlantToHistory(plant: Plant): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = currentUserId ?: return@withContext Result.failure(userIdException)
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

                db.collection(USERS).document(userId).collection(HISTORY)
                    .add(plantHistory).await()
                Result.success(Unit)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }


    suspend fun deletePlant(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = currentUserId ?: return@withContext Result.failure(userIdException)
                db.collection(USERS).document(userId).collection(PLANTS)
                    .document(PLANT_REF).delete()
                    .await()
                Result.success(Unit)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }

    /**
     * Fetches the current plant from Firestore, or null if document doesn't exist (user has no plant).
     */
    suspend fun fetchPlant(): Result<Plant?> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = currentUserId ?: return@withContext Result.failure(userIdException)
                val plant = db.collection(USERS).document(userId).collection(PLANTS)
                    .document(PLANT_REF).get()
                    .await().toObject<Plant>()
                Result.success(plant)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }

    /**
     * Overwrites the current plant on Firestore with a new one.
     */
    suspend fun savePlant(plant: Plant): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = currentUserId ?: return@withContext Result.failure(userIdException)
                db.collection(USERS).document(userId).collection(PLANTS).document(PLANT_REF)
                    .set(plant)
                    .await()
                Result.success(Unit)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }

    /**
     * Updates only the specified fields of the current plant on Firestore.
     */

    suspend fun updateRemotePlant(plant: Plant): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = currentUserId ?: return@withContext Result.failure(userIdException)
                val updates = mapOf(
                    "waterLevel" to plant.waterLevel,
                    "fertilizerLevel" to plant.fertilizerLevel,
                    "sunLevel" to plant.sunLevel,
                    "infected" to plant.infected,
                    "lastUpdated" to Timestamp.now(),
                    "protectedFromSun" to plant.protectedFromSun
                )
                val plantDoc =
                    db.collection(USERS).document(userId).collection(PLANTS).document(PLANT_REF)
                if (plantDoc.get().await().exists()) {
                    plantDoc.update(updates).await()
                }
                Result.success(Unit)

            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }

    /**
     * Fetches the user's plant history from Firestore as a one-time operation.
     * This retrieves all the plants that have died and been saved to history,
     * ordered from most recent to oldest.
     */
    suspend fun getPlantHistory(): Result<List<PlantHistory>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = currentUserId ?: return@withContext Result.failure(userIdException)
                val snapshot = db.collection(USERS).document(userId).collection(HISTORY)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get().await()
                val historyList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<PlantHistory>()
                }
                Result.success(historyList)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }
}