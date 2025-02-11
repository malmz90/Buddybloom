package com.example.buddybloom.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {

    private lateinit var weatherRepository: WeatherRepository

    @Before
    fun setUp() {
        weatherRepository = WeatherRepository()
    }

    @Test
    fun `getWeeklyWeatherReport() returns a valid weekly report with 7 days`() {
        val weeklyReport = weatherRepository.getWeeklyWeatherReport()
        assertEquals(7, weeklyReport.dailyReports.size)
    }

    @Test
    fun `updateWeeklyWeatherReport() shifts daily reports by one day`() {
        val beforeUpdate = weatherRepository.getWeeklyWeatherReport()
        val firstDayBefore = beforeUpdate.dailyReports.first()

        weatherRepository.updateWeeklyWeatherReport()
        val afterUpdate = weatherRepository.getWeeklyWeatherReport()
        val firstDayAfter = afterUpdate.dailyReports.first()

        assertEquals(7, afterUpdate.dailyReports.size)
        assertTrue(
            "First day's date should shift forward!",
            firstDayBefore.date.seconds < firstDayAfter.date.seconds
        )
    }
}