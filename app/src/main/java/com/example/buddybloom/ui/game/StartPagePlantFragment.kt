package com.example.buddybloom.ui.game

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.buddybloom.R
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.repository.AccountRepository
import com.example.buddybloom.databinding.FragmentStartPagePlantBinding
import com.example.buddybloom.ui.authentication.AccountViewModel
import com.example.buddybloom.ui.authentication.AccountViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class StartPagePlantFragment : Fragment() {

    private lateinit var binding: FragmentStartPagePlantBinding
    private lateinit var pvm: PlantViewModel
    private lateinit var avm: AccountViewModel
    private lateinit var soundPool: SoundPool
    private var waterSpraySound: Int = 0
    private var fertilizeSound: Int = 0
    private var wateringSound: Int = 0
    private var blindsSoundStart: Int = 0
    private var blindsSoundEnd: Int = 0
    private var bugSpraySound: Int = 0
    private var errorSound: Int = 0
    private var testPlant: Plant? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("!!!", "PlantFragment onCreateView")
        binding = FragmentStartPagePlantBinding.inflate(inflater, container, false)
        pvm = ViewModelProvider(requireActivity())[PlantViewModel::class.java]
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val accountRepository = AccountRepository(FirebaseAuth.getInstance(), googleSignInClient)
        val factory = AccountViewModelFactory(accountRepository)
        avm = ViewModelProvider(requireActivity(), factory)[AccountViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pvm.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        //Observes if user is logging in so null pic won't show, if slow network progressbar shows instead
        avm.isLoggingIn.observe(viewLifecycleOwner) { isLoggingIn ->
            binding.loadingProgressBar.visibility = if (isLoggingIn) View.VISIBLE else View.GONE
            binding.imgFlower.visibility = if (isLoggingIn) View.GONE else View.VISIBLE
        }

        binding.ivBlinds.setImageResource(R.drawable.iconimg_blinds)

        pvm.plantJustDied.observe(viewLifecycleOwner) { justDied ->
            if(justDied) {
                checkPlantDeath()
                pvm.resetPlantDeathState()
            }
        }

        pvm.localSessionPlant.observe(viewLifecycleOwner) { plant ->

            //Checks if user is logging in
            testPlant = plant

            binding.switchBlinds.isChecked = testPlant?.protectedFromSun ?: false
            updateBlindsVisibility(binding.ivBlinds)

            if (plant == null) return@observe
            if (avm.isLoggingIn.value != true) {
                binding.imgFlower.setImageResource(getPlantImageId(plant))
                binding.tvDaystreak.text = String.format(getDaysOld(plant).toString())
                //Progress indicator
                "${plant.waterLevel}%".also { binding.tvWaterLevel.text = it }
                binding.progressWater.progress = plant.waterLevel

                if (plant.infected) {
                    binding.imgBtnBugspray.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#852221"))
                    binding.imgBtnBugspray.animate()
                        .alpha(0.5f)
                        .setDuration(500)
                        .withEndAction {
                            binding.imgBtnBugspray.animate().alpha(1f).setDuration(500)
                                .start()
                        }
                        .start()
                } else {
                    binding.imgBtnBugspray.clearAnimation()
                    binding.imgBtnBugspray.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_circle)
                    binding.imgBtnBugspray.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.turquise
                        )
                    )
                }
                binding.btnPlantNeeds.setOnClickListener {
                    val plantNeedsDialog = PlantNeedsDialogFragment.newInstance(plant)
                    plantNeedsDialog.show(parentFragmentManager, "PlantNeedsDialogFragment")
                }
            }
        }

        if (pvm.isPlantThirsty()) {
            Toast.makeText(requireContext(), "Your plant is Thirsty", Toast.LENGTH_SHORT)
                .show()
        }

        soundPool = SoundPool.Builder().setMaxStreams(3).build()
        waterSpraySound = soundPool.load(requireContext(), R.raw.spray_sound, 1)
        fertilizeSound = soundPool.load(requireContext(), R.raw.fertilize_sound, 1)
        wateringSound = soundPool.load(requireContext(), R.raw.watering_sound, 1)
        blindsSoundStart = soundPool.load(requireContext(), R.raw.blinds_sound_start, 1)
        blindsSoundEnd = soundPool.load(requireContext(), R.raw.blinds_sound_end, 1)
        bugSpraySound = soundPool.load(requireContext(), R.raw.bugspray_sound, 1)
        errorSound = soundPool.load(requireContext(),R.raw.error_sound,1)
        showInfectedBugGif()// if it`s infected gif shows even if restart app

        binding.apply {

//------------------------------------WATER---------------------------------------------------------
            btnWater.setOnClickListener {

                // Play sound
                soundPool.play(wateringSound, 1f, 1f, 0, 0, 1f)

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
                        pvm.waterPlant()
                        checkPlantDeath()
                        showInfectedBugGif()}
                        , 3000)
                }
            }

            imgBtnWaterspray.setOnClickListener {
                // Play sound
                soundPool.play(waterSpraySound, 1f, 1f, 0, 0, 1f)

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
                        pvm.waterSpray()
                        checkPlantDeath()
                    }, 3000)


                }
            }


