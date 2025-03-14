package com.example.buddybloom.data.repository

import com.example.buddybloom.data.model.User
import com.example.buddybloom.data.model.WeatherReport
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar

class WeatherRepository {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId get() = auth.currentUser?.uid


    private fun generateDailyReport(): WeatherReport.Daily {
        val morningHours = (0..6).toList()
        val availableHours = (7..21).toList()
        val nightHours = (22 until 24).toList()

        val dayConditions = availableHours.map { hour ->
            val randomCondition = WeatherReport.Condition.entries.toTypedArray()
                .filter { it != WeatherReport.Condition.NIGHT }
                .random()
            WeatherReport.MyPair(hour, randomCondition)
        }
        val morningConditions = morningHours.map { hour ->
            val nightCondition = WeatherReport.Condition.NIGHT
            WeatherReport.MyPair(hour, nightCondition)
        }
        val nightConditions = nightHours.map { hour ->
            val nightCondition = WeatherReport.Condition.NIGHT
            WeatherReport.MyPair(hour, nightCondition)
        }

        val weatherConditions = dayConditions
            .plus(morningConditions)
            .plus(nightConditions)
            .sortedBy { it.first }

        val dailyReport = WeatherReport.Daily(
            hourlyWeather = weatherConditions,
            timestamp = System.currentTimeMillis()
        )
        return dailyReport
    }

    suspend fun fetchOrCreateDailyReport(): Result<WeatherReport.Daily> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = currentUserId
                    ?: return@withContext Result.failure(Exception("Firebase authentication error: User id is null."))
                val storedDailyReport = db.collection("users").document(userId).get().await()
                    .toObject<User>()?.dailyWeatherReport
                if (storedDailyReport != null) {
                    val now = Calendar.getInstance()
                    val convertTimestamp = Calendar.getInstance()
                    convertTimestamp.timeInMillis = storedDailyReport.timestamp
                    if (convertTimestamp.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR)) {
                        val newDailyReport = generateDailyReport()
                        db.collection("users").document(userId)
                            .update("dailyWeatherReport", newDailyReport).await()
                        Result.success(newDailyReport)
                    } else {
                        Result.success(storedDailyReport)
                    }
                } else {
                    val newDailyReport = generateDailyReport()
                    db.collection("users").document(userId)
                        .update("dailyWeatherReport", newDailyReport).await()
                    Result.success(newDailyReport)
                }
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }
}