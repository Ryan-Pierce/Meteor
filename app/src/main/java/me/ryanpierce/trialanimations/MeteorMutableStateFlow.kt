package me.ryanpierce.trialanimations

import android.view.ViewGroup
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

class MeteorMutableStateFlow(val layout: ViewGroup, initialValue: Meteor) {

    private val mutableStateFlow = MutableStateFlow(initialValue)

    var value
        get() = mutableStateFlow.value
        set(value) {
            mutableStateFlow.value = value
        }

    @OptIn(kotlinx.coroutines.InternalCoroutinesApi::class)
    suspend fun collect(location: Coordinate, block: suspend (Meteor) -> Unit) {
        mutableStateFlow.collect(
            object : FlowCollector<Meteor> {
                override suspend fun emit(value: Meteor) {
                    val copiedMeteor = value.copy().apply {
                        this@MeteorMutableStateFlow.layout.addView(this)
                        x += index * 120
                    }
                    copiedMeteor.actor.send(location)
                    block(copiedMeteor)
                }
            }
        )
    }
}