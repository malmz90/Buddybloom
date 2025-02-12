package com.example.buddybloom

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class PlantWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val myPlant = Plant("",100)
        myPlant.decreaseWaterLevel(10)
        myPlant.isThirsty()
    // Uppdatera UI eller spara data om det behövs

    // Returnera Result.success() när jobbet är klart
    return Result.success()
}
}