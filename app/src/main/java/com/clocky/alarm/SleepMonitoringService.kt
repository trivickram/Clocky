package com.clocky.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class SleepMonitoringService : Service() {

    private lateinit var prefs: PreferencesManager
    private var isReceiverRegistered = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    if (prefs.isEnabled) {
                        if (!prefs.isCurrentlyInBedtimeWindow()) {
                            return
                        }
                        val wakeTime = AlarmScheduler.scheduleSleepAlarm(context)
                        val formattedTime = AlarmScheduler.formatTime(wakeTime)
                        val durationText = if (prefs.sleepHours > 0) "${prefs.sleepHours}h ${prefs.sleepMinutes}m" else "${prefs.sleepMinutes}m"
                        updateNotification(
                            "Alarm active: Wake-up at $formattedTime",
                            "Calculated from your $durationText duration."
                        )
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    if (prefs.isEnabled && prefs.scheduledWakeTimeMillis > System.currentTimeMillis()) {
                        AlarmScheduler.cancelSleepAlarm(context)
                        updateNotification(
                            "Sleep Alarm Active",
                            "Screen turned on. Alarm reset. Put phone down to start."
                        )
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager(this)
        createNotificationChannel()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)
        isReceiverRegistered = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification(
            "Sleep Alarm Active",
            "Turn your screen off when ready for sleep."
        )
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverRegistered) {
            unregisterReceiver(screenReceiver)
            isReceiverRegistered = false
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Ensure service auto-respawns immediately even if user swipes app from Recent Tasks
        if (prefs.isEnabled) {
            val restartServiceIntent = Intent(applicationContext, SleepMonitoringService::class.java)
            val restartPendingIntent = PendingIntent.getService(
                applicationContext,
                101,
                restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            alarmManager?.set(
                android.app.AlarmManager.ELAPSED_REALTIME,
                android.os.SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Monitor Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors screen state to automatically set sleep alarms."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(title, content))
    }

    companion object {
        const val CHANNEL_ID = "clocky_sleep_monitor_channel"
        const val NOTIFICATION_ID = 101

        fun start(context: Context) {
            val intent = Intent(context, SleepMonitoringService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SleepMonitoringService::class.java)
            context.stopService(intent)
        }
    }
}
