package com.example.buddybloom.ui.game

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.ui.authentication.AccountViewModel
import com.google.android.material.button.MaterialButton

class PlantInfoDialogFragment(private val plant: Plant): DialogFragment(R.layout.dialog_plant_info) {

    private lateinit var plantViewModel : PlantViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        plantViewModel = ViewModelProvider(requireActivity())[PlantViewModel::class.java]
        val builder = AlertDialog.Builder(requireContext())
        val inflater =  requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_plant_info, null)

        builder.setView(view)

        val tvPlantInfo = view.findViewById<TextView>(R.id.tv_plant_info)
        val cancelButton : MaterialButton = view.findViewById(R.id.btn_cancel)
        val addPlantButton : MaterialButton = view.findViewById(R.id.btn_add_plant)

        tvPlantInfo.text = plant.name

        cancelButton.setOnClickListener {
            dismiss()
        }
        addPlantButton.setOnClickListener {
            plantViewModel.setSelectedPlant(plant)
            val replaceDialog = ReplacePlantDialogFragment(plant)
            replaceDialog.show(parentFragmentManager, "ReplacePlantDialog")
            dismiss()
        }

        val dialog = builder.create()
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}