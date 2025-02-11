package com.example.buddybloom.data.model

import com.google.firebase.Timestamp


sealed class WeatherReport {

    data class Hourly(
        val temperature: Int = 0,
        val timestamp: Timestamp = Timestamp.now()) :
        WeatherReport()

    data class Daily(
        val sunshineDuration: Int = 0,
        val condition: Condition? = null,
        val date: Timestamp = Timestamp.now()
    ) : WeatherReport()

    data class Weekly(
        val days: List<Daily>) : WeatherReport()

    enum class Condition {
        SUNNY, CLOUDY, RAIN
    }

}