package com.example.buddybloom.data.model

import com.google.firebase.Timestamp

data class PlantHistory(
    val name: String = "",
    //TODO streakCount needs to be implemented
    val streakCount: Int = 0,
    val timestamp: Timestamp = Timestamp.now()
)