package me.ryanpierce.trialanimations

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

@OptIn(kotlinx.coroutines.InternalCoroutinesApi::class)
suspend fun Flow<Meteor>.collect(location: Coordinate, block: (Meteor) -> Unit) = collect { meteor ->
    meteor.actor.send(location)
    delay(1000)
    block(meteor)
}