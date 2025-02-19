package com.example.buddybloom.data.model

import android.util.Log

class PlantManager {
    companion object {
        const val WATER_DECREASE_AMOUNT = 10
        val UPDATE_INTERVAL = TimeInterval.MINUTE
    }

    fun updateWaterBasedOnTimePassed(plant: Plant) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastUpdate = currentTime - plant.lastWaterUpdate
        val intervalsPassed = timeSinceLastUpdate / UPDATE_INTERVAL.milliseconds

        if (intervalsPassed >= 1) {
            val totalDecrease = (intervalsPassed * WATER_DECREASE_AMOUNT).toInt()

            plant.waterLevel = maxOf(0, plant.waterLevel - totalDecrease)

            plant.lastWaterUpdate = currentTime

            Log.d("PlantStatus", "Water level now: $plant.waterLevel after $intervalsPassed intervals")
        }
    }

    fun decreaseWaterLevel(plant: Plant, amount: Int) {
        plant.waterLevel = maxOf(0, plant.waterLevel - amount)
        plant.waterLevel -= amount
        Log.d("PlantStatus", "Your Plant lost water by $amount!")
    }

}