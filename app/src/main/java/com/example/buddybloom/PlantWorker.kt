package com.example.buddybloom

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class PlantWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

//        val myPlant = Plant("",100)
//        myPlant.decreaseWaterLevel(10)
//        myPlant.isThirsty()

        val firebaseManager = FirebaseManager()

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
}