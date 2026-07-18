package com.willykez.dialer.telecom

import android.telecom.Call
import android.telecom.InCallService

class DialerInCallService : InCallService() {

    override fun onCreate() {
        super.onCreate()
        CallManager.attachService(this)
    }

    override fun onDestroy() {
        CallManager.detachService(this)
        super.onDestroy()
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.onCallAdded(call)
        if (call.details?.state == Call.STATE_RINGING) {
            CallNotifications.showIncomingCall(applicationContext, call)
        } else {
            CallNotifications.showOngoingCall(applicationContext, call)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallNotifications.clearAll(applicationContext)
        CallManager.onCallRemoved(call)
    }
}
