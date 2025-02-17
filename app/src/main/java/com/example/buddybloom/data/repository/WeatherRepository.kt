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
                val weatherReport = snapshot?.toObject<User>()?.weeklyWeatherReport
                liveData.postValue(weatherReport)
            }
        }
        return liveData
    }

    fun getCurrentWeatherReport(onWeatherReportFetched: (WeatherReport.Weekly?) -> Unit) {
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { snapshot ->
                val report = snapshot?.toObject<User>()?.weeklyWeatherReport
                onWeatherReportFetched(report)
            }
        } else {
            Log.e("FirebaseUser error: ", "User id = null")
        }
    }

    /**
     * Generates one weekly weather report where every day is sunny and temperature and sunshine duration is random. Daily data is empty.
     */
    fun generateStartingSunnyWeeklyReport(startingDate: Calendar): WeatherReport.Weekly {
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
                    Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()
                )

            )
            days.add(dailyReport)
            startingDate.add(Calendar.DAY_OF_YEAR, 1)
        }
        return WeatherReport.Weekly(dailyReports = days)
    }

    fun updateWeatherReport(weeklyWeatherReport: WeatherReport.Weekly) {
        userId?.let {
            db.collection("users").document(it).update("weeklyWeatherReport", weeklyWeatherReport)
        }
    }

}