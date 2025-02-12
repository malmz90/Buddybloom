package com.example.buddybloom.data.repository

import java.util.Calendar
import com.example.buddybloom.data.model.WeatherReport
import com.google.firebase.Timestamp

class WeatherRepository {

    private var mockWeeklyReport: WeatherReport.Weekly = generateMockSunnyWeeklyReport()

    fun getWeeklyWeatherReport(): WeatherReport.Weekly = mockWeeklyReport

    fun updateWeeklyWeatherReport() {
        mockWeeklyReport = passOneMockDay(mockWeeklyReport)
    }

    /**
     * Manually pass one day in the mock weekly report for testing.
     */
    private fun passOneMockDay(currentWeeklyReport: WeatherReport.Weekly): WeatherReport.Weekly {
        val lastDailyInReport = currentWeeklyReport.dailyReports.last()
        val calendar = Calendar.getInstance()
        calendar.time = lastDailyInReport.date.toDate()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val nextDay = WeatherReport.Daily(
            sunshineDuration = (1..16).random(),
            condition = WeatherReport.Condition.SUNNY,
            date = Timestamp(calendar.time),
        )
        val newDailyReports = currentWeeklyReport.dailyReports.toMutableList().apply {
            add(nextDay)
            if (size > 7) removeAt(0)
        }
        return currentWeeklyReport.copy(dailyReports = newDailyReports)

    }

    /**
     * Generates a weekly weather report for testing with 7 randomized daily reports (without any hourly data)
     */
    private fun generateMockSunnyWeeklyReport(): WeatherReport.Weekly {
        val startingDate = Calendar.getInstance()
        startingDate.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val days = mutableListOf<WeatherReport.Daily>()
        repeat(7) {
            val randomSunshineDuration = (1..16).random()
            val dailyReport = WeatherReport.Daily(
                sunshineDuration = randomSunshineDuration,
                date = Timestamp(startingDate.time),
                condition = WeatherReport.Condition.SUNNY
            )
            days.add(dailyReport)
            startingDate.add(Calendar.DAY_OF_YEAR, 1)
        }
        return WeatherReport.Weekly(dailyReports = days)
    }
}
