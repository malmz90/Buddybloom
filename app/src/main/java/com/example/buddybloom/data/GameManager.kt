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
    private val onPlantEvent: (Plant?) -> Unit,
    /**
     * Choose what should happen when the auto save timer triggers.
     */
    private val onAutoSave: (Plant) -> Unit
    //TODO Add onError callback?
) {

    private object LocalGameState {
        var localPlant: Plant? = null
        var localDailyWeather: WeatherReport.Daily? = null
    }

    companion object {
        //Set auto save timer
        private const val AUTO_SAVE_MINUTES = 1

        //Set game loop timer
        private const val GAME_LOOP_MINUTES = 1

        // *** --- Game modifiers. Tune these to balance the game. --- ***

        //When the user presses a button in the game
        const val WATER_INCREASE = 10
        const val WATER_INCREASE_EASY = 8
        const val WATER_INCREASE_MEDIUM = 5
        const val WATER_INCREASE_HARD = 2

        const val FERTILIZER_INCREASE_EASY = 15
        const val FERTILIZER_INCREASE_MEDIUM = 10
        const val FERTILIZER_INCREASE_HARD = 7

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

        if (localPlant?.waterLevel == 0) {
            onPlantEvent(null) // Notify ViewModel to delete plant
        } else {
            onPlantEvent(localPlant)
        }
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

    fun updateLocalDailyWeather(newWeather: WeatherReport.Daily) {
        localDailyWeather = newWeather
    }

    /**
     * get user plant and if it not already is infected by bugs it randomly pix a number
     * and if it lower than 10 it will be infected and a bug gif will show on screen,
     * user needs to press bug spray button to get plant healthy again.
     */
    fun startRandomInfection() {
        localPlant?.let { plant ->
            if (plant.infected) {
                Log.d("GameManager", "Plant is already infected, skipping random check")
                return
            }
            val randomValue = (0..50).random()
            Log.d("GameManager", "Random value: $randomValue")

            if (randomValue <= 10) {
                plant.infected = true
                onPlantEvent(localPlant) // Update
                Log.d("GameManager", "Plant is now infected!")
            }
        }
    }

    /**
     * plant gets healhty after been infected by bugs
     */
    fun plantGetFreeFromBugs() {
        localPlant?.let { plant ->
            if (plant.infected) {
                plant.infected = false
                onPlantEvent(localPlant) // Update
                Log.d("GameManager", "Plant is now healthy!")
            }
        }
    }
    /**
     * When the user presses the water button.
     */
    fun waterPlant() {
        localPlant?.let {
            it.waterLevel = (minOf(100, it.waterLevel + WATER_INCREASE))
            startRandomInfection()
            onPlantEvent(localPlant)

        }
    }

    /**
     * When user presses on water spray button and increase waterLevel depending on
     * difficulty on plant
     */
    fun sprayWaterPlant(){
        localPlant?.let {
            it.waterLevel = when (it.difficulty.lowercase()) {
                "medium" -> minOf(100, it.waterLevel + WATER_INCREASE_MEDIUM)
                "hard" -> minOf(100, it.waterLevel + WATER_INCREASE_HARD)
                else -> {
                    minOf(100, it.waterLevel + WATER_INCREASE_EASY)
                }
            }
            onPlantEvent(localPlant)
        }
    }

    /**
     * Water decrease in the game loop.
     */
    private fun decreaseWaterLevel() {
        localPlant?.let {
            val baseLevel: Int = when (it.difficulty.lowercase()) {
                "medium" -> WATER_DECREASE_MEDIUM
                "hard" -> WATER_DECREASE_HARD
                else -> { WATER_DECREASE_EASY }
            }
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val sunnyLevel = when (localDailyWeather?.hourlyWeather?.get(currentHour)?.second) {
                WeatherReport.Condition.SUNNY -> baseLevel * 1.5
                WeatherReport.Condition.CLOUDY -> baseLevel
                WeatherReport.Condition.NIGHT -> baseLevel
                WeatherReport.Condition.PARTLY_CLOUDY -> baseLevel * 1.2
                WeatherReport.Condition.RAIN -> baseLevel
                WeatherReport.Condition.THUNDER -> baseLevel
                null -> baseLevel
            }
            Log.d("GameManager", "Base level: $baseLevel, Sunny level: $sunnyLevel")

            it.waterLevel = maxOf(0, it.waterLevel - sunnyLevel.toInt())
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