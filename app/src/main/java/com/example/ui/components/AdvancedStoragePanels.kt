package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.LargeFileThresholdPreset
import com.example.model.StorageBoostBenchmark
import com.example.model.StorageBoostMode
import com.example.model.StorageCleanHistoryRecord
import com.example.model.StorageTelemetry
import com.example.model.TempFileCategory
import com.example.model.TempFileEntry
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

@Composable
fun SmartStorageGuardCard(
    files: List<TempFileEntry>,
    largeThresholdBytes: Long,
    onSelectLargeThreshold: (Long) -> Unit,
    onApplyPreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val junkFiles = files.filter {
        it.category == TempFileCategory.TEMP_CACHE ||
            it.category == TempFileCategory.LOG_FILE ||
            it.category == TempFileCategory.RESIDUAL_PART ||
            it.category == TempFileCategory.EMPTY_FILE
    }
    val apkFiles = files.filter { it.category == TempFileCategory.APK_INSTALLER }
    val largeFiles = files.filter { it.category == TempFileCategory.LARGE_FILE }
    val duplicateFiles = files.filter { it.category == TempFileCategory.DUPLICATE_FILE }

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, SolarAmber.copy(alpha = 0.52f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_storage_guard_card")
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
                        .background(SolarAmber.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = SolarAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SMART STORAGE ANALYZER & PRESETS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolarAmber
                    )
                    Text(
                        text = "${junkFiles.size} junk • ${apkFiles.size} APKs • ${largeFiles.size} large • ${duplicateFiles.size} duplicates",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse Storage Analyzer" else "Expand Storage Analyzer",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }

            // One-tap Smart Presets Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StoragePresetPill("Safe Junk (${junkFiles.size})", NeonCyan, "storage_preset_junk") {
                    onApplyPreset("JUNK_ONLY")
                }
                StoragePresetPill("APKs (${apkFiles.size})", SolarAmber, "storage_preset_apks") {
                    onApplyPreset("APKS")
                }
                StoragePresetPill("Duplicates (${duplicateFiles.size})", HyperViolet, "storage_preset_duplicates") {
                    onApplyPreset("DUPLICATES")
                }
                StoragePresetPill("All (${files.size})", EmeraldPulse, "storage_preset_all") {
                    onApplyPreset("ALL")
                }
                StoragePresetPill("Clear", TextSecondary, "storage_preset_none") {
                    onApplyPreset("NONE")
                }
            }

            if (expanded) {
                HorizontalDivider(color = CyberBorderMuted.copy(alpha = 0.55f))

                Text(
                    text = "LARGE FILE DETECTOR LIMIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LargeFileThresholdPreset.entries.forEach { preset ->
                        val isSelected = largeThresholdBytes == preset.bytes
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectLargeThreshold(preset.bytes) },
                            label = {
                                Text(
                                    text = "≥ ${preset.label}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.22f),
                                selectedLabelColor = NeonCyan,
                                containerColor = ObsidianVoid,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CyberBorderMuted,
                                selectedBorderColor = NeonCyan
                            ),
                            modifier = Modifier.testTag("large_threshold_${preset.name.lowercase()}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoragePresetPill(
    label: String,
    accent: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        color = ObsidianCardElevated,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = accent
            )
        }
    }
}

