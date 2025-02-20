package com.example.buddybloom.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.buddybloom.data.model.User
import com.example.buddybloom.data.model.WeatherReport
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import java.util.Calendar
import java.util.Locale

class WeatherRepository {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    fun getWeatherReportLiveData(): LiveData<WeatherReport.Weekly?> {
        val liveData = MutableLiveData<WeatherReport.Weekly?>()
        userId?.let {
            db.collection("users").document(it).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firebase error", error.message.toString())
                    return@addSnapshotListener
                }
                var weatherReport = snapshot?.toObject<User>()?.weeklyWeatherReport
                if (weatherReport == null) {
                    weatherReport = generateNewSunnyWeeklyReport()
                    updateWeatherReport(weatherReport)
                }
                liveData.postValue(weatherReport)
            }
        }
        return liveData
    }

    fun fetchCurrentWeatherReport(onWeatherReportFetched: (WeatherReport.Weekly) -> Unit) {
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
    }

    fun updateWeatherReport(weeklyWeatherReport: WeatherReport.Weekly) {
        userId?.let {
            db.collection("users").document(it).update("weeklyWeatherReport", weeklyWeatherReport)
        }
    }

    /**
     * Generates one weekly weather report where every day is sunny and temperature and sunshine duration is random. Daily data is empty.
     */
    fun generateNewSunnyWeeklyReport(): WeatherReport.Weekly {
        val startingDate = Calendar.getInstance().apply {
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
                    Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()
                ))
            days.add(dailyReport)
            startingDate.add(Calendar.DAY_OF_YEAR, 1)
        }
        return WeatherReport.Weekly(dailyReports = days)
    }

    /**
     * Uses the current report and appends a new day for each day passed.
     */
    fun passDaysForWeather(
        currentWeeklyReport: WeatherReport.Weekly,
        daysPassed: Int
    ): WeatherReport.Weekly {

        if (daysPassed <= 0) return currentWeeklyReport

        var lastDailyReport = currentWeeklyReport.dailyReports.last()
        val newDailyReports = currentWeeklyReport.dailyReports.toMutableList()
        repeat(daysPassed) {
            val calendar = Calendar.getInstance()
            calendar.time = lastDailyReport.timestamp.toDate()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val nextDay = WeatherReport.Daily(
                sunshineDuration = (1..16).random(),
                condition = WeatherReport.Condition.SUNNY,
                timestamp = Timestamp(calendar.time),
                temperature = (-5..25).random(),
                weekDay = calendar.getDisplayName(
                    Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()
                )
            )
            newDailyReports.add(nextDay)
            lastDailyReport = newDailyReports.last()
        }
        return WeatherReport.Weekly( dailyReports =  newDailyReports)    }
}