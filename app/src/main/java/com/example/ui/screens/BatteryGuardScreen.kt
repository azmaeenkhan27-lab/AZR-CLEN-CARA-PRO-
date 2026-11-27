package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryOptimizationMode
import com.example.model.CleanerModule
import com.example.ui.AzroiaUiState
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.ModuleTopBar
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.RoyalSapphire
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.formatBytes
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun BatteryGuardScreen(
    uiState: AzroiaUiState,
    onSelectBatteryMode: (BatteryOptimizationMode) -> Unit,
    onToggleBatteryOptimization: (Boolean) -> Unit,
    onToggleAutoCleanCacheForBattery: (Boolean) -> Unit,
    onRunBatteryAutoCacheSweep: () -> Unit,
    onTriggerAutoAppCacheClean: () -> Unit,
    onOptimizeBattery: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val bat = uiState.batteryTelemetry
    val cellAccent = if (bat.thermalWarning) SolarAmber else EmeraldPulse

    Box(modifier = modifier.fillMaxSize()) {
        CyberBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModuleTopBar(
                title = CleanerModule.BATTERY.title,
                tag = CleanerModule.BATTERY.tag,
                accent = cellAccent,
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Hero Battery Plasma Cell & Master Optimization Card
                item {
                    CyberGlassCard(
                        accent = cellAccent,
                        secondaryAccent = NeonCyan
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                LiquidPlasmaBatteryCell(
                                    levelPercent = bat.levelPercent,
                                    isCharging = bat.isCharging,
                                    accent = cellAccent,
                                    modifier = Modifier.size(width = 120.dp, height = 172.dp)
                                )

                                Column(horizontalAlignment = Alignment.Start) {
                                    Surface(
                                        color = if (uiState.isBatteryOptimizationEnabled) {
                                            EmeraldPulse.copy(alpha = 0.16f)
                                        } else {
                                            SolarAmber.copy(alpha = 0.16f)
                                        },
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(
                                            1.dp,
                                            if (uiState.isBatteryOptimizationEnabled) EmeraldPulse else SolarAmber
                                        )
                                    ) {
                                        Text(
                                            text = if (uiState.isBatteryOptimizationEnabled) "OPT ACTIVE" else "OPT PAUSED",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (uiState.isBatteryOptimizationEnabled) EmeraldPulse else SolarAmber,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "HEALTH SCORE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${bat.healthScore}/100",
                                        style = MaterialTheme.typography.displayMedium,
                                        color = cellAccent
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (bat.isCharging) "CHARGING ACTIVE" else "BATTERY POWER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan
                                    )
                                    Text(
                                        text = "${bat.drainingAppsCount} background task(s)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            VoltCoreWaveformStrip(
                                temperatureCelsius = bat.temperatureCelsius,
                                voltageVolts = bat.voltageVolts,
                                accent = cellAccent
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Master Battery Optimization Switch
                            Surface(
                                color = ObsidianVoid.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CyberBorderMuted),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.BatteryChargingFull,
                                            contentDescription = null,
                                            tint = cellAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Battery Optimization Engine",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = if (uiState.isBatteryOptimizationEnabled) {
                                                    "Active power guard & background drain limit"
                                                } else {
                                                    "Enable to extend battery endurance"
                                                },
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = uiState.isBatteryOptimizationEnabled,
                                        onCheckedChange = onToggleBatteryOptimization,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = ObsidianVoid,
                                            checkedTrackColor = cellAccent
                                        ),
                                        modifier = Modifier.testTag("battery_optimization_master_switch")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Power Optimization Mode Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BatteryOptimizationMode.entries.forEach { mode ->
                                    val selected = uiState.selectedBatteryMode == mode
                                    Surface(
                                        color = if (selected) cellAccent.copy(alpha = 0.18f) else ObsidianCardElevated,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) cellAccent else CyberBorderMuted
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onSelectBatteryMode(mode) }
                                            .testTag("battery_mode_${mode.name.lowercase()}")
                                    ) {
                                        Text(
                                            text = mode.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (selected) cellAccent else TextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            uiState.batteryStatusMessage?.let { msg ->
                                Surface(
                                    color = ObsidianVoid.copy(alpha = 0.72f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(0.8.dp, EmeraldPulse.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                                        color = EmeraldPulse,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Button(
                                onClick = onOptimizeBattery,
                                enabled = !uiState.isOptimizingBattery,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = cellAccent,
                                    contentColor = ObsidianVoid
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("optimize_battery_button")
                            ) {
                                if (uiState.isOptimizingBattery) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = ObsidianVoid,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("OPTIMIZING & CLEANING CACHE…", style = MaterialTheme.typography.labelLarge)
                                } else {
                                    Icon(Icons.Filled.Bolt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("OPTIMIZE & COOL BATTERY", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }

                // 2. Automatic Battery Cache Cleaner System
                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.58f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("battery_auto_cache_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(NeonCyan.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "AUTO BATTERY CACHE CLEANER",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonCyan
                                        )
                                        Text(
                                            text = "Automatically purges drain-causing cache & temp files",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.autoCleanCacheForBattery,
                                    onCheckedChange = onToggleAutoCleanCacheForBattery,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = ObsidianVoid,
                                        checkedTrackColor = NeonCyan
                                    ),
                                    modifier = Modifier.testTag("battery_auto_cache_switch")
                                )
                            }

                            HorizontalDivider(color = CyberBorderMuted.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BatteryStatChip(
                                    label = "Last Cleaned",
                                    value = if (bat.lastCacheFreedBytes > 0L) {
                                        formatBytes(bat.lastCacheFreedBytes)
                                    } else {
                                        "${bat.lastCleanedFilesCount} files"
                                    },
                                    accent = EmeraldPulse,
                                    modifier = Modifier.weight(1f)
                                )
                                BatteryStatChip(
                                    label = "Total Saved",
                                    value = formatBytes(bat.totalBatteryCacheFreedBytes),
                                    accent = NeonCyan,
                                    modifier = Modifier.weight(1f)
                                )
                                BatteryStatChip(
                                    label = "App Cache",
                                    value = formatBytes(uiState.totalDetectedAppCacheBytes),
                                    accent = SolarAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onRunBatteryAutoCacheSweep,
                                    border = BorderStroke(1.dp, EmeraldPulse.copy(alpha = 0.65f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("battery_sweep_cache_now_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CleaningServices,
                                        contentDescription = null,
                                        tint = EmeraldPulse,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Clean Cache",
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                        color = EmeraldPulse,
                                        maxLines = 1
                                    )
                                }

                                OutlinedButton(
                                    onClick = onTriggerAutoAppCacheClean,
                                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.65f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("battery_auto_all_apps_cache_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "All Apps Cache",
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                        color = NeonCyan,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. 2x3 Live Hardware Telemetry Matrix
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TelemetryTile(
                                label = "TEMPERATURE",
                                value = String.format(Locale.US, "%.1f °C", bat.temperatureCelsius),
                                accent = if (bat.thermalWarning) SolarAmber else EmeraldPulse,
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryTile(
                                label = "VOLTAGE",
                                value = String.format(Locale.US, "%.2f V", bat.voltageVolts),
                                accent = NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TelemetryTile(
                                label = "CELL HEALTH",
                                value = bat.healthLabel,
                                accent = if (bat.isHealthy) EmeraldPulse else SolarAmber,
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryTile(
                                label = "POWER SOURCE",
                                value = bat.plugSource,
                                accent = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TelemetryTile(
                                label = "CHEMISTRY",
                                value = bat.technology,
                                accent = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryTile(
                                label = "LIVE CURRENT",
                                value = bat.currentMilliamps?.let { "$it mA" } ?: "Standard",
                                accent = NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 4. Thermal Guard & System Power Controls
                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CyberBorderMuted)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Thermostat,
                                    contentDescription = null,
                                    tint = cellAccent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (bat.thermalWarning) {
                                        "Thermal Load Elevated • Cooling advised"
                                    } else {
                                        "Battery Cell & Thermal Load Healthy"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        runCatching {
                                            context.startActivity(
                                                Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                            )
                                        }
                                    },
                                    border = BorderStroke(1.dp, cellAccent.copy(alpha = 0.65f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("open_battery_saver_button")
                                ) {
                                    Icon(Icons.Filled.BatterySaver, contentDescription = null, tint = cellAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Saver", color = cellAccent, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        runCatching {
                                            context.startActivity(
                                                Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                            )
                                        }
                                    },
                                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.65f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("open_battery_usage_button")
                                ) {
                                    Icon(Icons.Filled.Power, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Usage", color = NeonCyan, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        runCatching {
                                            context.startActivity(
                                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                            )
                                        }
                                    },
                                    border = BorderStroke(1.dp, SolarAmber.copy(alpha = 0.65f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("open_battery_opt_settings_button")
                                ) {
                                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = SolarAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("App Opt", color = SolarAmber, fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(14.dp)) }
            }
        }
    }
}

@Composable
private fun BatteryStatChip(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianVoid.copy(alpha = 0.78f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.8.dp, CyberBorderMuted),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                color = TextSecondary,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VoltCoreWaveformStrip(
    temperatureCelsius: Float,
    voltageVolts: Float,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "voltcore_wave_strip")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "wave_phase"
    )

    Surface(
        color = ObsidianVoid.copy(alpha = 0.78f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.38f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    text = "THERMAL FLUX",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextSecondary
                )
                Text(
                    text = String.format(Locale.US, "%.1f°C • %.2fV", temperatureCelsius, voltageVolts),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
                    color = accent
                )
            }
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
            ) {
                val midY = size.height / 2f
                val pathPrimary = Path()
                val pathSecondary = Path()
                val steps = 36
                for (i in 0..steps) {
                    val f = i / steps.toFloat()
                    val x = f * size.width
                    val amp = size.height * 0.34f * sin(f * PI).toFloat()
                    val y1 = midY + sin(f * 4f * PI + phase).toFloat() * amp
                    val y2 = midY + sin(f * 3f * PI - phase * 1.3f).toFloat() * (amp * 0.65f)
                    if (i == 0) {
                        pathPrimary.moveTo(x, y1)
                        pathSecondary.moveTo(x, y2)
                    } else {
                        pathPrimary.lineTo(x, y1)
                        pathSecondary.lineTo(x, y2)
                    }
                }
                drawPath(
                    path = pathSecondary,
                    color = NeonCyan.copy(alpha = 0.48f),
                    style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = pathPrimary,
                    color = accent,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun LiquidPlasmaBatteryCell(
    levelPercent: Int,
    isCharging: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "plasma_battery")
    val waveShift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "wave"
    )
    val haloPulse by infinite.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )
    val orbitAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing)),
        label = "orbit"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f + 4.dp.toPx())
            val maxR = size.minDimension * 0.56f

            // Ambient radial reactor aura behind cell
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.28f * haloPulse),
                        NeonCyan.copy(alpha = 0.12f * haloPulse),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxR
                ),
                radius = maxR,
                center = center
            )

            // Rotating orbital ring around the reactor cell
            rotate(orbitAngle, center) {
                val ringD = size.minDimension * 0.96f
                val tl = Offset(center.x - ringD / 2f, center.y - ringD / 2f)
                drawArc(
                    color = accent.copy(alpha = 0.45f),
                    startAngle = 0f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = tl,
                    size = Size(ringD, ringD),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = NeonCyan.copy(alpha = 0.40f),
                    startAngle = 180f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = tl,
                    size = Size(ringD, ringD),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            val cellW = size.width * 0.72f
            val bodyLeft = (size.width - cellW) / 2f
            val capW = cellW * 0.44f
            val capH = 10.dp.toPx()
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(accent, NeonCyan)),
                topLeft = Offset((size.width - capW) / 2f, 4.dp.toPx()),
                size = Size(capW, capH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            val bodyTop = capH + 6.dp.toPx()
            val bodyH = size.height - bodyTop - 6.dp.toPx()

            // Dark recessed interior
            drawRoundRect(
                color = ObsidianVoid.copy(alpha = 0.86f),
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(cellW, bodyH),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
            )

            clipRect(left = bodyLeft, top = bodyTop, right = bodyLeft + cellW, bottom = bodyTop + bodyH) {
                val fillFrac = (levelPercent / 100f).coerceIn(0.08f, 1f)
                val baseWaterY = bodyTop + bodyH * (1f - fillFrac)

                // Back harmonic plasma wave
                val backPath = Path().apply {
                    moveTo(bodyLeft, bodyTop + bodyH)
                    lineTo(bodyLeft, baseWaterY)
                    val steps = 22
                    for (i in 0..steps) {
                        val frac = i / steps.toFloat()
                        val x = bodyLeft + frac * cellW
                        val y = baseWaterY + sin(frac * 2.5f * PI - waveShift * 1.2f).toFloat() * 4.5.dp.toPx()
                        lineTo(x, y)
                    }
                    lineTo(bodyLeft + cellW, bodyTop + bodyH)
                    close()
                }
                drawPath(
                    path = backPath,
                    color = HyperViolet.copy(alpha = 0.35f)
                )

                // Foreground luminous plasma wave
                val liquidPath = Path().apply {
                    moveTo(bodyLeft, bodyTop + bodyH)
                    lineTo(bodyLeft, baseWaterY)
                    val steps = 22
                    for (i in 0..steps) {
                        val frac = i / steps.toFloat()
                        val x = bodyLeft + frac * cellW
                        val y = baseWaterY + sin(frac * 2.5f * PI + waveShift).toFloat() * 4.dp.toPx()
                        lineTo(x, y)
                    }
                    lineTo(bodyLeft + cellW, bodyTop + bodyH)
                    close()
                }
                drawPath(
                    path = liquidPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.90f), NeonCyan.copy(alpha = 0.55f), RoyalSapphire.copy(alpha = 0.5f))
                    )
                )

                // Segmented scale tick lines inside cell
                for (tick in 1..4) {
                    val ty = bodyTop + bodyH * (tick / 5f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.16f),
                        start = Offset(bodyLeft + 6.dp.toPx(), ty),
                        end = Offset(bodyLeft + 16.dp.toPx(), ty),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Specular glass highlight streak along left inner wall
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    start = Offset(bodyLeft + 6.dp.toPx(), bodyTop + 12.dp.toPx()),
                    end = Offset(bodyLeft + 6.dp.toPx(), bodyTop + bodyH * 0.72f),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Outer glowing cell chassis frame
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(accent, NeonCyan)),
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(cellW, bodyH),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                style = Stroke(width = 2.8.dp.toPx())
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isCharging) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = SolarAmber, modifier = Modifier.size(22.dp))
            }
            Text(
                text = "$levelPercent%",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun TelemetryTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianCard.copy(alpha = 0.92f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.1.dp, accent.copy(alpha = 0.38f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = 11.dp)
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = accent)
        }
    }
}
