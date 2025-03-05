package com.example.buddybloom.ui.game

import android.content.res.ColorStateList
import android.graphics.Color
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
import com.example.buddybloom.data.AnimationManager
import com.example.buddybloom.data.SoundManager
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
    private var observerPlant: Plant? = null
    private lateinit var animationManager: AnimationManager
    private lateinit var soundManager: SoundManager

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

        animationManager = AnimationManager(
            requireContext(),
            Handler(Looper.getMainLooper()))

        soundManager = SoundManager(requireContext())

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

        pvm.localSessionPlant.value?.let { plant ->
        plantImageManager.showInfectedBugGif(plant)}// if it`s infected gif shows even if restart app

        binding.apply {

//------------------------------------WATER---------------------------------------------------------
            btnWater.setOnClickListener {
                animationManager.showWateringAnimation(binding.ivAnimationWateringCan, binding.btnWater)
                soundManager.playWateringSound()

                pvm.waterPlant()
                pvm.localSessionPlant.value?.let { plant ->
                    plantImageManager.checkPlantDeath(plant, binding)
                    plantImageManager.showInfectedBugGif(plant)
                }
            }

            imgBtnWaterspray.setOnClickListener {
                animationManager.showWaterSprayAnimation(binding.ivAnimationWateringCan)
                soundManager.playWaterSpraySound()

                pvm.waterSpray()
                pvm.localSessionPlant.value?.let { plant ->
                    plantImageManager.checkPlantDeath(plant, binding)}
            }

//------------------------------------FERTILIZE-------------------------------------------------------
            btnFertilize.setOnClickListener {
                animationManager.showFertilizeAnimation(binding.ivAnimationWateringCan, binding.btnFertilize)
                soundManager.playFertilizeSound()

                pvm.fertilizePlant()
                pvm.localSessionPlant.value?.let { plant ->
                    plantImageManager.checkPlantDeath(plant, binding)}
            }

//------------------------------------BLINDS-------------------------------------------------------
            switchBlinds.setOnClickListener {
                pvm.toggleBlinds()
                if (observerPlant!!.protectedFromSun) {
                    // Play sound
                    soundManager.playBlindsSoundStart()
                    // Show blinds
                    animationManager.showBlinds(binding.ivBlinds)
                } else {
                    // Play sound
                    soundManager.playBlindsSoundEnd()
                    // Hide blinds
                    animationManager.hideBlinds(binding.ivBlinds)
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
                        soundManager.playBugSpraySound()
                        // If plant is infected show buggspray-gif
                        animationManager.showInfectedBugGif(binding.ivAnimationWateringCan, binding.imgBtnBugspray, binding.ivInfectedBug)
                        pvm.sprayOnBugs()
                    } else {
                        //Play Sound
                        soundManager.playErrorSound()
                        // If plant not is infected show NoBug gif
                        animationManager.hideInfectedBugGif(binding.ivAnimationWateringCan)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
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

    private fun updateBlindsVisibility(ivBlinds: View) {
        if (observerPlant?.protectedFromSun == true) {
            ivBlinds.visibility = View.VISIBLE
        } else {
            ivBlinds.visibility = View.INVISIBLE
        }
    }
}