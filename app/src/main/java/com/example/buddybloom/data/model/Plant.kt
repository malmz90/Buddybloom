package com.example.buddybloom.data.model

import android.util.Log
import com.example.buddybloom.R


data class Plant(
    val name: String = "",
    var waterLevel: Int = 100,
    var sunLevel: Int = 100,
    var fertilizerLevel: Int = 100,
    val info: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    val streakDays: Int = 0,
    val difficulty: String = "Easy",
    var lastWaterUpdate: Long = System.currentTimeMillis()
) {

    companion object {
        const val WATER_DECREASE_AMOUNT = 10
        val UPDATE_INTERVAL = TimeInterval.MINUTE
    }

    fun updateWaterBasedOnTime() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastUpdate = currentTime - lastWaterUpdate
        val intervalsPassed = timeSinceLastUpdate / UPDATE_INTERVAL.milliseconds

        if (intervalsPassed >= 1) {
            val totalDecrease = (intervalsPassed * WATER_DECREASE_AMOUNT).toInt()
            waterLevel = maxOf(0, waterLevel - totalDecrease)
            lastWaterUpdate = currentTime
            Log.d("PlantStatus", "Water decreased by $totalDecrease after $intervalsPassed intervals")
        }
    }


    fun decreaseWaterLevel(amount: Int) {
        waterLevel = maxOf(0, waterLevel - amount)
        waterLevel -= amount
        Log.d("PlantStatus", "Your Plant lost water by $amount!")
    }

    fun plantThirsty(): Boolean {
        val isThirsty = waterLevel < 30
        if (isThirsty) {
            Log.d("PlantStatus", "Your plant is thirsty!")
        }
        return isThirsty
    }
}