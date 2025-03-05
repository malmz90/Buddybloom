package com.example.buddybloom.data.model

import com.google.firebase.Timestamp

data class PlantHistory(
    val name: String = "",
    val streakCount: Int = 0,
    val timestamp: Timestamp = Timestamp.now()
)