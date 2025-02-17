package com.example.buddybloom.ui.game

import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.buddybloom.R
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

    @RequiresApi(Build.VERSION_CODES.P)
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

                    // Show the animation of watering can.
                    val drawable: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.gif_water)
                    if (drawable is AnimatedImageDrawable) {
                        binding.ivAnimationWateringCan.visibility = View.VISIBLE
                        binding.ivAnimationWateringCan.setImageDrawable(drawable)
                        drawable.start()

                        // Hide the animation after 3 seconds.
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.ivAnimationWateringCan.visibility = View.INVISIBLE
                        }, 3000)
                    }
                }

                btnFertilize.setOnClickListener {
                    Toast.makeText(requireContext(),
                        "Your plant increased nutrition with 10",
                        Toast.LENGTH_SHORT).show()

                    val drawable: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.gif_nutrition)
                    if (drawable is AnimatedImageDrawable) {
                        binding.ivAnimationWateringCan.visibility = View.VISIBLE
                        binding.ivAnimationWateringCan.setImageDrawable(drawable)
                        drawable.start()

                        // Hide the animation after 3 seconds.
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.ivAnimationWateringCan.visibility = View.INVISIBLE
                        }, 3000)
                    }
                }

                var isBlindsVisible = false // Boolean for blinds toggle button.
                switchBlinds.setOnClickListener {
                    // Toggling between visible/invisible on the blinds.
                    isBlindsVisible = !isBlindsVisible
                    binding.ivBlinds.setImageResource(R.drawable.iconimg_blinds)
                    if(isBlindsVisible) {
                        binding.ivBlinds.visibility = View.VISIBLE
                        Toast.makeText(
                            requireContext(),
                            "You've successfully protected your plant!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.ivBlinds.visibility = View.INVISIBLE
                        Toast.makeText(
                            requireContext(),
                            "You've removed your protection from your plant!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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