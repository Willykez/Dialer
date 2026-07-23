package com.willykez.dialer.telecom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import android.telecom.Call
import com.willykez.dialer.DialerApplication
import com.willykez.dialer.MainActivity
import com.willykez.dialer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Call notifications built on the real [NotificationCompat.CallStyle] template — the system's
 * native call layout (matching Google/Samsung's own dialer notifications, including the
 * lock-screen full-bleed treatment) — rather than a hand-rolled generic notification.
 *
 * On Android 16+ (API 36), ongoing and incoming calls also opt in to "Live Updates"
 * (Promoted Ongoing notifications), which earns the notification a persistent status-bar
 * chip and elevated placement at the top of the shade for the duration of the call.
 */
object CallNotifications {

    const val CHANNEL_INCOMING = "channel_incoming_calls"
    const val CHANNEL_ONGOING = "channel_ongoing_call"
    const val CHANNEL_MISSED = "channel_missed_calls"

    const val NOTIFICATION_ID_INCOMING = 1001
    const val NOTIFICATION_ID_ONGOING = 1002

    const val ACTION_ANSWER = "com.willykez.dialer.action.ANSWER"
    const val ACTION_DECLINE = "com.willykez.dialer.action.DECLINE"
    const val ACTION_HANGUP = "com.willykez.dialer.action.HANGUP"

    // Short-lived, fire-and-forget scope used only to resolve a contact photo in the
    // background and refresh an already-posted call notification once it's found — the
    // initial notification is always posted synchronously first so ringing is never delayed.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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

        fun buildAndPost(person: Person) {
            val builder = NotificationCompat.Builder(context, CHANNEL_INCOMING)
                .setSmallIcon(R.drawable.ic_notification_call)
                .setContentTitle(name)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setStyle(NotificationCompat.CallStyle.forIncomingCall(person, declineIntent, answerIntent))
                .setRequestPromotedOngoing(true)

            notify(context, NOTIFICATION_ID_INCOMING, builder.build())
        }

        // Post immediately with a generic icon so the ring/full-screen intent is never
        // delayed, then upgrade in place to the contact's real photo once it's resolved.
        buildAndPost(fallbackPerson(name))
        resolvePhotoAndRepost(context, number, name, ::buildAndPost)
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

        fun buildAndPost(person: Person) {
            val builder = NotificationCompat.Builder(context, CHANNEL_ONGOING)
                .setSmallIcon(R.drawable.ic_notification_call)
                .setContentTitle(name)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .setStyle(NotificationCompat.CallStyle.forOngoingCall(person, hangupIntent))
                .setRequestPromotedOngoing(true)

            notify(context, NOTIFICATION_ID_ONGOING, builder.build())
        }

        buildAndPost(fallbackPerson(name))
        resolvePhotoAndRepost(context, number, name, ::buildAndPost)
    }

    fun clearAll(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(NOTIFICATION_ID_INCOMING)
        manager.cancel(NOTIFICATION_ID_ONGOING)
    }

    private fun fallbackPerson(name: String): Person =
        Person.Builder()
            .setName(name)
            .setIcon(null)
            .setImportant(true)
            .build()

    /** Looks up the caller's saved contact photo in the background and re-posts with it. */
    private fun resolvePhotoAndRepost(
        context: Context,
        number: String,
        name: String,
        post: (Person) -> Unit
    ) {
        val app = context.applicationContext as? DialerApplication ?: return
        if (number.isBlank()) return

        scope.launch {
            val contact = runCatching { app.contactsRepository.findContactByNumber(number) }.getOrNull()
            val photoUri = contact?.photoUri ?: return@launch
            val icon = runCatching {
                context.contentResolver.openInputStream(android.net.Uri.parse(photoUri))?.use { stream ->
                    IconCompat.createWithBitmap(android.graphics.BitmapFactory.decodeStream(stream))
                }
            }.getOrNull() ?: return@launch

            val person = Person.Builder()
                .setName(contact.displayName.ifBlank { name })
                .setIcon(icon)
                .setImportant(true)
                .build()
            post(person)
        }
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
