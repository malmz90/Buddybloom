package com.example.buddybloom.ui.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.repository.PlantRepository
import com.example.buddybloom.databinding.FragmentChoosePlantBinding
import com.google.firebase.auth.FirebaseAuth


class ChoosePlantFragment : Fragment() {

    private lateinit var binding: FragmentChoosePlantBinding
    private lateinit var adapter : ChoosePlantRecyclerAdapter
    private val plants = mutableListOf<Plant>()
    private var userPlant : Plant? = null
    private val plantRepository = PlantRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChoosePlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val plantElephant = Plant("Elephant", 100)
        val plantHibiscus = Plant("Hibiscus", 100)
        val plantZebra = Plant("Zebra", 100)
        plants.add(plantElephant)
        plants.add(plantHibiscus)
        plants.add(plantZebra)

        binding.rvChoosePlant.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChoosePlantRecyclerAdapter(plants) { chosenPlant ->
            savePlant(chosenPlant)
        }
        binding.rvChoosePlant.adapter = adapter
    }

    private fun savePlant(plant: Plant) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }
        plantRepository.saveUserPlant(userId, plant) { success ->
            if (success) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Plant saved successfully!", Toast.LENGTH_SHORT).show()
                    (activity as? GameActivity)?.showStartPagePlantFragment()
                }
            } else {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to save plant", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}