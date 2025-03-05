package com.example.buddybloom.ui.game

import AddPlantDialogFragment
import android.media.AudioAttributes
import android.media.SoundPool
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.databinding.FragmentChoosePlantBinding

class ChoosePlantFragment : Fragment() {

    private lateinit var binding: FragmentChoosePlantBinding
    private lateinit var adapter: ChoosePlantRecyclerAdapter
    private val plants = mutableListOf<Plant>()
    private lateinit var pvm: PlantViewModel

    private lateinit var soundPool: SoundPool
    private var gameOver: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChoosePlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pvm = ViewModelProvider(requireActivity())[PlantViewModel::class.java]

        // Creating SoundPool to play gameOver Sound
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        gameOver = soundPool.load(requireContext(), R.raw.game_over, 1)

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                // Loading sound, now its Ready to play when plant dies
                if (sampleId == gameOver) {
                    soundPool.play(gameOver, 1.0f, 1.0f, 1, 0, 1.0f)
                }
            }
        }

        pvm.plantDiedFromOverWatering.observe(viewLifecycleOwner) { justDrowned ->
            if (justDrowned) {
                soundPool.play(gameOver, 1.0f, 1.0f, 1, 0, 1.0f)
                showOverWateringDialog()
                pvm.resetOverWateringDeathState() // Reset Plant
            }
        }

        pvm.plantJustDied.observe(viewLifecycleOwner) { justDied ->
            if (justDied) {
                soundPool.play(gameOver, 1.0f, 1.0f, 1, 0, 1.0f)
                showPlantDeathDialog()
               pvm.resetPlantDeathState()  // Reset Plant
            }
        }

        val plantElephant = Plant(
            name = "Elephant",
            info = getString(R.string.desc_elephant),
            difficulty = "Easy"
        )
        val plantHibiscus = Plant(
            name = "Hibiscus",
            info = getString(R.string.desc_hibiscus),
            difficulty = "Hard"
        )
        val plantZebra = Plant(
            name = "Zebra",
            info = getString(R.string.desc_zebra),
            difficulty = "Medium"
        )
        val plantFicus = Plant (
            name = "Ficus",
            info = getString(R.string.desc_ficus),
            difficulty = "Medium"
        )
        val plantColeus = Plant (
            name = "Coleus",
            info = getString(R.string.desc_colues),
            difficulty = "Easy"
        )
        plants.add(plantElephant)
        plants.add(plantHibiscus)
        plants.add(plantZebra)
        plants.add(plantFicus)
        plants.add(plantColeus)

        binding.rvChoosePlant.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChoosePlantRecyclerAdapter(plants) { chosenPlant ->
            showAddPlantDialogFragment(chosenPlant)
        }
        binding.rvChoosePlant.adapter = adapter
    }


    private fun showOverWateringDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Your Plant Has Drowned")
            .setMessage("Oh no! Your plant couldn't survive. You watered it too much. Don't worry, you can choose a new plant now!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
    
    //dead plant dialog
    private fun showPlantDeathDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_dead_plant, null)
            val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val btnYes = dialogView.findViewById<Button>(R.id.btn_yes_pls)
        btnYes.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()
    }

    //add plant dialog
    private fun showAddPlantDialogFragment(plant: Plant) {
        parentFragmentManager.beginTransaction().apply {
            show(AddPlantDialogFragment(plant))
            commit()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        soundPool.release()
    }
}