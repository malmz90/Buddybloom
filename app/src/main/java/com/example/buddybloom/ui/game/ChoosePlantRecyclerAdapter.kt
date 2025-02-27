package com.example.buddybloom.ui.game

import AddPlantDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant

class ChoosePlantRecyclerAdapter(val plants : MutableList<Plant>, val onPlantClicked : (Plant) -> Unit) : RecyclerView.Adapter<ChoosePlantRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivChoosePlant = itemView.findViewById<ImageView>(R.id.iv_choose_plant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_choose_plant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chosenPlant = plants[position]
        val imageResource = when(chosenPlant.name) {
            "Elephant" -> R.drawable.flower_elefant4
            "Hibiscus" -> R.drawable.flower_hibiscus4
            "Zebra" -> R.drawable.flower_zebra4
            "Ficus" -> R.drawable.flower_ficus5
            "Coleus" -> R.drawable.flower_coleus4
            else -> R.drawable.flower_elefant4
        }
        holder.ivChoosePlant.setImageResource(imageResource)
        holder.itemView.setOnClickListener {
            val dialog = AddPlantDialogFragment(chosenPlant)
            dialog.show((holder.itemView.context as AppCompatActivity).supportFragmentManager, "PlantInfoDialog")
           onPlantClicked(chosenPlant)
        }
    }

    override fun getItemCount(): Int {
        return plants.size
    }
}