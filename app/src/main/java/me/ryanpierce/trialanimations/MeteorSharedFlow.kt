package me.ryanpierce.trialanimations

class MeteorSharedFlow(private val meteorMutableSharedFlow: MeteorMutableSharedFlow) {

    suspend fun emit(meteor: Meteor) = meteorMutableSharedFlow.emit(meteor)

    suspend fun collect(location: Coordinate, block: suspend (Meteor) -> Unit) =
        meteorMutableSharedFlow.collect(location, block)
}