package com.clocky.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlarmScheduler {
    private const val ALARM_REQUEST_CODE = 1001

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleSleepAlarm(context: Context): Long {
        val prefs = PreferencesManager(context)
        var totalMinutes = (prefs.sleepHours * 60L) + prefs.sleepMinutes
        if (totalMinutes <= 0) totalMinutes = 1
        val durationMillis = totalMinutes * 60L * 1000L
        val now = System.currentTimeMillis()
        val wakeTime = now + durationMillis

        prefs.screenOffTimeMillis = now
        prefs.scheduledWakeTimeMillis = wakeTime

        setAlarm(context, wakeTime)
        return wakeTime
    }

    fun snoozeAlarm(context: Context): Long {
        val prefs = PreferencesManager(context)
        val snoozeMillis = prefs.snoozeMinutes * 60L * 1000L
        val wakeTime = System.currentTimeMillis() + snoozeMillis

        prefs.scheduledWakeTimeMillis = wakeTime
        setAlarm(context, wakeTime)
        return wakeTime
    }

    fun cancelSleepAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        val prefs = PreferencesManager(context)
        prefs.clearSession()
    }

    fun restoreAlarmIfValid(context: Context) {
        val prefs = PreferencesManager(context)
        val wakeTime = prefs.scheduledWakeTimeMillis
        val now = System.currentTimeMillis()

        if (wakeTime > now) {
            setAlarm(context, wakeTime)
        } else {
            prefs.clearSession()
        }
    }

    private fun setAlarm(context: Context, wakeTimeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
        }
        val alarmPendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            0,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(wakeTimeMillis, showPendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, alarmPendingIntent)
    }

    fun formatTime(timestampMillis: Long): String {
        if (timestampMillis <= 0) return "--:--"
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestampMillis))
    }

    fun formatRemaining(wakeTimeMillis: Long): String {
        val diff = wakeTimeMillis - System.currentTimeMillis()
        if (diff <= 0) return "Due now"
        val totalSeconds = diff / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }
}
