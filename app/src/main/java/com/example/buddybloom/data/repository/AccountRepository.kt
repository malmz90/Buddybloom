package com.example.buddybloom.data.repository

import android.util.Log
import com.example.buddybloom.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

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

    /**
     * Registers a new user with the provided email and password.
     * Callback with a boolean indicating success or failure.
     * Callback is called upon in the viewmodel to set the registration result (livedata).
     */
    fun registerUser(email: String, password: String, name: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = auth.currentUser
                val user = User(
                    id = currentUser?.uid ?: "", email = email, name = name
                )
                // Calls function to save user to Firestore.
                saveUser(user) { success ->
                    callback(success)
                }
            } else {
                Log.d("!!!", "user not created ${task.exception}")
                callback(false)
            }
        }
    }

    /**
     * Saves a user to the Firestore database.
     * Called upon in registerUser above.
     */
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

    //Send email link to reset password
    fun sendPasswordResetEmail(email: String): Task<Void> {
        return auth.sendPasswordResetEmail(email)
    }

    /**
     * Updates userName in fireBase
     */
    suspend fun updateUserName(newUserName:String):Result<Unit>{
        val user = auth.currentUser ?: return Result.failure(Exception("No User has logged in"))

        return try {
            // Updates username in Firebase Authentication
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newUserName)
                .build()
            user.updateProfile(profileUpdates).await()
            // Uppdatera Firestore
            val userRef = db.collection("users").document(user.uid)

            userRef.update("name", newUserName).await()

            Log.d("AccountRepository", "Username updated successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AccountRepository", "Failed to update username: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Function that updates Email when user presses save button in profile fragment
     * and user needs to verify email address to change email, User automatically sign out
     * and comes to login page. User needs to sign in again.
     */
    suspend fun updateUserEmail(newEmail: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user has logged in"))
        val userId = user.uid

        return try {
            //Updates email in Firebase Authentication
            // And send email verification to email address
            user.verifyBeforeUpdateEmail(newEmail).await()
            user.sendEmailVerification().await()

            //Updates firestore to with email
            val userRef = db.collection("users").document(userId)
            val updates = mapOf(
                "email" to newEmail

            )
            userRef.update(updates).await()

            // User logs out, and needs to sign in again after verification of email
            auth.signOut()

            Log.d("AccountRespitory", "Email updated. User need to signin again.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AccountRespitory", "Failed to update Email: ${e.message}")
            Result.failure(e)
        }
    }

    //Function to delete account from app
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