package me.ryanpierce.trialanimations

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.Flow

// Call the whole project Meteor, just like how Wharton does random single namer's
// History: was looking for animator too, but they were all to hard to use and
// didn't even accomplish what I wanted without lots of work or an enterprise subscription.
// Decided, maybe i'll just animate straight in android. It felt like a joke at first, but then
// i realized, If i could bind it to the code, it would become a visualization-debugging tool.

// Note to self: If you keep this simple, you may be able to open-source this in time for
// the speech.

typealias Coordinate = Pair<Float,Float>

class Meteor(
    start: Coordinate?,
    scope: CoroutineScope,
    context: Context,
    label: String
) : AppCompatImageView(context) {

    init {
        start?.run {
            x = first
            y = second
        }
        setImageResource(R.drawable.circle)
        setPadding(10, 10, 10, 10)
    }

    val actor = scope.actor<Coordinate> {
        for (coordinate in channel) {
            animateTransition(coordinate)
        }
    }

    companion object {
        fun ViewGroup.addMeteors(meteors: List<Meteor>) = meteors.forEachIndexed { index, meteor ->
            addView(meteor)
            meteor.setPadding(index * 120 + 10, 10, 10, 10)
        }
    }

    fun animateTransition(coordinate: Coordinate) {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(this@Meteor, "translationX", x, coordinate.first),
                ObjectAnimator.ofFloat(this@Meteor, "translationY", y, coordinate.second)
            )
            duration = 1000
            start()
        }
    }
}