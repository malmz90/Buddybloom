package com.example.buddybloom.data

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.example.buddybloom.R

class AnimationManager(
    private val context : Context,
    private val handler: Handler,
    private val progressWater: ProgressBar
) {

    private var redBlinkAnimator: ObjectAnimator? = null
    private var yellowBlinkAnimator: ObjectAnimator? = null

    init {
        redBlinkAnimator = ObjectAnimator.ofFloat(progressWater, "alpha", 1f, 0.5f).apply {
            duration = 400
            repeatMode = ValueAnimator.REVERSE
            repeatCount = androidx.core.animation.ValueAnimator.INFINITE
        }
        yellowBlinkAnimator = ObjectAnimator.ofFloat(progressWater, "alpha", 1f, 0.5f).apply {
            duration = 1000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = androidx.core.animation.ValueAnimator.INFINITE
        }
    }

    fun alarmProgressBar(waterLevel: Int) {
        if(waterLevel <= 30 || waterLevel > 110) {
            progressWater.progressTintList = ColorStateList.valueOf(Color.parseColor("#852221"))
            yellowBlinkAnimator?.cancel()
            redBlinkAnimator?.start()
        } else if(waterLevel in 31..49 || waterLevel in 101..110) {
            progressWater.progressTintList = ColorStateList.valueOf(Color.parseColor("#C19B2F"))
            redBlinkAnimator?.cancel()
            yellowBlinkAnimator?.start()
        } else {
            progressWater.progressTintList = ColorStateList.valueOf(Color.parseColor("#24231F"))
            redBlinkAnimator?.cancel()
            yellowBlinkAnimator?.cancel()
            progressWater.alpha = 1f // Se till att den är helt synlig
        }
    }

    fun stopAlarmProgressBar() {
        redBlinkAnimator?.cancel()
        yellowBlinkAnimator?.cancel()
    }

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