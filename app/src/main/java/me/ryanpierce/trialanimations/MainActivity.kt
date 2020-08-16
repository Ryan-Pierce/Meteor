package me.ryanpierce.trialanimations

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import me.ryanpierce.trialanimations.Meteor.Factory.Companion.addMeteors
import me.ryanpierce.trialanimations.Demo.*

// GOAL OF METEOR
// The idea is that meteor is something you can use to visualize the mechanics
//  of your flows. Specifically, you can send meteors into your flows and the
//  app will visualize the meteors moving through the machinery

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
        when (STATE_FLOW_DEMO) {
            PARALLEL_FLOW_DEMO -> parallelFlowDemo()
            SHARED_FLOW_DEMO -> sharedFlowDemo()
            STATE_FLOW_DEMO -> stateFlowDemo()
            FLOW_DEMO -> flowDemo()
            WORKER_POOL_DEMO -> workerPoolDemo()
        }
    }

    fun parallelFlowDemo() {
        val origin = 10f x 100f
        val meteors = Meteor.Factory(origin, this, layout, this).addMeteors(6)

        val config = MeteorCoroutineScope.Config(layout, this)
        val scope = MeteorCoroutineScope(this, config)

        scope.launch(80f x 200f, "Start") { location ->
            meteors
                .asFlow()
                .parallel(scope)
                .collect(location) { meteor ->
                    meteor.landAsMeteorite()
                }
        }
    }

    fun sharedFlowDemo() {
        val origin = 60f x 200f
        val meteors = Meteor.Factory(origin, this, layout, this).addMeteors(4)

        val mutableSharedFlow = MeteorMutableSharedFlow(meteors.first(), layout)
        val sharedFlow = MeteorSharedFlow(mutableSharedFlow)

        val config = MeteorCoroutineScope.Config(layout, this)
        val scope = MeteorCoroutineScope(this, config)

        scope.launch(400f x 500f, "one") { location ->
            sharedFlow.collect(location) { meteor ->
                meteor.landAsMeteorite()
                delay(3000) // Making it a slow collector
            }
        }

        scope.launch(400f x 900f, "two") { location ->
            sharedFlow.collect(location) { meteor ->
                meteor.landAsMeteorite()
            }
        }

        // Regular coroutine
        launch {
            meteors.forEach { meteor ->
                delay(1500)
                mutableSharedFlow.value = meteor
            }
        }
    }

    fun stateFlowDemo() {
        val origin = 60f x 200f
        val meteors = Meteor.Factory(origin, this, layout, this).addMeteors(4)

        val mutableStateFlow = MeteorMutableStateFlow(meteors.first(), layout)
        val stateFlow = MeteorStateFlow(mutableStateFlow)

        val config = MeteorCoroutineScope.Config(layout, this)
        val scope = MeteorCoroutineScope(this, config)

        scope.launch(400f x 500f, "one") { location ->
            stateFlow.collect(location) { meteor ->
                meteor.landAsMeteorite()
                delay(4000) // Making it a slow collector
            }
        }

        scope.launch(400f x 900f, "two") { location ->
            stateFlow.collect(location) { meteor ->
                meteor.landAsMeteorite()
            }
        }

        // Regular coroutine
        launch {
            meteors.forEach { meteor ->
                delay(1500)
                mutableStateFlow.value = meteor
            }
        }
    }

    fun flowDemo() {
        val origin = 60f x 200f
        val meteors = Meteor.Factory(origin, this, layout, this).addMeteors(2)

        val config = MeteorCoroutineScope.Config(layout, this)
        val scope = MeteorCoroutineScope(this, config)

        scope.launch(400f x 500f, "Start") { location ->
            meteors.asFlow().collect(location) { meteor ->
                meteor.setBackgroundResource(R.drawable.blue_circle)
            }
        }
    }

    fun workerPoolDemo() {
        val origin = 60f x 200f
        val meteors = Meteor.Factory(origin, this, layout, this).addMeteors(4)


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

        scope.launch(200f x 900f, "Worker") { location ->
            fanOutChannel.forEach(location) { meteor ->
                delay(1000)
                fanInChannel.send(meteor)
            }
        }

        scope.launch(600f x 900f) { location ->
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