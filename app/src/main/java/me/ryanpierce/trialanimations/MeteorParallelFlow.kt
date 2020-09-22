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

        scope.launch(coordinate.first x coordinate.second + 600) { location ->
            receiveChannel.forEach(location) { meteor ->
                block(meteor)
            }
        }

        repeat(concurrency) { count ->
            val workerLocation = coordinate.first + (count * 300) x coordinate.second + 300
            scope.launch(workerLocation, "Worker $count") { location ->
                sendChannel.forEach(location) { meteor ->
                    delay(1000) // Allows meteors to linger, demonstrating parallelism
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

/**
 * The parallel contract might look something like this
 *
 *   someFlow
 *       .parallel { T
 *           val result: R = transform(T)
 *           result
 *       }
 *       .moreOperatorsNotInParallel { R -> //... }
 *
 *  Tricky questions:
 *  -Should order be maintained? If so, should it be an option so
 *      that dev's can decline maintaining order to boost performance.
 *  -How is the concurrency count determined? ideally this value is dyamically
 *      calculated and throttled based on the hardware and available resources.
 *  -Compare the behavior of this and under-the-hood implementation to the
 *  Flow.flatMapMerge() operator. (flatMapMerge is bascially the channelFlow {} idea vs.
 *  a pool of coroutines that never cancel and are fed by a fan-out channel.) My pool
 *  is more reactive (since it stays alive until the lifecycle and thus coroutinescope is cancelled)
 *  and spends less time spinning up new coroutines and is less wasteful
 *  with coroutines by reusing the existing pool.
 */

fun Flow<Meteor>.parallel(
    meteorCoroutineScope: MeteorCoroutineScope,
    concurrency: Int = 3,
    transform: suspend (Meteor) -> Meteor = { it }
    // When Meteor<T> exists with T as data, transform should be (Meteor<T>) -> Meteor<R>
) = MeteorParallelFlow(this, meteorCoroutineScope, concurrency, transform)


