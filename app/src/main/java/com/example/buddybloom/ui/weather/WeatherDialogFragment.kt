package com.example.buddybloom.ui.weather

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
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
    private lateinit var viewModel: WeatherViewModel


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
        closeButton = view.findViewById(R.id.btn_close)
        recyclerView = view.findViewById(R.id.rv_weather_dialog)
        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        val weatherAdapter = WeatherAdapter(null)
        val linearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {

            }
        recyclerView.apply {
            adapter = weatherAdapter
            layoutManager = linearLayoutManager
            //Disables scrolling and tap/drag animations
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    v.performClick()
                }
                true
            }
        }

        viewModel.weeklyWeatherReport.observe(this) {
            it?.let {
                weatherAdapter.update(it)
            }
        }
        closeButton.setOnClickListener { dismiss() }
    }


}