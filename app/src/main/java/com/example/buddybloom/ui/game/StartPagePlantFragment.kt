package com.example.buddybloom.ui.game

import android.graphics.Color
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.media.SoundPool
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
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.databinding.FragmentStartPagePlantBinding
import com.example.buddybloom.ui.weather.WeatherDialogFragment

class StartPagePlantFragment : Fragment() {

    private lateinit var binding: FragmentStartPagePlantBinding
    private lateinit var plantViewModel: PlantViewModel
    private lateinit var soundPool: SoundPool
    private var waterSpraySoundId: Int = 0
    private var fertilizeSoundId: Int = 0
    private var wateringSoundId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartPagePlantBinding.inflate(inflater, container, false)
        plantViewModel = ViewModelProvider(requireActivity())[PlantViewModel::class.java]
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Boolean for blinds toggle button.
        var isBlindsVisible = false
        plantViewModel.currentPlant.observe(viewLifecycleOwner) { plant ->
            binding.imgFlower.setImageResource(getPlantImageId(plant))
            binding.tvDaystreak.text = String.format(getDaysOld(plant).toString())
            binding.btnPlantNeeds.setOnClickListener {
                if (plant != null) {
                    val plantNeedsDialog = PlantNeedsDialogFragment.newInstance(plant)
                    plantNeedsDialog.show(parentFragmentManager, "PlantNeedsDialogFragment")
                }
            }
        }

        if (plantViewModel.isPlantThirsty()) {
            Toast.makeText(requireContext(), "Your plant is Thirsty", Toast.LENGTH_SHORT).show()
        }

        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        waterSpraySoundId = soundPool.load(requireContext(), R.raw.spray_sound, 1)
        fertilizeSoundId =  soundPool.load(requireContext(), R.raw.fertilize_sound, 1)
        wateringSoundId =  soundPool.load(requireContext(), R.raw.watering_sound, 1)

        binding.apply {
            btnWater.setOnClickListener {
                plantViewModel.increaseWaterLevel(10)
                Toast.makeText(
                    requireContext(),
                    "Your plant increased water level with 10",
                    Toast.LENGTH_SHORT
                ).show()

                // Play sound
                soundPool.play(wateringSoundId, 1f, 1f, 0, 0, 1f)

                // Show the animation of watering can.
                val drawable: Drawable? =
                    ContextCompat.getDrawable(requireContext(), R.drawable.gif_water)
                if (drawable is AnimatedImageDrawable) {
                    binding.ivAnimationWateringCan.visibility = View.VISIBLE
                    binding.ivAnimationWateringCan.setImageDrawable(drawable)
                    drawable.start()

                    //Disable button while animation is running.
                    binding.btnWater.setBackgroundColor(Color.parseColor("#DEDEDE"))
                    binding.btnWater.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.btnWater.isEnabled = false

                    // Hide the animation after 3 seconds.
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.ivAnimationWateringCan.visibility = View.INVISIBLE

                        // Enable button after animation is done.
                        binding.btnWater.setBackgroundColor(Color.parseColor("#F6F1DE"))
                        binding.btnWater.setTextColor(Color.parseColor("#246246"))
                        binding.btnWater.isEnabled = true

                    }, 3000)
                }
            }

            btnFertilize.setOnClickListener {
                plantViewModel.increaseFertilizeLevel(10)
                Toast.makeText(
                    requireContext(),
                    "Your plant increased nutrition with 10",
                    Toast.LENGTH_SHORT
                ).show()

                // Play sound
                soundPool.play(fertilizeSoundId, 1f, 1f, 0, 0, 1f)

                //show the animation of fertilizing
                val drawable: Drawable? =
                    ContextCompat.getDrawable(requireContext(), R.drawable.gif_fertilize)
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
            switchBlinds.setOnClickListener {
                // Toggling between visible/invisible on the blinds.
                isBlindsVisible = !isBlindsVisible
                binding.ivBlinds.setImageResource(R.drawable.iconimg_blinds)
                if (isBlindsVisible) {
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

            imgBtnWaterspray.setOnClickListener {
                plantViewModel.checkDifficultyWaterSpray()
                Toast.makeText(
                    requireContext(),
                    "You've successfully sprayed water on your plant!",
                    Toast.LENGTH_SHORT
                ).show()

                // Play sound
                soundPool.play(waterSpraySoundId, 1f, 1f, 0, 0, 1f)

                // show the animation for waterspray
                val drawable: Drawable? =
                    ContextCompat.getDrawable(requireContext(), R.drawable.gif_waterspray)
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

            imgBtnBugspray.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    "You've successfully saved your plant from bugs!",
                    Toast.LENGTH_SHORT
                ).show()

                //show the animation for bugspray
                val drawable: Drawable? =
                    ContextCompat.getDrawable(requireContext(), R.drawable.gif_bugspray)
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

    /**
     * Calculates how old the plant is in days, based on the current time and time of creation.
     */
    private fun getDaysOld(plant: Plant?): Int {
        if (plant != null) {
            val daysOld = (System.currentTimeMillis() - plant.createdAt) / (1000 * 60 * 60 * 24)
            return daysOld.toInt()
        } else {
            return 0
        }
    }

    /**
     * Returns the correct image based on how old the plant is.
     */
    private fun getPlantImageId(plant: Plant?): Int {
        if (plant == null) return R.drawable.icon_obs
        val daysOld = (System.currentTimeMillis() - plant.createdAt) / (1000 * 60 * 60 * 24)

        val stage = when {
            daysOld >= 6 -> 4
            daysOld >= 4 -> 3
            daysOld >= 2 -> 2
            else -> 1
        }

        return when (plant.name.lowercase()) {
            "elephant" -> when (stage) {
                1 -> R.drawable.flower_elefant1
                2 -> R.drawable.flower_elefant2
                3 -> R.drawable.flower_elefant3
                4 -> R.drawable.flower_elefant4
                else -> R.drawable.flower_elefant1
            }

            "hibiscus" -> when (stage) {
                1 -> R.drawable.flower_hibiscus1
                2 -> R.drawable.flower_hibiscus2
                3 -> R.drawable.flower_hibiscus3
                4 -> R.drawable.flower_hibiscus4
                else -> R.drawable.flower_hibiscus1
            }

            "zebra" -> when (stage) {
                1 -> R.drawable.flower_zebra1
                2 -> R.drawable.flower_zebra2
                3 -> R.drawable.flower_zebra3
                4 -> R.drawable.flower_zebra4
                else -> R.drawable.flower_zebra1
            }

            else -> R.drawable.flower_elefant1
        }
    }
}