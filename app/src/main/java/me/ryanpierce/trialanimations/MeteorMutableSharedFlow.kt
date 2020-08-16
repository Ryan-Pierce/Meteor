package me.ryanpierce.trialanimations

import android.view.ViewGroup
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer

class MeteorMutableSharedFlow(initialValue: Meteor, val layout: ViewGroup) {

    private val mutableSharedFlow = MutableStateFlow(initialValue)

    var value
        get() = mutableSharedFlow.value
        set(value) {
            mutableSharedFlow.value = value
        }

    @OptIn(kotlinx.coroutines.InternalCoroutinesApi::class)
    suspend fun collect(location: Coordinate, block: suspend (Meteor) -> Unit) {
        mutableSharedFlow
            .buffer(UNLIMITED)
            .collect(
                object : FlowCollector<Meteor> {
                    override suspend fun emit(value: Meteor) {
                        val copiedMeteor = value.copy().apply {
                            this@MeteorMutableSharedFlow.layout.addView(this)
                            x += index * 120
                        }
                        copiedMeteor.actor.send(location)
                        block(copiedMeteor)
                    }
                }
        )
    }
}