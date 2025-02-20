package com.example.buddybloom.ui.game

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import com.example.buddybloom.R
import com.example.buddybloom.data.PlantWorker
import com.example.buddybloom.databinding.ActivityGameBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.TimeUnit

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var plantViewModel: PlantViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        plantWorksSchedule() // Begin schedule
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        plantViewModel = ViewModelProvider(this)[PlantViewModel::class.java]

        // Add this line to check for plant when activity starts
        checkUserPlant()

        val bottomNavigationView: BottomNavigationView = binding.navbarMenu

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (plantViewModel.currentPlant.value == null && (item.itemId == R.id.nav_profile || item.itemId == R.id.nav_home)){
                Snackbar.make(binding.root, "You must choose a plant first!", Snackbar.LENGTH_SHORT).show()
                return@setOnItemSelectedListener false
            }
            when (item.itemId) {
                R.id.nav_profile -> {
                    showFragment(ProfileFragment())
                    true
                }

                R.id.nav_plant -> {
                    showFragment(StartPagePlantFragment())
                    true
                }

                R.id.nav_home -> {
                    showFragment(ChoosePlantFragment())
                    true
                }

                else -> false
            }
        }
        showFragment(StartPagePlantFragment())
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fvc_game_activity, fragment)
            .commit()
    }

    private fun checkUserPlant() {
        plantViewModel.currentPlant.observe(this) { plant ->
            val bottomNavigationView : BottomNavigationView = binding.navbarMenu

            if (plant == null) {
                binding.navbarMenu.selectedItemId = R.id.nav_home
                showFragment(ChoosePlantFragment())

                //Unable to press home or profile if not choosen a plant first time logged in
                bottomNavigationView.menu.findItem(R.id.nav_profile).isEnabled = false
                bottomNavigationView.menu.findItem(R.id.nav_home).isEnabled = false
            } else {
                binding.navbarMenu.selectedItemId = R.id.nav_plant
                Log.d("GameAct", "User already has a plant")
                showFragment(StartPagePlantFragment())

                bottomNavigationView.menu.findItem(R.id.nav_profile).isEnabled = true
                bottomNavigationView.menu.findItem(R.id.nav_home).isEnabled = true
            }
        }
    }

    /**
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
                        15, TimeUnit.MINUTES
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