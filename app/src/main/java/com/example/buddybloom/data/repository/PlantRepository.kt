package com.example.buddybloom.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.buddybloom.data.model.PlantHistory
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

class PlantRepository {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserPlant(callback: (Plant?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            val user = document.toObject(User::class.java)
            callback(user?.userPlants?.firstOrNull())
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error getting plant: ${e.message}")
            callback(null)
        }
    }

    fun saveUserPlant(userId: String, plant: Plant, callback: (Boolean) -> Unit) {
//        val userId = auth.currentUser?.uid ?: return

        val plantToSave = mapOf(
            "name" to plant.name,
            "waterLevel" to plant.waterLevel,
            "createdAt" to plant.createdAt,
            "streakDays" to plant.streakDays,
            "info" to plant.info,
            "difficulty" to plant.difficulty
        )

        db.collection("users").document(userId).update("userPlants", listOf(plantToSave))
            .addOnSuccessListener {
                Log.d("Firebase", "Plant saved successfully")
                callback(true)
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Error saving plant: ${e.message}")
                callback(false)
            }
    }

    fun savePlantHistory(plant: Plant, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFailure(Exception("Firebase error: Could not find logged in user's id."))
            return
        }
        val docRef = db.collection("users").document(userId).collection("history").document()
        docRef.set(PlantHistory(name = plant.name, streakCount = plant.streakDays))
            .addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {
                onFailure(it)
            }
    }

    fun getPlantHistoryLiveData(): LiveData<List<PlantHistory>> {
        val liveData = MutableLiveData<List<PlantHistory>>()
        val userId = auth.currentUser?.uid
        userId?.let {
            db.collection("users").document(it).collection("history")
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