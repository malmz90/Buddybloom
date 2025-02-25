package com.example.buddybloom.data
//
//import android.content.Context
//import android.util.Log
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//import com.example.buddybloom.data.model.WeatherReport
//import com.example.buddybloom.data.repository.PlantRepository
//import com.example.buddybloom.data.repository.WeatherRepository
//import com.example.buddybloom.ui.game.PlantViewModel
//import com.google.firebase.auth.FirebaseAuth
//import java.util.Calendar
//import java.util.Locale
//import java.util.concurrent.TimeUnit
//
//class PlantWorker(appContext: Context, workerParams: WorkerParameters) :
//    Worker(appContext, workerParams) {
//
//    private lateinit var weatherRepository: WeatherRepository
//    private lateinit var plantRepository: PlantRepository
//    private val plantViewModel = PlantViewModel()
//
//    override fun doWork(): Result {
//        weatherRepository = WeatherRepository()
//        plantRepository = PlantRepository()
//
//        decreaseWaterLevelForCurrentPlant()
//        updateWeather()
//        return Result.success()
//    }
//
//    private fun decreaseWaterLevelForCurrentPlant() {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId == null) {
//            Log.e("PlantWorker", "No user logged in, cannot update plant.")
//            return
//        }
//        plantRepository.snapshotOfCurrentUserPlant { plant ->
//            if (plant != null) {
//           plantRepository.decreaseWaterLevel(plant,10)
//                plantViewModel.checkDifficultyFertilizeDecrease()
//
//            plantRepository.saveUserPlant(plant) { success ->
//                if (success) {
//                    Log.d("PlantWorker", "Plant data updated successfully!")
//                } else {
//                    Log.e("PlantWorker", "Failed to update plant data.")
//                }
//            }
//        } else {
//            Log.e("PlantWorker", "No plant found to update.")
//            }
//        }
//    }
//
//
//}