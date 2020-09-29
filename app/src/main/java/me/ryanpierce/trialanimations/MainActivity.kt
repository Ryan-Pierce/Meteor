package me.ryanpierce.trialanimations

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import me.ryanpierce.trialanimations.Meteor.Factory.Companion.addMeteors
import me.ryanpierce.trialanimations.Demo.*

// GOAL OF METEOR
// Meteor visualizes the mechanics
//  of your flows. Specifically, you can send meteors into your flows and the
//  app will visualize the meteors moving through the machinery.

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    lateinit var layout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layout = findViewById(R.id.mainLayout)
    }

    override fun onStart() {
        super.onStart()

        // Choose the demo you'd like to see from the strings in this when statement
        when (PARALLEL_FLOW_DEMO) {
            PARALLEL_FLOW_DEMO -> parallelFlowDemo()
            SHARED_FLOW_DEMO -> sharedFlowDemo()
            STATE_FLOW_DEMO -> stateFlowDemo()
            FLOW_DEMO -> flowDemo()
            WORKER_POOL_DEMO -> workerPoolDemo()
        }
    }

    fun parallelFlowDemo() {
        val origin = 280f x 100f
        val meteors = Meteor.Factory(origin, this, layout, this).addMeteors(4)

        val config = MeteorCoroutineScope.Config(layout, this)
        val scope = MeteorCoroutineScope(this, config)

        scope.launch(375f x 300f) { location ->
            delay(1000)
            meteors
                .asFlow()
                .parallel(scope)
                .collect(location) { meteor ->
                    meteor.landAsMeteorite()
                }
        }
    }

    fun sharedFlowDemo() {
        val origin = 200f x 200f
        val meteors = Meteor.Factory(origin, this, layout, this).addMeteors(4)

        val mutableSharedFlow = MeteorMutableSharedFlow(layout, 0)
        val sharedFlow = MeteorSharedFlow(mutableSharedFlow) // TODO make similiar to SharedFlow contract

        val config = MeteorCoroutineScope.Config(layout, this)
        val scope = MeteorCoroutineScope(this, config)

        scope.launch(100f x 400f, "Fast") { location ->
            sharedFlow.collect(location) { meteor ->
                meteor.landAsMeteorite()
            }
        }

        scope.launch(500f x 400f, "Slow") { location ->
            sharedFlow.collect(location) { meteor ->
                meteor.landAsMeteorite()
                delay(1300) // Making it a slow collector
            }
        }

        // Regular coroutine
        launch {
            meteors.forEach { meteor ->
                delay(900)
                mutableSharedFlow.emit(meteor)
            }
        }
    }

    fun stateFlowDemo() {
        val origin = 200f x 200f
        val meteors = Meteor.Factory(origin, this, layout, this).addMeteors(4)

        val mutableStateFlow = MeteorMutableStateFlow(layout, meteors.first())
        val stateFlow = MeteorStateFlow(mutableStateFlow) // TODO make similiar to StateFlow contract

        val config = MeteorCoroutineScope.Config(layout, this)
        val scope = MeteorCoroutineScope(this, config)

        scope.launch(500f x 400f, "Slow") { location ->
            stateFlow.collect(location) { meteor ->
                meteor.landAsMeteorite()
                delay(1500) // Making it a slow collector
            }
        }

        scope.launch(100f x 400f, "Fast") { location ->
            stateFlow.collect(location) { meteor ->
                meteor.landAsMeteorite()
            }
        }

        // Regular coroutine
        launch {
            meteors.forEach { meteor ->
                delay(1000)
                mutableStateFlow.value = meteor
            }
        }
    }

    fun flowDemo() {
        val origin = 300f x 300f
        val meteors = Meteor.Factory(origin, this, layout, this).addMeteors(4).asFlow()

        val config = MeteorCoroutineScope.Config(layout, this)
        val coroutineScope = MeteorCoroutineScope(this, config)

        coroutineScope.launch { location ->
            meteors.collect(location) { meteor ->
                meteor.setBackgroundResource(R.drawable.green_circle)
            }
        }
    }

    fun workerPoolDemo() {
        val origin = 60f x 200f
        val meteors = Meteor.Factory(origin, this, layout, this).addMeteors(4)

        // TODO show channels as lines
        val channel = MeteorChannel()
        val fanOutChannel = MeteorChannel()
        val fanInChannel = MeteorChannel()

        val config = MeteorCoroutineScope.Config(layout, this)
        val scope = MeteorCoroutineScope(this, config)

        scope.launch(origin, "Start") {
            meteors.forEach { channel.send(it) }
        }

        scope.launch(400f x 500f, "Fan Out") { location ->
            channel.forEach(location) { meteor ->
                fanOutChannel.send(meteor)
            }
        }

        scope.launch(200f x 850f, "Worker 1") { location ->
            fanOutChannel.forEach(location) { meteor ->
                delay(1000)
                fanInChannel.send(meteor)
            }
        }

        scope.launch(600f x 850f, "Worker 2") { location ->
            fanOutChannel.forEach(location) { meteor ->
                delay(1000)
                fanInChannel.send(meteor)
            }
        }

        scope.launch(400f x 1200f, "End") { location ->
            fanInChannel.forEach(location) { meteor ->
                meteor.landAsMeteorite()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}

infix fun Float.x(that: Float) = this to that

enum class Demo {
    PARALLEL_FLOW_DEMO,
    SHARED_FLOW_DEMO,
    STATE_FLOW_DEMO,
    FLOW_DEMO,
    WORKER_POOL_DEMO
}