package com.willykez.dialer.telecom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.telecom.Call
import com.willykez.dialer.MainActivity
import com.willykez.dialer.R

object CallNotifications {

    const val CHANNEL_INCOMING = "channel_incoming_calls"
    const val CHANNEL_ONGOING = "channel_ongoing_call"
    const val CHANNEL_MISSED = "channel_missed_calls"

    const val NOTIFICATION_ID_INCOMING = 1001
    const val NOTIFICATION_ID_ONGOING = 1002

    const val ACTION_ANSWER = "com.willykez.dialer.action.ANSWER"
    const val ACTION_DECLINE = "com.willykez.dialer.action.DECLINE"
    const val ACTION_HANGUP = "com.willykez.dialer.action.HANGUP"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val incoming = NotificationChannel(
            CHANNEL_INCOMING,
            context.getString(R.string.call_notification_channel_incoming),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
            enableVibration(false)
        }

        val ongoing = NotificationChannel(
            CHANNEL_ONGOING,
            context.getString(R.string.call_notification_channel_ongoing),
            NotificationManager.IMPORTANCE_LOW
        )

        val missed = NotificationChannel(
            CHANNEL_MISSED,
            context.getString(R.string.call_notification_channel_missed),
            NotificationManager.IMPORTANCE_DEFAULT
        )

        manager.createNotificationChannels(listOf(incoming, ongoing, missed))
    }

    fun showIncomingCall(context: Context, call: Call) {
        val number = call.details?.handle?.schemeSpecificPart.orEmpty()
        val name = call.details?.callerDisplayName?.takeIf { it.isNotBlank() } ?: number

        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val answerIntent = actionPendingIntent(context, ACTION_ANSWER, 1)
        val declineIntent = actionPendingIntent(context, ACTION_DECLINE, 2)

        val builder = NotificationCompat.Builder(context, CHANNEL_INCOMING)
            .setSmallIcon(R.drawable.ic_notification_call)
            .setContentTitle(name)
            .setContentText(context.getString(R.string.tab_recents))
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(0, context.getString(R.string.incoming_call_decline), declineIntent)
            .addAction(0, context.getString(R.string.incoming_call_answer), answerIntent)

        notify(context, NOTIFICATION_ID_INCOMING, builder.build())
    }

    fun showOngoingCall(context: Context, call: Call) {
        val number = call.details?.handle?.schemeSpecificPart.orEmpty()
        val name = call.details?.callerDisplayName?.takeIf { it.isNotBlank() } ?: number

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val hangupIntent = actionPendingIntent(context, ACTION_HANGUP, 3)

        val builder = NotificationCompat.Builder(context, CHANNEL_ONGOING)
            .setSmallIcon(R.drawable.ic_notification_call)
            .setContentTitle(name)
            .setContentText(context.getString(R.string.settings_default_dialer))
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(0, "Hang up", hangupIntent)

        notify(context, NOTIFICATION_ID_ONGOING, builder.build())
    }

    fun clearAll(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(NOTIFICATION_ID_INCOMING)
        manager.cancel(NOTIFICATION_ID_ONGOING)
    }

    private fun actionPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, CallActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notify(context: Context, id: Int, notification: android.app.Notification) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33
        ) {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }
}
