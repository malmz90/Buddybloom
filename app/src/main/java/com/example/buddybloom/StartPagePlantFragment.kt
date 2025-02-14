package com.example.buddybloom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.buddybloom.databinding.FragmentStartPagePlantBinding
import java.util.concurrent.TimeUnit


class StartPagePlantFragment : Fragment() {

    private val firebaseManager = FirebaseManager()
    private var userPlant: Plant? = null
    private lateinit var binding : FragmentStartPagePlantBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartPagePlantBinding.inflate(inflater, container, false)

        // Test if work is doing its job right one time
//        val workRequest = OneTimeWorkRequestBuilder<PlantWorker>()
//            .build()
//
//        WorkManager.getInstance(requireContext()).enqueue(workRequest)
        val workRequest = PeriodicWorkRequestBuilder<PlantWorker>(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "PlantWateringWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest

        )

        // shall check if workManager is running while app i closed, do not work yet
        val workManager = WorkManager.getInstance(requireContext())
        val workInfoLiveData = workManager.getWorkInfosByTagLiveData("myPlantTag")

        workInfoLiveData.observe(viewLifecycleOwner, Observer { workInfos ->

            workInfos?.forEach { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        Toast.makeText(requireContext(),
                            "Your plant lost water with 10 amounts",
                            Toast.LENGTH_SHORT).show()
                        Log.d("PlantWorker", "Work success!")
                    }
                    WorkInfo.State.FAILED -> {

                        Log.d("PlantWorker", "Work Faild!")
                    }

                    else -> {

                        Log.d("PlantWorker", "Work is going on!")
                    }
                }
            }
        })

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

                binding.btnWeather.setOnClickListener {
                    val weatherDialog = WeatherDialogFragment()
                    weatherDialog.show(parentFragmentManager, "WeatherDialogFragment")
                }

                binding.btnDailyCheck.setOnClickListener {
                    val dailyChecksDialog = DailyChecksDialogFragment()
                    dailyChecksDialog.show(parentFragmentManager, "DailyChecksDialogFragment")
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
}