package com.example.buddybloom

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.buddybloom.data.model.WeatherReport
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlantWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private lateinit var firebaseManager: FirebaseManager
    override fun doWork(): Result {

        firebaseManager = FirebaseManager()

        firebaseManager.getCurrentUserPlant { plant ->
            if (plant != null) {
                plant.decreaseWaterLevel(10)
                plant.isThirsty()
                firebaseManager.saveUserPlant(plant) { success ->
                    if (success) {
                        Log.d("PlantWorker", "Plant data updated successfully!")
                    } else {
                        Log.e("PlantWorker", "Failed to update plant data.")
                    }
                }
            } else {
                Log.e("PlantWorker", "No plant found to update.")
            }
        }
        return Result.success()
    }


        updateWeather()

        return Result.success()
    }

    /**
     * Checks how many days have passed since the weather was last updated on Firebase and adds that amount of days
     * to the weather report. Rebuilds weather from scratch if weather is off sync.
     *
     */
    private fun updateWeather() {
        firebaseManager.getUserCreationDate { creationDate ->
            firebaseManager.getLastUpdated { lastUpdated ->
                firebaseManager.getCurrentWeatherReport { currentWeeklyReport ->
                    if (lastUpdated == null || currentWeeklyReport == null || creationDate == null) {
                        Log.e(
                            "PlantWorker",
                            "Error: lastUpdated = $lastUpdated, currentWeeklyReport = $currentWeeklyReport"
                        )
                        return@getCurrentWeatherReport // Exit early if data is missing
                    }

                    val currentTime = System.currentTimeMillis()
                    val elapsedTime = currentTime - lastUpdated.seconds * 1000
                    val daysMissed = TimeUnit.MILLISECONDS.toDays(elapsedTime).toInt()

                    var updatedReport: WeatherReport.Weekly = currentWeeklyReport
                    if (daysMissed > 0) {
                        repeat(daysMissed) {
                            updatedReport = passOneDay(updatedReport)
                        }
                    }

                    val actualCurrentDay = Calendar.getInstance()
                        .getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
                    val storedCurrentDay = updatedReport.dailyReports[0].weekDay
                    //Rebuilds the weather from scratch if it gets out of sync.
                    if (actualCurrentDay != storedCurrentDay) {
                        val totalDaysSinceCreation = TimeUnit.MILLISECONDS.toDays(currentTime)
                            .toInt() - TimeUnit.SECONDS.toDays(creationDate.seconds).toInt()
                        var newStartingWeatherReport =
                            firebaseManager.generateStartingSunnyWeeklyReport(
                                Calendar.getInstance().apply {
                                    time = creationDate.toDate()
                                }
                            )
                        repeat(totalDaysSinceCreation) {
                            newStartingWeatherReport = passOneDay(newStartingWeatherReport)
                        }
                        updatedReport = newStartingWeatherReport
                        Log.i("PlantWorker", "Weather rebuilt.")
                    }
                    firebaseManager.updateLastUpdated(Timestamp.now())
                    firebaseManager.updateWeatherReport(updatedReport)
                }
            }
        }
    }

    /**
     * Takes in a weather report and adds a new randomized sunny day to it.
     */
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
            weekDay = calendar.getDisplayName(
                Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()
            )
        )
        val newDailyReports = currentWeeklyReport.dailyReports.toMutableList().apply {
            add(nextDay)
            if (size > 7) removeAt(0)
        }
        return currentWeeklyReport.copy(dailyReports = newDailyReports)
    }
}
