package com.example.buddybloom.data.model

data class User(
    var id: String = "",
    var email: String = "",
    var name: String = "",
    val weeklyWeatherReport: WeatherReport.Weekly? = null
)