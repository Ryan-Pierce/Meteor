package me.ryanpierce.trialanimations

import android.content.Context
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext


class MeteorCoroutine(
    val location: Coordinate,
    scope: CoroutineScope,
    context: Context,
    block: suspend (MeteorCoroutine) -> Unit
) : AppCompatImageView(context) {

    init {
        location.run {
            x = first
            y = second
        }
        setImageResource(R.drawable.square)
    }

    val coroutine = scope.launch(EmptyCoroutineContext, CoroutineStart.LAZY) {
        block(this@MeteorCoroutine)
    }

    companion object {
        fun ViewGroup.addCoroutines(coroutines: List<MeteorCoroutine>) = coroutines.forEach { addView(it) }
        fun List<MeteorCoroutine>.start() = this.forEach { it.start() }
    }

    fun start() {
        coroutine.start()
    }

}