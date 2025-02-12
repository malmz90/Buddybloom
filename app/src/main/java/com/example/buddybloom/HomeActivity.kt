package com.example.buddybloom

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.buddybloom.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val registerFragment = RegisterFragment()
        val loginFragment = LoginFragment()

        binding.btnRegister.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fcv_home,RegisterFragment())
            transaction.commit()
        }

        binding.btnSignIn.setOnClickListener {
             val transaction = supportFragmentManager.beginTransaction()
             transaction.replace(R.id.fcv_home,LoginFragment())
             transaction.commit()
            //TODO REMOVE!!!
            WeatherDialogFragment().show(supportFragmentManager, null)
        }


        binding.btnAbout.setOnClickListener {
            val aboutInfoFragment = AboutInfoFragment()
            aboutInfoFragment.show(supportFragmentManager, "AboutInfoFragmentTag")
        }
    }
}