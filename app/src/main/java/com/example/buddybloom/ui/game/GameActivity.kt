package com.example.buddybloom.ui.game

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.databinding.ActivityGameBinding
import com.example.buddybloom.ui.authentication.AuthenticationActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var pvm: PlantViewModel
    private val profileFragment = ProfileFragment()
    private val startPagePlantFragment = StartPagePlantFragment()
    private val choosePlantFragment = ChoosePlantFragment()
    private var observerPlant : Plant? = null

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
        pvm = ViewModelProvider(this)[PlantViewModel::class.java]

        //Displays a toast when an error occurs
        pvm.errorMessage.observe(this) {
            it?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        if (FirebaseAuth.getInstance().currentUser == null) {
            navigateToLogin()
            return
        }

        // Add this line to check for plant when activity starts
        checkUserPlant()

        val bottomNavigationView: BottomNavigationView = binding.navbarMenu

        bottomNavigationView.setOnItemSelectedListener { item ->
            //unable to press plant page and profile unless you've chosen a plant first
            val hasPlant = pvm.localSessionPlant.value != null
            if (!hasPlant && (item.itemId == R.id.nav_profile || item.itemId == R.id.nav_plant)) {
                Toast.makeText(this, "Choose a plant first!", Toast.LENGTH_SHORT).show()
                return@setOnItemSelectedListener false
            }
            when (item.itemId) {
                R.id.nav_profile -> {
                    showFragment(profileFragment)
                    true
                }
                R.id.nav_plant -> {
                    showFragment(startPagePlantFragment)
                    true
                }
                R.id.nav_home -> {
                    showFragment(choosePlantFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fvc_game_activity)
        if (currentFragment?.javaClass != fragment.javaClass) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fvc_game_activity, fragment)
                .commit()
        }
    }

    private fun checkUserPlant() {
        pvm.localSessionPlant.observe(this) { plant ->
            observerPlant = plant
            //Check Authentication status
            if (FirebaseAuth.getInstance().currentUser == null) {
                navigateToLogin()
                return@observe
            }
            if (plant == null) {
                binding.navbarMenu.selectedItemId = R.id.nav_home
                showFragment(choosePlantFragment)
            } else {
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fvc_game_activity)
                if (currentFragment?.javaClass != profileFragment.javaClass) {
                    binding.navbarMenu.selectedItemId = R.id.nav_plant
                    showFragment(startPagePlantFragment)
                }
            }
        }
    }


//    //TODO kolla upp detta med att spara plantans stadie när man stänger ner
//    override fun onDestroy() {
//        super.onDestroy()
//        observerPlant?.let {
//            runBlocking {
//                pvm.updateRemotePlant(it)
//            }
//        }
//    }
}