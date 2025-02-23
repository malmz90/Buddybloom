package com.example.buddybloom.data.model

import android.util.Log
import com.example.buddybloom.R
import com.google.firebase.Timestamp

data class Plant(
    val name: String = "",
    var waterLevel: Int = 100,
    var sunLevel: Int = 100,
    var fertilizerLevel: Int = 100,
    val info: String = "",
    var createdAt: Timestamp = Timestamp.now(),
    var lastUpdated: Timestamp = Timestamp.now(),
    val difficulty: String = "Easy"
)