package com.example.buddybloom.ui.game

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.DialogFragment
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.google.android.material.button.MaterialButton

class PlantNeedsDialogFragment : DialogFragment() {

    private var plant: Plant? = null

   companion object{
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

        val closeButton : MaterialButton = view.findViewById(R.id.btn_close)
        val iconDifficulty : ImageView = view.findViewById(R.id.icon_difficulty)
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

        return dialog
    }
}