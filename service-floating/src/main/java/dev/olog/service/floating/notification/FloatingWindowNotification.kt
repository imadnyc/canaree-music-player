package dev.olog.service.floating.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.olog.core.prefs.MusicPreferencesGateway
import dev.olog.service.floating.FloatingWindowService
import dev.olog.service.floating.R
import dev.olog.shared.android.coroutine.autoDisposeJob
import dev.olog.shared.android.extensions.asServicePendingIntent
import dev.olog.shared.android.extensions.colorControlNormal
import dev.olog.shared.android.extensions.systemService
import dev.olog.shared.android.utils.isOreo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val CHANNEL_ID = "0xfff"

class FloatingWindowNotification @Inject constructor(
    private val service: LifecycleService,
    private val musicPreferencesUseCase: MusicPreferencesGateway

) : DefaultLifecycleObserver {

    companion object {
        const val NOTIFICATION_ID = 0xABC
    }

    private val notificationManager = service.systemService<NotificationManager>()

    private val builder = NotificationCompat.Builder(
        service,
        CHANNEL_ID
    )
    private var job by autoDisposeJob()

    private var notificationTitle = ""

    init {
        service.lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        job = null
    }

    fun startObserving() {
        job = musicPreferencesUseCase.observeLastMetadata()
            .filter { it.isNotEmpty() }
            .onEach {
                notificationTitle = it.description
                val notification = builder.setContentTitle(notificationTitle).build()
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
            .flowOn(Dispatchers.Default)
            .launchIn(service.lifecycleScope)
    }

    fun buildNotification(): Notification {
        createChannel()

        return builder
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.vd_bird_singing)
            .setContentTitle(notificationTitle)
            .setContentText(service.getString(R.string.floating_window_notification_content_text))
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
        val name = service.getString(R.string.floating_window_notification_channel_title)
        val description =
            service.getString(R.string.floating_window_notification_channel_description)

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