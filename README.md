# Daybreak for Android

**Update channel paused.** Google Play Protect blocked the in-app installation test as potentially harmful. The APK release was converted to a draft and the update channel was disabled. Do not bypass Play Protect to install this draft build. Repository creation, source upload, download verification, Android confirmation and cancellation preservation were verified; completed installation was not. The reason for the Play Protect verdict has not been established.

Version 0.5.1 development build for Android 11 and later. The app bundles the black-and-white UI in a WebView, with Android alarm scheduling, audio playback and speech recognition implemented in Java. It does not require the localhost preview server.

## Install

Copy `Daybreak-debug.apk` to the phone or tablet and open it. If asked, allow installation from the file manager you are using. This is a debug-signed test build, not a Play Store release.

On first launch, review microphone and camera access, then allow **Alarms & reminders**, **Alarm notifications**, and **Full-screen alarms**. These are separate Android approvals. Without precise alarm access, saved alarms cannot be scheduled. Settings contains the same controls later.

## Use

- Set a time in Alarms, or say “Set alarm for seven thirty AM every day”. Clear commands save immediately, without a review popup. A short beep acknowledges recognition and speech confirms the saved time. If AM/PM is missing, answer the clarification or choose the time manually.
- On tablets, enter bedside mode for a large white clock on black. Android keeps the screen awake while Daybreak is visible. Tap the clock to reveal Return to app for six seconds, then tap it to leave fullscreen. Escape also exits fullscreen. Android Back leaves the activity.
- Allow Do Not Disturb access to activate an app-owned alarms-only rule during bedside mode. Leaving the activity or exiting bedside mode deactivates Daybreak's rule, without disabling other rules. Android system exceptions and manual overrides still apply.
- “Pin Daybreak on screen” in Settings invokes Android screen pinning, subject to the device's settings and confirmation. This is not managed-device kiosk mode.
- Ringing uses the device's alarm audio stream. It starts softly and grows over 20 seconds, up to the device's chosen alarm volume.
- When ringing, say “Start scan” or tap Start wake-up scan. The service temporarily pauses audio for at most 30 seconds. The camera checks that your face, shoulders and upper body are visible. It asks you to raise one randomly selected hand beside your face, lower it, then stay in view for 15 seconds. Hips, knees and feet are not required. Failure, interruption or timeout resumes ringing. A pass received within the service's deadline dismisses it.
- Settings → Test alarm exercises the real alarm and scan flow. It uses the same dismissal check as a scheduled alarm.

## In-app updates

Repository and update host: https://github.com/CHISOMCHRISTIANU3C/Daybreak

Install this updater-enabled APK once. For later versions, open Settings, tap **Check for updates**, then **Update now**. The app downloads the APK, checks the published SHA-256, package name, version and signing certificate, then opens Android's installation confirmation. If Android asks for permission to install updates from Daybreak, approve it in system settings, return to Daybreak and tap Install update again. A canceled installation leaves the installed app and saved alarms unchanged.

Installation is deferred while an alarm is ringing or due within five minutes. This is not a silent installer or a partial-APK patch system: it downloads a full APK inside the app, so you no longer have to find the file in chat or your Downloads folder. New versions must retain the signing key used for the installed app. The current builds use the existing local Android debug key; do not replace or publish that private key. A future production signing migration needs a separate plan.

The update manifest is `updates/update.json` on the repository's main branch. APKs are GitHub release assets. The app only accepts approved HTTPS hosts and APK paths belonging to this repository. A missing channel or network failure is reported as an error, never as “up to date”.

To publish a future version, increment versionCode and versionName, build using the same signing key, then run `Publish-Update.ps1 -Version <version>` while GitHub CLI is authenticated as the repository owner. The script uploads the release before changing the channel manifest.

## Weather and room comfort

In Settings, select **Enable location weather** and approve approximate location access. Outdoor weather uses the device's current location, not a city selector. It refreshes every 15 minutes while Daybreak is visible, and on return to the app. This feature does not request background location permission. Device location must be enabled. Denied permission and unavailable location are shown explicitly; cached weather is labeled and old observations are marked out of date.

Location coordinates are rounded to two decimal places before being sent to Open-Meteo. Outdoor values are weather-model estimates. Weather backgrounds represent the returned condition with monochrome cloud, rain, snow, or clear-sky effects. Animation can be disabled and honors the system reduced-motion setting.

