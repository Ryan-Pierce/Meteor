package me.ryanpierce.trialanimations

class MeteorSharedFlow(private val meteorMutableSharedFlow: MeteorMutableSharedFlow) {

    var value = meteorMutableSharedFlow.value
        private set

    suspend fun collect(location: Coordinate, block: suspend (Meteor) -> Unit) =
        meteorMutableSharedFlow.collect(location, block)
}