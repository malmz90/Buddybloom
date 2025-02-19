package com.example.buddybloom.ui.game

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.google.android.material.button.MaterialButton

class PlantNeedsDialogFragment : DialogFragment() {
    private lateinit var viewModel: PlantViewModel
    private lateinit var waterLevel: TextView
    private lateinit var fertilizerLevel: TextView
    private lateinit var sunLevel: TextView
    private lateinit var updateText: TextView
    private var plant: Plant? = null

    companion object {
        fun newInstance(plant: Plant): PlantNeedsDialogFragment {
            val fragment = PlantNeedsDialogFragment()
            val args = Bundle()
            args.putString("plant_name", plant.name)
            args.putString("plant_difficulty", plant.difficulty)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_plant_needs, null)

        builder.setView(view)

        val closeButton: MaterialButton = view.findViewById(R.id.btn_close)
        val iconDifficulty: ImageView = view.findViewById(R.id.icon_difficulty)
        val tvDifficulty: TextView = view.findViewById(R.id.tv_difficulty)

        val difficulty = arguments?.getString("plant_difficulty", "Easy") ?: "Easy"
        TooltipCompat.setTooltipText(iconDifficulty, difficulty)
        tvDifficulty.text = when (difficulty) {
            "Easy" -> "E"
            "Medium" -> "M"
            "Hard" -> "H"
            else -> ""
        }

        closeButton.setOnClickListener {
            dismiss()
        }
        val dialog = builder.create()

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        viewModel = ViewModelProvider(requireActivity())[PlantViewModel::class.java]
        //viewModel.getCurrentUserPlant()
        waterLevel = view.findViewById(R.id.tv_water_count)
        fertilizerLevel = view.findViewById(R.id.tv_fertilize_count)
        sunLevel = view.findViewById(R.id.tv_sun_count)
        updateText = view.findViewById(R.id.tv_updates)

        //TODO observe does not seem to trigger the latest changes while this dialog is up,
        // only when closed and opened again. Troubleshoot?
        viewModel.selectedPlant.observe(requireActivity()) { plant ->
            Log.i("PlantNeedsFragment", "Plant: $plant")
            plant?.let {
                waterLevel.text = String.format(" ${it.waterLevel}/100")
                fertilizerLevel.text = String.format(" ${it.fertilizerLevel}/100")
                sunLevel.text = String.format(" ${it.sunLevel}/100")
                updateText.text = when {
                    it.waterLevel < 50 -> String.format("Your plant needs water!")
                    it.fertilizerLevel < 50 -> String.format("Your plant needs more fertilizer!")
                    it.sunLevel < 50 -> String.format("Your plant needs more sunshine!")
                    else -> {
                        "Your plant is healthy!"
                    }
                }
            }
        }


        return dialog
    }

    //TODO onViewCreated() is never called in this fragment (???)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("PlantNeedsFragment", "onViewCreated() called!")


    }
}