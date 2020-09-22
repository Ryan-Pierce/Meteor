package me.ryanpierce.trialanimations

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class MeteorCoroutineScope(
    coroutineScope: CoroutineScope,
    val config: Config
) : CoroutineScope by coroutineScope {

    fun launch(
        location: Coordinate,
        name: String = "",
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.(Coordinate) -> Unit
    ) = launch(context + CoroutineName(name), start) {
        withContext(Dispatchers.Main) {
            TextView(config.context).run {
                location.run {
                    x = first
                    y = second
                }
                layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                text = " $name"
                setBackgroundResource(R.drawable.square)
                config.layout.addView(this)
            }
        }
        block(location)
    }

    data class Config (
        val layout: ViewGroup,
        val context: Context
    )
}