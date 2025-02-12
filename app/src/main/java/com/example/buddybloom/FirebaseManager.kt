package com.example.buddybloom

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class FirebaseManager {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()



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



}