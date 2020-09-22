package me.ryanpierce.trialanimations

import android.view.ViewGroup
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow

class MeteorMutableSharedFlow(
    val layout: ViewGroup,
    replay: Int,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
) {

    private val mutableSharedFlow = MutableSharedFlow<Meteor>(replay, extraBufferCapacity, onBufferOverflow)

    suspend fun emit(meteor: Meteor) = mutableSharedFlow.emit(meteor)

    @OptIn(kotlinx.coroutines.InternalCoroutinesApi::class)
    suspend fun collect(location: Coordinate, block: suspend (Meteor) -> Unit) {
        mutableSharedFlow.collect(
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