package com.example.buddybloom.data.repository

import com.example.buddybloom.data.model.WeatherReport

class WeatherRepository {

    fun getDailyWeatherReport(): WeatherReport.Daily = randomSunnyDailyWeather()

    private fun randomSunnyDailyWeather(): WeatherReport.Daily {
        val sunshineDuration = (1..16).random()
        return WeatherReport.Daily(sunshineDuration, WeatherReport.Condition.SUNNY)
    }
}