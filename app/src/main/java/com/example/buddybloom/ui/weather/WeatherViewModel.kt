package com.example.buddybloom.ui.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.buddybloom.data.repository.WeatherRepository
import com.example.buddybloom.data.model.WeatherReport

class WeatherViewModel : ViewModel() {
    private val weatherRepository = WeatherRepository()

    private var _weeklyWeatherReport = weatherRepository.getWeatherReportLiveData()
    val weeklyWeatherReport: LiveData<WeatherReport.Weekly?> get() = _weeklyWeatherReport
}