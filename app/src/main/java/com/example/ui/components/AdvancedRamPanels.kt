package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RamBoostHistoryRecord
import com.example.model.RamPressureThresholdPreset
import com.example.model.RamProcessEntry
import com.example.model.RamTelemetry
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.formatBytes
import com.example.util.formatHistoryTime
import com.example.util.formatRelativeTime

@Composable
fun SmartRamGuardCard(
    ram: RamTelemetry,
    pressureThresholdPercent: Int,
    isLiveMonitorEnabled: Boolean,
    isOptimizing: Boolean,
    onSelectThreshold: (Int) -> Unit,
    onToggleLiveMonitor: (Boolean) -> Unit,
    onCompactHeap: () -> Unit,
    onStartDeepHibernate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val isOverPressure = ram.usedPercent >= pressureThresholdPercent || ram.isLowMemory

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.2.dp,
            if (isOverPressure) ElectricCrimson.copy(alpha = 0.7f) else HyperViolet.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_ram_guard_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isOverPressure) ElectricCrimson.copy(alpha = 0.16f)
                            else HyperViolet.copy(alpha = 0.16f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOverPressure) Icons.Filled.WarningAmber else Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = if (isOverPressure) ElectricCrimson else HyperViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SMART RAM GUARD & DEEP HIBERNATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverPressure) ElectricCrimson else HyperViolet
                    )
                    Text(
                        text = if (isOverPressure) {
                            "High pressure (${ram.usedPercent}% ≥ $pressureThresholdPercent%) • ${ram.activeProcessCount} active"
                        } else {
                            "Guard limit: $pressureThresholdPercent% • ${ram.activeProcessCount} active apps"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse Smart RAM Guard" else "Expand Smart RAM Guard",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }

            // Dual Quick Action Row: Heap Compact + Deep Hibernate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCompactHeap,
                    enabled = !isOptimizing,
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("ram_compact_heap_button")
                ) {
                    Icon(Icons.Filled.CleaningServices, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Compact Heap",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onStartDeepHibernate,
                    enabled = !isOptimizing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCrimson,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.25f)
                        .height(48.dp)
                        .testTag("ram_deep_hibernate_button")
                ) {
                    Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Deep Hibernate",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp),
                        maxLines = 1
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(color = CyberBorderMuted.copy(alpha = 0.55f))

                Text(
                    text = "PRESSURE ALERT THRESHOLD",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolarAmber
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RamPressureThresholdPreset.entries.forEach { preset ->
                        val isSelected = pressureThresholdPercent == preset.percent
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectThreshold(preset.percent) },
                            label = {
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolarAmber.copy(alpha = 0.22f),
                                selectedLabelColor = SolarAmber,
                                containerColor = ObsidianVoid,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CyberBorderMuted,
                                selectedBorderColor = SolarAmber
                            ),
                            modifier = Modifier.testTag("ram_threshold_${preset.percent}")
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianVoid.copy(alpha = 0.72f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live 3s RAM Pulse Monitor",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Continuously samples live OS memory load & PSS every 3s",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isLiveMonitorEnabled,
                        onCheckedChange = onToggleLiveMonitor,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianVoid,
                            checkedTrackColor = EmeraldPulse,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = ObsidianCardElevated
                        ),
                        modifier = Modifier.testTag("ram_live_monitor_switch")
                    )
                }
            }
        }
    }
}

