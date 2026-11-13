package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CleanerModule
import com.example.ui.AzroiaUiState
import com.example.ui.components.AzrCyberEmblem
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.SquareModuleSlideCard
import com.example.ui.components.WideVirusShieldCard
import com.example.ui.theme.NeonCyan
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
    val cacheLabel = if (uiState.totalDetectedAppCacheBytes > 0L) {
        formatBytes(uiState.totalDetectedAppCacheBytes)
    } else {
        "${uiState.cacheEntries.size} Apps"
    }
    val cacheStatus = "${uiState.cacheEntries.size} apps • Sweep"
    val cacheRatio = if (uiState.totalDetectedAppCacheBytes > 0L) 0.72f else 0.48f

    val ram = uiState.ramTelemetry
    val ramRatio = (ram.usedPercent / 100f).coerceIn(0.1f, 1f)
    val ramStatus = "${formatGbShort(ram.usedBytes)} / ${formatGbShort(ram.totalBytes)}"

    val storage = uiState.storageTelemetry
    val storageRatio = (storage.usedPercent / 100f).coerceIn(0.1f, 1f)
    val storageStatus = "${storage.usedPercent}% used • Boost"

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

    Box(modifier = modifier.fillMaxSize()) {
        CyberBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Brand Bar with AZR Dustbin Emblem -> "AZR CLEAN CARE"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onReplayAzrLaunch)
                        .testTag("header_azr_emblem")
                ) {
                    AzrCyberEmblem(size = 42.dp)
                    Spacer(modifier = Modifier.width(12.dp))
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
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Tap emblem to replay launch",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = "PRO",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan
                )
            }

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

                // Size the 4 large squares so all 4 fill the initial viewport cleanly
                val maxTileW = (availW - gap) / 2f
                val maxTileH = (availH - gap * 2f) / 2f
                val bigSquareSize = minOf(maxTileW, maxTileH).coerceAtLeast(130.dp)

                // 5th Virus Card height: ~1/5 of screen (between 1/3 and 1/6 of mobile viewport)
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
                    // Row 1: 2 Large Squares (Cache Clear + RAM Optimize)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
                    ) {
                        SquareModuleSlideCard(
                            module = CleanerModule.CACHE,
                            metricValue = cacheLabel,
                            metricUnit = "CACHE",
                            statusLine = cacheStatus,
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

                    // Row 2: 2 Large Squares (Storage Clear + VoltCore Battery Guard)
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // Slide 5: Sleek Compact-Height Full-Width Zero Virus Shield Card below the 4 large squares
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
