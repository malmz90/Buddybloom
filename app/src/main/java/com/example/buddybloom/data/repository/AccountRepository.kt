package com.example.buddybloom.data.repository

import android.util.Log
import com.example.buddybloom.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

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
        docRef.set(user).addOnSuccessListener {
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

    fun deleteAccount(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        userId?.let { uid ->
            db.collection("users").document(uid).delete()
                .addOnSuccessListener {
                    Log.d("DB", "User deleted from Firestore")

                    auth.currentUser?.delete()?.addOnSuccessListener {
                        Log.d("Auth", "User deleted from Firebase Auth")
                        onSuccess()
                    }?.addOnFailureListener{ exception ->
                        Log.e("Auth", "Failed to delete from Firebase Auth ${exception.message}")
                        onFailure(exception)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DB", "Failed to delete from Firestore ${exception.message}")
                    onFailure(exception)
                }
        } ?:onFailure(Exception("User ID is null"))
    }
    fun signOut(callback: (Boolean) -> Unit){
        auth.signOut()
        callback(true)
    }
}