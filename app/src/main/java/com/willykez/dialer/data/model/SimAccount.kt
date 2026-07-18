package com.willykez.dialer.data.model

import android.telecom.PhoneAccountHandle

data class SimAccount(
    val handle: PhoneAccountHandle,
    val label: String
) {
    val storageKey: String
        get() = "${handle.component.flattenToString()}|${handle.id}"
}
