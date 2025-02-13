package com.example.buddybloom

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.buddybloom.data.model.WeatherReport
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Locale

class PlantWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val myPlant = Plant("", 100)
        myPlant.decreaseWaterLevel(10)
        myPlant.isThirsty()
        // Uppdatera UI eller spara data om det behövs

        // Returnera Result.success() när jobbet är klart
        return Result.success()
    }
}
// TODO Denna funktion får "en dag att gå" och genererar en ny uppdaterad veckorapport.
//  Bör köras en gång om dagen och den nya rapporten stoppas in i updateWeatherReport() i FirebaseManager för att uppdatera på Firestore
//private fun passOneDay(currentWeeklyReport: WeatherReport.Weekly): WeatherReport.Weekly {
//    val lastDailyInReport = currentWeeklyReport.dailyReports.last()
//    val calendar = Calendar.getInstance()
//    calendar.time = lastDailyInReport.timestamp.toDate()
//    calendar.add(Calendar.DAY_OF_YEAR, 1)
//    val nextDay = WeatherReport.Daily(
//        sunshineDuration = (1..16).random(),
//        condition = WeatherReport.Condition.SUNNY,
//        timestamp = Timestamp(calendar.time),
//        temperature = (-5..25).random(),
//        weekDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
//    )
//    val newDailyReports = currentWeeklyReport.dailyReports.toMutableList().apply {
//        add(nextDay)
//        if (size > 7) removeAt(0)
//    }
//    return currentWeeklyReport.copy(dailyReports = newDailyReports)
//}