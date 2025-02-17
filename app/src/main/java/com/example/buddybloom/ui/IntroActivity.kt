package com.example.buddybloom.ui


import android.content.Intent
import android.os.Bundle
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
    }
}