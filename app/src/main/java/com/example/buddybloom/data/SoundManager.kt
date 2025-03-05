package com.example.buddybloom.data

import android.content.Context
import android.media.SoundPool
import com.example.buddybloom.R

class SoundManager(
    context: Context,
) {

    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(3).build()
    private var waterSpraySound: Int = 0
    private var fertilizeSound: Int = 0
    private var wateringSound: Int = 0
    private var blindsSoundStart: Int = 0
    private var blindsSoundEnd: Int = 0
    private var bugSpraySound: Int = 0
    private var errorSound: Int = 0

    init {
        waterSpraySound = soundPool.load(context, R.raw.spray_sound, 1)
        fertilizeSound = soundPool.load(context, R.raw.fertilize_sound, 1)
        wateringSound = soundPool.load(context, R.raw.watering_sound, 1)
        blindsSoundStart = soundPool.load(context, R.raw.blinds_sound_start, 1)
        blindsSoundEnd = soundPool.load(context, R.raw.blinds_sound_end, 1)
        bugSpraySound = soundPool.load(context, R.raw.bugspray_sound, 1)
        errorSound = soundPool.load(context, R.raw.error_sound, 1)
    }

    fun playWateringSound() {
        soundPool.play(wateringSound, 1f, 1f, 0, 0, 1f)
    }

    fun playFertilizeSound(){
        soundPool.play(fertilizeSound, 1f, 1f, 0, 0, 1f)
    }

    fun playWaterSpraySound(){
        soundPool.play(waterSpraySound, 1f, 1f, 0, 0, 1f)
    }

    fun playBugSpraySound(){
        soundPool.play(bugSpraySound, 1f, 1f, 0, 0, 1f)
    }

    fun playBlindsSoundStart(){
        soundPool.play(blindsSoundStart, 1f, 1f, 0, 0, 1f)
    }

    fun playBlindsSoundEnd(){
        soundPool.play(blindsSoundEnd, 1f, 1f, 0, 0, 1f)
    }

    fun playErrorSound(){
        soundPool.play(errorSound, 1f, 1f, 0, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}