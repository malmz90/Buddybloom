package com.example.buddybloom.data

import android.util.Log
import com.example.buddybloom.data.GameEngine.LocalGameState.localPlant
import com.example.buddybloom.data.GameEngine.LocalGameState.localUserId
import com.example.buddybloom.data.GameEngine.LocalGameState.localWeatherReport
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.model.WeatherReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameEngine
    (
    private val scope: CoroutineScope,
    private val onPlantEvent: (Plant?) -> Unit,
    private val onAutoSave: (Plant) -> Unit
) {

    private object LocalGameState {
        var localUserId: String? = null
        var localPlant: Plant? = null
        var localWeatherReport: WeatherReport.Weekly? = null
    }

    companion object {
        //Set auto save timer
        private const val AUTO_SAVE_MINUTES = 2

        //Set game loop timer
        private const val GAME_LOOP_MINUTES = 1

        //Do not modify these
        const val AUTO_SAVE_TIMER = (AUTO_SAVE_MINUTES * 60 * 1000).toLong()
        const val GAME_LOOP_TIMER = (GAME_LOOP_MINUTES * 60 * 1000).toLong()

        //Modifiers for the game loop
        //TODO remake the model for easier handling of these
        private const val WATER_DECREASE_EASY = 10
        private const val FERTILIZER_DECREASE_EASY = 10
        private const val WATER_DECREASE_MEDIUM = 15
        private const val FERTILIZER_DECREASE_MEDIUM = 15
        private const val WATER_DECREASE_HARD = 20
        private const val FERTILIZER_DECREASE_HARD = 20

        //TODO Add weather modifiers
    }

    init {
        startAutoSave()
        startGameLoop ()
    }

    fun runGameLoop(iterations: Int = 1) {
        for (i in 1..iterations) {
            decreaseWaterLevel()
            decreaseFertilizerLevel()
        }
        onPlantEvent(localPlant)
    }


    fun initialSync(onInitialSync: (Plant?) -> Unit) {

    }

    private fun startAutoSave() {
        scope.launch {
            while (true) {
                delay(AUTO_SAVE_TIMER)
                Log.i("GameEngine", "AutoSave triggered")
                localPlant?.let {
                    withContext(Dispatchers.IO) {
                        onAutoSave(it)
                    }
                }
            }
        }
    }

    private fun startGameLoop() {
        scope.launch {
            while (true) {
                delay(GAME_LOOP_TIMER)
                Log.i("GameEngine", "Game loop triggered")
                runGameLoop()
            }
        }
    }


    fun setUserId(newId: String?) {
        localUserId = newId
    }

    fun updateLocalPlant(newPlant: Plant?) {
        localPlant = newPlant
        onPlantEvent(localPlant)
    }

    fun getPlant(): Plant? {
        return localPlant
    }

    fun waterPlant() {
        localPlant?.let {
            it.waterLevel = (minOf(100, it.waterLevel + 10))
            onPlantEvent(localPlant)
        }
    }

    private fun decreaseWaterLevel() {
        localPlant?.let {
            val newLevel: Int = when (it.difficulty.lowercase()) {
                "medium" -> maxOf(0, it.waterLevel - WATER_DECREASE_MEDIUM)
                "hard" -> maxOf(0, it.waterLevel - WATER_DECREASE_HARD)
                else -> {
                    maxOf(0, it.waterLevel - WATER_DECREASE_EASY)
                }
            }
            it.waterLevel = newLevel
        }
    }

    fun addFertilizer() {
        localPlant?.let {
            it.fertilizerLevel = (minOf(100, it.fertilizerLevel + 10))
            onPlantEvent(localPlant)
        }
    }

    private fun decreaseFertilizerLevel() {
        localPlant?.let {
            val newLevel: Int = when (it.difficulty.lowercase()) {
                "medium" -> maxOf(0, it.fertilizerLevel - FERTILIZER_DECREASE_MEDIUM)
                "hard" -> maxOf(0, it.fertilizerLevel - FERTILIZER_DECREASE_HARD)
                else -> {
                    maxOf(0, it.fertilizerLevel - FERTILIZER_DECREASE_EASY)
                }
            }
            it.fertilizerLevel = newLevel
        }
    }

    fun resetState() {
        localUserId = null
        localPlant = null
        localWeatherReport = null
    }
}
