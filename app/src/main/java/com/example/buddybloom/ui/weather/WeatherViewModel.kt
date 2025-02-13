package com.example.buddybloom.ui.weather

import androidx.lifecycle.LiveData

import androidx.lifecycle.ViewModel
import com.example.buddybloom.FirebaseManager
import com.example.buddybloom.data.model.WeatherReport


class WeatherViewModel : ViewModel() {
    private val firebaseManager = FirebaseManager()


    private var _weeklyWeatherReport = firebaseManager.getWeatherReport()
    val weeklyWeatherReport: LiveData<WeatherReport.Weekly?> get() = _weeklyWeatherReport



}