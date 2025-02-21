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
    val difficulty: String = "Easy"
)