package dev.olog.lib.media

import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat

fun MediaControllerCompat.skipToNext() {
    transportControls?.skipToNext()
}

fun MediaControllerCompat.skipToPrevious() {
    transportControls?.skipToPrevious()
}

fun MediaControllerCompat.playPause() {
    val playbackState = playbackState
    playbackState?.let {
        when (it.state) {
            PlaybackStateCompat.STATE_PLAYING -> transportControls?.pause()
            PlaybackStateCompat.STATE_PAUSED -> transportControls?.play()
            else -> {
            }
        }
    }
}

fun MediaControllerCompat.TransportControls.customAction(
    action: MusicServiceCustomAction,
    extras: Bundle? = null
) {
    sendCustomAction(action.name, extras)
}