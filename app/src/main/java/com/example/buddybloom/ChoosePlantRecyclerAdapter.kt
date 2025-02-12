package com.example.buddybloom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ChoosePlantRecyclerAdapter(val plants : MutableList<Plant>, val onPlantClicked : (Plant) -> Unit) : RecyclerView.Adapter<ChoosePlantRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivChoosePlant = itemView.findViewById<ImageView>(R.id.iv_choose_plant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePlantRecyclerAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_choose_plant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChoosePlantRecyclerAdapter.ViewHolder, position: Int) {
        val chosenPlant = plants[position]
        holder.ivChoosePlant.setImageResource(chosenPlant.imageId)
        holder.itemView.setOnClickListener {
            onPlantClicked(chosenPlant)
        }
    }

    override fun getItemCount(): Int {
        return plants.size
    }
}