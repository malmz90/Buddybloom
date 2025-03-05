package com.example.buddybloom.ui.game

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.google.android.material.button.MaterialButton

//Replace plant dialog that pops up after choosing a plant if you want to replace current plant
class ReplacePlantDialogFragment(private val newPlant: Plant) :
    DialogFragment(R.layout.dialog_replace_plant) {

    private lateinit var plantViewModel: PlantViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        plantViewModel = ViewModelProvider(requireActivity())[PlantViewModel::class.java]

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_replace_plant, null)

        builder.setView(view)

        val btnNo: MaterialButton = view.findViewById(R.id.btn_no)
        val btnYes: MaterialButton = view.findViewById(R.id.btn_yes)

        btnNo.setOnClickListener {
            dismiss()
        }
        btnYes.setOnClickListener {
            plantViewModel.savePlantToRemote(newPlant)
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