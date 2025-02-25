package com.example.buddybloom.data

import android.util.Log
import com.example.buddybloom.data.GameManager.LocalGameState.localPlant
import com.example.buddybloom.data.GameManager.LocalGameState.localUserId
import com.example.buddybloom.data.GameManager.LocalGameState.localWeatherReport
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.model.WeatherReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles game logic and modifiers and keeps a local game session running.
 */
class GameManager
    (
    /**
     * Which scope the game engine attaches to. Choose something that is alive for the whole game session.
     */
    private val scope: CoroutineScope,
    /**
     * This is called whenever the plant receives an update in the local game session. Choose how to react to it.
     */
    private val onPlantEvent: (Plant?) -> Unit,
    /**
     * Choose what should happen when the auto save timer triggers.
     */
    private val onAutoSave: (Plant) -> Unit
    //TODO Add onError callback?
) {

    private object LocalGameState {
        //TODO Do we really need to keep the id for anything?
        var localUserId: String? = null
        var localPlant: Plant? = null
        var localWeatherReport: WeatherReport.Weekly? = null
    }

    companion object {
        //Set auto save timer
        private const val AUTO_SAVE_MINUTES = 3

        //Set game loop timer
        private const val GAME_LOOP_MINUTES = 60

        // *** --- Game modifiers. Tune these to balance the game. --- ***

        //When the user presses a button in the game
        const val WATER_INCREASE = 10
        const val FERTILIZER_INCREASE = 10

        //TODO remake the plant model for easier handling of these?
        private const val WATER_DECREASE_EASY = 7
        private const val FERTILIZER_DECREASE_EASY = 7
        private const val WATER_DECREASE_MEDIUM = 10
        private const val FERTILIZER_DECREASE_MEDIUM = 10
        private const val WATER_DECREASE_HARD = 15
        private const val FERTILIZER_DECREASE_HARD = 15

        // *** -------------------------------------------------------- ***


        //Do not modify these
        private const val AUTO_SAVE_TIMER = (AUTO_SAVE_MINUTES * 60 * 1000).toLong()
        private const val GAME_LOOP_TIMER = (GAME_LOOP_MINUTES * 60 * 1000).toLong()

        //TODO Add weather modifiers
    }

    init {
        startAutoSave()
        startGameLoop()
    }

    /**
    This is whatever happens on each game tick (every hour). Use this to control game events.
     */
    fun runGameLoop(iterations: Int = 1) {
        for (i in 1..iterations) {
            decreaseWaterLevel()
            decreaseFertilizerLevel()
        }
        onPlantEvent(localPlant)
    }


    fun initialSync() {}

    /**
     * The auto save coroutine timer.
     */
    private fun startAutoSave() {
        scope.launch {
            while (true) {
                delay(AUTO_SAVE_TIMER)
                localPlant?.let {
                    withContext(Dispatchers.IO) {
                        onAutoSave(it)
                    }
                }
                Log.i("GameEngine", "AutoSave triggered!")
            }
        }
    }

    /**
     * The game loop coroutine timer.
     */
    private fun startGameLoop() {
        scope.launch {
            while (true) {
                delay(GAME_LOOP_TIMER)
                runGameLoop()
                Log.i("GameEngine", "Game loop triggered!")
            }
        }
    }

    /**
     * Updates the local version of the plant (runtime memory).
     */
    fun updateLocalPlant(newPlant: Plant?) {
        localPlant = newPlant
        onPlantEvent(localPlant)
    }

    /**
     * Fetches the current local copy of the plant.
     */
    fun getPlant(): Plant? {
        return localPlant
    }

    /**
     * When the user presses the water button.
     */
    fun waterPlant() {
        localPlant?.let {
            it.waterLevel = (minOf(100, it.waterLevel + WATER_INCREASE))
            onPlantEvent(localPlant)
        }
    }

    /**
     * Water decrease in the game loop.
     */
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

    /**
     * When the user presses the fertilizer button.
     */
    fun addFertilizer() {
        localPlant?.let {
            it.fertilizerLevel = (minOf(100, it.fertilizerLevel + FERTILIZER_INCREASE))
            onPlantEvent(localPlant)
        }
    }

    /**
     * Fertilizer decrease in the game loop.
     */
    private fun decreaseFertilizerLevel() {
        localPlant?.let {
            val newLevel = when (it.difficulty.lowercase()) {
                "medium" -> maxOf(0, it.fertilizerLevel - FERTILIZER_DECREASE_MEDIUM)
                "hard" -> maxOf(0, it.fertilizerLevel - FERTILIZER_DECREASE_HARD)
                else -> {
                    maxOf(0, it.fertilizerLevel - FERTILIZER_DECREASE_EASY)
                }
            }
            it.fertilizerLevel = newLevel
        }
    }

    /**
     * Resets the local game state.
     */
    //TODO Make sure this is called when the user logs out?
    fun resetState() {
        localUserId = null
        localPlant = null
        localWeatherReport = null
    }
}
