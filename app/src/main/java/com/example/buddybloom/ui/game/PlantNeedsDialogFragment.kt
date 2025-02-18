package com.example.buddybloom.ui.game

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.data.repository.PlantRepository
import com.google.android.material.button.MaterialButton

class PlantNeedsDialogFragment : DialogFragment(R.layout.dialog_plant_needs) {
    private lateinit var viewModel: PlantViewModel
    private lateinit var waterCount: TextView
    private lateinit var fertilizerCount: TextView
    private lateinit var sunCount: TextView


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val builder = AlertDialog.Builder(requireContext())
//        val inflater = requireActivity().layoutInflater
//        val view = inflater.inflate(R.layout.dialog_plant_needs, null)
//
//        builder.setView(view)
//
//
//
//
//        val dialog = builder.create()
//
//
//
//
//        return dialog
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("PlantNeedsFragment", "onViewCreated() called!")
        val closeButton: MaterialButton = view.findViewById(R.id.btn_close)


        viewModel = ViewModelProvider(this)[PlantViewModel::class.java]
        waterCount = view.findViewById(R.id.tv_water_count)
        fertilizerCount = view.findViewById(R.id.tv_fertilize_count)
        sunCount = view.findViewById(R.id.tv_sun_count)

        viewModel.getCurrentUserPlant()

        viewModel.selectedPlant.observe(viewLifecycleOwner) { plant ->
            Log.i("PlantNeedsFragment", "Plant: $plant")
            plant?.let {
                waterCount.text = String.format("${it.waterLevel}/100")
                fertilizerCount.text = String.format("${it.fertilizerLevel}/100")
                sunCount.text = String.format("${it.sunLevel}/100")
            }
        }
        closeButton.setOnClickListener {
            dismiss()
        }
    }
}