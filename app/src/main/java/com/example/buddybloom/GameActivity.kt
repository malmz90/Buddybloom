package com.example.buddybloom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.buddybloom.databinding.ActivityGameBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class GameActivity : AppCompatActivity() {
    private val firebaseManager = FirebaseManager()
    lateinit var binding : ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_game)
        plantWorksSchedule() // Begin schedule

        binding = ActivityGameBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Temporary logout-button to return to HomeActivity.
        binding.btnLogout.setOnClickListener{
            val logoutIntent = Intent(this, HomeActivity::class.java)
            startActivity(logoutIntent)
        }

        // Add this line to check for plant when activity starts
        checkUserPlant()

        val bottomNavigationView : BottomNavigationView = binding.navbarMenu

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
//                    replaceFragmentForNavbar(ProfileFragment())
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
        /*
    Sets up a Schedule for plants works that should run in background and
     updates firestore, functions that runs is placed in Plant-class and PlantWorker-class.
     */
    private fun plantWorksSchedule() {
        val workManager = WorkManager.getInstance(this)

        workManager.getWorkInfosForUniqueWorkLiveData("PlantWateringWork")
            .observe(this, Observer { workInfos ->
                val isJobRunning = workInfos.any {
                    it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
                }

                if (!isJobRunning) {
                    val workRequest = PeriodicWorkRequestBuilder<PlantWorker>(
                        1, TimeUnit.HOURS
                    ).setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED) //Drives only with InternetService
                            .build()
                    ).build()

                    workManager.enqueueUniquePeriodicWork(
                        "PlantWateringWork",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        workRequest
                    )

                    Log.d("WorkManager", "WorkManager schemalagd!")
                } else {
                    Log.d("WorkManager", "Jobb är redan schemalagt och körs.")
                }
            })
    }
}