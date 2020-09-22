package me.ryanpierce.trialanimations

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface.DEFAULT_BOLD
import android.view.Gravity.CENTER
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay

// Call the whole project Meteor, just like how Wharton does random single namer's
// History: was looking for animator too, but they were all to hard to use and
// didn't even accomplish what I wanted without lots of work or an enterprise subscription.
// Decided, maybe i'll just animate straight in android. It felt like a joke at first, but then
// i realized, If i could bind it to the code, it would become a visualization-debugging tool.

// Note to self: If you keep this simple, you may be able to open-source this in time for
// the speech.

typealias Coordinate = Pair<Float,Float>

data class Meteor(
    val start: Coordinate?,
    val scope: CoroutineScope,
    @get:JvmName("getContext_") val context: Context,
    val index: Int
) : AppCompatTextView(context) {

    companion object {
        const val ANIMATION_DURATION = 1_000L
    }

    init {
        start?.run {
            x = first
            y = second
        }
        setBackgroundResource(R.drawable.blue_circle)
        text = index.toString()
        typeface = DEFAULT_BOLD
        gravity = CENTER
        textSize = 20f
        setTextColor(Color.WHITE)
    }

    val actor = scope.actor<Coordinate> {
        for (coordinate in channel) {
            animateTransition(coordinate)
            delay(ANIMATION_DURATION)
        }
    }

    fun animateTransition(coordinate: Coordinate) {
        val adjustedCoordinate = coordinate.adjusted(index)
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(this@Meteor, "translationX", x, adjustedCoordinate.first),
                ObjectAnimator.ofFloat(this@Meteor, "translationY", y, adjustedCoordinate.second)
            )
            duration = ANIMATION_DURATION
            start()
        }
    }

    fun Coordinate.adjusted(index: Int): Coordinate {
        val X = if (index % 2 == 0) first else first + 120
        val Y = second + (index / 2 * 120)
        return X+15 x Y+30
    }

    suspend fun landAsMeteorite() {
        delay(ANIMATION_DURATION)
        setBackgroundResource(R.drawable.green_circle)
    }

    data class Factory(
        val start: Coordinate,
        val coroutineScope: CoroutineScope,
        val layout: ViewGroup,
        val context: Context
    ) {
        companion object {

            fun Factory.addMeteors(count: Int) = (0..count-1).map { index ->
                Meteor(start, coroutineScope, context, index).apply {
                    this@addMeteors.layout.addView(this)
                    x += index * 120
                }
            }
        }
    }
}