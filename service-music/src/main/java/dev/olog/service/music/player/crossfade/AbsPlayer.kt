package dev.olog.service.music.player.crossfade

import android.content.Context
import android.util.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.*
import dev.olog.core.Config
import dev.olog.service.music.R
import dev.olog.service.music.interfaces.IMaxAllowedPlayerVolume
import dev.olog.service.music.interfaces.IPlayerDelegate
import dev.olog.service.music.interfaces.ISourceFactory
import dev.olog.shared.android.extensions.toast
import dev.olog.shared.clamp

/**
 * This class handles playback
 */
internal abstract class AbsPlayer<T>(
    private val context: Context,
    lifecycle: Lifecycle,
    private val mediaSourceFactory: ISourceFactory<T>,
    volume: IMaxAllowedPlayerVolume,
    private val config: Config,

) : IPlayerDelegate<T>,
    Player.Listener,
    DefaultLifecycleObserver {

    private val factory = DefaultRenderersFactory(context).apply {
        setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

    }
    protected val player: ExoPlayer = ExoPlayer.Builder(context, factory).build()

    init {
        lifecycle.addObserver(this)

        volume.listener = object : IMaxAllowedPlayerVolume.Listener {
            override fun onMaxAllowedVolumeChanged(volume: Float) {
                player.volume = volume
            }
        }
    }

    @CallSuper
    override fun onDestroy(owner: LifecycleOwner) {
        player.release()
    }

    @CallSuper
    override fun prepare(mediaEntity: T, bookmark: Long) {
        val mediaSource = mediaSourceFactory.get(mediaEntity)
        player.prepare(mediaSource)
        player.playWhenReady = false
        player.seekTo(bookmark)
    }

    @CallSuper
    override fun play(mediaEntity: T, hasFocus: Boolean, isTrackEnded: Boolean) {
        val mediaSource = mediaSourceFactory.get(mediaEntity)
        player.prepare(mediaSource, true, true)
        if (mediaEntity is CrossFadePlayer.Model){
            player.seekTo(mediaEntity.playerMediaEntity.bookmark)
        }
        player.playWhenReady = hasFocus
    }

    @CallSuper
    override fun resume() {
        player.playWhenReady = true
    }

    @CallSuper
    override fun pause() {
        player.playWhenReady = false
    }

    @CallSuper
    override fun seekTo(where: Long) {
        val safeSeek = clamp(where, 0L, getDuration())
        player.seekTo(safeSeek)
    }

    @CallSuper
    override fun isPlaying(): Boolean {
        return player.playWhenReady
    }

    @CallSuper
    override fun getBookmark(): Long {
        return player.currentPosition
    }

    @CallSuper
    override fun getDuration(): Long {
        return player.duration
    }

    @CallSuper
    override fun setVolume(volume: Float) {
        player.volume = volume
    }

    @CallSuper
    override fun onPlayerError(error: PlaybackException) {
        if (error is ExoPlaybackException) {
            val what = when (error.type) {
                ExoPlaybackException.TYPE_SOURCE -> error.sourceException.message
                ExoPlaybackException.TYPE_RENDERER -> error.rendererException.message
                ExoPlaybackException.TYPE_UNEXPECTED -> error.unexpectedException.message
                else -> "Unknown: $error"
            }
            error.printStackTrace()

            if (config.isDebug) {
                Log.e("Player", "onPlayerError $what")
            }
            context.applicationContext.toast(R.string.music_player_error)
        } else {
            // TODO
        }
    }

}