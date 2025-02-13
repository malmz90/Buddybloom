package com.example.buddybloom

import com.example.buddybloom.data.model.WeatherReport

data class User(
    var id: String = "",
    var email: String = "",
    var name: String = "",
    var userPlants: MutableList<Plant> = mutableListOf(),
    val weeklyWeatherReport: WeatherReport.Weekly? = null
) {
    // Required no-argument constructor for Firebase
    constructor() : this("", "", "", mutableListOf())
}
