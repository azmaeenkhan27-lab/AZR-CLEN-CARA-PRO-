package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CleanerModule
import com.example.ui.AzroiaUiState
import com.example.ui.components.AzrCyberEmblem
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.SquareModuleSlideCard
import com.example.ui.components.WideVirusShieldCard
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianGlassTop
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.formatBytes
import com.example.util.formatGbShort
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: AzroiaUiState,
    onModuleSelected: (CleanerModule) -> Unit,
    onReplayAzrLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cacheLabel = formatBytes(uiState.totalDetectedAppCacheBytes)
    val cacheStatus = "${uiState.cacheEntries.size} apps • Cached"
    val cacheRemovedLine = "Removed: ${formatBytes(uiState.totalCacheFreedSessionBytes)}"
    val cacheRatio = if (uiState.totalDetectedAppCacheBytes > 0L) 0.74f else 0.35f

    val ram = uiState.ramTelemetry
    val ramRatio = (ram.usedPercent / 100f).coerceIn(0.1f, 1f)
    val ramStatus = "${formatGbShort(ram.usedBytes)} / ${formatGbShort(ram.totalBytes)}"

    val storage = uiState.storageTelemetry
    val storageRatio = (storage.usedPercent / 100f).coerceIn(0.1f, 1f)
    val tempCount = uiState.tempFiles.size
    val storageStatus = if (tempCount > 0) {
        "${storage.usedPercent}% used • $tempCount temp"
    } else {
        "${storage.usedPercent}% used • Temp & Boost"
    }

    val bat = uiState.batteryTelemetry
    val tempStr = String.format(Locale.US, "%.1f°C", bat.temperatureCelsius)
    val batteryStatus = "$tempStr • ${bat.healthLabel}"

    val sec = uiState.securityPosture
    val threats = sec.criticalCount + sec.suspiciousCount
    val virusVal = if (!sec.lastScanCompleted) "SHIELD" else if (threats == 0) "ZERO" else "$threats"
    val virusStatus = if (sec.lastScanCompleted) {
        "${sec.scannedCount} packages audited • Patch ${sec.securityPatch}"
    } else {
        "Deep package & permission guard"
    }

    // Unified Live Device Health Score
    val healthScore = remember(ram.usedPercent, storage.usedPercent, bat.healthScore, threats) {
        val ramHealth = (100 - ram.usedPercent).coerceIn(10, 100)
        val storageHealth = (100 - storage.usedPercent).coerceIn(10, 100)
        val threatPenalty = threats * 15
        (((ramHealth * 0.25f) + (storageHealth * 0.25f) + (bat.healthScore * 0.50f)).toInt() - threatPenalty)
            .coerceIn(25, 100)
    }
    val healthAccent = when {
        healthScore >= 80 -> EmeraldPulse
        healthScore >= 60 -> SolarAmber
        else -> ElectricCrimson
    }

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        CyberBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Floating Glassmorphic Command Header Bar
            Surface(
                color = ObsidianCard.copy(alpha = 0.86f),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(
                    1.1.dp,
                    Brush.horizontalGradient(
                        listOf(SolarAmber.copy(alpha = 0.55f), CyberBorderMuted, NeonCyan.copy(alpha = 0.55f))
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onReplayAzrLaunch)
                            .testTag("header_azr_emblem")
                    ) {
                        AzrCyberEmblem(size = 42.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AZR",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.6.sp
                                    ),
                                    color = SolarAmber
                                )
                                Text(
                                    text = " CLEAN CARE",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "Tap emblem to replay launch",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.5.sp),
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = ObsidianGlassTop.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, healthAccent.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(healthAccent)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "PRO $healthScore%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = NeonCyan,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Live Command Telemetry Strip
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(340)) + slideInVertically(tween(340, easing = FastOutSlowInEasing)) { -18 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp)
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommandTelemetryPill(
                        label = "CLEANED",
                        value = formatBytes(uiState.totalCacheFreedSessionBytes),
                        accent = EmeraldPulse,
                        modifier = Modifier.weight(1f)
                    )
                    CommandTelemetryPill(
                        label = "RAM FREE",
                        value = formatGbShort(ram.availableBytes),
                        accent = HyperViolet,
                        modifier = Modifier.weight(1f)
                    )
                    CommandTelemetryPill(
                        label = "THERMAL",
                        value = tempStr,
                        accent = if (bat.thermalWarning) ElectricCrimson else SolarAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4 Large Square Cards fill the 1st full screen + 5th Wide Virus Card just a short scroll below
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 16.dp)
            ) {
                val gap = 12.dp
                val availW = maxWidth
                val availH = maxHeight

                val maxTileW = (availW - gap) / 2f
                val maxTileH = (availH - gap * 2f) / 2f
                val bigSquareSize = minOf(maxTileW, maxTileH).coerceAtLeast(130.dp)
                val virusBannerHeight = (availH * 0.22f).coerceIn(118.dp, 152.dp)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 4.dp, bottom = 24.dp)
                        .testTag("dashboard_slides_scroll"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(gap)
                ) {
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(tween(420, delayMillis = 60)) + slideInVertically(tween(420, delayMillis = 60, easing = FastOutSlowInEasing)) { 28 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
                        ) {
                            SquareModuleSlideCard(
                                module = CleanerModule.CACHE,
                                metricValue = cacheLabel,
                                metricUnit = "CACHE",
                                statusLine = cacheStatus,
                                secondaryStatusLine = cacheRemovedLine,
                                progressRatio = cacheRatio,
                                actionLabel = "CACHE",
                                tileSize = bigSquareSize,
                                onClick = { onModuleSelected(CleanerModule.CACHE) }
                            )
                            SquareModuleSlideCard(
                                module = CleanerModule.RAM,
                                metricValue = "${ram.usedPercent}%",
                                metricUnit = "RAM",
                                statusLine = ramStatus,
                                progressRatio = ramRatio,
                                actionLabel = "RAM",
                                tileSize = bigSquareSize,
                                onClick = { onModuleSelected(CleanerModule.RAM) }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(tween(460, delayMillis = 130)) + slideInVertically(tween(460, delayMillis = 130, easing = FastOutSlowInEasing)) { 34 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
                        ) {
                            SquareModuleSlideCard(
                                module = CleanerModule.STORAGE,
                                metricValue = formatGbShort(storage.freeBytes),
                                metricUnit = "FREE",
                                statusLine = storageStatus,
                                progressRatio = storageRatio,
                                actionLabel = "BOOST",
                                tileSize = bigSquareSize,
                                onClick = { onModuleSelected(CleanerModule.STORAGE) }
                            )
                            SquareModuleSlideCard(
                                module = CleanerModule.BATTERY,
                                metricValue = "${bat.levelPercent}%",
                                metricUnit = "VOLTCORE",
                                statusLine = batteryStatus,
                                progressRatio = (bat.levelPercent / 100f).coerceIn(0.1f, 1f),
                                actionLabel = "BATTERY",
                                tileSize = bigSquareSize,
                                onClick = { onModuleSelected(CleanerModule.BATTERY) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200, easing = FastOutSlowInEasing)) { 36 }
                    ) {
                        WideVirusShieldCard(
                            metricValue = virusVal,
                            statusLine = virusStatus,
                            progressRatio = if (sec.lastScanCompleted && threats == 0) 1f else 0.78f,
                            cardHeight = virusBannerHeight,
                            onClick = { onModuleSelected(CleanerModule.VIRUS) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommandTelemetryPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianVoid.copy(alpha = 0.78f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.9.dp, accent.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, letterSpacing = 0.8.sp),
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
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.85f))
            )
        }
    }
}
