package com.example.buddybloom.data

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.buddybloom.data.model.PlantManager
import com.example.buddybloom.data.model.WeatherReport
import com.example.buddybloom.data.repository.PlantRepository
import com.example.buddybloom.data.repository.WeatherRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlantWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private lateinit var weatherRepository: WeatherRepository
    private lateinit var plantRepository: PlantRepository
    private val plantManager = PlantManager()

    override fun doWork(): Result {

        weatherRepository = WeatherRepository()
        plantRepository = PlantRepository()


        decreaseWaterLevelForCurrentPlant()
        updateWeather()
        return Result.success()
    }

    private fun decreaseWaterLevelForCurrentPlant() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("PlantWorker", "No user logged in, cannot update plant.")
            return
        }
        plantRepository.snapshotOfCurrentUserPlant { plant ->
            if (plant != null) {
           plantManager.decreaseWaterLevel(plant,10)

            plantRepository.saveUserPlant(plant) { success ->
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
    }

    /**
     * Checks how many days have passed since the weather was last updated on Firebase and adds that amount of days
     * to the weather report. Rebuilds weather from scratch if weather is off sync.
     *
     */
    private fun updateWeather() {
        weatherRepository.fetchCurrentWeatherReport { currentWeeklyReport ->

            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - currentWeeklyReport.lastUpdated.seconds * 1000
            val daysMissed = TimeUnit.MILLISECONDS.toDays(elapsedTime).toInt()

            //Update the report with the days passed since last login.
            var updatedReport: WeatherReport.Weekly = currentWeeklyReport
            if (daysMissed > 0) {
                updatedReport = weatherRepository.passDaysForWeather(updatedReport, daysMissed)
            }

            //Check if stored first weekday is out of sync of the actual week day for any reason
            // and create a new report when out of sync.
            val actualCurrentDay = Calendar.getInstance()
                .getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
            val storedCurrentDay = updatedReport.dailyReports.firstOrNull()?.weekDay
            if (actualCurrentDay != storedCurrentDay) {
                updatedReport =
                    weatherRepository.generateNewSunnyWeeklyReport()
                Log.i("PlantWorker", "Weather out of sync (rebuilt).")
            }
            weatherRepository.updateWeatherReport(updatedReport)
        }
    }
}