@Composable
fun StorageHealthAndHistoryCard(
    storage: StorageTelemetry,
    history: List<StorageCleanHistoryRecord>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHistoryList by rememberSaveable { mutableStateOf(history.isNotEmpty()) }

    val score = storage.healthScore
    val healthColor = when {
        score >= 80 -> EmeraldPulse
        score >= 55 -> SolarAmber
        else -> ElectricCrimson
    }
    val healthLabel = when {
        score >= 80 -> "OPTIMAL"
        score >= 55 -> "MODERATE"
        else -> "LOW SPACE"
    }

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, healthColor.copy(alpha = 0.52f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("storage_analytics_history_card")
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
                        text = "STORAGE HEALTH & SWEEP LOG",
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

            StorageSavingsSparkCanvas(
                history = history,
                totalReclaimedBytes = storage.totalStorageReclaimedBytes,
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
                        tint = SolarAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recent Storage Sweeps (${history.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (history.isNotEmpty()) {
                        TextButton(
                            onClick = onClearHistory,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.testTag("clear_storage_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear storage log",
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Clear", color = TextSecondary, fontSize = 11.5.sp)
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (showHistoryList) "Collapse storage log" else "Expand storage log",
                        tint = SolarAmber,
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
                            text = "No storage sweeps logged yet. Run Ignite Boost or clean junk files to track lifetime storage savings.",
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
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.categoryLabel} • ${formatHistoryTime(item.timestampMs)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = if (item.freedBytes > 0L) "+${formatBytes(item.freedBytes)}" else "${item.itemCount} items",
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
private fun StorageSavingsSparkCanvas(
    history: List<StorageCleanHistoryRecord>,
    totalReclaimedBytes: Long,
    accentColor: Color
) {
    val samples = history.take(10).reversed()
    val maxBytes = samples.maxOfOrNull { it.freedBytes }?.coerceAtLeast(1024L) ?: 1024L

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
                    text = "STORAGE RECOVERY TIMELINE",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary
                )
                Text(
                    text = "Lifetime Freed: ${formatBytes(totalReclaimedBytes)}",
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
                        record != null && record.itemCount > 0 -> 0.2f
                        record != null -> 0.14f
                        else -> 0.08f
                    }
                    val barH = size.height * ratio
                    val topY = size.height - barH

                    drawRoundRect(
                        brush = if (record != null) {
                            Brush.verticalGradient(listOf(SolarAmber, accentColor))
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

@Composable
fun StorageBoostEngineCard(
    selectedMode: StorageBoostMode,
    includeApksInBoost: Boolean,
    autoSweepSafInBoost: Boolean,
    hasLinkedFolder: Boolean,
    isBoosting: Boolean,
    activeStageIndex: Int,
    benchmark: StorageBoostBenchmark?,
    totalAppCacheBytes: Long,
    onSelectMode: (StorageBoostMode) -> Unit,
    onToggleIncludeApks: (Boolean) -> Unit,
    onToggleAutoSweepSaf: (Boolean) -> Unit,
    onTriggerAutoAppCacheBoost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stages = listOf(
        1 to "Scan Buffers",
        2 to "Purge Junk",
        3 to "Trim SQLite",
        4 to "Flash I/O"
    )

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.2.dp,
            if (isBoosting) EmeraldPulse.copy(alpha = 0.75f) else SolarAmber.copy(alpha = 0.62f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("storage_boost_engine_card")
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
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SolarAmber.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RocketLaunch,
                            contentDescription = null,
                            tint = SolarAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TURBO STORAGE BOOST ENGINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = SolarAmber
                        )
                        Text(
                            text = selectedMode.detail,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 3-Tier Boost Mode Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StorageBoostMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    val chipAccent = when (mode) {
                        StorageBoostMode.QUICK_SWEEP -> NeonCyan
                        StorageBoostMode.TURBO_COMPACT -> SolarAmber
                        StorageBoostMode.EXTREME_BOOST -> EmeraldPulse
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectMode(mode) },
                        enabled = !isBoosting,
                        label = {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipAccent.copy(alpha = 0.22f),
                            selectedLabelColor = chipAccent,
                            containerColor = ObsidianVoid,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = !isBoosting,
                            selected = isSelected,
                            borderColor = CyberBorderMuted,
                            selectedBorderColor = chipAccent
                        ),
                        modifier = Modifier.testTag("storage_boost_mode_${mode.name.lowercase()}")
                    )
                }
            }

            // 4-Stage Live Boost Pipeline Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                stages.forEach { (stepNum, label) ->
                    val isCompleted = activeStageIndex > stepNum || (!isBoosting && benchmark != null && activeStageIndex >= stepNum)
                    val isActive = isBoosting && activeStageIndex == stepNum
                    val pillColor = when {
                        isCompleted -> EmeraldPulse
                        isActive -> SolarAmber
                        else -> TextMuted
                    }
                    Surface(
                        color = if (isCompleted || isActive) pillColor.copy(alpha = 0.14f) else ObsidianVoid.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            0.9.dp,
                            if (isCompleted || isActive) pillColor.copy(alpha = 0.6f) else CyberBorderMuted.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldPulse,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = "$stepNum.$label",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = pillColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Boost Safety & Scope Toggles
            Surface(
                color = ObsidianVoid.copy(alpha = 0.72f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CyberBorderMuted)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Sweep Linked Folder Junk",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 12.5.sp),
                                color = TextPrimary
                            )
                            Text(
                                text = if (hasLinkedFolder) {
                                    "Removes safe .tmp, .log & 0-B files in linked SAF folder"
                                } else {
                                    "Pick a folder below to include external safe junk"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = autoSweepSafInBoost,
                            onCheckedChange = onToggleAutoSweepSaf,
                            enabled = !isBoosting,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianVoid,
                                checkedTrackColor = NeonCyan
                            ),
                            modifier = Modifier.testTag("boost_saf_sweep_switch")
                        )
                    }

                    if (selectedMode == StorageBoostMode.EXTREME_BOOST) {
                        HorizontalDivider(color = CyberBorderMuted.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Include Old .APK Installers",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 12.5.sp),
                                    color = SolarAmber
                                )
                                Text(
                                    text = "Purges stale .apk/.xapk packages during Extreme Boost",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = includeApksInBoost,
                                onCheckedChange = onToggleIncludeApks,
                                enabled = !isBoosting,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ObsidianVoid,
                                    checkedTrackColor = SolarAmber
                                ),
                                modifier = Modifier.testTag("boost_include_apks_switch")
                            )
                        }
                    }
                }
            }

            // Post-Boost Benchmark Summary & Deep App Cache Escalation
            if (benchmark != null) {
                Surface(
                    color = EmeraldPulse.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, EmeraldPulse.copy(alpha = 0.45f)),
                    modifier = Modifier.testTag("storage_boost_benchmark_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Speed,
                                    contentDescription = null,
                                    tint = EmeraldPulse,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${benchmark.modeLabel.uppercase()} BENCHMARK",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldPulse
                                )
                            }
                            if (benchmark.writeSpeedMbPerSec > 0f) {
                                Text(
                                    text = "${benchmark.writeSpeedMbPerSec.toInt()} MB/s Flash I/O",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
                                    color = NeonCyan
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BenchmarkChip(
                                title = "Swept",
                                value = formatBytes(benchmark.totalFreedBytes),
                                accent = EmeraldPulse,
                                modifier = Modifier.weight(1f)
                            )
                            BenchmarkChip(
                                title = "Files",
                                value = "${benchmark.localFilesDeletedCount + benchmark.safJunkDeletedCount}",
                                accent = SolarAmber,
                                modifier = Modifier.weight(1f)
                            )
                            BenchmarkChip(
                                title = "SQLite/VM",
                                value = if (benchmark.sqlitePagesTrimmed) "Trimmed" else "Verified",
                                accent = NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (totalAppCacheBytes > 0L) {
                            OutlinedButton(
                                onClick = onTriggerAutoAppCacheBoost,
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.65f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("boost_chain_app_cache_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto-Clean Used App Caches (${formatBytes(totalAppCacheBytes)})",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                    color = NeonCyan
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
private fun BenchmarkChip(
    title: String,
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                color = accent,
                maxLines = 1
            )
        }
    }
}
