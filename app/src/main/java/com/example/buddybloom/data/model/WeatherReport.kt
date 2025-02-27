package com.example.buddybloom.data.model


sealed class WeatherReport {

    data class Daily(
        val hourlyWeather: List<MyPair>? = null,
        val timestamp: Long = 0L
    ) : WeatherReport()

    data class MyPair(
        val first: Int = 0,
        val second: Condition = Condition.CLOUDY
    )

    enum class Condition {
        SUNNY, CLOUDY, PARTLY_CLOUDY, NIGHT, RAIN, THUNDER
    }
}