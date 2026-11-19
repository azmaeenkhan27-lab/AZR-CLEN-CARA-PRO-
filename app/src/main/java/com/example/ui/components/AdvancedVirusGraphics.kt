package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.example.model.SecurityPosture
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.formatBytes
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ZeroVirusDestroyCoreCanvas(
    posture: SecurityPosture,
    isScanning: Boolean,
    isDestroying: Boolean,
    scanProgress: Float,
    modifier: Modifier = Modifier
) {
    val activeBusy = isScanning || isDestroying
    val infinite = rememberInfiniteTransition(label = "virus_destroy_core")
    val radarSweep by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (activeBusy) 1400 else 5600, easing = LinearEasing)
        ),
        label = "virus_radar_sweep"
    )
    val counterSpin by infinite.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (activeBusy) 2200 else 8200, easing = LinearEasing)
        ),
        label = "virus_counter_spin"
    )
    val corePulse by infinite.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (isDestroying) 420 else 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "virus_core_pulse"
    )

    val unneutralizedCount = posture.findings.count {
        it.riskLevel != com.example.model.RiskLevel.SAFE && !it.isNeutralized
    }
    val allClear = posture.lastScanCompleted && unneutralizedCount == 0
    val coreAccent = when {
        isDestroying -> SolarAmber
        isScanning -> ElectricCrimson
        allClear -> EmeraldPulse
        posture.lastScanCompleted -> ElectricCrimson
        else -> NeonCyan
    }

    val animatedScore by animateIntAsState(
        targetValue = posture.threatDefenseScore.coerceIn(0, 100),
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "virus_score_anim"
    )

    Box(
        modifier = modifier
            .size(196.dp)
            .testTag("virus_destroy_core_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreAccent.copy(alpha = 0.24f * corePulse),
                        HyperViolet.copy(alpha = 0.1f * corePulse),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Outer Hex/Radar Ticks
            val tickInner = radius * 0.89f
            val tickOuter = radius * 0.97f
            for (i in 0 until 36) {
                val angleRad = Math.toRadians((i * 10).toDouble())
                val isMajor = i % 3 == 0
                val endR = if (isMajor) tickOuter else tickInner + (tickOuter - tickInner) * 0.5f
                drawLine(
                    color = if (isMajor) coreAccent.copy(alpha = 0.8f) else CyberBorderMuted.copy(alpha = 0.5f),
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

            // Concentric Radar Grid Circles
            drawCircle(
                color = CyberBorderMuted.copy(alpha = 0.45f),
                radius = radius * 0.76f,
                center = center,
                style = Stroke(width = 1.4.dp.toPx())
            )
            drawCircle(
                color = CyberBorderMuted.copy(alpha = 0.35f),
                radius = radius * 0.55f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Rotating Laser Destruction Sweep Arc
            val outerD = radius * 1.52f
            val outerTl = Offset(center.x - outerD / 2f, center.y - outerD / 2f)
            rotate(degrees = radarSweep, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color.Transparent, coreAccent.copy(alpha = 0.4f), coreAccent),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = if (isDestroying) 150f else 110f,
                    useCenter = false,
                    topLeft = outerTl,
                    size = Size(outerD, outerD),
                    style = Stroke(width = 5.5.dp.toPx(), cap = StrokeCap.Round)
                )
                val nodeRad = Math.toRadians(if (isDestroying) 150.0 else 110.0)
                val r = outerD / 2f
                drawCircle(
                    color = coreAccent,
                    radius = 4.dp.toPx(),
                    center = Offset(
                        center.x + (cos(nodeRad) * r).toFloat(),
                        center.y + (sin(nodeRad) * r).toFloat()
                    )
                )
            }

            // Counter-Rotating Shredder Blades
            rotate(degrees = counterSpin, pivot = center) {
                val innerD = radius * 1.12f
                val innerTl = Offset(center.x - innerD / 2f, center.y - innerD / 2f)
                for (k in 0..3) {
                    drawArc(
                        color = if (isDestroying) ElectricCrimson.copy(alpha = 0.85f) else NeonCyan.copy(alpha = 0.55f),
                        startAngle = k * 90f,
                        sweepAngle = 42f,
                        useCenter = false,
                        topLeft = innerTl,
                        size = Size(innerD, innerD),
                        style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Shockwave Blast Ring during Active Destruction / Scanning
            if (activeBusy) {
                val pulseR = radius * (0.72f + 0.22f * corePulse)
                drawCircle(
                    color = coreAccent.copy(alpha = (1.1f - corePulse).coerceIn(0.15f, 0.72f)),
                    radius = pulseR,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = if (isDestroying) Icons.Filled.DeleteForever else Icons.Filled.Shield,
                contentDescription = null,
                tint = coreAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when {
                    isDestroying -> "CRUSH"
                    isScanning -> "${(scanProgress * 100).toInt()}%"
                    !posture.lastScanCompleted -> "HUNT"
                    unneutralizedCount == 0 -> "$animatedScore"
                    else -> "$unneutralizedCount"
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 27.sp
                ),
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = when {
                    isDestroying -> "SHREDDING CORE"
                    isScanning -> "DEEP AUDIT"
                    !posture.lastScanCompleted -> "ALL LAYERS"
                    unneutralizedCount == 0 -> "SHIELD SCORE"
                    else -> "ACTIVE RISKS"
                },
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = coreAccent,
                maxLines = 1
            )
        }
    }
}

@Composable
fun VirusDestructionReportCard(
    posture: SecurityPosture,
    modifier: Modifier = Modifier
) {
    var showDetails by rememberSaveable { mutableStateOf(false) }
    val hasLogs = posture.deletedFileNames.isNotEmpty() || posture.destroyedThreatNames.isNotEmpty()

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.1.dp, EmeraldPulse.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("virus_destruction_report_card")
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
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = hasLogs) { showDetails = !showDetails },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldPulse,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "INTERNAL DESTRUCTION & SHREDDER REPORT",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPulse
                        )
                        Text(
                            text = "${posture.autoDestroyedProcessesCount} threat process(es) crushed • ${posture.shreddedPayloadsCount} shredded • ${formatBytes(posture.autoDeletedBytes)} wiped",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (hasLogs) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Toggle destroyed items log",
                        tint = EmeraldPulse,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(if (showDetails) 180f else 0f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DestructionStatPill("CRUSHED", "${posture.autoDestroyedProcessesCount}", SolarAmber, Modifier.weight(1f))
                DestructionStatPill("SHREDDED", "${posture.shreddedPayloadsCount}", ElectricCrimson, Modifier.weight(1f))
                DestructionStatPill("WIPED", formatBytes(posture.autoDeletedBytes), NeonCyan, Modifier.weight(1f))
                DestructionStatPill("TIME", "${posture.scanDurationMs}ms", EmeraldPulse, Modifier.weight(1f))
            }

            if (showDetails && hasLogs) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianVoid.copy(alpha = 0.78f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    posture.destroyedThreatNames.forEach { appName ->
                        Text(
                            text = "⚡ Neutralized & Stopped: $appName",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                            color = SolarAmber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    posture.deletedFileNames.forEach { fileName ->
                        Text(
                            text = "✓ Shredded & Wiped: $fileName",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                            color = EmeraldPulse,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DestructionStatPill(
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
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextSecondary,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
