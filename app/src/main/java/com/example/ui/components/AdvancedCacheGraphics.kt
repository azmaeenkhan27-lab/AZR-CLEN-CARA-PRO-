package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CleaningServices
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CacheAreaBreakdown
import com.example.ui.theme.CyberBorderMuted
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CacheReactorHeroCanvas(
    totalCacheBytes: Long,
    removedBytes: Long,
    breakdown: CacheAreaBreakdown,
    isScanningOrCleaning: Boolean,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "advanced_cache_reactor")
    val sweepRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (isScanningOrCleaning) 1400 else 5000, easing = LinearEasing)
        ),
        label = "radar_sweep"
    )
    val counterRotation by infinite.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (isScanningOrCleaning) 2400 else 7800, easing = LinearEasing)
        ),
        label = "counter_orbit"
    )
    val corePulse by infinite.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (isScanningOrCleaning) 650 else 1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    val totalArea = (breakdown.totalAreaBytes + removedBytes).coerceAtLeast(1L)
    val hasLiveBreakdown = breakdown.totalAreaBytes > 0L || removedBytes > 0L

    Box(
        modifier = modifier.size(218.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Deep Multi-Spectral Quantum Bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.28f * corePulse),
                        RoyalSapphire.copy(alpha = 0.16f * corePulse),
                        HyperViolet.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Active shockwave ring during sweeping/auto-cleaning
            if (isScanningOrCleaning) {
                drawCircle(
                    color = NeonCyan.copy(alpha = (1.1f - corePulse).coerceIn(0.18f, 0.75f)),
                    radius = radius * (0.76f + 0.20f * corePulse),
                    center = center,
                    style = Stroke(width = 2.4.dp.toPx())
                )
            }

            // 2. High-Precision 60-Tick Calibration HUD Ring
            val tickInner = radius * 0.89f
            val tickOuter = radius * 0.97f
            for (i in 0 until 60) {
                val angleRad = Math.toRadians((i * 6).toDouble())
                val isMajor = i % 5 == 0
                val isCardinal = i % 15 == 0
                val endR = when {
                    isCardinal -> tickOuter
                    isMajor -> tickInner + (tickOuter - tickInner) * 0.78f
                    else -> tickInner + (tickOuter - tickInner) * 0.42f
                }
                drawLine(
                    color = when {
                        isCardinal -> SolarAmber.copy(alpha = 0.9f)
                        isMajor -> NeonCyan.copy(alpha = 0.7f)
                        else -> CyberBorderMuted.copy(alpha = 0.48f)
                    },
                    start = Offset(
                        center.x + (cos(angleRad) * tickInner).toFloat(),
                        center.y + (sin(angleRad) * tickInner).toFloat()
                    ),
                    end = Offset(
                        center.x + (cos(angleRad) * endR).toFloat(),
                        center.y + (sin(angleRad) * endR).toFloat()
                    ),
                    strokeWidth = if (isCardinal) 2.6.dp.toPx() else if (isMajor) 1.8.dp.toPx() else 0.9.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 3. Multi-Segment Cache Area Ring + Soft Neon Bloom Underlay
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

            if (hasLiveBreakdown) {
                val segments = listOf(
                    breakdown.mediaSocialBytes to NeonCyan,
                    breakdown.browserToolsBytes to SolarAmber,
                    breakdown.otherAppsBytes to HyperViolet,
                    (breakdown.localBufferBytes + removedBytes) to EmeraldPulse
                ).filter { it.first > 0L }

                var startDeg = -90f
                val gapDeg = if (segments.size > 1) 6f else 0f
                val totalSweep = 360f - (gapDeg * segments.size)

                for ((bytes, color) in segments) {
                    val frac = (bytes.toFloat() / totalArea.toFloat()).coerceIn(0.05f, 1f)
                    val sweep = (totalSweep * frac).coerceAtLeast(10f)
                    // Soft glow bloom arc behind segment
                    drawArc(
                        color = color.copy(alpha = 0.24f * corePulse),
                        startAngle = startDeg,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = ringTopLeft,
                        size = ringSize,
                        style = Stroke(width = strokeW * 1.75f, cap = StrokeCap.Round)
                    )
                    // Sharp saturated foreground segment
                    drawArc(
                        color = color,
                        startAngle = startDeg,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = ringTopLeft,
                        size = ringSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                    startDeg += sweep + gapDeg
                }
            } else {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.28f), NeonCyan, SolarAmber, NeonCyan.copy(alpha = 0.28f)),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = 260f,
                    useCenter = false,
                    topLeft = ringTopLeft,
                    size = ringSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            // 4. Inner Rotating Sonar Beam + Dual Orbiting Plasma Nodes
            val innerD = radius * 1.22f
            val innerTl = Offset(center.x - innerD / 2f, center.y - innerD / 2f)
            rotate(degrees = sweepRotation, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color.Transparent, NeonCyan.copy(alpha = 0.58f), Color.White.copy(alpha = 0.9f)),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 125f,
                    useCenter = false,
                    topLeft = innerTl,
                    size = Size(innerD, innerD),
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )
                val orbAngle = Math.toRadians(125.0)
                val orbR = innerD / 2f
                val orbCenter = Offset(
                    center.x + (cos(orbAngle) * orbR).toFloat(),
                    center.y + (sin(orbAngle) * orbR).toFloat()
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.42f * corePulse),
                    radius = 7.dp.toPx(),
                    center = orbCenter
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = orbCenter
                )
            }

            rotate(degrees = counterRotation, pivot = center) {
                val subD = radius * 1.02f
                val subTl = Offset(center.x - subD / 2f, center.y - subD / 2f)
                for (k in 0..2) {
                    drawArc(
                        color = SolarAmber.copy(alpha = 0.65f),
                        startAngle = k * 120f,
                        sweepAngle = 52f,
                        useCenter = false,
                        topLeft = subTl,
                        size = Size(subD, subD),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Center Telemetry Core Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 22.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CleaningServices,
                contentDescription = null,
                tint = if (isScanningOrCleaning) SolarAmber else NeonCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = formatBytes(totalCacheBytes),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = if (isScanningOrCleaning) "QUANTUM SWEEP" else "TOTAL CACHE AREA",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, letterSpacing = 1.1.sp),
                color = if (isScanningOrCleaning) SolarAmber else NeonCyan,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CacheAreaDistributionPanel(
    breakdown: CacheAreaBreakdown,
    removedBytes: Long,
    modifier: Modifier = Modifier
) {
    val totalArea = (breakdown.totalAreaBytes + removedBytes).coerceAtLeast(1L)
    val mediaWeight = (breakdown.mediaSocialBytes.toFloat() / totalArea.toFloat()).coerceAtLeast(0.02f)
    val browserWeight = (breakdown.browserToolsBytes.toFloat() / totalArea.toFloat()).coerceAtLeast(0.02f)
    val otherWeight = (breakdown.otherAppsBytes.toFloat() / totalArea.toFloat()).coerceAtLeast(0.02f)
    val removedWeight = ((breakdown.localBufferBytes + removedBytes).toFloat() / totalArea.toFloat()).coerceAtLeast(0.02f)

    Surface(
        color = ObsidianVoid.copy(alpha = 0.78f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderMuted),
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
                    text = "TOTAL CACHE AREA BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )
                Text(
                    text = formatBytes(breakdown.totalAreaBytes),
                    style = MaterialTheme.typography.labelLarge,
                    color = SolarAmber
                )
            }

            // Segmented Multi-Color Cache Area Bar
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
                        .weight(mediaWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(NeonCyan)
                )
                Box(
                    modifier = Modifier
                        .weight(browserWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(SolarAmber)
                )
                Box(
                    modifier = Modifier
                        .weight(otherWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(HyperViolet)
                )
                Box(
                    modifier = Modifier
                        .weight(removedWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(EmeraldPulse)
                )
            }

            // 2x2 Area Legend Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CacheAreaLegendItem(
                    label = "Media & Social",
                    value = formatBytes(breakdown.mediaSocialBytes),
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                CacheAreaLegendItem(
                    label = "Browser & Files",
                    value = formatBytes(breakdown.browserToolsBytes),
                    color = SolarAmber,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CacheAreaLegendItem(
                    label = "Other Apps Cache",
                    value = formatBytes(breakdown.otherAppsBytes),
                    color = HyperViolet,
                    modifier = Modifier.weight(1f)
                )
                CacheAreaLegendItem(
                    label = "Removed Total",
                    value = formatBytes(removedBytes),
                    color = EmeraldPulse,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CacheAreaLegendItem(
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
