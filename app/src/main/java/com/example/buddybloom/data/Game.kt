package com.example.buddybloom.data

import com.example.buddybloom.data.model.LocalGameState
import com.example.buddybloom.data.model.Plant

class Game(val state: LocalGameState) {

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

    fun runGameLoop(iterations: Int = 1) {
        for (i in 1..iterations) {
            decreaseWaterLevel()
            decreaseFertilizerLevel()
        }
    }

    fun setUserId(newId: String?) {
        state.updateUserId(newId)
    }

    fun setPlant(newPlant: Plant?) {
        state.updatePlant(newPlant)
    }

    fun waterPlant() {
        state.plant?.let {
            state.updateWaterLevel(minOf(100, it.waterLevel + 10))
        }
    }

    private fun decreaseWaterLevel() {
        state.plant?.let {
            val newLevel: Int = when (it.difficulty.lowercase()) {
                "medium" -> maxOf(0, it.waterLevel - WATER_DECREASE_MEDIUM)
                "hard" -> maxOf(0, it.waterLevel - WATER_DECREASE_HARD)
                else -> {
                    maxOf(0, it.waterLevel - WATER_DECREASE_EASY)
                }
            }
            state.updateWaterLevel(newLevel)
        }
    }

    fun addFertilizer() {
        state.plant?.let {
            state.updateFertilizerLevel(minOf(100, it.fertilizerLevel + 10))
        }
    }

    private fun decreaseFertilizerLevel() {
        state.plant?.let {
            val newLevel: Int = when (it.difficulty.lowercase()) {
                "medium" -> maxOf(0, it.fertilizerLevel - FERTILIZER_DECREASE_MEDIUM)
                "hard" -> maxOf(0, it.fertilizerLevel - FERTILIZER_DECREASE_HARD)
                else -> {
                    maxOf(0, it.fertilizerLevel - FERTILIZER_DECREASE_EASY)
                }
            }
            state.updateWaterLevel(newLevel)
        }
    }

    fun resetState() {
        state.reset()
    }
}