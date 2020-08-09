package me.ryanpierce.trialanimations

import android.view.ViewGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

class MeteorMutableStateFlow(initialValue: Meteor, val layout: ViewGroup) {

    private val mutableStateFlow = MutableStateFlow(initialValue)

    var value
        get() = mutableStateFlow.value
        set(value) {
            mutableStateFlow.value = value
        }

    @OptIn(kotlinx.coroutines.InternalCoroutinesApi::class)
    suspend fun collect(location: Coordinate, block: (Meteor) -> Unit) {
        mutableStateFlow.collect(
            object : FlowCollector<Meteor> {
                override suspend fun emit(value: Meteor) {
                    val copiedMeteor = value.copy().apply {
                        this@MeteorMutableStateFlow.layout.addView(this)
                        x += index * 120
                    }
                    copiedMeteor.actor.send(location)
                    delay(1000)
                    block(copiedMeteor)
                }
            }
        )
    }
}