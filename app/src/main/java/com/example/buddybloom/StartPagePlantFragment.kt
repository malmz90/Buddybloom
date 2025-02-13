package com.example.buddybloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.buddybloom.databinding.FragmentStartPagePlantBinding
import java.util.concurrent.TimeUnit


class StartPagePlantFragment : Fragment() {
    private var binding: FragmentStartPagePlantBinding? = null
    private val firebaseManager = FirebaseManager()
    private var userPlant: Plant? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartPagePlantBinding.inflate(inflater, container, false)


        val workRequest = PeriodicWorkRequestBuilder<PlantWorker>(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "PlantWateringWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        // Get user's plant from Firebase
        firebaseManager.getCurrentUserPlant { plant ->
            plant?.let {
                userPlant = it
                activity?.runOnUiThread {
                    setupPlantUI()
                }
            }
        }

        return binding?.root
    }

    private fun setupPlantUI() {
        binding?.apply {
            userPlant?.let { plant ->
                // Set the plant image
                imgFlower.setImageResource(plant.getPlantImage())

                btnWater.setOnClickListener {
                    plant.increaseWaterLevel(10)
                    Toast.makeText(requireContext(),
                        "Your plant increased water level with 10",
                        Toast.LENGTH_SHORT).show()
                }

                btnFertilize.setOnClickListener {
                    Toast.makeText(requireContext(),
                        "Your plant increased nutrition with 10",
                        Toast.LENGTH_SHORT).show()
                }

                switchBlinds.setOnClickListener {
                    Toast.makeText(requireContext(),
                        "You've successfully protected your plant!",
                        Toast.LENGTH_SHORT).show()
                }

                imgBtnWaterspray.setOnClickListener {
                    Toast.makeText(requireContext(),
                        "You've successfully sprayed water on your plant!",
                        Toast.LENGTH_SHORT).show()
                }

                imgBtnBugspray.setOnClickListener {
                    Toast.makeText(requireContext(),
                        "You've successfully saved your plant from bugs!",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}