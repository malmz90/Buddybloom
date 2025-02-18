package com.example.buddybloom.data.model

import android.util.Log
import com.example.buddybloom.R
import com.example.buddybloom.data.repository.PlantRepository


data class Plant(
    val name: String = "",
    var waterLevel: Int = 100,
    var createdAt: Long = System.currentTimeMillis(),
    val streakDays: Int = 0
) {
    constructor() : this("", 100, System.currentTimeMillis())

    fun getPlantImage(): Int {
        val daysOld = (System.currentTimeMillis() - createdAt) / (1000 * 60 * 60 * 24)
        //  val daysOld = 3

        val stage = when {
            daysOld >= 6 -> 4
            daysOld >= 4 -> 3
            daysOld >= 2 -> 2
            else -> 1
        }

        return when (name.lowercase()) {
            "elephant" -> when (stage) {
                1 -> R.drawable.flower_elefant1
                2 -> R.drawable.flower_elefant2
                3 -> R.drawable.flower_elefant3
                4 -> R.drawable.flower_elefant4
                else -> R.drawable.flower_elefant1
            }

            "hibiscus" -> when (stage) {
                1 -> R.drawable.flower_hibiscus1
                2 -> R.drawable.flower_hibiscus2
                3 -> R.drawable.flower_hibiscus3
                4 -> R.drawable.flower_hibiscus4
                else -> R.drawable.flower_hibiscus1
            }

            "zebra" -> when (stage) {
                1 -> R.drawable.flower_zebra1
                2 -> R.drawable.flower_zebra2
                3 -> R.drawable.flower_zebra3
                4 -> R.drawable.flower_zebra4
                else -> R.drawable.flower_zebra1
            }

            else -> R.drawable.flower_elefant1
        }
    }

    fun decreaseWaterLevel(amount: Int) {
        waterLevel = maxOf(0, waterLevel - amount)
//        waterLevel -= amount
        Log.d("PlantStatus", "Your Plant lost water by $amount!")
    }

    fun isThirsty(): Boolean {
        val isThirsty = waterLevel < 30
        if (isThirsty) {
            Log.d("PlantStatus", "Your plant is thirsty!")
        }
        return isThirsty
    }
}