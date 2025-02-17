package com.example.buddybloom.ui.weather

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybloom.R
import com.example.buddybloom.data.model.WeatherReport
import com.google.android.material.textview.MaterialTextView

class WeatherAdapter(private var weeklyWeatherReport: WeatherReport.Weekly?) :
    RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {
    inner class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textCardView: CardView = itemView.findViewById(R.id.cv_text)
        val dayText: MaterialTextView = itemView.findViewById(R.id.tv_day)
        val weatherIcon: ImageView = itemView.findViewById(R.id.iv_weather)
        val tempText: MaterialTextView = itemView.findViewById(R.id.tv_temp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun getItemCount(): Int {
        return weeklyWeatherReport?.dailyReports?.size ?: 0
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val currentWeather = weeklyWeatherReport?.dailyReports?.get(position)
        currentWeather?.let {
            holder.dayText.text = it.weekDay?.uppercase() ?: "---"
            val background = GradientDrawable().apply {
                cornerRadius = 7f
                color = ColorStateList.valueOf(
                    if (position == 0) {
                        holder.itemView.context.getColor(R.color.day_green)
                    } else {
                        when (currentWeather.weekDay?.uppercase()) {
                            "SAT", "SUN" -> holder.itemView.context.getColor(R.color.day_red)
                            else -> {
                                holder.itemView.context.getColor(R.color.day_blue)
                            }
                        }
                    }
                )
            }
            holder.textCardView.background = background
            holder.weatherIcon.setImageResource(
                when (it.condition) {
                    WeatherReport.Condition.SUNNY -> R.drawable.icon_sunny
                    //TODO Add the correct icons when they are added to the project!
                    WeatherReport.Condition.CLOUDY -> R.drawable.icon_obs
                    WeatherReport.Condition.RAIN -> R.drawable.icon_obs
                    null -> R.drawable.icon_obs
                }
            )
            val tempString = "${it.temperature}°C"
            holder.tempText.text = tempString
        }
    }

    fun update(newReport: WeatherReport.Weekly) {
        weeklyWeatherReport = newReport
        notifyDataSetChanged()
    }
}