package dev.olog.feature.media.notification

import android.app.Notification
import android.app.Service
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.scopes.ServiceScoped
import dev.olog.core.ServiceScope
import dev.olog.core.entity.favorite.FavoriteEnum
import dev.olog.core.interactor.favorite.ObserveFavoriteAnimationUseCase
import dev.olog.feature.media.api.model.Event
import dev.olog.feature.media.api.model.MediaEntity
import dev.olog.feature.media.api.model.MetadataEntity
import dev.olog.feature.media.api.model.MusicNotificationState
import dev.olog.feature.media.interfaces.INotification
import dev.olog.feature.media.interfaces.IPlayerLifecycle
import dev.olog.shared.isOreo
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@ServiceScoped
internal class MusicNotificationManager @Inject constructor(
    private val service: Service,
    private val notificationImpl: INotification,
    observeFavoriteUseCase: ObserveFavoriteAnimationUseCase,
    playerLifecycle: IPlayerLifecycle,
    private val serviceScope: ServiceScope,
) : DefaultLifecycleObserver {

    companion object {
        private const val METADATA_PUBLISH_DELAY = 350L
        private const val STATE_PUBLISH_DELAY = 100L
        private const val FAVORITE_PUBLISH_DELAY = 100L
    }

    private var isForeground: Boolean = false

    private val publisher = Channel<Event>(Channel.UNLIMITED)
    private val currentState = MusicNotificationState()
    private var publishJob: Job? = null

    private val playerListener = object : IPlayerLifecycle.Listener {
        override fun onPrepare(metadata: MetadataEntity) {
            onNextMetadata(metadata.entity)
        }

        override fun onMetadataChanged(metadata: MetadataEntity) {
            onNextMetadata(metadata.entity)
        }

        override fun onStateChanged(state: PlaybackStateCompat) {
            onNextState(state)
        }
    }

    init {
        playerLifecycle.addListener(playerListener)

        publisher.consumeAsFlow()
            .filter { event ->
                when (event) {
                    is Event.Metadata -> currentState.isDifferentMetadata(event.entity)
                    is Event.State -> currentState.isDifferentState(event.state)
                    is Event.Favorite -> currentState.isDifferentFavorite(event.favorite)
                }
            }.onEach { consumeEvent(it) }
            .launchIn(serviceScope)

        observeFavoriteUseCase()
            .map { it == FavoriteEnum.FAVORITE }
            .onEach { onNextFavorite(it) }
            .launchIn(serviceScope)
    }

    private suspend fun consumeEvent(event: Event){
        publishJob?.cancel()
        when (event){
            is Event.Metadata -> {
                if (currentState.updateMetadata(event.entity)) {
                    publishNotification(currentState.deepCopy(), METADATA_PUBLISH_DELAY)
                }
            }
            is Event.State -> {
                if (currentState.updateState(event.state)){
                    publishNotification(currentState.deepCopy(), STATE_PUBLISH_DELAY)
                }
            }
            is Event.Favorite -> {
                if (currentState.updateFavorite(event.favorite)){
                    publishNotification(currentState.deepCopy(), FAVORITE_PUBLISH_DELAY)
                }
            }
        }
    }

    private suspend fun publishNotification(state: MusicNotificationState, delay: Long) {
        require(currentState !== state) // to avoid concurrency problems a copy is passed

        if (!isForeground && isOreo()) {
            // oreo needs to post notification immediately after calling startForegroundService
            issueNotification(state)
        } else {
            // post delayed
            publishJob = serviceScope.launch {
                delay(delay)
                issueNotification(state)
            }
        }
    }

    private suspend fun issueNotification(state: MusicNotificationState) {
        val notification = notificationImpl.update(state)
        if (state.isPlaying) {
            startForeground(notification)
        } else {
            pauseForeground()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        stopForeground()
    }

    private fun onNextMetadata(metadata: MediaEntity) {
        publisher.trySend(Event.Metadata(metadata))
    }

    private fun onNextState(playbackState: PlaybackStateCompat) {
        publisher.trySend(Event.State(playbackState))
    }

    private fun onNextFavorite(isFavorite: Boolean) {
        publisher.trySend(Event.Favorite(isFavorite))
    }

    private fun stopForeground() {
        if (!isForeground) {
            return
        }

        service.stopForeground(true)
        notificationImpl.cancel()

        isForeground = false
    }

    private fun pauseForeground() {
        if (!isForeground) {
            return
        }

        // state paused
        service.stopForeground(false)

        isForeground = false
    }

    private fun startForeground(notification: Notification) {
        if (isForeground) {
            return
        }

        service.startForeground(INotification.NOTIFICATION_ID, notification)

        isForeground = true
    }

}