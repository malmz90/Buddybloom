package com.example.buddybloom.data

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.buddybloom.R

class AnimationManager(
    private val context : Context,
    private val handler: Handler,
) {

    fun showWateringAnimation(ivAnimationWateringCan: ImageView, btnWater: Button){
        val drawable: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.gif_water)
        if (drawable is AnimatedImageDrawable) {
            ivAnimationWateringCan.visibility = View.VISIBLE
            ivAnimationWateringCan.setImageDrawable(drawable)
            drawable.start()
            //Disable button while animation is running.
            disableButton(btnWater)
            // Hide the animation after 3 seconds.
            handler.postDelayed({
                ivAnimationWateringCan.visibility = View.INVISIBLE
                // Enable button after animation is done.
                enableButton(btnWater)
            }, 3000)
        }
    }

    fun showFertilizeAnimation(ivAnimationWateringCan: ImageView, btnFertilize: Button){
        val drawable: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.gif_fertilize)
        if (drawable is AnimatedImageDrawable) {
            ivAnimationWateringCan.visibility = View.VISIBLE
            ivAnimationWateringCan.setImageDrawable(drawable)
            drawable.start()
            //Disable button while animation is running.
            disableButton(btnFertilize)
            // Hide the animation after 3 seconds.
            handler.postDelayed({
                ivAnimationWateringCan.visibility = View.INVISIBLE
                // Enable button after animation is done.
                enableButton(btnFertilize)
            }, 3000)
        }
    }

    fun showWaterSprayAnimation(ivAnimationWateringCan: ImageView){
        val drawable: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.gif_waterspray)
        if (drawable is AnimatedImageDrawable) {
            ivAnimationWateringCan.visibility = View.VISIBLE
            ivAnimationWateringCan.setImageDrawable(drawable)
            drawable.start()
            // Hide the animation after 3 seconds.
            handler.postDelayed({
                ivAnimationWateringCan.visibility = View.INVISIBLE
            }, 3000)
        }
    }

    fun showBlinds(ivBlinds: ImageView){
        handler.postDelayed({
            ivBlinds.visibility = View.VISIBLE
        }, 1000)
    }

    fun hideBlinds(ivBlinds: ImageView){
        handler.postDelayed({
            ivBlinds.visibility = View.INVISIBLE
        }, 1000)
    }

    fun showInfectedBugGif(ivAnimationWateringCan: ImageView, imgBtnBugspray: ImageButton, ivInfectedBug: ImageView){
        val drawableBugSpray: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.gif_bugspray)
        if (drawableBugSpray is AnimatedImageDrawable) {
            ivAnimationWateringCan.setImageDrawable(drawableBugSpray)
            imgBtnBugspray.setBackgroundColor(Color.TRANSPARENT)
            ivAnimationWateringCan.visibility = View.VISIBLE
            drawableBugSpray.start()}

        handler.postDelayed({
            ivAnimationWateringCan.visibility = View.INVISIBLE
            ivInfectedBug.visibility = View.INVISIBLE
        }, 3000)
    }

    fun hideInfectedBugGif(ivAnimationWateringCan: ImageView){
        val drawableNoBugs: Drawable? = ContextCompat.getDrawable(context, R.drawable.gif_bug)
        if (drawableNoBugs is AnimatedImageDrawable) {
            ivAnimationWateringCan.visibility = View.VISIBLE
            ivAnimationWateringCan.setImageDrawable(drawableNoBugs)
            drawableNoBugs.start()
            // Hide gif after 3 seconds
            handler.postDelayed({
                ivAnimationWateringCan.visibility = View.INVISIBLE
            }, 3000)
        }
    }

    fun disableButton(button: Button) {
        button.setBackgroundColor(Color.parseColor("#DEDEDE"))
        button.setTextColor(Color.parseColor("#FFFFFF"))
        button.isEnabled = false
    }

    fun enableButton(button: Button) {
        button.setBackgroundColor(Color.parseColor("#F6F1DE"))
        button.setTextColor(Color.parseColor("#246246"))
        button.isEnabled = true
    }
}