@Composable
fun RamAnalyticsAndHistoryCard(
    ram: RamTelemetry,
    history: List<RamBoostHistoryRecord>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHistoryList by rememberSaveable { mutableStateOf(history.isNotEmpty()) }

    val score = ram.healthScore
    val healthColor = when {
        score >= 78 -> EmeraldPulse
        score >= 52 -> SolarAmber
        else -> ElectricCrimson
    }
    val healthLabel = when {
        score >= 78 -> "OPTIMAL"
        score >= 52 -> "MODERATE"
        else -> "HEAVY LOAD"
    }

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, healthColor.copy(alpha = 0.52f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ram_analytics_history_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = null,
                        tint = healthColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MEMORY HEALTH & BOOST LOG",
                        style = MaterialTheme.typography.labelSmall,
                        color = healthColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = healthColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, healthColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "$score/100 • $healthLabel",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = healthColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            if (ram.memoryClassMb > 0 || ram.largeMemoryClassMb > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = ObsidianVoid.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, HyperViolet.copy(alpha = 0.35f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "VM Heap: ${ram.memoryClassMb} MB • Large: ${ram.largeMemoryClassMb} MB",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Surface(
                        color = ObsidianVoid.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, NeonCyan.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "Active: ${ram.activeProcessCount}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            RamBoostTimelineCanvas(
                history = history,
                totalReclaimedBytes = ram.totalReclaimedBytes,
                accentColor = healthColor
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showHistoryList = !showHistoryList }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = HyperViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recent Boost Runs (${history.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (history.isNotEmpty()) {
                        TextButton(
                            onClick = onClearHistory,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.testTag("clear_ram_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear RAM boost log",
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Clear", color = TextSecondary, fontSize = 11.5.sp)
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (showHistoryList) "Collapse RAM log" else "Expand RAM log",
                        tint = HyperViolet,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(if (showHistoryList) 180f else 0f)
                    )
                }
            }

            if (showHistoryList) {
                if (history.isEmpty()) {
                    Surface(
                        color = ObsidianVoid.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "No RAM boost sessions recorded yet. Run Turbo Release or Deep Hibernate to track memory drops.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = TextMuted,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        history.take(5).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ObsidianVoid.copy(alpha = 0.72f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.modeLabel} • ${item.killedCount} tasks (${item.beforePercent}% → ${item.afterPercent}%)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatHistoryTime(item.timestampMs),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = if (item.freedBytes > 0L) "+${formatBytes(item.freedBytes)}" else "${item.killedCount} freed",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                    color = if (item.freedBytes > 0L) EmeraldPulse else NeonCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RamBoostTimelineCanvas(
    history: List<RamBoostHistoryRecord>,
    totalReclaimedBytes: Long,
    accentColor: Color
) {
    val samples = history.take(10).reversed()
    val maxBytes = samples.maxOfOrNull { it.freedBytes }?.coerceAtLeast(1024L) ?: 1024L
    val maxKilled = samples.maxOfOrNull { it.killedCount }?.coerceAtLeast(1) ?: 1

    Surface(
        color = ObsidianVoid.copy(alpha = 0.8f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CyberBorderMuted)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT MEMORY RELEASE TIMELINE",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary
                )
                Text(
                    text = "Reclaimed: ${formatBytes(totalReclaimedBytes)}",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
                    color = EmeraldPulse
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                drawLine(
                    color = CyberBorderMuted.copy(alpha = 0.45f),
                    start = Offset(0f, size.height * 0.5f),
                    end = Offset(size.width, size.height * 0.5f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = CyberBorderMuted.copy(alpha = 0.7f),
                    start = Offset(0f, size.height - 1.dp.toPx()),
                    end = Offset(size.width, size.height - 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )

                val slots = 10
                val gap = 6.dp.toPx()
                val barWidth = ((size.width - gap * (slots - 1)) / slots).coerceAtLeast(4.dp.toPx())
                val offsetStart = slots - samples.size

                for (i in 0 until slots) {
                    val x = i * (barWidth + gap)
                    val record = if (i >= offsetStart) samples[i - offsetStart] else null
                    val ratio = when {
                        record != null && record.freedBytes > 0L ->
                            (record.freedBytes.toFloat() / maxBytes.toFloat()).coerceIn(0.22f, 1f)
                        record != null && record.killedCount > 0 ->
                            (record.killedCount.toFloat() / maxKilled.toFloat()).coerceIn(0.18f, 0.85f)
                        record != null -> 0.14f
                        else -> 0.08f
                    }
                    val barH = size.height * ratio
                    val topY = size.height - barH

                    drawRoundRect(
                        brush = if (record != null) {
                            Brush.verticalGradient(listOf(HyperViolet, accentColor))
                        } else {
                            Brush.verticalGradient(
                                listOf(CyberBorderMuted.copy(alpha = 0.35f), CyberBorderMuted.copy(alpha = 0.15f))
                            )
                        },
                        topLeft = Offset(x, topY),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamProcessInspectorSheet(
    entry: RamProcessEntry,
    onDismiss: () -> Unit,
    onToggleWhitelist: () -> Unit,
    onForceStopApp: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianCardElevated,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (entry.icon != null) {
                    Image(
                        bitmap = entry.icon,
                        contentDescription = "${entry.appName} icon",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ObsidianCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Android, contentDescription = null, tint = HyperViolet, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.appName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entry.packageName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${entry.importanceLabel} • ${formatRelativeTime(entry.lastUsedTimeMs)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                        color = NeonCyan
                    )
                }
            }

            Surface(
                color = ObsidianVoid.copy(alpha = 0.8f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CyberBorderMuted)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LIVE PROCESS PSS MEMORY",
                            style = MaterialTheme.typography.labelSmall,
                            color = HyperViolet
                        )
                        Text(
                            text = if (entry.processPssBytes > 0L) formatBytes(entry.processPssBytes) else "System Protected / Cached",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Surface(
                        color = (if (entry.isActiveProcess) ElectricCrimson else EmeraldPulse).copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (entry.isActiveProcess) "RUNNING" else "IDLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (entry.isActiveProcess) ElectricCrimson else EmeraldPulse,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Surface(
                color = if (entry.isWhitelisted) EmeraldPulse.copy(alpha = 0.14f) else ObsidianCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    if (entry.isWhitelisted) EmeraldPulse.copy(alpha = 0.65f) else CyberBorderMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onToggleWhitelist)
                    .testTag("ram_inspector_toggle_whitelist")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (entry.isWhitelisted) Icons.Filled.Shield else Icons.Filled.Security,
                        contentDescription = null,
                        tint = if (entry.isWhitelisted) EmeraldPulse else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (entry.isWhitelisted) "Shielded from RAM Release" else "Shield / Keep Alive in RAM",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (entry.isWhitelisted) EmeraldPulse else TextPrimary
                        )
                        Text(
                            text = if (entry.isWhitelisted) {
                                "Protected — skipped by Turbo Release & Deep Hibernate."
                            } else {
                                "Tap to protect music, calls, or alarm apps from closing."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = if (entry.isWhitelisted) "ON" else "OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entry.isWhitelisted) EmeraldPulse else TextSecondary
                    )
                }
            }

            Button(
                onClick = {
                    onDismiss()
                    onForceStopApp()
                },
                enabled = !entry.isWhitelisted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCrimson,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("ram_inspector_force_stop")
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Force Stop to Drop RAM", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
