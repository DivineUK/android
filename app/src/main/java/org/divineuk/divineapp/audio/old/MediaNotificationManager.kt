package org.divineuk.divineapp.audio.old

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.divineuk.divineapp.activities.MainActivity
import org.divineuk.divineapp.R

private const val PRIMARY_CHANNEL = "PRIMARY_CHANNEL_ID"
private const val PRIMARY_CHANNEL_NAME = "PRIMARY"


class MediaNotificationManager(private val service: RadioService) {

    private val strAppName: String
    private var strLiveBroadcast: String
    private val resources: Resources = service.resources
    private val notificationManager: NotificationManagerCompat

    init {
        strAppName = resources.getString(R.string.app_name) //todo
        strLiveBroadcast = resources.getString(R.string.app_name) //todo
        notificationManager = NotificationManagerCompat.from(service)
    }

    fun setStrLiveBroadcast(strLiveBroadcast: String) {
        this.strLiveBroadcast = strLiveBroadcast
    }

    fun startNotify(playbackStatus: String) {

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.divine_logo)
        var icon = R.drawable.ic_pause_white
        val playbackAction = Intent(service, RadioService::class.java)
        playbackAction.action = RadioService.ACTION_PAUSE
        var action = PendingIntent.getService(service, 1, playbackAction, 0)

        if (playbackStatus == PlaybackStatus.PAUSED) {
            icon = R.drawable.ic_play_white
            playbackAction.action = RadioService.ACTION_PLAY
            action = PendingIntent.getService(service, 2, playbackAction, 0)
        }

        val stopIntent = Intent(service, RadioService::class.java)
        stopIntent.action = RadioService.ACTION_STOP

        val stopAction = PendingIntent.getService(service, 3, stopIntent, 0)
        val intent = Intent(service, MainActivity::class.java) //TODO Change(******
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val pendingIntent = PendingIntent.getActivity(service, 0, intent, 0)
        notificationManager.cancel(NOTIFICATION_ID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                PRIMARY_CHANNEL,
                PRIMARY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            manager.createNotificationChannel(channel)
        }

        val builder =
            NotificationCompat.Builder(service, PRIMARY_CHANNEL)
                .setAutoCancel(false)
                .setContentTitle(strLiveBroadcast)
                .setContentText(strAppName)
                .setLargeIcon(largeIcon)
                .setColorized(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .addAction(icon, "pause", action)
                .addAction(R.drawable.ic_stop_white, "stop", stopAction)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(service.mediaSession?.sessionToken)
                        .setShowActionsInCompactView(0, 1)
                        .setShowCancelButton(true)
                )

        service.startForeground(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotify() {
        service.stopForeground(true)
    }

    companion object {
        const val NOTIFICATION_ID = 555
    }


}



