package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.example.model.StorageTelemetry
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.RoyalSapphire
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.formatBytes
import com.example.util.formatGbShort
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StorageReactorHeroCanvas(
    storage: StorageTelemetry,
    detectedJunkBytes: Long,
    isBoostingOrScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "storage_reactor_hero")
    val primarySweep by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (isBoostingOrScanning) 1200 else 5000, easing = LinearEasing)
        ),
        label = "storage_primary_sweep"
    )
    val counterSweep by infinite.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (isBoostingOrScanning) 2000 else 7800, easing = LinearEasing)
        ),
        label = "storage_counter_sweep"
    )
    val corePulse by infinite.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "storage_core_pulse"
    )

    val animatedPercent by animateIntAsState(
        targetValue = storage.usedPercent.coerceIn(0, 100),
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "storage_percent_anim"
    )
    val animatedUsedFraction by animateFloatAsState(
        targetValue = (storage.usedPercent / 100f).coerceIn(0.05f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "storage_used_fraction"
    )

    val statusColor = when {
        storage.usedPercent >= 90 -> ElectricCrimson
        storage.usedPercent >= 75 -> SolarAmber
        else -> EmeraldPulse
    }
    val statusBadge = when {
        isBoostingOrScanning -> "CLEANING…"
        storage.usedPercent >= 90 -> "LOW SPACE"
        storage.usedPercent >= 75 -> "MODERATE"
        else -> "OPTIMAL"
    }

    val safeTotal = storage.totalBytes.coerceAtLeast(1L).toFloat()
    val cacheFrac = (storage.totalAppCacheBytes.toFloat() / safeTotal).coerceIn(0.02f, 0.25f)
    val junkFrac = ((storage.residualTempBytes + detectedJunkBytes).toFloat() / safeTotal).coerceIn(0.02f, 0.2f)
    val freeFrac = (storage.freeBytes.toFloat() / safeTotal).coerceIn(0.04f, 1f)
    val otherUsedFrac = (animatedUsedFraction - cacheFrac - junkFrac).coerceAtLeast(0.08f)

    Box(
        modifier = modifier.size(210.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SolarAmber.copy(alpha = 0.22f * corePulse),
                        NeonCyan.copy(alpha = 0.11f * corePulse),
                        RoyalSapphire.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 40 Outer Precision Ticks
            val tickInner = radius * 0.90f
            val tickOuter = radius * 0.97f
            for (i in 0 until 40) {
                val angleRad = Math.toRadians((i * 9).toDouble())
                val isMajor = i % 4 == 0
                val endR = if (isMajor) tickOuter else tickInner + (tickOuter - tickInner) * 0.52f
                drawLine(
                    color = if (isMajor) SolarAmber.copy(alpha = 0.72f) else CyberBorderMuted.copy(alpha = 0.55f),
                    start = Offset(
                        center.x + (cos(angleRad) * tickInner).toFloat(),
                        center.y + (sin(angleRad) * tickInner).toFloat()
                    ),
                    end = Offset(
                        center.x + (cos(angleRad) * endR).toFloat(),
                        center.y + (sin(angleRad) * endR).toFloat()
                    ),
                    strokeWidth = if (isMajor) 2.2.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Multi-Segment Storage Sector Donut Ring
            val ringD = radius * 1.58f
            val ringTopLeft = Offset(center.x - ringD / 2f, center.y - ringD / 2f)
            val ringSize = Size(ringD, ringD)
            val strokeW = 10.5.dp.toPx()

            drawArc(
                color = CyberBorderMuted.copy(alpha = 0.42f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            val segments = listOf(
                otherUsedFrac to SolarAmber,
                cacheFrac to NeonCyan,
                junkFrac to HyperViolet,
                freeFrac to EmeraldPulse
            )
            val weightSum = segments.sumOf { it.first.toDouble() }.toFloat().coerceAtLeast(0.01f)
            val gapDeg = 6f
            val totalSweep = 360f - (gapDeg * segments.size)
            var cursor = -90f
            for ((w, color) in segments) {
                val sweep = (totalSweep * (w / weightSum)).coerceAtLeast(12f)
                drawArc(
                    color = color,
                    startAngle = cursor,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = ringTopLeft,
                    size = ringSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
                cursor += sweep + gapDeg
            }

            // Inner Rotating Sector Beam + Orbiting Telemetry Node
            val innerD = radius * 1.24f
            val innerTl = Offset(center.x - innerD / 2f, center.y - innerD / 2f)
            rotate(degrees = primarySweep, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color.Transparent, SolarAmber.copy(alpha = 0.6f), NeonCyan),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 125f,
                    useCenter = false,
                    topLeft = innerTl,
                    size = Size(innerD, innerD),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                val orbAngle = Math.toRadians(125.0)
                val orbR = innerD / 2f
                drawCircle(
                    color = SolarAmber,
                    radius = 4.2.dp.toPx(),
                    center = Offset(
                        center.x + (cos(orbAngle) * orbR).toFloat(),
                        center.y + (sin(orbAngle) * orbR).toFloat()
                    )
                )
            }

            // Counter-Rotating Sub-Arcs
            rotate(degrees = counterSweep, pivot = center) {
                val subD = radius * 1.03f
                val subTl = Offset(center.x - subD / 2f, center.y - subD / 2f)
                for (k in 0..2) {
                    drawArc(
                        color = NeonCyan.copy(alpha = if (isBoostingOrScanning) 0.9f else 0.55f),
                        startAngle = k * 120f,
                        sweepAngle = 44f,
                        useCenter = false,
                        topLeft = subTl,
                        size = Size(subD, subD),
                        style = Stroke(width = if (isBoostingOrScanning) 2.6.dp.toPx() else 1.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Active Turbo Shockwave Ring during Boost or Scan
            if (isBoostingOrScanning) {
                val pulseR = radius * (0.86f + 0.12f * corePulse)
                drawCircle(
                    color = EmeraldPulse.copy(alpha = (1.15f - corePulse).coerceIn(0.15f, 0.7f)),
                    radius = pulseR,
                    center = center,
                    style = Stroke(width = 2.4.dp.toPx())
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 18.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.SdStorage,
                contentDescription = null,
                tint = SolarAmber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "$animatedPercent%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 27.sp
                ),
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = "${formatGbShort(storage.freeBytes)} Free",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = EmeraldPulse,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = statusColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, statusColor.copy(alpha = 0.55f))
            ) {
                Text(
                    text = if (storage.lastWriteSpeedMbPerSec > 0f && !isBoostingOrScanning) {
                        "$statusBadge • ${storage.lastWriteSpeedMbPerSec.toInt()} MB/s"
                    } else {
                        statusBadge
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StorageSectorDistributionPanel(
    storage: StorageTelemetry,
    detectedJunkBytes: Long,
    modifier: Modifier = Modifier
) {
    val safeTotal = storage.totalBytes.coerceAtLeast(1L).toFloat()
    val cacheBytes = storage.totalAppCacheBytes.coerceAtLeast(0L)
    val junkBytes = (storage.residualTempBytes + detectedJunkBytes).coerceAtLeast(0L)
    val appsMediaBytes = (storage.usedBytes - cacheBytes - junkBytes).coerceAtLeast(0L)

    val appsWeight = (appsMediaBytes.toFloat() / safeTotal).coerceAtLeast(0.06f)
    val cacheWeight = (cacheBytes.toFloat() / safeTotal).coerceAtLeast(0.03f)
    val junkWeight = (junkBytes.toFloat() / safeTotal).coerceAtLeast(0.02f)
    val freeWeight = (storage.freeBytes.toFloat() / safeTotal).coerceAtLeast(0.06f)

    Surface(
        color = ObsidianVoid.copy(alpha = 0.78f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CyberBorderMuted),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STORAGE SECTOR MATRIX",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolarAmber
                )
                Text(
                    text = "${formatGbShort(storage.usedBytes)} / ${formatGbShort(storage.totalBytes)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }

            if (storage.lastBoostFreedBytes > 0L || storage.lastWriteSpeedMbPerSec > 0f) {
                Surface(
                    color = EmeraldPulse.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.9.dp, EmeraldPulse.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("storage_freed_delta_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDownward,
                                contentDescription = null,
                                tint = EmeraldPulse,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (storage.lastBoostModeLabel.isNotBlank()) {
                                    "${storage.lastBoostModeLabel} • ${storage.lastBoostItemsCleaned} purged"
                                } else {
                                    "Storage Reclaimed This Session"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = EmeraldPulse
                            )
                        }
                        Text(
                            text = buildString {
                                append("+${formatBytes(storage.lastBoostFreedBytes)}")
                                if (storage.lastWriteSpeedMbPerSec > 0f) {
                                    append(" • ${storage.lastWriteSpeedMbPerSec.toInt()} MB/s")
                                }
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
                            color = EmeraldPulse
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ObsidianCard)
                    .border(0.6.dp, CyberBorderMuted, RoundedCornerShape(50))
                    .padding(1.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(appsWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(SolarAmber)
                )
                Box(
                    modifier = Modifier
                        .weight(cacheWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(NeonCyan)
                )
                Box(
                    modifier = Modifier
                        .weight(junkWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(HyperViolet)
                )
                Box(
                    modifier = Modifier
                        .weight(freeWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(EmeraldPulse)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StorageLegendCell(
                    label = "System & Data",
                    value = formatGbShort(appsMediaBytes),
                    color = SolarAmber,
                    modifier = Modifier.weight(1f)
                )
                StorageLegendCell(
                    label = "App Cache Pool",
                    value = formatBytes(cacheBytes),
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StorageLegendCell(
                    label = "Temp & Junk",
                    value = formatBytes(junkBytes),
                    color = HyperViolet,
                    modifier = Modifier.weight(1f)
                )
                StorageLegendCell(
                    label = "Free Headroom",
                    value = formatGbShort(storage.freeBytes),
                    color = EmeraldPulse,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StorageLegendCell(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ObsidianCard.copy(alpha = 0.72f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.5.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StorageRocketBoosterStage(
    isBoosting: Boolean,
    boostProgress: Float,
    modeLabel: String,
    lastWriteSpeedMbPerSec: Float,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "storage_rocket_stage")
    val flameFlicker by infinite.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (isBoosting) 300 else 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rocket_flame_flicker"
    )
    val warpShift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (isBoosting) 1800 else 3600, easing = LinearEasing)
        ),
        label = "rocket_warp_shift"
    )

    // Slow, smooth upward ascent animation so the rocket rises steadily instead of snapping quickly
    val animatedFlightFrac by animateFloatAsState(
        targetValue = if (isBoosting) boostProgress.coerceIn(0.04f, 0.96f) else 0.06f,
        animationSpec = tween(
            durationMillis = if (isBoosting) 1100 else 1400,
            easing = FastOutSlowInEasing
        ),
        label = "rocket_vertical_ascent"
    )
    val machSpeed = if (isBoosting) {
        0.8f + (animatedFlightFrac.coerceIn(0f, 1f) * 4.2f)
    } else if (lastWriteSpeedMbPerSec > 0f) {
        (lastWriteSpeedMbPerSec / 120f).coerceIn(1.0f, 6.5f)
    } else {
        0.0f
    }

    Surface(
        color = ObsidianVoid.copy(alpha = 0.88f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.1.dp,
            if (isBoosting) EmeraldPulse.copy(alpha = 0.75f) else SolarAmber.copy(alpha = 0.45f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("storage_rocket_booster_stage")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBoosting) "ROCKET ASCENT • ${modeLabel.uppercase()}" else "ROCKET LAUNCHPAD",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isBoosting) EmeraldPulse else SolarAmber
                )
                Text(
                    text = if (isBoosting) {
                        String.format(Locale.US, "ALT %d%% • MACH %.1f", (animatedFlightFrac * 100).toInt(), machSpeed)
                    } else if (machSpeed > 0f) {
                        String.format(Locale.US, "PEAK MACH %.1f", machSpeed)
                    } else {
                        "LAUNCHPAD READY"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isBoosting) NeonCyan else TextSecondary
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianVoid,
                                ObsidianCard.copy(alpha = 0.9f),
                                RoyalSapphire.copy(alpha = 0.16f)
                            )
                        )
                    )
            ) {
                val h = size.height
                val w = size.width
                val centerX = w / 2f

                // Gentle downward-drifting vertical starfield streaks (creates steady upward flight feel)
                val streakCount = 16
                for (i in 0 until streakCount) {
                    val baseYFrac = ((i * 0.17f + warpShift) % 1f)
                    val startY = baseYFrac * h
                    val streakLen = if (isBoosting) (14.dp.toPx() + (i % 3) * 10.dp.toPx()) else 5.dp.toPx()
                    val xPos = ((i * 47.dp.toPx() + 18.dp.toPx()) % (w - 36.dp.toPx())) + 18.dp.toPx()
                    if (kotlin.math.abs(xPos - centerX) > 22.dp.toPx()) {
                        drawLine(
                            color = if (i % 2 == 0) NeonCyan.copy(alpha = if (isBoosting) 0.52f else 0.22f)
                            else SolarAmber.copy(alpha = if (isBoosting) 0.42f else 0.16f),
                            start = Offset(xPos, startY),
                            end = Offset(xPos, (startY + streakLen).coerceAtMost(h)),
                            strokeWidth = if (isBoosting) 1.7.dp.toPx() else 1.1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Vertical Launch Guidance Rail + Altitude Ticks on left & right flanks
                val railTop = 14.dp.toPx()
                val padY = h - 14.dp.toPx()
                drawLine(
                    color = CyberBorderMuted.copy(alpha = 0.35f),
                    start = Offset(centerX, railTop),
                    end = Offset(centerX, padY),
                    strokeWidth = 1.dp.toPx()
                )
                for (step in 0..4) {
                    val tickY = padY - (padY - railTop) * (step / 4f)
                    drawLine(
                        color = if (step == 4) EmeraldPulse.copy(alpha = 0.55f) else CyberBorderMuted.copy(alpha = 0.55f),
                        start = Offset(14.dp.toPx(), tickY),
                        end = Offset(26.dp.toPx(), tickY),
                        strokeWidth = 1.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = if (step == 4) EmeraldPulse.copy(alpha = 0.55f) else CyberBorderMuted.copy(alpha = 0.55f),
                        start = Offset(w - 26.dp.toPx(), tickY),
                        end = Offset(w - 14.dp.toPx(), tickY),
                        strokeWidth = 1.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Launchpad Base Platform at Bottom
                drawLine(
                    color = SolarAmber.copy(alpha = 0.65f),
                    start = Offset(centerX - 38.dp.toPx(), padY),
                    end = Offset(centerX + 38.dp.toPx(), padY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Rocket Geometry (Pointing Straight UP, ascending slowly from padY toward top)
                val rocketLen = 46.dp.toPx()
                val rocketHalfW = 10.dp.toPx()
                val minNoseY = 16.dp.toPx()
                val maxNoseY = padY - rocketLen - 10.dp.toPx()
                val rocketNoseY = (maxNoseY - (maxNoseY - minNoseY) * animatedFlightFrac)
                    .coerceIn(minNoseY, maxNoseY)
                val rocketTailY = rocketNoseY + rocketLen

                // Soft thrust glow below tail
                val glowAlpha = if (isBoosting) 0.28f else 0.12f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SolarAmber.copy(alpha = glowAlpha),
                            ElectricCrimson.copy(alpha = glowAlpha * 0.6f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, rocketTailY + 12.dp.toPx()),
                        radius = 34.dp.toPx()
                    ),
                    radius = 34.dp.toPx(),
                    center = Offset(centerX, rocketTailY + 12.dp.toPx())
                )

                // Downward Plasma Exhaust Flame Cone
                val maxFlameSpace = (h - rocketTailY - 4.dp.toPx()).coerceAtLeast(8.dp.toPx())
                val flameLen = ((if (isBoosting) 36.dp.toPx() else 12.dp.toPx()) * flameFlicker)
                    .coerceAtMost(maxFlameSpace)
                val outerFlame = Path().apply {
                    moveTo(centerX - rocketHalfW * 0.62f, rocketTailY - 2.dp.toPx())
                    lineTo(centerX, rocketTailY + flameLen)
                    lineTo(centerX + rocketHalfW * 0.62f, rocketTailY - 2.dp.toPx())
                    close()
                }
                drawPath(
                    path = outerFlame,
                    brush = Brush.verticalGradient(
                        colors = listOf(NeonCyan, SolarAmber, ElectricCrimson, Color.Transparent),
                        startY = rocketTailY - 2.dp.toPx(),
                        endY = rocketTailY + flameLen
                    )
                )

                val innerCoreFlame = Path().apply {
                    moveTo(centerX - rocketHalfW * 0.32f, rocketTailY - 2.dp.toPx())
                    lineTo(centerX, rocketTailY + flameLen * 0.56f)
                    lineTo(centerX + rocketHalfW * 0.32f, rocketTailY - 2.dp.toPx())
                    close()
                }
                drawPath(
                    path = innerCoreFlame,
                    color = TextPrimary.copy(alpha = 0.92f)
                )

                if (isBoosting) {
                    for (s in 0..3) {
                        val sparkY = (rocketTailY + flameLen * (0.35f + s * 0.2f)).coerceAtMost(h - 4.dp.toPx())
                        val sparkX = centerX + (if (s % 2 == 0) -1 else 1) * (4.dp.toPx() + s * 2.2.dp.toPx()) * flameFlicker
                        drawCircle(
                            color = if (s % 2 == 0) SolarAmber else NeonCyan,
                            radius = 2.dp.toPx(),
                            center = Offset(sparkX, sparkY)
                        )
                    }
                }

                // Rocket Swept Left & Right Stabilizer Fins
                val leftFin = Path().apply {
                    moveTo(centerX - rocketHalfW * 0.8f, rocketTailY - 12.dp.toPx())
                    lineTo(centerX - rocketHalfW * 1.65f, rocketTailY + 3.dp.toPx())
                    lineTo(centerX - rocketHalfW * 0.55f, rocketTailY - 2.dp.toPx())
                    close()
                }
                val rightFin = Path().apply {
                    moveTo(centerX + rocketHalfW * 0.8f, rocketTailY - 12.dp.toPx())
                    lineTo(centerX + rocketHalfW * 1.65f, rocketTailY + 3.dp.toPx())
                    lineTo(centerX + rocketHalfW * 0.55f, rocketTailY - 2.dp.toPx())
                    close()
                }
                drawPath(leftFin, color = SolarAmber)
                drawPath(rightFin, color = SolarAmber)

                // Sleek Vertical Rocket Fuselage Body (Nose at top, Nozzle at bottom)
                val bodyPath = Path().apply {
                    moveTo(centerX, rocketNoseY)
                    quadraticTo(
                        centerX - rocketHalfW,
                        rocketNoseY + rocketLen * 0.32f,
                        centerX - rocketHalfW * 0.85f,
                        rocketTailY - 5.dp.toPx()
                    )
                    lineTo(centerX - rocketHalfW * 0.55f, rocketTailY)
                    lineTo(centerX + rocketHalfW * 0.55f, rocketTailY)
                    lineTo(centerX + rocketHalfW * 0.85f, rocketTailY - 5.dp.toPx())
                    quadraticTo(
                        centerX + rocketHalfW,
                        rocketNoseY + rocketLen * 0.32f,
                        centerX,
                        rocketNoseY
                    )
                    close()
                }
                drawPath(
                    path = bodyPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(RoyalSapphire, NeonCyan, TextPrimary)
                    )
                )

                // Cockpit Porthole Window
                val windowY = rocketNoseY + rocketLen * 0.42f
                drawCircle(
                    color = ObsidianVoid,
                    radius = 4.6.dp.toPx(),
                    center = Offset(centerX, windowY)
                )
                drawCircle(
                    color = EmeraldPulse,
                    radius = 2.8.dp.toPx(),
                    center = Offset(centerX, windowY)
                )
            }
        }
    }
}

