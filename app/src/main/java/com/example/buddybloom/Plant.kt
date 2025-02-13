package com.example.buddybloom

import android.content.Context
import android.util.Log
import android.widget.Toast


data class Plant(val name : String, var waterLevel: Int, var createdAt: Long = System.currentTimeMillis()) {
    constructor() : this("", 100,System.currentTimeMillis())

    fun getPlantImage(): Int {
        val daysOld = (System.currentTimeMillis() - createdAt) / (1000 * 60 * 60 * 24)
    //  val daysOld = 3

        val stage = when {
            daysOld >= 6 -> 4
            daysOld >= 4 -> 3
            daysOld >= 2 -> 2
            else -> 1
        }

        return when(name.lowercase()) {
            "elephant" -> when(stage) {
                1 -> R.drawable.flower_elefant1
                2 -> R.drawable.flower_elefant2
                3 -> R.drawable.flower_elefant3
                4 -> R.drawable.flower_elefant4
                else -> R.drawable.flower_elefant1
            }
            "hibiscus" -> when(stage) {
                1 -> R.drawable.flower_hibiscus1
                2 -> R.drawable.flower_hibiscus2
                3 -> R.drawable.flower_hibiscus3
                4 -> R.drawable.flower_hibiscus4
                else -> R.drawable.flower_hibiscus1
            }
            "zebra" -> when(stage) {
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
        waterLevel -= amount
        if (waterLevel < 0) waterLevel = 0
    }

    fun increaseWaterLevel(amount: Int) {
        waterLevel += amount
        Log.d("PlantStatus", "Your plant increasde!")
        if (waterLevel > 100) waterLevel = 100
    }

    fun isThirsty(): Boolean {
        val isThirsty = waterLevel < 100
        if (isThirsty) {
            Log.d("PlantStatus", "Your plant is thirsty!")
        }

        return waterLevel < 30
    }

}