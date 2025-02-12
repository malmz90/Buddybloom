package com.example.buddybloom

import android.os.Bundle
import android.util.TypedValue
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

    //TODO For testing, to be removed
    private val testRepo = WeatherRepository()
    private lateinit var testButton :MaterialButton

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val widthInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 375f, resources.displayMetrics).toInt()
            setLayout(widthInPixels, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton = view.findViewById(R.id.btn_close)
        recyclerView = view.findViewById(R.id.rv_weather_dialog)
        testButton = view.findViewById(R.id.btn_test_pass_day)
        val weatherAdapter = WeatherAdapter(null)
        val linearLayoutManager = LinearLayoutManager(context,  LinearLayoutManager.HORIZONTAL, false)
        recyclerView.apply {
            adapter = weatherAdapter
            layoutManager = linearLayoutManager
        }

        //TODO add game logic here
        weatherAdapter.update(testRepo.getWeeklyWeatherReport())
        closeButton.setOnClickListener { dismiss() }
        testButton.setOnClickListener {
            testRepo.updateWeeklyWeatherReport()
            weatherAdapter.update(testRepo.getWeeklyWeatherReport())
        }
    }


}