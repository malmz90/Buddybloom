package com.example.buddybloom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class GameActivity : AppCompatActivity() {
    private val firebaseManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Add this line to check for plant when activity starts
        checkUserPlant()

        // Existing code
        val startPagePlantFragment = StartPagePlantFragment()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navbar_menu)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
//                    val transaction = supportFragmentManager.beginTransaction()
//                    transaction.replace(R.id.fvc_game_activity,profileFragment)
//                    transaction.commit()
                    true
                }
                R.id.nav_plant -> {
                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fvc_game_activity,startPagePlantFragment)
                    transaction.commit()
                    true
                }
                R.id.nav_home -> {
                    val intent = Intent(this,HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
    fun showStartPagePlantFragment() {
        val startPagePlantFragment = StartPagePlantFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fvc_game_activity, startPagePlantFragment)
            .commit()
    }
    private fun checkUserPlant() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            showChoosePlantFragment()
            return
        }

        firebaseManager.getCurrentUserPlant { plant ->
            if (plant == null) {
                showChoosePlantFragment()
            } else {
                showStartPagePlantFragment()
            }
        }
    }


    private fun showChoosePlantFragment() {
        val choosePlantFragment = ChoosePlantFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fvc_game_activity, choosePlantFragment)
            .commit()
    }
}