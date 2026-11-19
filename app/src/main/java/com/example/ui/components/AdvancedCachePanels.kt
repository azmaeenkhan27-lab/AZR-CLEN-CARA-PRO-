package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.OpenInNew
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
import com.example.model.AppCacheEntry
import com.example.model.CacheCleanHistoryRecord
import com.example.model.CacheSelectionPreset
import com.example.model.CacheThresholdPreset
import com.example.model.InternalBufferBreakdown
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.RoyalSapphire
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import com.example.util.formatBytes
import com.example.util.formatHistoryTime
import com.example.util.formatRelativeTime

@Composable
fun SmartCacheGuardPanel(
    selectedThresholdBytes: Long,
    overThresholdCount: Int,
    overThresholdTotalBytes: Long,
    skipZeroCacheAutoClean: Boolean,
    internalBuffer: InternalBufferBreakdown,
    onSelectThreshold: (Long) -> Unit,
    onApplyPreset: (CacheSelectionPreset) -> Unit,
    onToggleSkipZero: (Boolean) -> Unit,
    onCleanOverThreshold: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (overThresholdCount > 0) ElectricCrimson.copy(alpha = 0.65f) else HyperViolet.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_cache_guard_card")
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
                            if (overThresholdCount > 0) ElectricCrimson.copy(alpha = 0.16f)
                            else HyperViolet.copy(alpha = 0.16f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (overThresholdCount > 0) Icons.Filled.WarningAmber else Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = if (overThresholdCount > 0) ElectricCrimson else HyperViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SMART CACHE GUARD & PRESETS",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (overThresholdCount > 0) ElectricCrimson else HyperViolet
                    )
                    Text(
                        text = if (overThresholdCount > 0) {
                            "$overThresholdCount app(s) over ${formatBytes(selectedThresholdBytes)} • ${formatBytes(overThresholdTotalBytes)}"
                        } else {
                            "Alert limit: ${formatBytes(selectedThresholdBytes)} • All apps within limit"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse Smart Guard" else "Expand Smart Guard",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }

            // One-tap Smart Presets Row (always accessible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CacheSelectionPreset.entries.forEach { preset ->
                    Surface(
                        color = ObsidianCardElevated,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onApplyPreset(preset) }
                            .testTag("cache_preset_${preset.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = NeonCyan
                            )
                        }
                    }
                }
            }

            if (overThresholdCount > 0) {
                Button(
                    onClick = onCleanOverThreshold,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCrimson,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("clean_over_threshold_button")
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CLEAN $overThresholdCount OVER-LIMIT (${formatBytes(overThresholdTotalBytes)})",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(color = CyberBorderMuted.copy(alpha = 0.55f))

                Text(
                    text = "CACHE ALERT LIMIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolarAmber
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CacheThresholdPreset.entries.forEach { preset ->
                        val isSelected = selectedThresholdBytes == preset.bytes
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectThreshold(preset.bytes) },
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
                            modifier = Modifier.testTag("threshold_chip_${preset.name.lowercase()}")
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianVoid.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Skip 0 B Cache Apps",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Speed up Auto Clear by skipping apps with 0 B cache",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = skipZeroCacheAutoClean,
                        onCheckedChange = onToggleSkipZero,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianVoid,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = ObsidianCardElevated
                        ),
                        modifier = Modifier.testTag("skip_zero_cache_switch")
                    )
                }

                // Internal Buffer Directory Telemetry
                Text(
                    text = "AZROIA INTERNAL BUFFER BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InternalBufferCell("CacheDir", formatBytes(internalBuffer.cacheDirBytes), NeonCyan, Modifier.weight(1f))
                    InternalBufferCell("Code", formatBytes(internalBuffer.codeCacheDirBytes), HyperViolet, Modifier.weight(1f))
                    InternalBufferCell("External", formatBytes(internalBuffer.externalCacheDirBytes), SolarAmber, Modifier.weight(1f))
                    InternalBufferCell("Logs", formatBytes(internalBuffer.tempLogsBytes), EmeraldPulse, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InternalBufferCell(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianVoid.copy(alpha = 0.78f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, accent.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = TextSecondary,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                color = accent,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CacheHealthAndHistoryCard(
    healthScore: Int,
    history: List<CacheCleanHistoryRecord>,
    totalRemovedBytes: Long,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHistoryList by rememberSaveable { mutableStateOf(history.isNotEmpty()) }

    val healthColor = when {
        healthScore >= 85 -> EmeraldPulse
        healthScore >= 60 -> SolarAmber
        else -> ElectricCrimson
    }
    val healthLabel = when {
        healthScore >= 85 -> "OPTIMAL"
        healthScore >= 60 -> "MODERATE LOAD"
        else -> "HEAVY CACHE"
    }

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, healthColor.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("cache_analytics_history_card")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = null,
                        tint = healthColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CACHE HEALTH & CLEAN LOG",
                        style = MaterialTheme.typography.labelSmall,
                        color = healthColor
                    )
                }

                Surface(
                    color = healthColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, healthColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "$healthScore/100 • $healthLabel",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = healthColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            // Custom Canvas Savings Spark Graph
            CacheSavingsSparkCanvas(
                history = history,
                totalRemovedBytes = totalRemovedBytes,
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
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recent Clean Events (${history.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (history.isNotEmpty()) {
                        TextButton(
                            onClick = onClearHistory,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.testTag("clear_cache_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear clean log",
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Clear", color = TextSecondary, fontSize = 11.5.sp)
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (showHistoryList) "Collapse history" else "Expand history",
                        tint = NeonCyan,
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
                            text = "No clean events logged yet. Run Deep Scan or Auto Clear to record real freed cache history.",
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
                                        text = item.appName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.methodLabel} • ${formatHistoryTime(item.timestampMs)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = if (item.freedBytes > 0L) "+${formatBytes(item.freedBytes)}" else "Checked",
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
private fun CacheSavingsSparkCanvas(
    history: List<CacheCleanHistoryRecord>,
    totalRemovedBytes: Long,
    accentColor: Color
) {
    val samples = history.take(10).reversed()
    val maxBytes = samples.maxOfOrNull { it.freedBytes }?.coerceAtLeast(1024L) ?: 1024L

    Surface(
        color = ObsidianVoid.copy(alpha = 0.8f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderMuted)
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
                    text = "RECENT SAVINGS TELEMETRY",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary
                )
                Text(
                    text = "Lifetime Freed: ${formatBytes(totalRemovedBytes)}",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
                    color = EmeraldPulse
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                // Baseline and subtle horizontal guide
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
                    val ratio = if (record != null && record.freedBytes > 0L) {
                        (record.freedBytes.toFloat() / maxBytes.toFloat()).coerceIn(0.18f, 1f)
                    } else if (record != null) {
                        0.12f
                    } else {
                        0.08f
                    }
                    val barH = size.height * ratio
                    val topY = size.height - barH

                    drawRoundRect(
                        brush = if (record != null) {
                            Brush.verticalGradient(listOf(NeonCyan, accentColor))
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
fun AppCacheInspectorSheet(
    entry: AppCacheEntry,
    thresholdBytes: Long,
    onDismiss: () -> Unit,
    onToggleWhitelist: () -> Unit,
    onAutoCleanNow: () -> Unit,
    onOpenSystemAppInfo: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val totalFootprint = entry.totalFootprintBytes.coerceAtLeast(1L)
    val appWeight = (entry.appBytes.toFloat() / totalFootprint.toFloat()).coerceAtLeast(0.04f)
    val dataWeight = (entry.dataBytes.toFloat() / totalFootprint.toFloat()).coerceAtLeast(0.04f)
    val cacheWeight = (entry.cacheBytes.toFloat() / totalFootprint.toFloat()).coerceAtLeast(0.04f)
    val cacheRatioPct = ((entry.cacheBytes.toDouble() / totalFootprint.toDouble()) * 100.0).toInt().coerceIn(0, 100)
    val isOverLimit = entry.cacheBytes >= thresholdBytes

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
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ObsidianCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Android, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
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
                        text = buildString {
                            if (entry.versionName.isNotBlank()) append("v${entry.versionName} • ")
                            if (entry.installerLabel.isNotBlank()) append("${entry.installerLabel} • ")
                            append(formatRelativeTime(entry.lastUsedTimeMs))
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                        color = NeonCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 3-Segment App Footprint Breakdown
            Surface(
                color = ObsidianVoid.copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderMuted)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "APP STORAGE FOOTPRINT",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan
                        )
                        Text(
                            text = "${formatBytes(entry.totalFootprintBytes)} ($cacheRatioPct% cache)",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isOverLimit) ElectricCrimson else SolarAmber
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(ObsidianCard),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(appWeight)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(RoyalSapphire)
                        )
                        Box(
                            modifier = Modifier
                                .weight(dataWeight)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(HyperViolet)
                        )
                        Box(
                            modifier = Modifier
                                .weight(cacheWeight)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(if (isOverLimit) ElectricCrimson else SolarAmber)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FootprintMetric("App Binary", formatBytes(entry.appBytes), RoyalSapphire)
                        FootprintMetric("User Data", formatBytes(entry.dataBytes), HyperViolet)
                        FootprintMetric(
                            "Cleanable Cache",
                            formatBytes(entry.cacheBytes),
                            if (isOverLimit) ElectricCrimson else SolarAmber
                        )
                    }
                }
            }

            // Shield Protection Card
            Surface(
                color = if (entry.isWhitelisted) EmeraldPulse.copy(alpha = 0.14f) else ObsidianCard,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (entry.isWhitelisted) EmeraldPulse.copy(alpha = 0.65f) else CyberBorderMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onToggleWhitelist)
                    .testTag("inspector_toggle_whitelist")
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
                            text = if (entry.isWhitelisted) "Shielded from Auto Clear" else "Shield / Whitelist App",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (entry.isWhitelisted) EmeraldPulse else TextPrimary
                        )
                        Text(
                            text = if (entry.isWhitelisted) {
                                "Protected — skipped during Auto Clear All & Select All."
                            } else {
                                "Tap to protect this app from batch cache cleaning."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = TextSecondary
                        )
                    }
                    Surface(
                        color = if (entry.isWhitelisted) EmeraldPulse.copy(alpha = 0.2f) else ObsidianVoid,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (entry.isWhitelisted) "ON" else "OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (entry.isWhitelisted) EmeraldPulse else TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenSystemAppInfo,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderMuted),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("inspector_open_app_info")
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("App Info", color = TextPrimary)
                }

                Button(
                    onClick = {
                        onDismiss()
                        onAutoCleanNow()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = ObsidianVoid
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .testTag("inspector_auto_clean_now")
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto Clean Cache", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun FootprintMetric(label: String, value: String, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                color = TextSecondary
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
            color = TextPrimary
        )
    }
}
