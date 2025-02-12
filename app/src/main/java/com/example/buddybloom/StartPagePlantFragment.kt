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

    private var binding : FragmentStartPagePlantBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val workRequest = PeriodicWorkRequestBuilder<PlantWorker>(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "PlantWateringWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        val binding = FragmentStartPagePlantBinding.inflate(inflater,container,false)
        val myPlant = Plant("elefant",100)
        val thirsty = myPlant.isThirsty()
        while(thirsty){
            myPlant.isThirsty()
        }


        binding.btnWater.setOnClickListener {
            myPlant.increaseWaterLevel(10)
            Toast.makeText(requireContext(), "Your plant increased water level with 10", Toast.LENGTH_SHORT).show()

        }
        binding.btnFertilize.setOnClickListener {
            Toast.makeText(requireContext(), "Your plant increased nutrition with 10", Toast.LENGTH_SHORT).show()
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.switchBlinds?.setOnClickListener {
            Toast.makeText(requireContext(), "You've successfully protected your plant!", Toast.LENGTH_SHORT).show()
        }

        binding?.imgBtnWaterspray?.setOnClickListener {
            Toast.makeText(requireContext(), "You've successfully sprayed water on your plant!", Toast.LENGTH_SHORT).show()
        }
        binding?.imgBtnBugspray?.setOnClickListener {
            Toast.makeText(requireContext(), "You've successfully saved your plant from bugs!", Toast.LENGTH_SHORT).show()
        }

    }
}