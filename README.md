# Clocky — Screen-Off Sleep Alarm

A minimal, offline-first personal Android utility built with **KISS principles** (Keep It Simple, Stupid).

---

## 💡 How It Works

1. **Enable the switch** in the app and set your desired sleep duration (e.g. `7h 15m`).
2. **Put your phone down** and turn off the screen when you go to bed.
3. The app detects the **Screen-Off** event:
   - Calculates `wakeTime = screenOffTime + sleepDuration`
   - Schedules an exact, Doze-proof alarm via `AlarmManager.setAlarmClock()`
4. If you turn your **Screen ON** before the alarm (e.g. checking a notification):
   - The pending alarm is cancelled immediately.
   - When you turn the screen off again, a fresh sleep session starts.
5. When the alarm triggers:
   - Screen turns on automatically over the lock screen.
   - Standard alarm audio and vibration ring.
   - Tap **STOP** to dismiss, or **SNOOZE** for an extra 10 minutes.

---

## 🛠 Tech Stack & Architecture

- **UI:** Jetpack Compose + Material 3 (Single Activity)
- **Background Event Detection:** `SleepMonitoringService` (Lightweight Foreground Service with dynamic `ACTION_SCREEN_OFF` / `ACTION_SCREEN_ON` receivers)
- **Alarm Engine:** Android `AlarmManager.setAlarmClock()` (highest reliability, bypasses Doze, shows lock-screen alarm badge)
- **Storage:** `SharedPreferences` via `PreferencesManager` (zero database overhead)
- **Boot Recovery:** `BootReceiver` (restores alarm on phone reboot)
- **No Cloud, No Accounts, No Trackers:** 100% private and offline.

---

## 🚀 How to Run / Build

1. Open this folder in **Android Studio** (Hedgehog, Iguana, Jellyfish, Koala or Ladybug).
2. Let Gradle sync dependencies.
3. Connect an Android phone (or launch an emulator).
4. Click **Run** ▶️.

### Required Setup on Device:
- **Notification Permission:** Required on Android 13+ to display ongoing status & heads-up alarm.
- **Exact Alarms:** Enabled by default (or toggleable in app settings on Android 12+).
- **Battery Optimization:** Tap "Exempt" on the setup card so your phone OEM (Samsung, Xiaomi, OnePlus, Pixel) doesn't kill the background screen monitor.
