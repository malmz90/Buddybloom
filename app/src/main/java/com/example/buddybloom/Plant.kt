package com.example.buddybloom

import android.content.Context
import android.util.Log
import android.widget.Toast


data class Plant(val name : String, var waterLevel: Int) {
    constructor() : this("", 100)

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