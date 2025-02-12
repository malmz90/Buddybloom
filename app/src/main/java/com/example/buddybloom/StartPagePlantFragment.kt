package com.example.buddybloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.buddybloom.databinding.FragmentStartPagePlantBinding

class StartPagePlantFragment : Fragment() {

    private var binding : FragmentStartPagePlantBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartPagePlantBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.switchBlinds?.setOnClickListener {
            Toast.makeText(requireContext(), "You've successfully protected your plant!", Toast.LENGTH_SHORT).show()
        }

        binding?.imgBtnWaterspray?.setOnClickListener {
            Toast.makeText(requireContext(), "You've successfully sprayed water on your plant!", Toast.LENGTH_SHORT).show()
        }
        binding?.imgBtnBugspray?.setOnClickListener {
            Toast.makeText(requireContext(), "You've successfully saved your plant from bugs!", Toast.LENGTH_SHORT).show()
        }

    }
}