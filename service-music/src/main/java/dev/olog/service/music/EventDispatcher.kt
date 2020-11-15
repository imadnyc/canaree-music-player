package dev.olog.service.music

import android.app.Service
import android.media.AudioManager
import android.util.Log
import android.view.KeyEvent
import dev.olog.shared.android.extensions.systemService
import javax.inject.Inject

internal class EventDispatcher @Inject constructor(
    service: Service
) {

    companion object {
        private val TAG = "SM:${EventDispatcher::class.java.simpleName}"
    }

    private val audioManager = service.systemService<AudioManager>()

    enum class Event {
        PLAY_PAUSE,
        PLAY,
        PAUSE,
        STOP,
        SKIP_NEXT,
        SKIP_PREVIOUS,
        TRACK_ENDED
    }

    fun dispatchEvent(event: Event) {
        Log.v(TAG, "dispatchEvent $event")

        val keycode = when (event) {
            Event.PLAY_PAUSE -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            Event.PLAY -> KeyEvent.KEYCODE_MEDIA_PLAY
            Event.PAUSE -> KeyEvent.KEYCODE_MEDIA_PAUSE
            Event.STOP -> KeyEvent.KEYCODE_MEDIA_STOP
            Event.SKIP_NEXT -> KeyEvent.KEYCODE_MEDIA_NEXT
            Event.SKIP_PREVIOUS -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            Event.TRACK_ENDED -> KeyEvent.KEYCODE_MEDIA_FAST_FORWARD // TODO bad
        }
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keycode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keycode))
    }

}