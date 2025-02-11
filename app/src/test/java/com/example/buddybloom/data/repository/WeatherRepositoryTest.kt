package com.example.buddybloom.data.repository

import com.example.buddybloom.data.model.WeatherReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryTest {

    private val weatherRepo = WeatherRepository()

    @Test
    fun `getRandomSunnyDailyWeather() always returns sunny and random sunshine duration (1-16)`() {
        val dailyWeather = weatherRepo.getDailyWeatherReport()
        repeat(1000) {
            assertEquals(WeatherReport.Condition.SUNNY, dailyWeather.condition)
            assertTrue(
                "Sunshine hours out of range! ${dailyWeather.sunshineDuration}",
                dailyWeather.sunshineDuration in 1..16
            )
        }
    }

}