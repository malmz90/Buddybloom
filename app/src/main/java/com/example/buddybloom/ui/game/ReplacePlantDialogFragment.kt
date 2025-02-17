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

class ReplacePlantDialogFragment(private val plant: Plant) : DialogFragment(R.layout.dialog_replace_plant) {

    private val plantRepository = PlantRepository()
    private lateinit var plantViewModel: PlantViewModel
    private lateinit var auth : FirebaseAuth

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        plantViewModel = ViewModelProvider(requireActivity())[PlantViewModel::class.java]
        auth = FirebaseAuth.getInstance()

        val builder = AlertDialog.Builder(requireContext())
        val inflater =  requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_replace_plant, null)

        builder.setView(view)

        val noButton : MaterialButton = view.findViewById(R.id.btn_no)
        val yesButton : MaterialButton = view.findViewById(R.id.btn_yes)

        noButton.setOnClickListener {
            dismiss()
        }
        yesButton.setOnClickListener {
            plantViewModel.savePlantForCurrentUser()
            Toast.makeText(requireContext(), "${plant.name} Selected!", Toast.LENGTH_SHORT).show()
            dismiss()

            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fvc_game_activity, StartPagePlantFragment())
            transaction.addToBackStack(null)
            transaction.commit()
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