package dev.olog.lib.media

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import dev.olog.core.MediaId
import dev.olog.lib.media.connection.IMediaConnectionCallback
import dev.olog.lib.media.connection.MusicServiceConnection
import dev.olog.lib.media.connection.MusicServiceConnectionState
import dev.olog.lib.media.connection.OnConnectionChanged
import dev.olog.lib.media.controller.IMediaControllerCallback
import dev.olog.lib.media.controller.MediaControllerCallback
import dev.olog.lib.media.model.*
import dev.olog.shared.android.Permissions
import dev.olog.shared.autoDisposeJob
import dev.olog.shared.exhaustive
import dev.olog.shared.lazyFast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaExposer(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onConnectionChanged: OnConnectionChanged,
    private val musicServiceClass: Class<*>,
) : IMediaControllerCallback,
    IMediaConnectionCallback {

    private val mediaBrowser: MediaBrowserCompat by lazyFast {
        MediaBrowserCompat(
            context,
            ComponentName(context, musicServiceClass),
            MusicServiceConnection(this),
            null
        )
    }

    private var connectionJob by autoDisposeJob()
    private var queueJob by autoDisposeJob()

    val callback: MediaControllerCompat.Callback = MediaControllerCallback(this)

    private val connectionPublisher = MutableStateFlow<MusicServiceConnectionState?>(null)

    private val metadataPublisher = MutableStateFlow<PlayerMetadata?>(null)
    private val statePublisher = MutableStateFlow<PlayerPlaybackState?>(null)
    private val repeatModePublisher = MutableStateFlow<PlayerRepeatMode?>(null)
    private val shuffleModePublisher = MutableStateFlow<PlayerShuffleMode?>(null)
    private val queuePublisher = MutableStateFlow<List<PlayerItem>>(listOf())

    fun connect() {
        if (!Permissions.canReadStorage(context)) {
            Timber.w("Storage permission is not granted")
            return
        }
        connectionJob = connectionPublisher
            .filterNotNull()
            .onEach { state ->
                Timber.d("Connection state=$state")
                when (state) {
                    MusicServiceConnectionState.CONNECTED -> onConnectionChanged.onConnectedSuccess(
                        mediaBrowser = mediaBrowser,
                        callback = callback
                    )
                    MusicServiceConnectionState.FAILED -> onConnectionChanged.onConnectedFailed(
                        mediaBrowser = mediaBrowser,
                        callback = callback
                    )
                }.exhaustive
            }.launchIn(coroutineScope)

        if (!mediaBrowser.isConnected){
            mediaBrowser.connect()
        }
    }

    fun disconnect() {
        connectionPublisher.value = null
        connectionJob = null
        queueJob = null
        if (mediaBrowser.isConnected){
            mediaBrowser.disconnect()
        }
    }

    /**
     * Populate publishers with current data
     */
    fun initialize(mediaController: MediaControllerCompat) {
        callback.onMetadataChanged(mediaController.metadata)
        callback.onPlaybackStateChanged(mediaController.playbackState)
        callback.onRepeatModeChanged(mediaController.repeatMode)
        callback.onShuffleModeChanged(mediaController.shuffleMode)
        callback.onQueueChanged(mediaController.queue)
    }

    override fun onConnectionStateChanged(state: MusicServiceConnectionState) {
        connectionPublisher.value = state
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        metadata ?: return
        metadataPublisher.value = PlayerMetadata(metadata)
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        state ?: return
        statePublisher.value = PlayerPlaybackState(state)
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        repeatModePublisher.value = PlayerRepeatMode.of(repeatMode)
    }

    override fun onShuffleModeChanged(shuffleMode: Int) {
        shuffleModePublisher.value = PlayerShuffleMode.of(shuffleMode)
    }

    override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
        queue ?: return
        queueJob = coroutineScope.launch(Dispatchers.Default) {
            val result = queue.mapNotNull(MediaSessionCompat.QueueItem::toDisplayableItem)
            queuePublisher.value = result
        }
    }

    val metadata: Flow<PlayerMetadata>
        get() = metadataPublisher.filterNotNull()

    val playbackState: Flow<PlayerPlaybackState>
        get() = statePublisher.filterNotNull()

    val repeat: Flow<PlayerRepeatMode>
        get() = repeatModePublisher.filterNotNull()

    val shuffle: Flow<PlayerShuffleMode>
        get() = shuffleModePublisher.filterNotNull()

    val queue: Flow<List<PlayerItem>>
        get() = queuePublisher

}

private fun MediaSessionCompat.QueueItem.toDisplayableItem(): PlayerItem? {
    val description = this.description ?: return null
    val mediaId = description.mediaId ?: return null
    val title = description.title?.toString() ?: return null
    val subtitle = description.title?.toString() ?: return null

    return PlayerItem(
        mediaId = MediaId.fromString(mediaId),
        title = title,
        subtitle = subtitle,
        serviceProgressive = this.queueId
    )
}