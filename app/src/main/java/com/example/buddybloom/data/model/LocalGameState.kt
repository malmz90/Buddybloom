package com.example.buddybloom.data.model



object LocalGameState {

    private var userId: String? = null
    var plant: Plant? = null
        private set
    var weatherReport: WeatherReport.Weekly? = null
        private set

     fun reset() {
        userId = null
        plant = null
    }

     fun updateUserId(newId: String?) {
        userId = newId
    }

     fun updateWaterLevel(level: Int) {
        plant?.let {
            it.waterLevel = level
        }
    }

     fun updateFertilizerLevel(level: Int) {
        plant?.let {
            it.fertilizerLevel = level
        }
    }

     fun updatePlant(newPlant: Plant?) {
        plant = newPlant
    }
}
