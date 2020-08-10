package me.ryanpierce.trialanimations

class MeteorStateFlow(private val meteorMutableStateFlow: MeteorMutableStateFlow) {

    var value = meteorMutableStateFlow.value
        private set

    suspend fun collect(location: Coordinate, block: suspend (Meteor) -> Unit) =
        meteorMutableStateFlow.collect(location, block)
}