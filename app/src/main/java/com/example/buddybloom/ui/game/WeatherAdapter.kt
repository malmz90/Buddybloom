package com.example.buddybloom.ui.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybloom.R
import com.example.buddybloom.data.model.WeatherReport

class WeatherAdapter(private var dailyWeatherReport: WeatherReport.Daily?) :
    RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {
    inner class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val textCardView: CardView = itemView.findViewById(R.id.cv_text)
        val tvHour: TextView = itemView.findViewById(R.id.tv_hour)
        val weatherIcon: ImageView = itemView.findViewById(R.id.iv_weather)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dailyWeatherReport?.hourlyWeather?.size ?: 0
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val currentHourlyWeather = dailyWeatherReport?.hourlyWeather?.get(position)

        holder.tvHour.text = currentHourlyWeather?.first.toString()

        val imageId = when(currentHourlyWeather?.second) {
            WeatherReport.Condition.SUNNY -> R.drawable.icon_sun
            WeatherReport.Condition.CLOUDY -> R.drawable.icon_cloudy
            WeatherReport.Condition.NIGHT -> R.drawable.icon_moon
            WeatherReport.Condition.PARTLY_CLOUDY -> R.drawable.icon_partly_cloudy
            WeatherReport.Condition.RAIN -> R.drawable.icon_rainy
            WeatherReport.Condition.THUNDER -> R.drawable.icon_thunder
            null -> R.drawable.icon_cloudy
        }
        holder.weatherIcon.setImageResource(imageId)


        /*val currentWeather = dailyWeatherReport?.dailyReports?.get(position)
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
        }*/
    }

    fun updateAdapter(newReport: WeatherReport.Daily) {
        dailyWeatherReport = newReport
        notifyDataSetChanged()
    }
}