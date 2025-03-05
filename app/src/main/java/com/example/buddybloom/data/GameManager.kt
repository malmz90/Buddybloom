package com.example.buddybloom.data

import android.util.Log
import com.example.buddybloom.data.GameManager.LocalGameState.localDailyWeather
import com.example.buddybloom.data.GameManager.LocalGameState.localPlant
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.model.WeatherReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

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
     val onPlantEvent: (Plant?) -> Unit,

    /**
     * Choose what should happen when the auto save timer triggers.
     */
    private val onAutoSave: (Plant) -> Unit
) {

    private object LocalGameState {
        var localPlant: Plant? = null
        var localDailyWeather: WeatherReport.Daily? = null
    }

    companion object {
        //Set auto save timer
        private const val AUTO_SAVE_MINUTES = 2

        //Set game loop timer
        private const val GAME_LOOP_MINUTES = 60

        // *** --- Game modifiers. Tune these to balance the game. --- ***

        //When the user presses a button in the game
        const val WATER_INCREASE = 10
        const val WATERSPRAY_INCREASE_EASY = 8
        const val WATERSPRAY_INCREASE_MEDIUM = 5
        const val WATERSPRAY_INCREASE_HARD = 2

        const val FERTILIZER_INCREASE_EASY = 8
        const val FERTILIZER_INCREASE_MEDIUM = 5
        const val FERTILIZER_INCREASE_HARD = 3

        private const val WATER_DECREASE_EASY = 4
        private const val FERTILIZER_DECREASE_EASY = 1
        private const val WATER_DECREASE_MEDIUM = 7
        private const val FERTILIZER_DECREASE_MEDIUM = 3
        private const val WATER_DECREASE_HARD = 10
        private const val FERTILIZER_DECREASE_HARD = 5

        // *** -------------------------------------------------------- ***

        // Sets true if plant dies to stop loop while its dead.
        private var isPlantDead = false

        //Do not modify these
        private const val AUTO_SAVE_TIMER = (AUTO_SAVE_MINUTES * 60 * 1000).toLong()
        private const val GAME_LOOP_TIMER = (GAME_LOOP_MINUTES * 60 * 1000).toLong()
    }

    init {
        startAutoSave()
        startGameLoop()
    }

    /**
    This is whatever happens on each game tick (every hour). Use this to control game events.
     */
    fun runGameLoop(iterations: Int = 1) {
        // check if plant is dead before looping
        if (isPlantDead) {
            Log.d("GameManager", "Plant is dead, skipping game loop.")
            return  // Stop loop if plant is dead
        }
        // For every iteration check plant health
        for (i in 1..iterations) {
            //  is plant dead or alive
            if (localPlant?.waterLevel == 0 || localPlant?.fertilizerLevel == 0) {
                Log.d("GameManager", "Plant died due to lack of water or fertilizer.")
                isPlantDead = true
                onPlantEvent(null)  // Notify viewmodel plant is dead
                return  // Abort loop
            }
            else if ((localPlant?.waterLevel ?: 0) > 120) {
                Log.d("GameManager", "Plant died due to overwatering.")
                isPlantDead = true
                onPlantEvent(null)  // Notify plant is dead
                return  // Abort loop
            }
            // If plant is alive loop continues
            decreaseWaterLevel()
            decreaseFertilizerLevel()
        }
        // Update plant if it is alive
        onPlantEvent(localPlant)
    }

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
            while (!isPlantDead) {
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
    fun resetPlantDeathState() {
        isPlantDead = false // Reset death to start gameLoop

    }
    fun updateLocalDailyWeather(newWeather: WeatherReport.Daily) {
        localDailyWeather = newWeather
    }

    /**
     * get user plant and if it not already is infected by bugs it randomly pix a number
     * and if it lower than 10 it will be infected and a bug gif will show on screen,
     * user needs to press bug spray button to get plant healthy again.
     */
    private fun startRandomInfection() {
        localPlant?.let { plant ->
            if (plant.infected) {
                return
            }
            val randomValue = (0..50).random()

            if (randomValue <= 10) {
                plant.infected = true
                onPlantEvent(localPlant) // Update
            }
        }
    }

    /**
     * plant gets healthy after been infected by bugs
     */
    fun plantGetFreeFromBugs() {
        localPlant?.let { plant ->
            if (plant.infected) {
                plant.infected = false
                onPlantEvent(localPlant) // Update
            }
        }
    }

    /**
     * When the user presses the water button.
     */
    fun waterPlant() {
        localPlant?.let {
            it.waterLevel = (minOf(140, it.waterLevel + WATER_INCREASE))
            startRandomInfection()
            onPlantEvent(localPlant)
            // triggers to kill plant if overWatering plant when user presses watering button,
            // instead of waiting for loop
            if (it.waterLevel > 120) {
                onPlantEvent(null)
            } else {
                onPlantEvent(localPlant)
            }
        }
    }

    /**
     * When user presses on water spray button and increase waterLevel depending on
     * difficulty on plant
     */
    fun sprayWaterPlant(){
        localPlant?.let {
            it.waterLevel = when (it.difficulty.lowercase()) {
                "medium" -> minOf(100, it.waterLevel + WATERSPRAY_INCREASE_MEDIUM)
                "hard" -> minOf(100, it.waterLevel + WATERSPRAY_INCREASE_HARD)
                else -> {
                    minOf(100, it.waterLevel + WATERSPRAY_INCREASE_EASY)
                }
            }
            onPlantEvent(localPlant)
        }
    }

    /**
     * Water decrease in the game loop.
     */
    private fun decreaseWaterLevel() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        localPlant?.let {
            val baseLevel: Int = when (it.difficulty.lowercase()) {
                "medium" -> WATER_DECREASE_MEDIUM
                "hard" -> WATER_DECREASE_HARD
                else -> { WATER_DECREASE_EASY }
            }
            val sunnyLevel = when (localDailyWeather?.hourlyWeather?.get(currentHour)?.second) {
                WeatherReport.Condition.SUNNY -> baseLevel * 1.5
                WeatherReport.Condition.CLOUDY -> baseLevel
                WeatherReport.Condition.NIGHT -> baseLevel
                WeatherReport.Condition.PARTLY_CLOUDY -> baseLevel * 1.2
                WeatherReport.Condition.RAIN -> baseLevel
                WeatherReport.Condition.THUNDER -> baseLevel
                null -> baseLevel
            }
            if(localPlant!!.protectedFromSun) {
                it.waterLevel = maxOf(0, it.waterLevel - baseLevel)

            } else {
                it.waterLevel = maxOf(0, it.waterLevel - sunnyLevel.toInt())
            }
        }
    }

    fun toggleBlinds() {
        localPlant?.let {
            it.protectedFromSun = !it.protectedFromSun
        }
    }

    /**
     * When the user presses the fertilizer button. Amount depends on difficulty
     */
    fun addFertilizer() {
        localPlant?.let {
            it.fertilizerLevel = when (it.difficulty.lowercase()) {
                "medium" -> minOf(100, it.fertilizerLevel + FERTILIZER_INCREASE_MEDIUM)
                "hard" -> minOf(100, it.fertilizerLevel + FERTILIZER_INCREASE_HARD)
                else -> {
                    minOf(100, it.fertilizerLevel + FERTILIZER_INCREASE_EASY)
                }
            }
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
}