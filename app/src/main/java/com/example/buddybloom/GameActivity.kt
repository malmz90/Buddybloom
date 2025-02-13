package com.example.buddybloom

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.buddybloom.databinding.ActivityGameBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class GameActivity : AppCompatActivity() {
    private val firebaseManager = FirebaseManager()
    lateinit var binding : ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGameBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Add this line to check for plant when activity starts
        checkUserPlant()

        val bottomNavigationView : BottomNavigationView = binding.navbarMenu

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
//                    replaceFragmentForNavbar(ProfileFragment())
//                    true
                    true
                }
                R.id.nav_plant -> {
                    replaceFragmentForNavbar(StartPagePlantFragment())
                    true
                }
                R.id.nav_home -> {
                    replaceFragmentForNavbar(ChoosePlantFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragmentForNavbar(StartPagePlantFragment())
        binding.navbarMenu.selectedItemId = R.id.nav_plant
    }

    private fun replaceFragmentForNavbar(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply{
            replace(R.id.fvc_game_activity, fragment)
            commit()
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