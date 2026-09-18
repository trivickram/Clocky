package com.clocky.alarm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)

        if (prefs.isEnabled) {
            SleepMonitoringService.start(this)
        }

        setContent {
            MaterialTheme {
                AppleGradeClockyApp(prefs = prefs)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (prefs.isEnabled) {
            SleepMonitoringService.start(this)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// APPLE SF DESIGN PALETTE
// ─────────────────────────────────────────────────────────────────────────────
private val AppleBlack = Color(0xFF000000)
private val AppleSecondaryBg = Color(0xFF121318)
private val AppleElevatedBg = Color(0xFF1C1D24)
private val AppleBorder = Color(0x1AFFFFFF)
private val AppleCyan = Color(0xFF64D2FF)
private val AppleBlue = Color(0xFF0A84FF)
private val AppleIndigo = Color(0xFF5E5CE6)
private val ApplePurple = Color(0xFFBF5AF2)
private val AppleGreen = Color(0xFF30D158)
private val AppleOrange = Color(0xFFFF9F0A)
private val AppleCoral = Color(0xFFFF453A)
private val AppleLabel = Color(0xFFFFFFFF)
private val AppleSecondaryLabel = Color(0xFF8E8E93)
private val AppleTertiaryLabel = Color(0xFF636366)

@Composable
fun AppleGradeClockyApp(prefs: PreferencesManager) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var activeTab by remember { mutableIntStateOf(0) }

    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    // Silent background auto-check on launch (once per 12 hours)
    LaunchedEffect(Unit) {
        if (prefs.autoCheckUpdates) {
            val now = System.currentTimeMillis()
            if (now - prefs.lastUpdateCheckMillis > 12 * 60 * 60 * 1000L) {
                val currentCode = AppUpdateManager.getCurrentVersionCode(context)
                val res = AppUpdateManager.checkForUpdate(prefs.updateEndpointUrl, currentCode)
                if (res is UpdateState.Available) {
                    updateState = res
                    showUpdateDialog = true
                    prefs.lastUpdateCheckMillis = now
                }
            }
        }
    }

    if (showUpdateDialog) {
        AppleUpdateDialog(
            updateState = updateState,
            onDismiss = {
                showUpdateDialog = false
                updateState = UpdateState.Idle
            },
            onDownloadAndInstall = { info ->
                coroutineScope.launch {
                    updateState = UpdateState.Downloading(0f)
                    val downloadedApk = AppUpdateManager.downloadApk(context, info.apkUrl) { prog ->
                        updateState = UpdateState.Downloading(prog)
                    }
                    if (downloadedApk != null) {
                        updateState = UpdateState.ReadyToInstall(downloadedApk)
                        AppUpdateManager.installApk(context, downloadedApk)
                    } else {
                        updateState = UpdateState.Error("Failed to download APK. Check internet connection.")
                    }
                }
            },
            onInstallApk = { apkFile ->
                AppUpdateManager.installApk(context, apkFile)
            }
        )
    }

    Scaffold(
        containerColor = AppleBlack,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A0B0E).copy(alpha = 0.95f),
                tonalElevation = 0.dp,
                modifier = Modifier.border(BorderStroke(0.5.dp, AppleBorder))
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Bedtime, contentDescription = "Sleep") },
                    label = { Text("Sleep Schedule", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppleCyan,
                        selectedTextColor = AppleCyan,
                        indicatorColor = Color(0x1F64D2FF),
                        unselectedIconColor = AppleSecondaryLabel,
                        unselectedTextColor = AppleSecondaryLabel
                    )
                )

                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Stages") },
                    label = { Text("Sleep Stages", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppleGreen,
                        selectedTextColor = AppleGreen,
                        indicatorColor = Color(0x1F30D158),
                        unselectedIconColor = AppleSecondaryLabel,
                        unselectedTextColor = AppleSecondaryLabel
                    )
                )

                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Audio") },
                    label = { Text("Alarm & Sound", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppleIndigo,
                        selectedTextColor = AppleIndigo,
                        indicatorColor = Color(0x1F5E5CE6),
                        unselectedIconColor = AppleSecondaryLabel,
                        unselectedTextColor = AppleSecondaryLabel
                    )
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (activeTab) {
                0 -> AppleSleepScheduleScreen(prefs = prefs)
                1 -> AppleSleepStagesScreen(prefs = prefs)
                2 -> AppleAlarmSettingsScreen(
                    prefs = prefs,
                    onTriggerUpdateDialog = { res ->
                        updateState = res
                        showUpdateDialog = true
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 1: APPLE SLEEP SCHEDULE HUB
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppleSleepScheduleScreen(prefs: PreferencesManager) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(prefs.isEnabled) }
    var sleepHours by remember { mutableIntStateOf(prefs.sleepHours) }
    var sleepMinutes by remember { mutableIntStateOf(prefs.sleepMinutes) }
    var scheduledWakeTime by remember { mutableLongStateOf(prefs.scheduledWakeTimeMillis) }
    var showCustomModal by remember { mutableStateOf(false) }
    var activeCategory by remember { mutableIntStateOf(if (sleepHours == 0) 1 else 0) }

    var canExactAlarm by remember { mutableStateOf(AlarmScheduler.canScheduleExactAlarms(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            scheduledWakeTime = prefs.scheduledWakeTimeMillis
            canExactAlarm = AlarmScheduler.canScheduleExactAlarms(context)
        }
    }

    fun updateDuration(h: Int, m: Int) {
        val coercedH = h.coerceIn(0, 23)
        var coercedM = m.coerceIn(0, 59)
        if (coercedH == 0 && coercedM == 0) coercedM = 1
        sleepHours = coercedH
        sleepMinutes = coercedM
        prefs.sleepHours = coercedH
        prefs.sleepMinutes = coercedM
        activeCategory = if (coercedH == 0) 1 else 0
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Apple Health Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SLEEP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = AppleCyan
                )
                Text(
                    text = "Sleep Schedule",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleLabel,
                    letterSpacing = (-0.5).sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick WhatsApp Share Button
                Surface(
                    shape = CircleShape,
                    color = Color(0x1F25D366),
                    border = BorderStroke(1.dp, Color(0x4D25D366)),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { shareAppViaWhatsApp(context) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share via WhatsApp",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        !isEnabled -> Color(0x1AFFFFFF)
                        scheduledWakeTime > System.currentTimeMillis() -> Color(0x2DFF9F0A)
                        else -> Color(0x2230D158)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            !isEnabled -> Color(0x22FFFFFF)
                            scheduledWakeTime > System.currentTimeMillis() -> Color(0x80FF9F0A)
                            else -> Color(0x8030D158)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        !isEnabled -> AppleSecondaryLabel
                                        scheduledWakeTime > System.currentTimeMillis() -> AppleOrange
                                        else -> AppleGreen
                                    }
                                )
                                .alpha(if (isEnabled) pulseAlpha else 1f)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = when {
                                !isEnabled -> "OFF"
                                scheduledWakeTime > System.currentTimeMillis() -> "ARMED"
                                else -> "LISTENING"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = when {
                                !isEnabled -> AppleSecondaryLabel
                                scheduledWakeTime > System.currentTimeMillis() -> AppleOrange
                                else -> AppleGreen
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Apple Bedtime Ring with Moon & Sun icons
        AppleBedtimeDial(
            hours = sleepHours,
            minutes = sleepMinutes,
            onDurationChanged = { h, m -> updateDuration(h, m) },
            onCenterTap = { showCustomModal = true }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Armed Alarm Live Banner (Animated)
        AnimatedVisibility(
            visible = scheduledWakeTime > System.currentTimeMillis(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1308)),
                border = BorderStroke(1.dp, Color(0x66FF9F0A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Alarm, contentDescription = null, tint = AppleOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AUTOMATIC ALARM ARMED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = AppleOrange
                            )
                        }

                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0x33FF9F0A)) {
                            Text(
                                text = AlarmScheduler.formatRemaining(scheduledWakeTime),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFEF08A),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = AlarmScheduler.formatTime(scheduledWakeTime),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        color = AppleLabel
                    )
                    Text(
                        text = "Calculated from phone cessation. Screen on will cancel.",
                        fontSize = 13.sp,
                        color = Color(0xFFD97706)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            AlarmScheduler.cancelSleepAlarm(context)
                            scheduledWakeTime = 0L
                            Toast.makeText(context, "Alarm Disarmed", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.AlarmOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disarm Alarm", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Master Switch Group (Apple Inset Grouped Style)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
            border = BorderStroke(0.5.dp, AppleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isEnabled) Color(0x2264D2FF) else Color(0x11FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Nightlight, contentDescription = null, tint = if (isEnabled) AppleCyan else AppleSecondaryLabel, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text("Screen-Off Automation", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppleLabel)
                        Text(
                            text = if (isEnabled) "Active for upcoming bedtime" else "Paused",
                            fontSize = 13.sp,
                            color = if (isEnabled) AppleCyan else AppleSecondaryLabel
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        isEnabled = checked
                        prefs.isEnabled = checked
                        if (checked) {
                            SleepMonitoringService.start(context)
                            Toast.makeText(context, "Monitoring activated", Toast.LENGTH_SHORT).show()
                        } else {
                            SleepMonitoringService.stop(context)
                            AlarmScheduler.cancelSleepAlarm(context)
                            scheduledWakeTime = 0L
                            Toast.makeText(context, "Monitoring paused", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppleGreen,
                        uncheckedThumbColor = AppleSecondaryLabel,
                        uncheckedTrackColor = Color(0xFF2C2C2E),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Presets & Architecture Card (Apple Style)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
            border = BorderStroke(0.5.dp, AppleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Segmented control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF000000))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (activeCategory == 0) AppleElevatedBg else Color.Transparent)
                            .clickable {
                                activeCategory = 0
                                if (sleepHours == 0) updateDuration(7, 15)
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Full Sleep Cycles", fontSize = 13.sp, fontWeight = if (activeCategory == 0) FontWeight.Bold else FontWeight.Medium, color = if (activeCategory == 0) AppleLabel else AppleSecondaryLabel)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (activeCategory == 1) Color(0xFF2C1E0A) else Color.Transparent)
                            .clickable {
                                activeCategory = 1
                                if (sleepHours > 0) updateDuration(0, 2)
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = if (activeCategory == 1) AppleOrange else AppleSecondaryLabel, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fast Test (1-5m)", fontSize = 13.sp, fontWeight = if (activeCategory == 1) FontWeight.Bold else FontWeight.Medium, color = if (activeCategory == 1) AppleOrange else AppleSecondaryLabel)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeCategory == 1) {
                    Text("INSTANT VERIFICATION PILLS", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = AppleOrange)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(1, 2, 3, 5).forEach { min ->
                            val isSel = (sleepHours == 0 && sleepMinutes == min)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) AppleOrange else AppleElevatedBg,
                                border = BorderStroke(0.5.dp, if (isSel) Color(0xFFFDE68A) else AppleBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { updateDuration(0, min) }
                            ) {
                                Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$min", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isSel) Color(0xFF000000) else AppleLabel)
                                    Text("min", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF000000) else AppleSecondaryLabel)
                                }
                            }
                        }
                    }
                } else {
                    Text("HUMAN 90-MIN ULTRADIAN CYCLES", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = AppleSecondaryLabel)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(
                            Pair(6, 0),
                            Pair(7, 30),
                            Pair(8, 0),
                            Pair(9, 0)
                        ).forEach { (h, m) ->
                            val isSel = (sleepHours == h && sleepMinutes == m)
                            val cyc = (h * 60 + m) / 90f
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) AppleBlue else AppleElevatedBg,
                                border = BorderStroke(0.5.dp, if (isSel) AppleCyan else AppleBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { updateDuration(h, m) }
                            ) {
                                Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${h}h${if (m > 0) "${m}m" else ""}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isSel) Color.White else AppleLabel)
                                    Text(String.format("%.1f cyc", cyc), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFFBFDBFE) else AppleSecondaryLabel)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Fine Steppers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fine Adjustment", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppleSecondaryLabel)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AppleElevatedBg,
                            border = BorderStroke(0.5.dp, AppleBorder),
                            modifier = Modifier.clickable {
                                val total = (sleepHours * 60 + sleepMinutes) - 1
                                updateDuration((total / 60).coerceAtLeast(0), (total % 60).coerceAtLeast(if (sleepHours == 0) 1 else 0))
                            }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = AppleLabel, modifier = Modifier.size(13.dp))
                                Text(" 1m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppleLabel)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AppleElevatedBg,
                            border = BorderStroke(0.5.dp, AppleBorder),
                            modifier = Modifier.clickable {
                                val total = (sleepHours * 60 + sleepMinutes) + 1
                                updateDuration(total / 60, total % 60)
                            }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = AppleLabel, modifier = Modifier.size(13.dp))
                                Text(" 1m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppleLabel)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AppleElevatedBg,
                            border = BorderStroke(0.5.dp, AppleBorder),
                            modifier = Modifier.clickable {
                                val total = (sleepHours * 60 + sleepMinutes) + 15
                                updateDuration(total / 60, total % 60)
                            }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = AppleCyan, modifier = Modifier.size(13.dp))
                                Text(" 15m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppleCyan)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showCustomModal) {
        var inputHours by remember { mutableStateOf(sleepHours.toString()) }
        var inputMinutes by remember { mutableStateOf(sleepMinutes.toString()) }

        AlertDialog(
            onDismissRequest = { showCustomModal = false },
            title = { Text("Custom Sleep Duration", fontWeight = FontWeight.Bold, color = AppleLabel) },
            text = {
                Column {
                    Text("Type custom hours and minutes (e.g. 0h 2m for quick test):", fontSize = 13.sp, color = AppleSecondaryLabel)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = inputHours,
                            onValueChange = { inputHours = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("Hours") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppleCyan,
                                unfocusedBorderColor = AppleBorder,
                                focusedTextColor = AppleLabel,
                                unfocusedTextColor = AppleLabel
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = inputMinutes,
                            onValueChange = { inputMinutes = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppleCyan,
                                unfocusedBorderColor = AppleBorder,
                                focusedTextColor = AppleLabel,
                                unfocusedTextColor = AppleLabel
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = inputHours.toIntOrNull() ?: 0
                        val m = inputMinutes.toIntOrNull() ?: 0
                        updateDuration(h, m)
                        showCustomModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomModal = false }) { Text("Cancel", color = AppleSecondaryLabel) }
            },
            containerColor = AppleElevatedBg,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 2: APPLE HEALTH HYPNOGRAM & CIRCADIAN ENERGY CURVE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppleSleepStagesScreen(prefs: PreferencesManager) {
    val history = remember { prefs.getSleepHistory() }
    val totalMinutes = remember { prefs.sleepHours * 60 + prefs.sleepMinutes }
    val avgDuration = remember {
        if (history.isNotEmpty()) history.map { it.durationMinutes }.average().roundToInt() else 435
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Text("STAGES & RECOVERY", fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = AppleIndigo)
        Text("Sleep Architecture", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppleLabel, letterSpacing = (-0.5).sp)
        Text("Ultradian hypnogram stages and circadian energy dynamics.", fontSize = 13.sp, color = AppleSecondaryLabel)

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Apple Health Iconic 4-Tier Hypnogram Chart
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
            border = BorderStroke(0.5.dp, AppleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("HYPNOGRAM (SLEEP STAGES)", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = AppleSecondaryLabel)
                    Text("Total: ${avgDuration / 60}h ${avgDuration % 60}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppleCyan)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Canvas Hypnogram
                AppleHypnogramCanvas(durationMinutes = avgDuration)

                Spacer(modifier = Modifier.height(20.dp))

                // Stage Breakdown Inset Rows
                SleepStageLegendRow(name = "Awake", duration = "18m", percentage = "4%", color = AppleOrange)
                Spacer(modifier = Modifier.height(8.dp))
                SleepStageLegendRow(name = "REM (Dreaming / Memory)", duration = "1h 45m", percentage = "24%", color = AppleCyan)
                Spacer(modifier = Modifier.height(8.dp))
                SleepStageLegendRow(name = "Core / Light Sleep", duration = "3h 48m", percentage = "52%", color = AppleBlue)
                Spacer(modifier = Modifier.height(8.dp))
                SleepStageLegendRow(name = "Deep (Cellular Repair)", duration = "1h 24m", percentage = "20%", color = AppleIndigo)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Circadian Rhythm & Melatonin Curve (Rise Sleep style)
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
            border = BorderStroke(0.5.dp, AppleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CIRCADIAN RHYTHM & MELATONIN", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = AppleSecondaryLabel)
                    Text("Optimal Bedtime", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppleOrange)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Canvas Bezier Wave
                CircadianWaveCanvas()

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AppleOrange))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Melatonin Window: 10:15 PM – 11:30 PM. Screen cessation during this window triggers peak endogenous melatonin release.",
                        fontSize = 12.sp,
                        color = AppleSecondaryLabel,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Apple Sleep Debt & Recovery Index
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
                border = BorderStroke(0.5.dp, AppleBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("RECOVERY SCORE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AppleSecondaryLabel)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("88/100", fontSize = 24.sp, fontWeight = FontWeight.Black, color = AppleLabel)
                    Text("Restorative Sleep", fontSize = 12.sp, color = AppleGreen)
                }
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
                border = BorderStroke(0.5.dp, AppleBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("SLEEP DEBT", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AppleSecondaryLabel)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("+15 min", fontSize = 24.sp, fontWeight = FontWeight.Black, color = AppleLabel)
                    Text("On Target (0 Debt)", fontSize = 12.sp, color = AppleCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 3: APPLE ALARM SOUND PICKER & CONTROLS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppleAlarmSettingsScreen(
    prefs: PreferencesManager,
    onTriggerUpdateDialog: (UpdateState) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var gradualVolume by remember { mutableStateOf(prefs.gradualVolume) }
    var vibrateEnabled by remember { mutableStateOf(prefs.vibrateEnabled) }
    var snoozeMinutes by remember { mutableIntStateOf(prefs.snoozeMinutes) }
    var currentToneTitle by remember { mutableStateOf(prefs.alarmToneTitle) }

    var isPreviewPlaying by remember { mutableStateOf(false) }
    var ringtoneInstance by remember { mutableStateOf<Ringtone?>(null) }

    var autoCheckUpdates by remember { mutableStateOf(prefs.autoCheckUpdates) }
    var updateUrl by remember { mutableStateOf(prefs.updateEndpointUrl) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var updateCheckResultText by remember { mutableStateOf<String?>(null) }

    var useBedtimeWindow by remember { mutableStateOf(prefs.useBedtimeWindow) }
    var windowStartHour by remember { mutableIntStateOf(prefs.windowStartHour) }
    var windowEndHour by remember { mutableIntStateOf(prefs.windowEndHour) }
    var isInsideWindow by remember { mutableStateOf(prefs.isCurrentlyInBedtimeWindow()) }

    var canExactAlarm by remember { mutableStateOf(AlarmScheduler.canScheduleExactAlarms(context)) }
    var isIgnoringBattery by remember {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mutableStateOf(pm.isIgnoringBatteryOptimizations(context.packageName))
    }

    // System Ringtone Picker Launcher
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }

            if (uri != null) {
                val title = RingtoneManager.getRingtone(context, uri)?.getTitle(context) ?: "Selected Ringtone"
                prefs.alarmToneUri = uri.toString()
                prefs.alarmToneTitle = title
                currentToneTitle = title
                Toast.makeText(context, "Sound set: $title", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        while (true) {
            delay(1000)
            canExactAlarm = AlarmScheduler.canScheduleExactAlarms(context)
            isIgnoringBattery = pm.isIgnoringBatteryOptimizations(context.packageName)
            isInsideWindow = prefs.isCurrentlyInBedtimeWindow()
        }
    }

    fun toggleAudioPreview() {
        if (isPreviewPlaying) {
            ringtoneInstance?.stop()
            isPreviewPlaying = false
        } else {
            try {
                val uriStr = prefs.alarmToneUri
                val uri = if (!uriStr.isNullOrEmpty()) Uri.parse(uriStr) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val rt = RingtoneManager.getRingtone(context, uri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rt.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                rt.play()
                ringtoneInstance = rt
                isPreviewPlaying = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Text("EXPERIENCE & SOUNDS", fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = AppleIndigo)
        Text("Alarm & Audio", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppleLabel, letterSpacing = (-0.5).sp)
        Text("Pick device sounds, gentle volume fade, and snooze cadence.", fontSize = 13.sp, color = AppleSecondaryLabel)

        Spacer(modifier = Modifier.height(20.dp))

        // Sound Selection with System Ringtone Picker
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
            border = BorderStroke(0.5.dp, AppleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = AppleIndigo, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Active Sound Tone", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppleLabel)
                    }

                    OutlinedButton(
                        onClick = { toggleAudioPreview() },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isPreviewPlaying) AppleCoral else AppleIndigo)
                    ) {
                        Icon(
                            imageVector = if (isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isPreviewPlaying) AppleCoral else AppleIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPreviewPlaying) "Stop" else "Preview", fontSize = 12.sp, color = AppleLabel)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Tone Display with Apple Chevron
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppleElevatedBg,
                    border = BorderStroke(0.5.dp, AppleBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val currentUri = prefs.alarmToneUri?.let { Uri.parse(it) }
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Wake-Up Sound")
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                            }
                            ringtonePickerLauncher.launch(intent)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Current Sound", fontSize = 12.sp, color = AppleSecondaryLabel)
                            Text(currentToneTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppleLabel)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Change", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppleCyan)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppleSecondaryLabel, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Gentle Volume & Vibration (Apple Style Switch List)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
            border = BorderStroke(0.5.dp, AppleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Gradual Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gentle Wake-Up (Gradual Volume)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppleLabel)
                        Text("Fades in volume over 60 seconds (prevents cortisol spike)", fontSize = 12.sp, color = AppleSecondaryLabel)
                    }

                    Switch(
                        checked = gradualVolume,
                        onCheckedChange = {
                            gradualVolume = it
                            prefs.gradualVolume = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppleGreen,
                            uncheckedThumbColor = AppleSecondaryLabel,
                            uncheckedTrackColor = Color(0xFF2C2C2E),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vibration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Deep Haptic Vibration", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppleLabel)
                        Text("Rhythmic pulse pattern synchronized with alarm", fontSize = 12.sp, color = AppleSecondaryLabel)
                    }

                    Switch(
                        checked = vibrateEnabled,
                        onCheckedChange = {
                            vibrateEnabled = it
                            prefs.vibrateEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppleGreen,
                            uncheckedThumbColor = AppleSecondaryLabel,
                            uncheckedTrackColor = Color(0xFF2C2C2E),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Snooze Cadence
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
            border = BorderStroke(0.5.dp, AppleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("SNOOZE DURATION", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = AppleSecondaryLabel)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(5, 10, 15, 20).forEach { mins ->
                        val isSel = snoozeMinutes == mins
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) AppleIndigo else AppleElevatedBg,
                            border = BorderStroke(0.5.dp, if (isSel) AppleCyan else AppleBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    snoozeMinutes = mins
                                    prefs.snoozeMinutes = mins
                                }
                        ) {
                            Text(
                                text = "$mins min",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else AppleSecondaryLabel,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Daytime Battery Shield Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
            border = BorderStroke(0.5.dp, AppleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AppleGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Daytime Battery Shield", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppleLabel)
                        }
                        Text("Only arms during sleep window. Completely ignores daytime screen locks.", fontSize = 12.sp, color = AppleSecondaryLabel)
                    }

                    Switch(
                        checked = useBedtimeWindow,
                        onCheckedChange = {
                            useBedtimeWindow = it
                            prefs.useBedtimeWindow = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppleGreen,
                            uncheckedThumbColor = AppleSecondaryLabel,
                            uncheckedTrackColor = Color(0xFF2C2C2E),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                if (useBedtimeWindow) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isInsideWindow) Color(0x2230D158) else Color(0x2264D2FF),
                        border = BorderStroke(0.5.dp, if (isInsideWindow) Color(0x6630D158) else Color(0x6664D2FF))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isInsideWindow) AppleGreen else AppleCyan))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isInsideWindow) "🟢 In Night Window ($windowStartHour:00 - $windowEndHour:00): Active" else "🛡️ Daytime Shield Active: Screen locks ignored",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInsideWindow) Color(0xFFA7F3D0) else Color(0xFFBAE6FD)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(Triple("9PM-6AM", 21, 6), Triple("10PM-7AM", 22, 7), Triple("11PM-8AM", 23, 8)).forEach { (label, s, e) ->
                            val isSel = (windowStartHour == s && windowEndHour == e)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) AppleGreen else AppleElevatedBg,
                                border = BorderStroke(0.5.dp, if (isSel) Color(0xFFA7F3D0) else AppleBorder),
                                modifier = Modifier.weight(1f).clickable {
                                    windowStartHour = s
                                    windowEndHour = e
                                    prefs.windowStartHour = s
                                    prefs.windowEndHour = e
                                }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color(0xFF000000) else AppleSecondaryLabel,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Spread the Word / Direct APK Share Card (No Play Store Needed)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
            border = BorderStroke(0.5.dp, Color(0x3325D366)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2225D366)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Direct APK Share",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleLabel
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x2230D158),
                                border = BorderStroke(0.5.dp, Color(0x6630D158))
                            ) {
                                Text(
                                    text = "STANDALONE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AppleGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Sends the full Clocky.apk file directly. No Google Play Store required.",
                            fontSize = 12.sp,
                            color = AppleSecondaryLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // WhatsApp Primary Direct APK Share Button
                Button(
                    onClick = { shareAppViaWhatsApp(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Clocky.apk via WhatsApp",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Share / Universal Chooser
                OutlinedButton(
                    onClick = { shareAppGeneral(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.5.dp, AppleBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppleSecondaryLabel
                    )
                ) {
                    Text(
                        text = "Quick Share / Bluetooth / Others",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleSecondaryLabel
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Installation Instructions Box for Friends
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppleElevatedBg,
                    border = BorderStroke(0.5.dp, AppleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "📲 How your friend installs without Play Store:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1. Tap the Clocky.apk received in WhatsApp\n2. If prompted: tap 'Settings' → allow 'Install unknown apps'\n3. Tap 'Install' — ready to use instantly!",
                            fontSize = 11.sp,
                            color = AppleSecondaryLabel,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Software Updates Card (Standalone In-App Updater)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppleSecondaryBg),
            border = BorderStroke(0.5.dp, AppleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2264D2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = AppleCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Software Updates",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleLabel
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x2264D2FF),
                                border = BorderStroke(0.5.dp, Color(0x6664D2FF))
                            ) {
                                Text(
                                    text = "AUTO-UPDATER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AppleCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Installed: v${AppUpdateManager.getCurrentVersionName(context)} (Build ${AppUpdateManager.getCurrentVersionCode(context)})",
                            fontSize = 12.sp,
                            color = AppleSecondaryLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Check for Updates Action Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isCheckingUpdates = true
                            updateCheckResultText = null
                            val currentCode = AppUpdateManager.getCurrentVersionCode(context)
                            val res = AppUpdateManager.checkForUpdate(prefs.updateEndpointUrl, currentCode)
                            isCheckingUpdates = false
                            when (res) {
                                is UpdateState.Available -> {
                                    onTriggerUpdateDialog(res)
                                }
                                is UpdateState.UpToDate -> {
                                    updateCheckResultText = "✨ Clocky is up to date (v${AppUpdateManager.getCurrentVersionName(context)})"
                                    Toast.makeText(context, "Clocky is up to date!", Toast.LENGTH_SHORT).show()
                                }
                                is UpdateState.Error -> {
                                    updateCheckResultText = "❌ ${res.message}"
                                    Toast.makeText(context, "Check failed: ${res.message}", Toast.LENGTH_LONG).show()
                                }
                                else -> {}
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isCheckingUpdates,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppleIndigo,
                        contentColor = Color.White
                    )
                ) {
                    if (isCheckingUpdates) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Checking release server...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check for Updates Now", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (updateCheckResultText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = updateCheckResultText!!,
                        fontSize = 12.sp,
                        color = if (updateCheckResultText!!.startsWith("✨")) AppleGreen else AppleCoral,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auto-check Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Check on Launch", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppleLabel)
                        Text("Silently checks once a day on startup", fontSize = 12.sp, color = AppleSecondaryLabel)
                    }

                    Switch(
                        checked = autoCheckUpdates,
                        onCheckedChange = {
                            autoCheckUpdates = it
                            prefs.autoCheckUpdates = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppleGreen,
                            uncheckedThumbColor = AppleSecondaryLabel,
                            uncheckedTrackColor = Color(0xFF2C2C2E),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Update Server URL Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppleElevatedBg,
                    border = BorderStroke(0.5.dp, AppleBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showUrlDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Update Server Endpoint", fontSize = 11.sp, color = AppleSecondaryLabel)
                            Text(
                                text = updateUrl,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleCyan,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppleCyan)
                    }
                }
            }
        }

        if (showUrlDialog) {
            var inputUrl by remember { mutableStateOf(updateUrl) }
            AlertDialog(
                onDismissRequest = { showUrlDialog = false },
                title = { Text("Update Server Endpoint", fontWeight = FontWeight.Bold, color = AppleLabel) },
                text = {
                    Column {
                        Text(
                            text = "Enter raw JSON URL or GitHub latest release API (e.g. https://api.github.com/repos/your-user/Clocky/releases/latest):",
                            fontSize = 12.sp,
                            color = AppleSecondaryLabel
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            label = { Text("Endpoint URL") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppleCyan,
                                unfocusedBorderColor = AppleBorder,
                                focusedTextColor = AppleLabel,
                                unfocusedTextColor = AppleLabel
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = inputUrl.trim()
                            if (trimmed.isNotEmpty()) {
                                updateUrl = trimmed
                                prefs.updateEndpointUrl = trimmed
                            }
                            showUrlDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUrlDialog = false }) { Text("Cancel", color = AppleSecondaryLabel) }
                },
                containerColor = AppleElevatedBg,
                shape = RoundedCornerShape(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BESPOKE APPLE CANVAS VISUALIZERS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AppleBedtimeDial(
    hours: Int,
    minutes: Int,
    onDurationChanged: (Int, Int) -> Unit,
    onCenterTap: () -> Unit
) {
    val totalMinutes = (hours * 60 + minutes).coerceAtLeast(1)
    val targetSweep = (totalMinutes.toFloat() / 720f).coerceIn(0.02f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "dialSweep"
    )

    val wakePreview = remember(hours, minutes) {
        val targetMs = System.currentTimeMillis() + (totalMinutes * 60L * 1000L)
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(targetMs))
    }

    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val touch = change.position
                    val angleRad = atan2(touch.y - center.y, touch.x - center.x)
                    var deg = Math.toDegrees(angleRad.toDouble()).toFloat()
                    if (deg < 0) deg += 360f

                    var relAngle = deg - 135f
                    if (relAngle < 0) relAngle += 360f

                    if (relAngle <= 270f) {
                        val progress = (relAngle / 270f).coerceIn(0.02f, 1f)
                        val calculatedTotal = (progress * 720f).roundToInt()
                        onDurationChanged(calculatedTotal / 60, calculatedTotal % 60)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Apple Inactive Track
            drawArc(
                color = Color(0x1AFFFFFF),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )

            // 2. Apple Bedtime Glowing Gradient Arc
            val arcGradient = Brush.sweepGradient(
                colors = if (hours == 0) {
                    listOf(Color(0xFFFF9F0A), Color(0xFFFF375F), Color(0xFFFF9F0A))
                } else {
                    listOf(AppleCyan, AppleIndigo, AppleBlue)
                },
                center = center
            )

            drawArc(
                brush = arcGradient,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )

            // 3. Apple Start Moon Icon Anchor
            val startRad = 135f * (PI.toFloat() / 180f)
            val startX = center.x + radius * cos(startRad)
            val startY = center.y + radius * sin(startRad)
            drawCircle(color = Color(0xFF1C1D24), radius = strokeWidth * 0.65f, center = Offset(startX, startY))
            drawCircle(color = AppleCyan, radius = strokeWidth * 0.35f, center = Offset(startX, startY))

            // 4. Apple End Sun Knob
            val angleRad = (135f + 270f * animatedProgress) * (PI.toFloat() / 180f)
            val knobX = center.x + radius * cos(angleRad)
            val knobY = center.y + radius * sin(angleRad)

            // Outer Aura
            drawCircle(
                color = if (hours == 0) AppleOrange.copy(alpha = 0.4f) else AppleCyan.copy(alpha = 0.4f),
                radius = strokeWidth * 0.95f,
                center = Offset(knobX, knobY)
            )
            // Inner Core Knob
            drawCircle(color = Color.White, radius = strokeWidth * 0.45f, center = Offset(knobX, knobY))
        }

        // Center Digital Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onCenterTap)
                .padding(24.dp)
        ) {
            Text(
                text = if (hours == 0) "⚡ TEST RUN" else "SLEEP TARGET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = if (hours == 0) AppleOrange else AppleCyan
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                if (hours > 0) {
                    Text(
                        text = "$hours",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = AppleLabel
                    )
                    Text(
                        text = "h ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleSecondaryLabel,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = "$minutes",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    color = AppleLabel
                )
                Text(
                    text = "m",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleSecondaryLabel,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x1FFFFFFF),
                border = BorderStroke(0.5.dp, Color(0x2EFFFFFF))
            ) {
                Text(
                    text = "🌅 Wake ~$wakePreview",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AppleHypnogramCanvas(durationMinutes: Int) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val w = size.width
        val h = size.height

        // 4 Levels: 0 = Awake, 1 = REM, 2 = Core/Light, 3 = Deep
        val yLevels = listOf(
            0.12f * h, // Awake
            0.38f * h, // REM
            0.65f * h, // Core
            0.92f * h  // Deep
        )

        // Draw horizontal grid lines
        yLevels.forEach { y ->
            drawLine(
                color = Color(0x14FFFFFF),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )
        }

        // Stepped Hypnogram Curve
        val steps = listOf(
            Pair(0.00f, 0), // Awake start
            Pair(0.06f, 2), // Sleep onset -> Core
            Pair(0.18f, 3), // Deep 1
            Pair(0.32f, 1), // REM 1
            Pair(0.44f, 2), // Core
            Pair(0.58f, 3), // Deep 2
            Pair(0.72f, 1), // REM 2
            Pair(0.85f, 2), // Core
            Pair(0.95f, 1), // REM 3
            Pair(1.00f, 0)  // Awake end
        )

        val path = Path()
        val fillPath = Path()

        var currentX = 0f
        var currentY = yLevels[steps[0].second]

        path.moveTo(currentX, currentY)
        fillPath.moveTo(0f, h)
        fillPath.lineTo(currentX, currentY)

        for (i in 1 until steps.size) {
            val nextX = steps[i].first * w
            val nextY = yLevels[steps[i].second]

            // Step horizontally, then vertically
            path.lineTo(nextX, currentY)
            path.lineTo(nextX, nextY)

            fillPath.lineTo(nextX, currentY)
            fillPath.lineTo(nextX, nextY)

            currentX = nextX
            currentY = nextY
        }

        fillPath.lineTo(w, h)
        fillPath.close()

        // Draw Gradient Fill under hypnogram
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(AppleCyan.copy(alpha = 0.25f), AppleIndigo.copy(alpha = 0.05f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        // Draw Crisp Stepped Line
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(AppleOrange, AppleCyan, AppleIndigo, AppleCyan, AppleOrange)),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun CircadianWaveCanvas() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        val w = size.width
        val h = size.height

        val path = Path()
        path.moveTo(0f, 0.7f * h) // Morning rise

        // Peak energy at 10 AM
        path.cubicTo(
            0.2f * w, 0.15f * h,
            0.35f * w, 0.15f * h,
            0.5f * w, 0.55f * h  // Afternoon dip at 2 PM
        )

        // Second wind at 6 PM -> Melatonin dip at 10 PM
        path.cubicTo(
            0.65f * w, 0.35f * h,
            0.82f * w, 0.40f * h,
            w, 0.90f * h          // Deep night melatonin
        )

        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(AppleCyan, AppleOrange, AppleIndigo)),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Glowing dot at Melatonin Window (night)
        drawCircle(color = AppleOrange.copy(alpha = 0.3f), radius = 10.dp.toPx(), center = Offset(0.88f * w, 0.65f * h))
        drawCircle(color = AppleOrange, radius = 5.dp.toPx(), center = Offset(0.88f * w, 0.65f * h))
    }
}

@Composable
fun SleepStageLegendRow(name: String, duration: String, percentage: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(10.dp))
            Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppleLabel)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(duration, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppleLabel)
            Spacer(modifier = Modifier.width(8.dp))
            Text(percentage, fontSize = 12.sp, color = AppleSecondaryLabel)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// APPLE SOFTWARE UPDATE DIALOG
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AppleUpdateDialog(
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onDownloadAndInstall: (UpdateInfo) -> Unit,
    onInstallApk: (File) -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (updateState !is UpdateState.Downloading) {
                onDismiss()
            }
        },
        containerColor = AppleElevatedBg,
        shape = RoundedCornerShape(26.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x2264D2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = AppleCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Software Update",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleLabel
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x2230D158),
                    border = BorderStroke(0.5.dp, Color(0x6630D158))
                ) {
                    Text(
                        text = "STANDALONE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = AppleGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                when (updateState) {
                    is UpdateState.Available -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Clocky v${updateState.info.versionName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x1FFFFFFF)
                            ) {
                                Text(
                                    text = "Build ${updateState.info.versionCode}",
                                    fontSize = 10.sp,
                                    color = AppleSecondaryLabel,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "WHAT'S NEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = AppleSecondaryLabel
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF14151A),
                            border = BorderStroke(0.5.dp, AppleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = updateState.info.changelog,
                                fontSize = 12.sp,
                                color = AppleLabel,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "✨ All your alarms, sleep history, and settings are preserved during update.",
                            fontSize = 11.sp,
                            color = AppleSecondaryLabel
                        )
                    }

                    is UpdateState.Downloading -> {
                        Text(
                            text = "Downloading standalone update...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleLabel
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { updateState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AppleCyan,
                            trackColor = Color(0xFF2C2C2E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(updateState.progress * 100).toInt()}% completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleCyan
                        )
                    }

                    is UpdateState.ReadyToInstall -> {
                        Text(
                            text = "Download complete!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap 'Install Now' to complete the update. If Android asks, allow permission to install from this app.",
                            fontSize = 12.sp,
                            color = AppleSecondaryLabel
                        )
                    }

                    is UpdateState.Error -> {
                        Text(
                            text = "Update Check Failed",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleCoral
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = updateState.message,
                            fontSize = 12.sp,
                            color = AppleSecondaryLabel
                        )
                    }

                    else -> {}
                }
            }
        },
        confirmButton = {
            when (updateState) {
                is UpdateState.Available -> {
                    Button(
                        onClick = { onDownloadAndInstall(updateState.info) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Download & Install", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
                is UpdateState.ReadyToInstall -> {
                    Button(
                        onClick = { onInstallApk(updateState.apkFile) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Install Now", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
                is UpdateState.Error -> {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleElevatedBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", color = AppleLabel)
                    }
                }
                else -> {}
            }
        },
        dismissButton = {
            if (updateState is UpdateState.Available) {
                TextButton(onClick = onDismiss) {
                    Text("Later", color = AppleSecondaryLabel)
                }
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// SHARE HELPERS (DIRECT APK VIA WHATSAPP & SYSTEM SHARE SHEET - NO PLAY STORE)
// ─────────────────────────────────────────────────────────────────────────────

fun shareAppViaWhatsApp(context: Context) {
    shareClockyApk(context, targetPackage = "com.whatsapp")
}

fun shareAppGeneral(context: Context) {
    shareClockyApk(context, targetPackage = null)
}

fun shareClockyApk(context: Context, targetPackage: String? = null) {
    try {
        val srcApk = File(context.applicationInfo.sourceDir)
        if (!srcApk.exists()) {
            Toast.makeText(context, "Cannot locate installed APK on device", Toast.LENGTH_SHORT).show()
            return
        }

        // Copy to app cache dir with human-readable name Clocky.apk
        val cacheApk = File(context.cacheDir, "Clocky.apk")
        srcApk.copyTo(cacheApk, overwrite = true)

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheApk
        )

        val installGuide = "🌙 Here is Clocky — the intelligent screen-off sleep alarm!\n\n" +
                "📲 How to install (No Play Store needed):\n" +
                "1. Tap Clocky.apk above\n" +
                "2. If prompted: tap Settings → turn ON 'Allow from this source'\n" +
                "3. Tap Install & wake up refreshed!"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, apkUri)
            putExtra(Intent.EXTRA_TEXT, installGuide)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (targetPackage != null) {
                setPackage(targetPackage)
            }
        }

        if (targetPackage != null) {
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                if (targetPackage == "com.whatsapp") {
                    try {
                        intent.setPackage("com.whatsapp.w4b")
                        context.startActivity(intent)
                    } catch (e2: Exception) {
                        intent.setPackage(null)
                        val chooser = Intent.createChooser(intent, "Send Clocky.apk via...")
                        context.startActivity(chooser)
                    }
                } else {
                    intent.setPackage(null)
                    val chooser = Intent.createChooser(intent, "Send Clocky.apk via...")
                    context.startActivity(chooser)
                }
            }
        } else {
            val chooser = Intent.createChooser(intent, "Send Clocky.apk via...")
            context.startActivity(chooser)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error preparing APK: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

