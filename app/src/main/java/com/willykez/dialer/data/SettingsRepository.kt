package com.willykez.dialer.data

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("dialer_settings", Context.MODE_PRIVATE)

    private val defaultRingtoneUri: Uri? =
        RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)

    private val _vibrateOnRing = MutableStateFlow(prefs.getBoolean(KEY_VIBRATE, true))
    val vibrateOnRing: StateFlow<Boolean> = _vibrateOnRing.asStateFlow()

    private val _ringtoneUri = MutableStateFlow(
        prefs.getString(KEY_RINGTONE, defaultRingtoneUri?.toString())?.let { Uri.parse(it) }
    )
    val ringtoneUri: StateFlow<Uri?> = _ringtoneUri.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    private val _preferredSimKey = MutableStateFlow(prefs.getString(KEY_PREFERRED_SIM, null))
    val preferredSimKey: StateFlow<String?> = _preferredSimKey.asStateFlow()

    fun setVibrateOnRing(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATE, enabled).apply()
        _vibrateOnRing.value = enabled
    }

    fun setRingtoneUri(uri: Uri?) {
        prefs.edit().putString(KEY_RINGTONE, uri?.toString()).apply()
        _ringtoneUri.value = uri
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _dynamicColorEnabled.value = enabled
    }

    fun setPreferredSimKey(key: String?) {
        prefs.edit().putString(KEY_PREFERRED_SIM, key).apply()
        _preferredSimKey.value = key
    }

    companion object {
        private const val KEY_VIBRATE = "vibrate_on_ring"
        private const val KEY_RINGTONE = "ringtone_uri"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_PREFERRED_SIM = "preferred_sim_key"
        const val SIM_MODE_ASK = "ASK_EACH_TIME"
    }
}
