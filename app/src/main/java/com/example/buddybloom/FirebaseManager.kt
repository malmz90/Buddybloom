package com.example.buddybloom

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class FirebaseManager {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserPlant(callback: (Plant?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                callback(user?.userPlants?.firstOrNull())
            }
            .addOnFailureListener { e ->
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

        db.collection("users").document(userId)
            .update("userPlants", listOf(plantToSave))
            .addOnSuccessListener {
                Log.d("Firebase", "Plant saved successfully")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error saving plant: ${e.message}")
                callback(false)
            }
    }

    fun loginUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "Login successful")
                    callback(true)
                } else {
                    Log.e("Firebase", "Login failed: ${task.exception?.message}")
                    callback(false)
                }
            }
    }

    fun registerUser(email : String, password : String, name : String, callback: (Boolean) -> Unit) {
       auth.createUserWithEmailAndPassword(email, password)
           .addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    val currentUser = auth.currentUser
                    val user = User(
                        id = currentUser?.uid ?: "",
                        email = email,
                        name = name
                    )
                    saveUser(user) { success ->
                        callback(success)
                    }
                } else {
                    Log.d("!!!", "user not created ${task.exception}")
                    callback(false)
                }
           }
    }

    fun saveUser(user : User, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.i("Firebase", "User added successfully")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to add user ${e.message}")
                callback(false)
            }
    }

    fun sendPasswordResetEmail(email: String): Task<Void>{
        return auth.sendPasswordResetEmail(email)
    }


}