//------------------------------------FERTILIZE-------------------------------------------------------
            btnFertilize.setOnClickListener {

                // Play sound
                soundPool.play(fertilizeSound, 1f, 1f, 0, 0, 1f)

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
                        pvm.fertilizePlant()
                        checkPlantDeath()
                    }, 3000)
                }
            }


//------------------------------------BLINDS-------------------------------------------------------
            switchBlinds.setOnClickListener {
                pvm.toggleBlinds()
                if (testPlant!!.protectedFromSun) {
                    // Play sound
                    soundPool.play(blindsSoundStart, 1f, 1f, 0, 0, 1f)

                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.ivBlinds.visibility = View.VISIBLE

                    }, 1000)
                } else {
                    // Play sound
                    soundPool.play(blindsSoundEnd, 1f, 1f, 0, 0, 1f)
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.ivBlinds.visibility = View.INVISIBLE
                    }, 1000)
                }
            }


//------------------------------------WEATHER-------------------------------------------------------
            binding.btnWeather.setOnClickListener {
                val weatherDialog = WeatherDialogFragment()
                weatherDialog.show(parentFragmentManager, "WeatherDialogFragment")
            }


//------------------------------------BUGSPRAY-------------------------------------------------------
            imgBtnBugspray.setOnClickListener {
                //Checks if plant is infected or not
                pvm.localSessionPlant.value?.let { currentPlant ->
                    if (currentPlant.infected) {

                        // Play sound
                        soundPool.play(bugSpraySound, 1f, 1f, 0, 0, 1f)

                        // If plant is infected show buggspray-gif
                        val drawableBugSpray: Drawable? =
                            ContextCompat.getDrawable(requireContext(), R.drawable.gif_bugspray)
                        if (drawableBugSpray is AnimatedImageDrawable) {
                            binding.ivAnimationWateringCan.setImageDrawable(drawableBugSpray)
                            binding.imgBtnBugspray.setBackgroundColor(Color.TRANSPARENT)
                            binding.ivAnimationWateringCan.visibility = View.VISIBLE
                            drawableBugSpray.start()}

                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.ivAnimationWateringCan.visibility = View.INVISIBLE
                            pvm.sprayOnBugs()
                            binding.ivInfectedBug.visibility = View.INVISIBLE
                        }, 3000)

                    } else {
                        //Play Sound
                        soundPool.play(errorSound, 1f, 1f, 0, 0, 1f)

                        // If plant not is infected show NoBug gif
                        val drawableNoBugs: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.gif_bug)
                        if (drawableNoBugs is AnimatedImageDrawable) {
                            binding.ivAnimationWateringCan.visibility = View.VISIBLE
                            binding.ivAnimationWateringCan.setImageDrawable(drawableNoBugs)
                            drawableNoBugs.start()

                            // Hide gif after 3 seconds
                            Handler(Looper.getMainLooper()).postDelayed({
                                binding.ivAnimationWateringCan.visibility = View.INVISIBLE
                            }, 3000)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
        Log.d("!!!", "PlantFragment onDestroyView")
    }

    /**
     * Calculates how old the plant is in days, based on the current time and time of creation.
     */
    private fun getDaysOld(plant: Plant?): Int {
        if (plant != null) {
            val daysOld =
                ((System.currentTimeMillis() - (plant.createdAt.seconds * 1000)) / (1000 * 60 * 60 * 24))
            return daysOld.toInt()
        } else {
            return 0
        }
    }

    /**
     *  function shows infected gif
      */
    private fun showInfectedBugGif() {
        pvm.localSessionPlant.value?.let { currentPlant ->
            if (currentPlant.infected) {
                val bugDrawable: Drawable? = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.gif_infected_bugs
                )
                if (bugDrawable is AnimatedImageDrawable) {
                    binding.ivInfectedBug.visibility = View.VISIBLE
                    binding.ivInfectedBug.setImageDrawable(bugDrawable)
                    bugDrawable.start()
                }
            }
        }
    }
//*******************
    private fun updateBlindsVisibility(myImageView: View) {
        if (testPlant?.protectedFromSun == true) {
            myImageView.visibility = View.VISIBLE
        } else {
            myImageView.visibility = View.INVISIBLE
        }
    }
