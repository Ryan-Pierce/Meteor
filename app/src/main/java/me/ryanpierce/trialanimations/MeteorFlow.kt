package me.ryanpierce.trialanimations

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

@OptIn(kotlinx.coroutines.InternalCoroutinesApi::class)
suspend fun Flow<Meteor>.collect(location: Coordinate, block: (Meteor) -> Unit) {
    collect(
        object : FlowCollector<Meteor> {
            override suspend fun emit(value: Meteor) {
                value.actor.send(location)
                delay(1000)
                block(value)
            }
        }
    )
}