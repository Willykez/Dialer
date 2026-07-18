# Dialer

A native Android phone/dialer app built with Jetpack Compose and the Android Telecom framework.

## Features

- Real contacts and call log, read directly from the system content providers (no mock data)
- Can be set as the device's default phone app (Android `RoleManager` / `TelecomManager`)
- Incoming calls trigger a full-screen UI over the lock screen plus a heads-up notification with Answer/Decline actions
- Outgoing calls are placed through `TelecomManager`, so they behave like any system call
- In-call screen with mute, speaker, hold, in-call keypad (DTMF) and end call
- Contact detail screen (not a dialog) with call, message, edit-in-system-contacts, favorite, custom ringtone and block/unblock
- Dual-SIM aware: on a two-SIM device it asks which SIM to call from (or uses a remembered default), tags recent calls and the active call screen with the SIM used, and lets you set a default outgoing SIM in Settings
- Dialpad with T9 letter matching against contacts, haptics, and system ringtone/DTMF tones
- Settings screen: default dialer, ringtone picker, vibrate toggle, blocked number management
- Material 3 (dynamic color on Android 12+) with a pill-shaped, swipeable bottom navigation bar

## Requirements

- Android Studio Ladybug (or newer)
- JDK 17
- A device or emulator running Android 8.0 (API 26) or newer, with a SIM/telephony stack to fully exercise real call flows

## Permissions

The app requests, at runtime: `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_CALL_LOG`, `WRITE_CALL_LOG`,
`CALL_PHONE`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `POST_NOTIFICATIONS`.

To receive and place real cellular calls through this app's UI, set it as the default phone app from
**Settings -> Contacts and calls**, which is also offered in-app from the Settings screen.

## Building

```
./gradlew assembleDebug
```

(No Gradle wrapper jar is committed; run `gradle wrapper` once with a local Gradle install, or let
the CI workflow's `gradle/actions/setup-gradle` handle it.)

## CI

`.github/workflows/android-ci.yml` runs lint, unit tests, and assembles a debug APK on every push and
pull request to `main`.

## Notes and limitations

- Built and reviewed at the source level; it has not been compiled on a physical Android
  Studio/Gradle toolchain in this environment, since no Android SDK is available here. Expect to run
  a first build in Android Studio and fix any small API-level issues that surface.
- Ringing sound for incoming cellular calls is played by the platform's Telecom stack, not by this app;
  the in-app ringtone picker sets the ringtone stored against a contact (or as an app preference), which
  the system's ringer already honors for standard telephony calls.
- Call recording is intentionally not implemented, since it is heavily carrier/region restricted.
