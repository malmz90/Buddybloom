package com.example.buddybloom.ui.game

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private var observerPlant: Plant? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        val plantImageManager = PlantImageManager(requireContext(), binding)

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
                pvm.localSessionPlant.value?.let { plant ->
                    plantImageManager.checkPlantDeath(plant, binding)
                }
                pvm.resetPlantDeathState()
            }
        }

        pvm.localSessionPlant.observe(viewLifecycleOwner) { plant ->
            //Checks if user is logging in
            observerPlant = plant

            binding.switchBlinds.isChecked = observerPlant?.protectedFromSun ?: false
            updateBlindsVisibility(binding.ivBlinds)

            if (plant == null) return@observe
            if (avm.isLoggingIn.value != true) {
                binding.imgFlower.setImageResource(plantImageManager.getPlantImageId(plant))
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
                    plantImageManager.showInfectedBugGif(plant)
                } else {
                    binding.imgBtnBugspray.clearAnimation()
                    binding.imgBtnBugspray.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_circle)
                    binding.imgBtnBugspray.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.turquise
                        ))
                }
                binding.btnPlantNeeds.setOnClickListener {
                    val plantNeedsDialog = PlantNeedsDialogFragment.newInstance(plant)
                    plantNeedsDialog.show(parentFragmentManager, "PlantNeedsDialogFragment")
                }
            }
        }

        if (pvm.isPlantThirsty()) {
            Toast.makeText(requireContext(),
                getString(R.string.your_plant_is_thirsty), Toast.LENGTH_SHORT)
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
        pvm.localSessionPlant.value?.let { plant ->
        plantImageManager.showInfectedBugGif(plant)}// if it`s infected gif shows even if restart app

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
                        pvm.localSessionPlant.value?.let { plant ->
                            plantImageManager.checkPlantDeath(plant, binding)
                            plantImageManager.showInfectedBugGif(plant)
                        }
                    }
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
                        pvm.localSessionPlant.value?.let { plant ->
                            plantImageManager.checkPlantDeath(plant, binding)}
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

                    //Disable button while animation is running.
                    binding.btnFertilize.setBackgroundColor(Color.parseColor("#DEDEDE"))
                    binding.btnFertilize.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.btnFertilize.isEnabled = false

                    // Hide the animation after 3 seconds.
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.ivAnimationWateringCan.visibility = View.INVISIBLE
                        pvm.fertilizePlant()

                        // Enable button after animation is done.
                        binding.btnFertilize.setBackgroundColor(Color.parseColor("#F6F1DE"))
                        binding.btnFertilize.setTextColor(Color.parseColor("#246246"))
                        binding.btnFertilize.isEnabled = true

                        pvm.localSessionPlant.value?.let { plant ->
                        plantImageManager.checkPlantDeath(plant, binding)}
                    }, 3000)
                }
            }

//------------------------------------BLINDS-------------------------------------------------------
            switchBlinds.setOnClickListener {
                pvm.toggleBlinds()
                if (observerPlant!!.protectedFromSun) {
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

    private fun updateBlindsVisibility(myImageView: View) {
        if (observerPlant?.protectedFromSun == true) {
            myImageView.visibility = View.VISIBLE
        } else {
            myImageView.visibility = View.INVISIBLE
        }
    }
}