package com.example.buddybloom.ui

import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Looper.prepare
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.buddybloom.R
import com.example.buddybloom.ui.authentication.AuthenticationActivity

class IntroActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageView = findViewById(R.id.iv_logo_animated)
        Glide.with(this).load(R.raw.icon_animated).into(imageView)
        imageView.postDelayed({
            startActivity(Intent(this, AuthenticationActivity::class.java))
        }, 3000)

        // Creating Soundpool
        val startMelody = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1) // Amount of sounds
            .setAudioAttributes(startMelody)
            .build()

        // Loading sound
        soundId = soundPool.load(this, R.raw.intro_melody, 1)
        Log.d("SoundPool", "Sound ID: $soundId")

        soundPool.setOnLoadCompleteListener { _, _, status ->
            Log.d("SoundPool", "Load status: $status")
            if (status == 0) {
                // Playing sound
                soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                Log.d("Soundpool","Success play sound!")
            }  else {
            Log.d("SoundPool", "Failed to load sound")
           }
            }
        }

    // Cleans up when activity destroys
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}