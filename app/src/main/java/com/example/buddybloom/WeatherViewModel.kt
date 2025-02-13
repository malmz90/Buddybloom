package com.example.buddybloom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddybloom.data.model.WeatherReport
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Locale

class WeatherViewModel : ViewModel() {
    private val firebaseManager = FirebaseManager()


    private var _weeklyWeatherReport = MutableLiveData<WeatherReport.Weekly?>(null)
    val weeklyWeatherReport: LiveData<WeatherReport.Weekly?> get() = _weeklyWeatherReport

    private fun passOneDay(currentWeeklyReport: WeatherReport.Weekly): WeatherReport.Weekly {
        val lastDailyInReport = currentWeeklyReport.dailyReports.last()
        val calendar = Calendar.getInstance()
        calendar.time = lastDailyInReport.timestamp.toDate()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val nextDay = WeatherReport.Daily(
            sunshineDuration = (1..16).random(),
            condition = WeatherReport.Condition.SUNNY,
            timestamp = Timestamp(calendar.time),
            temperature = (-5..25).random(),
            weekDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
        )
        val newDailyReports = currentWeeklyReport.dailyReports.toMutableList().apply {
            add(nextDay)
            if (size > 7) removeAt(0)
        }
        return currentWeeklyReport.copy(dailyReports = newDailyReports)
    }

}