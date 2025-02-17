package com.example.buddybloom.data.repository

import android.util.Log
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class PlantRepository {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

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

    fun saveUserPlant(plant: Plant, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        val plantToSave = mapOf(
            "name" to plant.name,
            "waterLevel" to plant.waterLevel,
            "createdAt" to plant.createdAt,
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
}