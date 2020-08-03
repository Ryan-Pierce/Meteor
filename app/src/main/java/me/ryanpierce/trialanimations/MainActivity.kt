package me.ryanpierce.trialanimations

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.*
import me.ryanpierce.trialanimations.Meteor.Companion.addMeteors
import me.ryanpierce.trialanimations.MeteorCoroutine.Companion.addCoroutines
import me.ryanpierce.trialanimations.MeteorCoroutine.Companion.start

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

        // Locations
        val origin = (60f to 200f)
        val coroutine1 = origin
        val coroutine2 = (400f to 500f)
        val coroutine3 = (200f to 900f)
        val coroutine4 = (600f to 900f)
        val coroutine5 = (400f to 1200f)

        // Meteors
        val meteors = listOf(
            Meteor(origin, this, this, "1"),
            Meteor(origin, this, this, "2")
        )
        layout.addMeteors(meteors)

        // Coroutines
        val channel = MeteorChannel()
        val fanOutChannel = MeteorChannel()
        val fanInChannel = MeteorChannel()
        val coroutines = listOf(
            MeteorCoroutine(coroutine1, this, this) {
                meteors.forEach { channel.send(it) }
            },
            MeteorCoroutine(coroutine2, this, this) { meteorCoroutine ->
                meteors.forEach {
                    val meteor = channel.receive(meteorCoroutine)
                    fanOutChannel.send(meteor)
                }
            },
            MeteorCoroutine(coroutine3, this, this) { meteorCoroutine ->
                meteors.forEach {
                    val meteor = fanOutChannel.receive(meteorCoroutine)
                    fanInChannel.send(meteor)
                }
            },
            MeteorCoroutine(coroutine4, this, this) { meteorCoroutine ->
                meteors.forEach {
                    val meteor = fanOutChannel.receive(meteorCoroutine)
                    fanInChannel.send(meteor)
                }
            },
            MeteorCoroutine(coroutine5, this, this) { meteorCoroutine ->
                meteors.forEach {
                    fanInChannel.receive(meteorCoroutine)
                }
            }
        )
        layout.addCoroutines(coroutines)
        coroutines.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}
