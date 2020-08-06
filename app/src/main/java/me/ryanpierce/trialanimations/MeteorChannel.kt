package me.ryanpierce.trialanimations

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.delay

class MeteorChannel {

    val channel = Channel<Meteor>()

    suspend fun send(meteor: Meteor) {
        delay(1000)
        channel.send(meteor)
    }

    suspend fun receive(coordinate: Coordinate): Meteor {
        val meteor = channel.receive()
        meteor.actor.send(coordinate)
        return meteor
    }
}