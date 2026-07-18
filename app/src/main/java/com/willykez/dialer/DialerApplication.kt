package com.willykez.dialer

import android.app.Application
import com.willykez.dialer.data.BlockedNumberRepository
import com.willykez.dialer.data.CallLogRepository
import com.willykez.dialer.data.ContactsRepository
import com.willykez.dialer.data.PhoneAccountsRepository
import com.willykez.dialer.data.SettingsRepository
import com.willykez.dialer.telecom.CallManager
import com.willykez.dialer.telecom.CallNotifications

class DialerApplication : Application() {

    lateinit var contactsRepository: ContactsRepository
        private set
    lateinit var callLogRepository: CallLogRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var blockedNumberRepository: BlockedNumberRepository
        private set
    lateinit var phoneAccountsRepository: PhoneAccountsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        contactsRepository = ContactsRepository(this)
        callLogRepository = CallLogRepository(this)
        settingsRepository = SettingsRepository(this)
        blockedNumberRepository = BlockedNumberRepository(this)
        phoneAccountsRepository = PhoneAccountsRepository(this)
        CallNotifications.createChannels(this)
        CallManager.init(this)
    }
}