Indoor temperature comes only from Android's ambient-temperature sensor when present, reporting a fresh reliable sample, and otherwise from a manual room-thermometer reading. An absent sensor is displayed as unavailable. No estimate is made from battery temperature, processor heat, camera data or outdoor weather. Sensor readings expire after five minutes without a fresh sample; manual readings expire after one hour. Fresh sensor readings take priority over manual ones. Sensor listeners stop when the activity is paused.

The default comfort target of 21°C is an adjustable preference, not a medical recommendation or an average of indoor and outdoor temperature. Advice compares a fresh indoor reading to your target. It does not control a thermostat. Bluetooth room-sensor pairing is not implemented; use a room thermometer and enter its reading if the device has no ambient sensor.

The free Open-Meteo endpoint is used for this personal development build. Review the provider's commercial terms before public or commercial distribution.

## Reliability and limits

Alarms are stored in app-private preferences and scheduled with `AlarmManager.setAlarmClock`. Repeat alarms are scheduled individually, and future schedules are restored after reboot, app update, time changes and exact-alarm access being granted. A foreground media playback service owns ringing and the scan deadline, independently of the WebView.

Android force-stop, uninstalling, revoking alarm permissions, silencing the alarm stream, powering off, or device-specific battery restrictions can prevent ringing. An already-ringing alarm interrupted by device reboot is not replayed. Do not rely on this development build as your only alarm yet.

Speech recognition runs while the app is visible. It does not provide an unrestricted background hotword service. The installed Android recognition service may require internet access. Recognition in loud alarm audio has not been validated on a physical device.

The pose model and WebAssembly runtime are bundled, so the face and upper-body check does not download a model at scan time. Camera frames are processed locally and are not recorded. This is an experimental posture heuristic, not identity, liveness or wakefulness verification. It can be completed while seated and does not establish that a person is standing. Physical-device testing in low light and an alternative for people unable to perform the hand movement are still needed.

## Validation

- Gradle `assembleDebug`, `lintDebug` and `testDebugUnitTest` pass. Lint reports nonblocking warnings, including synchronous persistence and WebView JavaScript usage.
- Ten update-manifest tests cover the repository allowlist, HTTPS, package identity, checksum format, size limits, version format and redirect hosts. The live update download, verification, Android confirmation and cancellation paths were checked on the emulator. Google Play Protect blocked installation after its scan, so the release is held as a draft and the channel is paused.
- Six scheduling tests cover one-time alarms, weekday repeats, exact-minute rollover and both daylight-saving transitions.
- Browser interaction checks verify tap-to-reveal fullscreen exit, immediate daily voice alarm saving, acknowledgment and confirmation events, duplicate suppression, spoken AM/PM clarification and rollback without false confirmation on save failure. Actual beep and speech audibility still need device testing.
- Nine synthetic-landmark checks cover upper-body framing, missing face/shoulders and visible hand movement. A browser integration test passes the arm challenge and 15-second hold with legs outside frame, releases the camera, and verifies a missing face leads to a 30-second timeout. These checks do not replace real-camera accuracy testing.
- Installed and launched on a headless Android API 36.1 tablet emulator at 1280 × 800.
- A real scheduled alarm triggered its native foreground service after the app process was terminated and the emulator screen was put to sleep.
- Emulator camera opened, the bundled pose model ran, and the 30-second failed scan resumed the ringing state.
- Do Not Disturb entered alarms-only mode and returned to normal after exiting bedside mode.
- Fourteen climate checks cover missing/unreliable/stale sensors, manual-source labels, source precedence, weather conditions and target advice. Browser flow tests cover location permission denial, a mocked weather update, manual room readings and reduced motion. A live provider request confirmed the response schema. The Android emulator also returned an approximate location, accepted a simulated ambient temperature, rejected its invalid placeholder reading, and displayed provider weather and the indoor sensor source. Physical-device weather reception and ambient-sensor availability remain device-dependent.
- Voice accuracy and audible feedback on physical devices, successful upper-body challenge with a real person, phone layout on Android hardware, and OEM battery behavior remain unverified.

## Build

Install JDK 17 and Android SDK platform 36. Set `JAVA_HOME` and `ANDROID_HOME`, or create a local `local.properties` with your SDK path. From this directory run:

```powershell
.\gradlew.bat assembleDebug lintDebug testDebugUnitTest
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Source assets are under `app/src/main/assets/ui`. Java code is under `app/src/main/java/com/daybreak/clock`. No API key is needed.


