package com.example.buddybloom.data.repository

import android.util.Log
import com.example.buddybloom.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class AccountRepository {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid



    fun loginUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "Login successful")
                    callback(true)
                } else {
                    Log.e("Firebase", "Login failed: ${task.exception?.message}")
                    callback(false)
                }
            }
    }

    fun registerUser(email: String, password: String, name: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    val user = User(
                        id = currentUser?.uid ?: "", email = email, name = name
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



    private fun saveUser(user: User, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(userId)
        val batch = db.batch()
       // val startingWeeklyWeatherReport = generateStartingSunnyWeeklyReport(Calendar.getInstance())

        batch.set(docRef, user)
        // batch.update(docRef, "weeklyWeatherReport", startingWeeklyWeatherReport)

        batch.commit().addOnSuccessListener {
            Log.i("Firebase", "User added successfully")
            callback(true)
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Failed to add user ${e.message}")
            callback(false)
        }
    }

    fun sendPasswordResetEmail(email: String): Task<Void> {
        return auth.sendPasswordResetEmail(email)
    }

    fun getUserCreationDate(onCreationDateFetched: (Timestamp?) -> Unit) {
        userId?.let {
            db.collection("users").document(it).get().addOnSuccessListener { snapshot ->
                val timestamp = snapshot.toObject<User>()?.creationDate
                onCreationDateFetched(timestamp)
            }
        }
    }

    fun getLastUpdated(onLastUpdateFetched: (Timestamp?) -> Unit) {
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { snapshot ->
                val timestamp = snapshot.toObject<User>()?.lastUpdated
                onLastUpdateFetched(timestamp)
            }.addOnFailureListener { error ->
                    Log.e(
                        "Unable to fetch timestamp lastUpdated", error.message.toString()
                    )
                }
        } else {
            Log.e("FirebaseUser error: ", "User id = null")
        }
    }

    fun updateLastUpdated(timestamp: Timestamp) {
        Log.e("FirebaseManager: ", "updateLastUpdate triggered")
        userId?.let {
            db.collection("users").document(it).update("lastUpdated", timestamp)
            Log.i("FirebaseManager:", "lastUpdate updated!")
        }
    }








}


