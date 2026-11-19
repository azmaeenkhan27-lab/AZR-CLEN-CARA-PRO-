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
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KernelMemoryBreakdown
import com.example.model.RamTelemetry
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
fun RamNeuralReactorCanvas(
    usedPercent: Int,
    totalBytes: Long,
    usedBytes: Long,
    availableBytes: Long,
    thresholdBytes: Long,
    isLowMemory: Boolean,
    isOptimizing: Boolean,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "ram_neural_reactor")
    val primarySweep by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (isOptimizing) 1150 else 4600, easing = LinearEasing)
        ),
        label = "ram_primary_sweep"
    )
    val counterSweep by infinite.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (isOptimizing) 1900 else 7400, easing = LinearEasing)
        ),
        label = "ram_counter_sweep"
    )
    val corePulse by infinite.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ram_core_pulse"
    )

    val animatedPercent by animateIntAsState(
        targetValue = usedPercent.coerceIn(0, 100),
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "ram_percent_counter"
    )
    val animatedUsedFraction by animateFloatAsState(
        targetValue = (usedPercent / 100f).coerceIn(0.05f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "ram_used_fraction"
    )

    val safeTotal = totalBytes.coerceAtLeast(1L).toFloat()
    val thresholdFrac = (thresholdBytes.toFloat() / safeTotal).coerceIn(0.02f, 0.25f)
    val availFrac = (availableBytes.toFloat() / safeTotal).coerceIn(0.02f, 1f)
    val pressureColor = when {
        isLowMemory || usedPercent >= 88 -> ElectricCrimson
        usedPercent >= 70 -> SolarAmber
        else -> HyperViolet
    }
    val statusBadge = when {
        isOptimizing -> "RELEASING…"
        isLowMemory || usedPercent >= 88 -> "HIGH LOAD"
        usedPercent >= 70 -> "MODERATE"
        else -> "OPTIMAL"
    }

    Box(
        modifier = modifier.size(210.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Aurora Violet + Cyan Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        pressureColor.copy(alpha = 0.25f * corePulse),
                        NeonCyan.copy(alpha = 0.12f * corePulse),
                        RoyalSapphire.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 2. Outer Calibration Tick Ring (40 ticks)
            val tickInner = radius * 0.90f
            val tickOuter = radius * 0.97f
            for (i in 0 until 40) {
                val angleRad = Math.toRadians((i * 9).toDouble())
                val isMajor = i % 4 == 0
                val endR = if (isMajor) tickOuter else tickInner + (tickOuter - tickInner) * 0.52f
                drawLine(
                    color = if (isMajor) HyperViolet.copy(alpha = 0.72f) else CyberBorderMuted.copy(alpha = 0.55f),
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

            // 3. Multi-Segment Memory Allocation Arc
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

            val gapDeg = 6f
            val totalSweep = 360f - (gapDeg * 3f)
            val usedRaw = if (usedBytes > 0L) animatedUsedFraction else 0.5f
            val weightSum = (usedRaw + thresholdFrac + availFrac).coerceAtLeast(0.01f)
            val usedSweep = (totalSweep * (usedRaw / weightSum)).coerceAtLeast(18f)
            val reserveSweep = (totalSweep * (thresholdFrac / weightSum)).coerceAtLeast(12f)
            val freeSweep = (totalSweep - usedSweep - reserveSweep).coerceAtLeast(18f)

            var cursorAngle = -90f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(pressureColor, NeonCyan, pressureColor),
                    center = center
                ),
                startAngle = cursorAngle,
                sweepAngle = usedSweep,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            cursorAngle += usedSweep + gapDeg

            drawArc(
                color = SolarAmber,
                startAngle = cursorAngle,
                sweepAngle = reserveSweep,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            cursorAngle += reserveSweep + gapDeg

            drawArc(
                color = EmeraldPulse,
                startAngle = cursorAngle,
                sweepAngle = freeSweep,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // 4. Inner Rotating Neural Pulse Ring & Orbiting Node
            val innerD = radius * 1.24f
            val innerTl = Offset(center.x - innerD / 2f, center.y - innerD / 2f)
            rotate(degrees = primarySweep, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color.Transparent, HyperViolet.copy(alpha = 0.6f), NeonCyan),
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
                    color = NeonCyan,
                    radius = 4.2.dp.toPx(),
                    center = Offset(
                        center.x + (cos(orbAngle) * orbR).toFloat(),
                        center.y + (sin(orbAngle) * orbR).toFloat()
                    )
                )
            }

            // 5. Counter-Rotating Sub-Arcs
            rotate(degrees = counterSweep, pivot = center) {
                val subD = radius * 1.03f
                val subTl = Offset(center.x - subD / 2f, center.y - subD / 2f)
                for (k in 0..2) {
                    drawArc(
                        color = EmeraldPulse.copy(alpha = 0.55f),
                        startAngle = k * 120f,
                        sweepAngle = 44f,
                        useCenter = false,
                        topLeft = subTl,
                        size = Size(subD, subD),
                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Center Telemetry Core Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 18.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Memory,
                contentDescription = null,
                tint = HyperViolet,
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
                text = formatGbShort(usedBytes),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = NeonCyan,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = pressureColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, pressureColor.copy(alpha = 0.55f))
            ) {
                Text(
                    text = statusBadge,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                    color = pressureColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun RamMemoryAllocationPanel(
    ram: RamTelemetry,
    modifier: Modifier = Modifier
) {
    val safeTotal = ram.totalBytes.coerceAtLeast(1L).toFloat()
    val usedWeight = (ram.usedBytes.toFloat() / safeTotal).coerceAtLeast(0.05f)
    val thresholdWeight = (ram.lowMemoryThresholdBytes.toFloat() / safeTotal).coerceIn(0.03f, 0.25f)
    val freeWeight = (ram.availableBytes.toFloat() / safeTotal).coerceAtLeast(0.05f)
    val pctDrop = (ram.lastDropBeforePercent - ram.lastDropAfterPercent).coerceAtLeast(0)

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
                    text = "LIVE RAM ALLOCATION MATRIX",
                    style = MaterialTheme.typography.labelSmall,
                    color = HyperViolet
                )
                Text(
                    text = "${formatGbShort(ram.usedBytes)} / ${formatGbShort(ram.totalBytes)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan
                )
            }

            if (ram.lastDropBytes > 0L || ram.lastStoppedProcessPssBytes > 0L || pctDrop > 0) {
                Surface(
                    color = EmeraldPulse.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.9.dp, EmeraldPulse.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ram_drop_delta_banner")
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
                                text = if (pctDrop > 0) {
                                    "RAM Reduced: ${ram.lastDropBeforePercent}% → ${ram.lastDropAfterPercent}% (-$pctDrop%)"
                                } else {
                                    "Process Memory Released"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = EmeraldPulse
                            )
                        }
                        Text(
                            text = "-${formatBytes(maxOf(ram.lastDropBytes, ram.lastStoppedProcessPssBytes))}",
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
                        .weight(usedWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(HyperViolet)
                )
                Box(
                    modifier = Modifier
                        .weight(thresholdWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(SolarAmber)
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
                RamLegendCell(
                    label = "Active Used",
                    value = formatGbShort(ram.usedBytes),
                    color = HyperViolet,
                    modifier = Modifier.weight(1f)
                )
                RamLegendCell(
                    label = "Free Headroom",
                    value = formatGbShort(ram.availableBytes),
                    color = EmeraldPulse,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RamLegendCell(
                    label = "Low-RAM Limit",
                    value = if (ram.lowMemoryThresholdBytes > 0L) formatBytes(ram.lowMemoryThresholdBytes) else "Auto",
                    color = SolarAmber,
                    modifier = Modifier.weight(1f)
                )
                RamLegendCell(
                    label = "Total Reclaimed",
                    value = formatBytes(ram.totalReclaimedBytes),
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun KernelZramTelemetryCard(
    kernel: KernelMemoryBreakdown,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.1.dp, NeonCyan.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("kernel_zram_card")
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
                        imageVector = Icons.Filled.DeveloperBoard,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "KERNEL PAGE CACHE & ZRAM ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan
                    )
                }
                Text(
                    text = if (kernel.ownAppPssBytes > 0L) "AZR: ${formatBytes(kernel.ownAppPssBytes)}" else "Live /proc",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = SolarAmber
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RamLegendCell(
                    label = "Kernel Free",
                    value = formatBytes(kernel.memFreeBytes),
                    color = EmeraldPulse,
                    modifier = Modifier.weight(1f)
                )
                RamLegendCell(
                    label = "Page Cache",
                    value = formatBytes(kernel.pageCacheBytes + kernel.buffersBytes),
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            if (kernel.swapTotalBytes > 0L) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ZRAM / Compressed Swap (${kernel.swapUsedPercent}%)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                            color = TextSecondary
                        )
                        Text(
                            text = "${formatBytes(kernel.swapUsedBytes)} / ${formatBytes(kernel.swapTotalBytes)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = HyperViolet
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (kernel.swapUsedPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color = HyperViolet,
                        trackColor = CyberBorderMuted.copy(alpha = 0.45f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun RamLegendCell(
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
