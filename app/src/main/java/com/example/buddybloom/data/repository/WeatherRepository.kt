package com.example.buddybloom.data.repository

import android.util.Log
import com.example.buddybloom.data.model.User
import com.example.buddybloom.data.model.WeatherReport
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import java.util.Calendar

//TODO Remake this so it triggers somewhere in the game loop, or create a new timer for weather.

class WeatherRepository {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid


    fun generateDailyReport(): WeatherReport.Daily {
        val morningHours = (0..6).toList()
        val availableHours = (7..21).toList()
        val nightHours = (22 until 24).toList()

        val dayConditions = availableHours.map { hour ->
            val randomCondition = WeatherReport.Condition.entries.toTypedArray().filter { it != WeatherReport.Condition.NIGHT }.random() //Condition.values().random()
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
//            timestamp = Calendar.getInstance()
            timestamp = System.currentTimeMillis()
        )
        return dailyReport
    }

    fun fetchOrCreateDailyReport(onReportFetched : (WeatherReport.Daily) -> Unit) {
        val now = Calendar.getInstance()
        val convertTimestamp = Calendar.getInstance()

        userId?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
                val storedDailyReport = snapshot.toObject<User>()?.dailyWeatherReport
                if (storedDailyReport != null) {
                    convertTimestamp.timeInMillis = storedDailyReport.timestamp
                    if(convertTimestamp.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR)) {
                        val newDailyReport = generateDailyReport()
                        db.collection("users").document(uid).update("dailyWeatherReport", newDailyReport)
                            .addOnSuccessListener {
                                onReportFetched(newDailyReport)
                            }
                            .addOnFailureListener {
                                Log.e("Weather repo","Error updating daily weather report", it)
                            }
                    } else {
                        onReportFetched(storedDailyReport)
                    }
                } else {
                    val newDailyReport = generateDailyReport()
                    db.collection("users").document(uid).update("dailyWeatherReport", newDailyReport)
                        .addOnSuccessListener {
                            onReportFetched(newDailyReport)
                        }
                        .addOnFailureListener {
                            Log.e("Weather repo","Error updating daily weather report", it)
                        }
                }
            }.addOnFailureListener {
                Log.e("Weather repo","Error fetching daily weather report", it)
            }
        }
    }


    /*fun fetchCurrentWeatherReport(onWeatherReportFetched: (WeatherReport.Weekly) -> Unit) {
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { snapshot ->
                var report = snapshot?.toObject<User>()?.weeklyWeatherReport
                if (report == null) {
                    report = generateNewSunnyWeeklyReport()
                    updateWeatherReport(report)
                }
                onWeatherReportFetched(report)
            }.addOnFailureListener {
                Log.e("Error fetching current weather report from Firebase!", it.message.toString())
            }
        } else {
            Log.e("FirebaseUser error: ", "User id = null")
        }
    }*/

    fun updateWeatherReport(weeklyWeatherReport: WeatherReport.Daily) {
        userId?.let {
            db.collection("users").document(it).update("weeklyWeatherReport", weeklyWeatherReport)
        }
    }


}