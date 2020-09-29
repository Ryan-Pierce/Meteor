package me.ryanpierce.trialanimations

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

data class MeteorParallelFlow(
    val meteorFlow: Flow<Meteor>,
    val scope: MeteorCoroutineScope,
    val concurrency: Int,
    val transform: suspend (Meteor) -> Meteor
) {

    @OptIn(kotlinx.coroutines.InternalCoroutinesApi::class)
    suspend fun collect(coordinate: Coordinate, block: suspend (Meteor) -> Unit) {

        val sendChannel = MeteorChannel()
        val receiveChannel = MeteorChannel()

        scope.launch(coordinate.first x coordinate.second + 800) { location ->
            receiveChannel.forEach(location) { meteor ->
                block(meteor)
            }
        }

        repeat(concurrency) { count ->
            val workerLocation = coordinate.first - 350 + (count * 350) x coordinate.second + 400
            scope.launch(workerLocation, "Worker $count") { location ->
                sendChannel.forEach(location) { meteor ->
                    delay(1000) // Allows meteors to linger, demonstrating concurrency
                    receiveChannel.send(transform(meteor))
                }
            }
        }

        this.meteorFlow.collect(
            object : FlowCollector<Meteor> {
                override suspend fun emit(value: Meteor) {
                    value.actor.send(coordinate)
                    sendChannel.send(value)
                }
            }
        )
    }
}

fun Flow<Meteor>.parallel(
    meteorCoroutineScope: MeteorCoroutineScope,
    concurrency: Int = 3,
    transform: suspend (Meteor) -> Meteor = { it }
    // When Meteor<T> exists with T as data, transform should be (Meteor<T>) -> Meteor<R>
) = MeteorParallelFlow(this, meteorCoroutineScope, concurrency, transform)


