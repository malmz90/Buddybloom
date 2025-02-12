package com.example.buddybloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.buddybloom.databinding.FragmentChoosePlantBinding


class ChoosePlantFragment : Fragment() {
    private lateinit var binding: FragmentChoosePlantBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChoosePlantBinding.inflate(inflater, container, false)

        binding.btnSavePlant.setOnClickListener {
            (activity as? GameActivity)?.showStartPagePlantFragment()
        }

        return binding.root
    }
}