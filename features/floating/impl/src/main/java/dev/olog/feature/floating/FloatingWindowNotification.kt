package dev.olog.feature.floating

import android.app.*
import android.content.Intent
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.olog.core.gateway.PlayingItemGateway
import dev.olog.shared.android.extensions.asServicePendingIntent
import dev.olog.shared.android.extensions.colorControlNormal
import dev.olog.shared.android.utils.isOreo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CHANNEL_ID = "0xfff"

class FloatingWindowNotification @Inject constructor(
    private val service: Service,
    lifecycleOwner: LifecycleOwner,
    private val notificationManager: NotificationManager,
    private val playingItemGateway: PlayingItemGateway,

) : DefaultLifecycleObserver {

    companion object {
        const val NOTIFICATION_ID = 0xABC
    }

    private val builder = NotificationCompat.Builder(
        service,
        CHANNEL_ID
    )
    private var disposable: Job? = null

    private var notificationTitle = ""

    init {
        lifecycleOwner.lifecycle.addObserver(this)

    }

    override fun onDestroy(owner: LifecycleOwner) {
        disposable?.cancel()
    }

    fun startObserving() {
        disposable?.cancel()
        disposable = GlobalScope.launch {
            // keeps playing song in sync
            playingItemGateway.observe()
                .filterNotNull()
                .collect {
                    val description = when (it.artist) {
                        MediaStore.UNKNOWN_STRING -> it.artist
                        else -> "${it.title} ${it.artist}"
                    }
                    notificationTitle = description
                    val notification = builder.setContentTitle(notificationTitle).build()
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
        }
    }

    fun buildNotification(): Notification {
        createChannel()

        return builder
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(dev.olog.shared.android.R.drawable.vd_bird_singing)
            .setContentTitle(notificationTitle)
            .setContentText(service.getString(localization.R.string.floating_window_notification_content_text))
            .setColor(service.colorControlNormal())
            .setContentIntent(createContentIntent())
            .setGroup("dev.olog.msc.FLOATING")
            .build()
    }

    private fun createChannel() {
        if (!isOreo()){
            return
        }
        val nowPlayingChannelExists = notificationManager.getNotificationChannel(CHANNEL_ID) != null
        if (nowPlayingChannelExists){
            return
        }

        // create notification channel
        val name = service.getString(localization.R.string.floating_window_notification_channel_title)
        val description =
            service.getString(localization.R.string.floating_window_notification_channel_description)

        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(service, FloatingWindowService::class.java)
        intent.action = FloatingWindowService.ACTION_STOP
        return intent.asServicePendingIntent(service, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}