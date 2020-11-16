package dev.olog.service.music

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.scopes.ServiceScoped
import dev.olog.core.MediaId
import dev.olog.core.gateway.FavoriteGateway
import dev.olog.intents.MusicServiceCustomAction
import dev.olog.service.music.interfaces.IPlayer
import dev.olog.service.music.interfaces.IQueue
import dev.olog.service.music.model.PlayerMediaEntity
import dev.olog.service.music.model.SkipType
import dev.olog.service.music.queue.SKIP_TO_PREVIOUS_THRESHOLD
import dev.olog.service.music.state.MusicServicePlaybackState
import dev.olog.service.music.state.MusicServiceRepeatMode
import dev.olog.service.music.state.MusicServiceShuffleMode
import dev.olog.shared.android.utils.assertBackgroundThread
import dev.olog.shared.android.utils.assertMainThread
import dev.olog.shared.autoDisposeJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ServiceScoped
internal class MediaSessionCallback @Inject constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val queue: IQueue,
    private val player: IPlayer,
    private val repeatMode: MusicServiceRepeatMode,
    private val shuffleMode: MusicServiceShuffleMode,
    private val mediaButton: MediaButton,
    private val playerState: MusicServicePlaybackState,
    private val favoriteGateway: FavoriteGateway
) : MediaSessionCompat.Callback() {

    private var retrieveDataJob by autoDisposeJob()

    override fun onPrepare() {
        onPrepareInternal(forced = true)
    }

    private fun onPrepareInternal(forced: Boolean){
        assertMainThread()

        if (queue.isEmpty() || forced){
            val track = queue.prepare()
            if (track != null){
                player.prepare(track)
            }
        }
    }

    private fun retrieveAndPlay(retrieve: suspend () -> PlayerMediaEntity?) {
        retrieveDataJob = lifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            assertBackgroundThread()
            val entity = retrieve()
            if (entity != null) {
                withContext(Dispatchers.Main) {
                    player.play(entity)
                }
            } else {
                onEmptyQueue()
            }
        }
    }

    private fun onEmptyQueue() {
        onStop()
    }

    override fun onPlayFromMediaId(stringMediaId: String, extras: Bundle?) {
        onPrepareInternal(false)

        retrieveAndPlay {
            updatePodcastPosition()

            when (val mediaId = MediaId.fromString(stringMediaId)) {
                MediaId.shuffleId() -> {
                    // android auto call 'onPlayFromMediaId' with 'MediaId.shuffleId()'
                    queue.handlePlayShuffle(mediaId, null)
                }
                else -> {
                    val filter = extras?.getString(MusicServiceCustomAction.ARGUMENT_FILTER)
                    queue.handlePlayFromMediaId(mediaId, filter)
                }
            }
        }
    }

    override fun onPlay() {
        onPrepareInternal(false)
        player.resume()
    }

    override fun onPlayFromSearch(query: String, extras: Bundle) {
        onPrepareInternal(false)

        retrieveAndPlay {
            updatePodcastPosition()
            queue.handlePlayFromGoogleSearch(query, extras)
        }
    }

    override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
        onPrepareInternal(false)

        retrieveAndPlay {
            updatePodcastPosition()
            queue.handlePlayFromUri(uri)
        }
    }

    override fun onPause() {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            updatePodcastPosition()
            player.pause(true)
        }
    }

    override fun onStop() {
        onPause()
    }

    override fun onSkipToNext() {
        onSkipToNext(false)
    }

    override fun onSkipToPrevious() {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {

            updatePodcastPosition()
            queue.handleSkipToPrevious(player.getBookmark())?.let { metadata ->

                val skipType = if (!metadata.mediaEntity.isPodcast && player.getBookmark() > SKIP_TO_PREVIOUS_THRESHOLD)
                        SkipType.RESTART else SkipType.SKIP_PREVIOUS
                player.playNext(metadata, skipType)
            }
        }
    }

    private fun onTrackEnded() {
        onSkipToNext(true)
    }

    /**
     * Try to skip to next song, if can't, restart current and pause
     */
    private fun onSkipToNext(trackEnded: Boolean) = lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
        updatePodcastPosition()
        val metadata = queue.handleSkipToNext(trackEnded)
        if (metadata != null) {
            val skipType = if (trackEnded) SkipType.TRACK_ENDED else SkipType.SKIP_NEXT
            player.playNext(metadata, skipType)
        } else {
            val currentSong = queue.getPlayingSong()
            if (currentSong != null) {
                player.play(currentSong)
                player.pause(true)
                player.seekTo(0L)
            } else {
                onEmptyQueue()
            }
        }
    }

    override fun onSkipToQueueItem(id: Long) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {

            updatePodcastPosition()
            val mediaEntity = queue.handleSkipToQueueItem(id)
            if (mediaEntity != null) {
                player.play(mediaEntity)
            } else {
                onEmptyQueue()
            }
        }
    }

    override fun onSeekTo(pos: Long) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            updatePodcastPosition()
            player.seekTo(pos)
        }
    }

    override fun onSetRating(rating: RatingCompat?) {
        onSetRating(rating, null)
    }

    override fun onSetRating(rating: RatingCompat?, extras: Bundle?) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Default) { favoriteGateway.toggleFavorite() }
    }

    override fun onCustomAction(action: String, extras: Bundle?) {
        onPrepareInternal(false)

        val musicAction = MusicServiceCustomAction.values().find { it.name == action }
            ?: return // other apps can request custom action

        when (musicAction) {
            MusicServiceCustomAction.SHUFFLE -> {
                requireNotNull(extras)
                val mediaId = extras.getString(MusicServiceCustomAction.ARGUMENT_MEDIA_ID)!!
                val filter = extras.getString(MusicServiceCustomAction.ARGUMENT_FILTER)
                retrieveAndPlay {
                    updatePodcastPosition()
                    queue.handlePlayShuffle(MediaId.fromString(mediaId), filter)
                }
            }
            MusicServiceCustomAction.SWAP -> {
                requireNotNull(extras)
                val from = extras.getInt(MusicServiceCustomAction.ARGUMENT_SWAP_FROM, 0)
                val to = extras.getInt(MusicServiceCustomAction.ARGUMENT_SWAP_TO, 0)
                queue.handleSwap(from, to)
            }
            MusicServiceCustomAction.SWAP_RELATIVE -> {
                requireNotNull(extras)
                val from = extras.getInt(MusicServiceCustomAction.ARGUMENT_SWAP_FROM, 0)
                val to = extras.getInt(MusicServiceCustomAction.ARGUMENT_SWAP_TO, 0)
                queue.handleSwapRelative(from, to)
            }
            MusicServiceCustomAction.REMOVE -> {
                requireNotNull(extras)
                val position = extras.getInt(MusicServiceCustomAction.ARGUMENT_POSITION, -1)
                queue.handleRemove(position)
            }
            MusicServiceCustomAction.REMOVE_RELATIVE -> {
                requireNotNull(extras)
                val position = extras.getInt(MusicServiceCustomAction.ARGUMENT_POSITION, -1)
                queue.handleRemoveRelative(position)
            }
            MusicServiceCustomAction.PLAY_RECENTLY_ADDED -> {
                requireNotNull(extras)
                val mediaId = extras.getString(MusicServiceCustomAction.ARGUMENT_MEDIA_ID)!!
                retrieveAndPlay {
                    updatePodcastPosition()
                    queue.handlePlayRecentlyAdded(MediaId.fromString(mediaId))
                }
            }
            MusicServiceCustomAction.PLAY_MOST_PLAYED -> {
                requireNotNull(extras)
                val mediaId = extras.getString(MusicServiceCustomAction.ARGUMENT_MEDIA_ID)!!
                retrieveAndPlay {
                    updatePodcastPosition()
                    queue.handlePlayMostPlayed(MediaId.fromString(mediaId))
                }
            }
            MusicServiceCustomAction.FORWARD_10 -> player.forwardTenSeconds()
            MusicServiceCustomAction.FORWARD_30 -> player.forwardThirtySeconds()
            MusicServiceCustomAction.REPLAY_10 -> player.replayTenSeconds()
            MusicServiceCustomAction.REPLAY_30 -> player.replayThirtySeconds()
            MusicServiceCustomAction.TOGGLE_FAVORITE -> onSetRating(null)
            MusicServiceCustomAction.ADD_TO_PLAY_LATER -> {
                lifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                    requireNotNull(extras)
                    val mediaIds =
                        extras.getLongArray(MusicServiceCustomAction.ARGUMENT_MEDIA_ID_LIST)!!
                    val isPodcast = extras.getBoolean(MusicServiceCustomAction.ARGUMENT_IS_PODCAST)

                    val position = queue.playLater(mediaIds.toList(), isPodcast)
                    playerState.toggleSkipToActions(position)
                }
            }
            MusicServiceCustomAction.ADD_TO_PLAY_NEXT -> {
                lifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                    requireNotNull(extras)
                    val mediaIds =
                        extras.getLongArray(MusicServiceCustomAction.ARGUMENT_MEDIA_ID_LIST)!!
                    val isPodcast = extras.getBoolean(MusicServiceCustomAction.ARGUMENT_IS_PODCAST)

                    val position = queue.playNext(mediaIds.toList(), isPodcast)
                    playerState.toggleSkipToActions(position)
                }
            }
            MusicServiceCustomAction.MOVE_RELATIVE -> {
                requireNotNull(extras)
                val position = extras.getInt(MusicServiceCustomAction.ARGUMENT_POSITION)
                queue.handleMoveRelative(position)
            }
        }
    }

    override fun onSetRepeatMode(repeatMode: Int) {
        this.repeatMode.update()
        playerState.toggleSkipToActions(queue.getCurrentPositionInQueue())
        queue.onRepeatModeChanged()
    }

    override fun onSetShuffleMode(unused: Int) {
        val newShuffleMode = this.shuffleMode.update()
        if (newShuffleMode) {
            queue.shuffle()
        } else {
            queue.sort()
        }
        playerState.toggleSkipToActions(queue.getCurrentPositionInQueue())
    }

    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
        onPrepareInternal(false)

        val event = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)!!
        if (event.action == KeyEvent.ACTION_DOWN) {

            when (event.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> handlePlayPause()
                KeyEvent.KEYCODE_MEDIA_NEXT -> onSkipToNext()
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> onSkipToPrevious()
                KeyEvent.KEYCODE_MEDIA_STOP -> player.stopService()
                KeyEvent.KEYCODE_MEDIA_PAUSE -> player.pause(false)
                KeyEvent.KEYCODE_MEDIA_PLAY -> onPlay()
                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> onTrackEnded()
                KeyEvent.KEYCODE_HEADSETHOOK -> mediaButton.onHeatSetHookClick()
                else -> throw IllegalArgumentException("not handled")
            }
        }

        return true
    }

    /**
     * this function DO NOT KILL service on pause
     */
    fun handlePlayPause() {
        if (player.isPlaying()) {
            player.pause(false)
        } else {
            onPlay()
        }
    }

    private suspend fun updatePodcastPosition() {
        val bookmark = withContext(Dispatchers.Main) { player.getBookmark() }
        withContext(Dispatchers.IO){
            queue.updatePodcastPosition(bookmark)
        }
    }

}