package com.willykez.dialer.telecom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            CallNotifications.ACTION_ANSWER -> CallManager.answer()
            CallNotifications.ACTION_DECLINE -> CallManager.reject()
            CallNotifications.ACTION_HANGUP -> CallManager.hangUp()
        }
        CallNotifications.clearAll(context)
    }
}
