package com.example.buddybloom.ui.game

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybloom.R
import com.google.android.material.button.MaterialButton

class WeatherDialogFragment : DialogFragment(R.layout.dialog_weather) {
    private lateinit var closeButton: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var pvm: PlantViewModel
    lateinit var weatherAdapter: WeatherAdapter

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val widthInPixels = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                375f,
                resources.displayMetrics
            ).toInt()
            setLayout(widthInPixels, WindowManager.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pvm = ViewModelProvider(requireActivity())[PlantViewModel::class.java]

        closeButton = view.findViewById(R.id.btn_history_close)
        recyclerView = view.findViewById(R.id.rv_weather_dialog)

        weatherAdapter = WeatherAdapter(null)
        val linearLayoutManager = LinearLayoutManager(context)

        recyclerView.apply {
            adapter = weatherAdapter
            layoutManager = linearLayoutManager
        }

        pvm.currentWeatherReport.observe(viewLifecycleOwner) { report ->
            weatherAdapter.updateAdapter(report)
            scrollToCurrentHour(linearLayoutManager)
        }

        closeButton.setOnClickListener { dismiss() }
    }

    private fun scrollToCurrentHour(layoutManager: LinearLayoutManager) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        layoutManager.scrollToPositionWithOffset(currentHour, 0)
    }
}