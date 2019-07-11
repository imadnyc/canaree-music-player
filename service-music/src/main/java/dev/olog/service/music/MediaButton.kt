package dev.olog.service.music

import dev.olog.injection.dagger.PerService
import dev.olog.service.music.EventDispatcher.Event
import kotlinx.coroutines.*
import javax.inject.Inject


@PerService
internal class MediaButton @Inject internal constructor(
    private val eventDispatcher: EventDispatcher

) : CoroutineScope by MainScope() {

    companion object {
        internal const val DELAY = 300L
        internal const val MAX_ALLOWED_CLICKS = 3
    }

    private var clicks = 0

    private var job: Job? = null

    fun onHeatSetHookClick() {
        clicks++

        if (clicks <= MAX_ALLOWED_CLICKS) {
            job?.cancel()
            job = launch {
                // TODO check if works
                delay(DELAY)
                dispatchEvent(clicks)
                clicks = 0
            }
        }
    }

    private fun dispatchEvent(clicks: Int) {
        when (clicks) {
            0 -> {
            }
            1 -> eventDispatcher.dispatchEvent(Event.PLAY_PAUSE)
            2 -> eventDispatcher.dispatchEvent(Event.SKIP_NEXT)
            3 -> eventDispatcher.dispatchEvent(Event.SKIP_PREVIOUS)
            else -> { // TODO speech ??
            }
        }
    }

}
