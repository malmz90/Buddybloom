package com.example.buddybloom.data.repository

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.buddybloom.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AccountRepository
    (
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val googleSignInClient: GoogleSignInClient
) {

    private val db = Firebase.firestore
    private val currentUser get() = auth.currentUser
    private val userException =
        Exception("Firebase authentication error: FirebaseUser is null.")

    //To get userdata to fill fields in profilefragment
    suspend fun getUserData(): Result<User?> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = currentUser?.uid ?: return@withContext Result.failure(userException)
                val user = db.collection("users").document(userId).get().await().toObject<User>()
                Result.success(user)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                Result.success(Unit)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }

    /**
     * Registers a new user with the provided email and password.
     * Callback with a boolean indicating success or failure.
     * Callback is called upon in the viewmodel to set the registration result (livedata).
     */
    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                val userId = currentUser?.uid ?: return@withContext Result.failure(userException)
                val newUser = User(
                    id = userId, email = email, name = name
                )
                saveUser(newUser)
                Result.success(Unit)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }

    /**
     * Saves a user to the Firestore database.
     * Called upon in registerUser above.
     */
    private suspend fun saveUser(user: User) {
        val userId = currentUser?.uid ?: throw userException
        db.collection("users").document(userId).set(user).await()
    }

    //Send email link to reset password
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                auth.sendPasswordResetEmail(email).await()
                Result.success(Unit)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }

    /**
     * Updates userName in fireBase
     */
    suspend fun updateUserName(newUserName: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val user = currentUser ?: return@withContext Result.failure(userException)
                // Updates username in Firebase Authentication
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newUserName)
                    .build()
                user.updateProfile(profileUpdates).await()
                // Uppdatera Firestore
                val userRef = db.collection("users").document(user.uid)
                userRef.update("name", newUserName).await()
                Result.success(Unit)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }

    /**
     * Function that updates Email when user presses save button in profile fragment
     * and user needs to verify email address to change email, User automatically sign out
     * and comes to login page. User needs to sign in again.
     */
    suspend fun updateUserEmail(newEmail: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val user =
                    currentUser ?: return@withContext Result.failure(userException)
                //Updates email in Firebase Authentication
                // And send email verification to email address
                user.verifyBeforeUpdateEmail(newEmail).await()
                user.sendEmailVerification().await()
                //Updates firestore to with email
                val userRef = db.collection("users").document(user.uid)
                val updates = mapOf(
                    "email" to newEmail
                )
                userRef.update(updates).await()
                // User logs out, and needs to sign in again after verification of email
                auth.signOut()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    //Function to delete account from app
    suspend fun deleteAccount(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val user = currentUser ?: return@withContext Result.failure(userException)
                db.collection("users").document(user.uid).delete().await()
                user.delete().await()
                googleSignInClient.signOut().await()
                auth.signOut()
                Result.success(Unit)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun signInGoogleIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = currentUser ?: return@withContext Result.failure(userException)
                if (authResult.additionalUserInfo?.isNewUser == true) {
                    val user = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: ""
                    )
                    saveUser(user)
                    Result.success(Unit)
                } else {
                    Result.success(Unit)
                }
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }
}