//*******************

    private fun getPlantStage(plant:Plant):Int {
        val daysOld = ((System.currentTimeMillis() - (plant.createdAt.seconds * 1000)) / (1000 * 60 * 60 * 24))

        return when {
            daysOld >= 6 -> 6
            daysOld >= 4 -> 4
            daysOld >= 2 -> 2
            else -> 1
        }
    }

    /**
     * Returns the correct image based on how old the plant is.
     */
    private fun getPlantImageId(plant: Plant?, deathStage: Int? = null): Int {
        if (plant == null) return R.drawable.icon_obs

        // Check if plant is dried (water level below 30)
        if (plant.waterLevel < 30) {
            val stage = deathStage ?: getPlantStage(plant)
            return when (plant.name.lowercase()) {
                "elephant" ->  when(stage) {
                    1 -> R.drawable.flower_elephant_dead1_2
                    2 -> R.drawable.flower_elephant_dead1_2
                    3 -> R.drawable.flower_elephant_dead3
                    4 -> R.drawable.flower_elephant_dead4
                    5 -> R.drawable.flower_elephant_dead5
                    6 -> R.drawable.flower_elephant_dead6
                    else -> R.drawable.flower_elephant_dead1_2
                }
                "hibiscus" -> when(stage) {
                    1 -> R.drawable.flower_hibiscus_dead1
                    2 -> R.drawable.flower_hibiscus_dead2_3
                    3 -> R.drawable.flower_hibiscus_dead2_3
                    4 -> R.drawable.flower_hibiscus_dead4
                    5 -> R.drawable.flower_hibiscus_dead5
                    else -> R.drawable.flower_hibiscus_dead1
                }
                "zebra" -> when(stage) {
                    1 -> R.drawable.flower_zebra_dead1_2
                    2 -> R.drawable.flower_zebra_dead1_2
                    3 -> R.drawable.flower_zebra_dead3
                    4 -> R.drawable.flower_zebra_dead4
                    5 -> R.drawable.flower_zebra_dead5
                    6 -> R.drawable.flower_zebra_dead6
                    else -> R.drawable.flower_zebra_dead1_2
                }
                "ficus" -> when(stage) {
                    1 -> R.drawable.flower_ficus_dead1_2
                    2 -> R.drawable.flower_ficus_dead1_2
                    3 -> R.drawable.flower_ficus_dead3
                    4 -> R.drawable.flower_ficus_dead4
                    5 -> R.drawable.flower_ficus_dead5
                    6 -> R.drawable.flower_ficus_dead6
                    else -> R.drawable.flower_ficus_dead1_2
                }
                "coleus" -> when(stage) {
                    1 -> R.drawable.flower_coleus_dead1_2
                    2 -> R.drawable.flower_coleus_dead1_2
                    3 -> R.drawable.flower_coleus_dead3
                    4 -> R.drawable.flower_coleus_dead4
                    5 -> R.drawable.flower_coleus_dead5
                    else -> R.drawable.flower_coleus_dead1_2
                }
                else -> R.drawable.flower_elephant_dead1_2
            }
        }

        val daysOld =
            ((System.currentTimeMillis() - (plant.createdAt.seconds * 1000)) / (1000 * 60 * 60 * 24))

        val stage = when {
            daysOld >= 6 -> 6
            daysOld >= 4 -> 4
            daysOld >= 2 -> 2
            else -> 1
        }

        return when (plant.name.lowercase()) {
            "elephant" -> when (stage) {
                1 -> R.drawable.flower_elephant1
                2 -> R.drawable.flower_elephant2
                3 -> R.drawable.flower_elephant3
                4 -> R.drawable.flower_elephant4
                5 -> R.drawable.flower_elephant5
                6 -> R.drawable.flower_elephant6
                else -> R.drawable.flower_elephant1
            }

            "hibiscus" -> when (stage) {
                1 -> R.drawable.flower_hibiscus1
                2 -> R.drawable.flower_hibiscus2
                3 -> R.drawable.flower_hibiscus3
                4 -> R.drawable.flower_hibiscus4
                5 -> R.drawable.flower_hibiscus5
                else -> R.drawable.flower_hibiscus1
            }

            "zebra" -> when (stage) {
                1 -> R.drawable.flower_zebra1
                2 -> R.drawable.flower_zebra2
                3 -> R.drawable.flower_zebra3
                4 -> R.drawable.flower_zebra4
                5 -> R.drawable.flower_zebra5
                6 -> R.drawable.flower_zebra6
                else -> R.drawable.flower_zebra1
            }

            "ficus" -> when (stage) {
                1 -> R.drawable.flower_ficus1
                2 -> R.drawable.flower_ficus2
                3 -> R.drawable.flower_ficus3
                4 -> R.drawable.flower_ficus4
                5 -> R.drawable.flower_ficus5
                6 -> R.drawable.flower_ficus6
                else -> R.drawable.flower_ficus1
            }

            "coleus" -> when (stage) {
                1 -> R.drawable.flower_coleus1
                2 -> R.drawable.flower_coleus2
                3 -> R.drawable.flower_coleus3
                4 -> R.drawable.flower_coleus4
                5 -> R.drawable.flower_coleus5
                else -> R.drawable.flower_coleus1
            }

            else -> R.drawable.flower_elephant1
        }
    }

    private fun checkPlantDeath() {
        pvm.localSessionPlant.value?.let { plant ->
            if(plant.waterLevel < 30) {
                val deathStage = getPlantStage(plant)

                binding.imgFlower.setImageResource(getPlantImageId(plant, deathStage))
                pvm.resetPlantDeathState()
            }
        }
    }
}