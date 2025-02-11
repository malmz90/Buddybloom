package com.example.buddybloom

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
       // val profileFragment = ProfileFragment()
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
}