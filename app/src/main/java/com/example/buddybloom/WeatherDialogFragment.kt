package com.example.buddybloom

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybloom.data.repository.WeatherRepository
import com.google.android.material.button.MaterialButton

class WeatherDialogFragment : DialogFragment(R.layout.dialog_weather) {
    private lateinit var closeButton: MaterialButton
    private lateinit var recyclerView: RecyclerView

    //TODO For testing, to be replaced with viewmodel
    private val testRepo = WeatherRepository()

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

        //TODO for testing, replace with viewmodel/observer later
        weatherAdapter.update(testRepo.getWeeklyWeatherReport())
        closeButton.setOnClickListener { dismiss() }
    }


}