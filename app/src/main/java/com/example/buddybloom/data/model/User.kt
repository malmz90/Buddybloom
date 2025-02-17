package com.example.buddybloom.data.model

import com.google.firebase.Timestamp

data class User(
    var id: String = "",
    var email: String = "",
    var name: String = "",
    var userPlants: MutableList<Plant> = mutableListOf(),
    val weeklyWeatherReport: WeatherReport.Weekly? = null,
    val lastUpdated: Timestamp = Timestamp.now(),
    val creationDate: Timestamp = Timestamp.now()
) {
    // Required no-argument constructor for Firebase
    constructor() : this("", "", "", mutableListOf())
}
