package com.example.buddybloom.data.model

data class User(
    var id: String = "",
    var email: String = "",
    var name: String = "",
    var userPlants: MutableList<Plant> = mutableListOf(),
    val weeklyWeatherReport: WeatherReport.Weekly? = null,
) {
    // Required no-argument constructor for Firebase
    constructor() : this("", "", "", mutableListOf())
}
