package com.example.buddybloom.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.buddybloom.data.model.PlantHistory
import com.example.buddybloom.data.model.Plant
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

class PlantRepository {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

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
        docRef.set(PlantHistory(name = plant.name, streakCount = plant.streakDays))
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