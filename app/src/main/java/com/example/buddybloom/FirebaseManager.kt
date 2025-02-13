package com.example.buddybloom

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.buddybloom.data.model.WeatherReport
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import java.util.Calendar
import java.util.Locale

class FirebaseManager {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

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
            "createdAt" to plant.createdAt
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

    fun registerUser(email: String, password: String, name: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
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


    /**
     * Saves a user and a starting weekly weather report to the database.
     */
    private fun saveUser(user: User, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(userId)
        val batch = db.batch()
        val startingWeeklyWeatherReport = generateStartingSunnyWeeklyReport()

        batch.set(docRef, user)
        batch.update(docRef, "weeklyWeatherReport", startingWeeklyWeatherReport)

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


    fun getWeatherReport(): LiveData<WeatherReport.Weekly?> {
        val liveData = MutableLiveData<WeatherReport.Weekly?>()
        userId?.let {
            db.collection("users").document(it).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firebase error", error.message.toString())
                    return@addSnapshotListener
                }
                val weatherReport = snapshot?.toObject<User>()?.weeklyWeatherReport
                liveData.postValue(weatherReport)
            }
        }
        return liveData
    }

    fun updateWeatherReport(weeklyWeatherReport: WeatherReport.Weekly) {
        userId?.let {
            db.collection("users").document(it).update("weeklyWeatherReport", weeklyWeatherReport)
        }
    }

    /**
     * Generates one weekly weather report where every day is sunny and temperature and sunshine duration is random. Daily data is empty.
     */
    private fun generateStartingSunnyWeeklyReport(): WeatherReport.Weekly {
        val startingDate = Calendar.getInstance()
        startingDate.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val days = mutableListOf<WeatherReport.Daily>()
        repeat(7) {
            val randomSunshineDuration = (1..16).random()
            val dailyReport = WeatherReport.Daily(
                sunshineDuration = randomSunshineDuration,
                timestamp = Timestamp(startingDate.time),
                condition = WeatherReport.Condition.SUNNY,
                temperature = (-5..25).random(),
                weekDay = startingDate.getDisplayName(
                    Calendar.DAY_OF_WEEK,
                    Calendar.SHORT,
                    Locale.getDefault()
                )

            )
            days.add(dailyReport)
            startingDate.add(Calendar.DAY_OF_YEAR, 1)
        }
        return WeatherReport.Weekly(dailyReports = days)
    }
}