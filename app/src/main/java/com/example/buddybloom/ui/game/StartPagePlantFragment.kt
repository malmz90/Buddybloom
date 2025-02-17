package com.example.buddybloom.ui.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.repository.PlantRepository
import com.example.buddybloom.databinding.FragmentStartPagePlantBinding
import com.example.buddybloom.ui.weather.WeatherDialogFragment


class StartPagePlantFragment : Fragment() {

    private val plantRepository = PlantRepository()
    private var userPlant: Plant? = null
    private lateinit var binding : FragmentStartPagePlantBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartPagePlantBinding.inflate(inflater, container, false)

        // Get user's plant from Firebase
        plantRepository.getCurrentUserPlant { plant ->
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
                    val dailyChecksDialog = PlantNeedsDialogFragment()
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