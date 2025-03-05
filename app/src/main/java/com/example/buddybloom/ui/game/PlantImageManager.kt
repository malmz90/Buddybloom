package com.example.buddybloom.ui.game

import android.content.Context
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.databinding.FragmentStartPagePlantBinding

class PlantImageManager
    (private val context: Context, private val binding : FragmentStartPagePlantBinding){

   fun getPlantStage(plant: Plant):Int {
        val daysOld = ((System.currentTimeMillis() - (plant.createdAt.seconds * 1000)) / (1000 * 60 * 60 * 24))

       Log.d("PlantImageManager", "Plant: ${plant.name}, Days old (test): $daysOld")

        return when {
            daysOld >= 5 -> 6
            daysOld >= 4 -> 5
            daysOld >= 3 -> 4
            daysOld >= 2 -> 3
            daysOld >= 1 -> 2
            else -> 1
        }
    }

    /**
     * Returns the correct image based on how old the plant is and in which stage it dies.
     */
    fun getPlantImageId(plant: Plant?, deathStage: Int? = null): Int {
        if (plant == null) return R.drawable.icon_obs

        // Check if plant is dried (water level below 30)
        if (plant.waterLevel < 30) {
            val stage = deathStage ?: getPlantStage(plant)
            return when (plant.name.lowercase()) {
                "elephant" ->  when(stage) {
                    1 -> R.drawable.flower_elephant_dead1_2
                    2 -> R.drawable.flower_elephant_dead1_2
                    3 -> R.drawable.flower_elephant_dead3
                    4 -> R.drawable.flower_elephant_dead4
                    5 -> R.drawable.flower_elephant_dead5
                    6 -> R.drawable.flower_elephant_dead6
                    else -> R.drawable.flower_elephant_dead1_2
                }
                "hibiscus" -> when(stage) {
                    1 -> R.drawable.flower_hibiscus_dead1
                    2 -> R.drawable.flower_hibiscus_dead2_3
                    3 -> R.drawable.flower_hibiscus_dead2_3
                    4 -> R.drawable.flower_hibiscus_dead4
                    5 -> R.drawable.flower_hibiscus_dead5
                    6 -> R.drawable.flower_hibiscus_dead5
                    else -> R.drawable.flower_hibiscus_dead1
                }
                "zebra" -> when(stage) {
                    1 -> R.drawable.flower_zebra_dead1
                    2 -> R.drawable.flower_zebra_dead2
                    3 -> R.drawable.flower_zebra_dead3
                    4 -> R.drawable.flower_zebra_dead4
                    5 -> R.drawable.flower_zebra_dead5
                    6 -> R.drawable.flower_zebra_dead6
                    else -> R.drawable.flower_zebra_dead1
                }
                "ficus" -> when(stage) {
                    1 -> R.drawable.flower_ficus_dead1_2
                    2 -> R.drawable.flower_ficus_dead1_2
                    3 -> R.drawable.flower_ficus_dead3
                    4 -> R.drawable.flower_ficus_dead4
                    5 -> R.drawable.flower_ficus_dead5
                    6 -> R.drawable.flower_ficus_dead6
                    else -> R.drawable.flower_ficus_dead1_2
                }
                "coleus" -> when(stage) {
                    1 -> R.drawable.flower_coleus_dead1
                    2 -> R.drawable.flower_coleus_dead2
                    3 -> R.drawable.flower_coleus_dead3
                    4 -> R.drawable.flower_coleus_dead4
                    5 -> R.drawable.flower_coleus_dead5
                    6 -> R.drawable.flower_coleus_dead5
                    else -> R.drawable.flower_coleus_dead1
                }
                else -> R.drawable.flower_elephant_dead1_2
            }
        }

        val stage = getPlantStage(plant)

        //sets the growing plant pitures
        return when (plant.name.lowercase()) {
            "elephant" -> when (stage) {
                1 -> R.drawable.flower_elephant1
                2 -> R.drawable.flower_elephant2
                3 -> R.drawable.flower_elephant3
                4 -> R.drawable.flower_elephant4
                5 -> R.drawable.flower_elephant5
                6 -> R.drawable.flower_elephant6
                else -> R.drawable.flower_elephant1
            }

            "hibiscus" -> when (stage) {
                1 -> R.drawable.flower_hibiscus1
                2 -> R.drawable.flower_hibiscus2
                3 -> R.drawable.flower_hibiscus3
                4 -> R.drawable.flower_hibiscus4
                5 -> R.drawable.flower_hibiscus5
                6 -> R.drawable.flower_hibiscus5
                else -> R.drawable.flower_hibiscus1
            }

            "zebra" -> when (stage) {
                1 -> R.drawable.flower_zebra1
                2 -> R.drawable.flower_zebra2
                3 -> R.drawable.flower_zebra3
                4 -> R.drawable.flower_zebra4
                5 -> R.drawable.flower_zebra5
                6 -> R.drawable.flower_zebra6
                else -> R.drawable.flower_zebra1
            }

            "ficus" -> when (stage) {
                1 -> R.drawable.flower_ficus1
                2 -> R.drawable.flower_ficus2
                3 -> R.drawable.flower_ficus3
                4 -> R.drawable.flower_ficus4
                5 -> R.drawable.flower_ficus5
                6 -> R.drawable.flower_ficus6
                else -> R.drawable.flower_ficus1
            }

            "coleus" -> when (stage) {
                1 -> R.drawable.flower_coleus1
                2 -> R.drawable.flower_coleus2
                3 -> R.drawable.flower_coleus3
                4 -> R.drawable.flower_coleus4
                5 -> R.drawable.flower_coleus5
                6 -> R.drawable.flower_coleus5
                else -> R.drawable.flower_coleus1
            }
            else -> R.drawable.flower_elephant1
        }
    }
    /**
     *  function shows infected gif
     */
     fun showInfectedBugGif(plant: Plant) {
            if (plant.infected) {
                val bugDrawable: Drawable? = ContextCompat.getDrawable(
                    context,
                    R.drawable.gif_infected_bugs
                )
                if (bugDrawable is AnimatedImageDrawable) {
                    binding.ivInfectedBug.visibility = View.VISIBLE
                    binding.ivInfectedBug.setImageDrawable(bugDrawable)
                    bugDrawable.start()
                }
            } else {
                binding.ivInfectedBug.visibility = View.GONE
        }
    }

    //Checks if plant is dead and sets right picture based on stage
     fun checkPlantDeath(plant: Plant, binding: FragmentStartPagePlantBinding) {
            if(plant.waterLevel < 30) {
                val deathStage = getPlantStage(plant)

                binding.imgFlower.setImageResource(getPlantImageId(plant, deathStage))
            }
        }
}