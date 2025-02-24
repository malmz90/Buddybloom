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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.data.repository.PlantRepository
import com.example.buddybloom.ui.authentication.AccountViewModel
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

//Replace plant dialog that pops up after choosing a plant if you want to replace current plant
class ReplacePlantDialogFragment(private val plant: Plant) :
    DialogFragment(R.layout.dialog_replace_plant) {

    private lateinit var plantViewModel: PlantViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        plantViewModel = ViewModelProvider(requireActivity())[PlantViewModel::class.java]

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_replace_plant, null)

        builder.setView(view)

        val noButton: MaterialButton = view.findViewById(R.id.btn_no)
        val yesButton: MaterialButton = view.findViewById(R.id.btn_yes)

        noButton.setOnClickListener {
            dismiss()
        }
        yesButton.setOnClickListener {
            plantViewModel.savePlantToRemote(plant)
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