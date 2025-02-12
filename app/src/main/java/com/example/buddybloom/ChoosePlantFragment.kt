package com.example.buddybloom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddybloom.databinding.FragmentChoosePlantBinding


class ChoosePlantFragment : Fragment() {

    private lateinit var binding: FragmentChoosePlantBinding
    private lateinit var adapter : ChoosePlantRecyclerAdapter
    private val plants = mutableListOf<Plant>()
    private var userPlant : Plant? = null

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

        val plantElephant = Plant("Elephant", R.drawable.flower_elefant4, 100)
        val plantHibiscus = Plant("Hibiscus", R.drawable.flower_hibiscus4, 100)
        val plantZebra = Plant("Zebra", R.drawable.flower_zebra4, 100)
        plants.add(plantElephant)
        plants.add(plantHibiscus)
        plants.add(plantZebra)

        binding.rvChoosePlant.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChoosePlantRecyclerAdapter(plants) { chosenPlant ->
            userPlant = chosenPlant // Remove when code created for saving plant
            Log.d("!!!", "Selected plant: ${chosenPlant.name}")
            (activity as? GameActivity)?.showStartPagePlantFragment()
        }
        binding.rvChoosePlant.adapter = adapter
    }
}