package com.example.buddybloom.data.model

import com.google.firebase.Timestamp


sealed class WeatherReport {

    data class Hourly(
        val temperature: Int = 0,
        val condition: Condition? = null,
        val timestamp: Timestamp = Timestamp.now(),
    ) :
        WeatherReport()

    data class Daily(
        val sunshineDuration: Int = 0,
        val temperature: Int = 0,
        val condition: Condition? = null,
        val hourlyReports: MutableList<Hourly> = mutableListOf(),
        val timestamp: Timestamp = Timestamp.now(),
        val weekDay: String? = null
    ) : WeatherReport()

    data class Weekly(
        val dailyReports: MutableList<Daily> = mutableListOf()
    ) : WeatherReport()

    enum class Condition {
        SUNNY, CLOUDY, RAIN
    }

}