package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CleanerModule
import com.example.ui.theme.CyberBevelHighlight
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianGlassBottom
import com.example.ui.theme.ObsidianGlassTop
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.RoyalSapphire
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun moduleAccentColor(module: CleanerModule): Color = when (module) {
    CleanerModule.CACHE -> NeonCyan
    CleanerModule.RAM -> HyperViolet
    CleanerModule.STORAGE -> SolarAmber
    CleanerModule.VIRUS -> ElectricCrimson
    CleanerModule.BATTERY -> EmeraldPulse
}

fun moduleIcon(module: CleanerModule): ImageVector = when (module) {
    CleanerModule.CACHE -> Icons.Filled.CleaningServices
    CleanerModule.RAM -> Icons.Filled.Memory
    CleanerModule.STORAGE -> Icons.Filled.RocketLaunch
    CleanerModule.VIRUS -> Icons.Filled.Security
    CleanerModule.BATTERY -> Icons.Filled.BatteryChargingFull
}

@Composable
fun CyberBackdrop(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "cyber_backdrop_fx")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 8200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aurora_drift"
    )
    val twinkle by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_twinkle"
    )
    val radarSweep by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 22000, easing = LinearEasing)
        ),
        label = "bg_radar"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Multi-stop deep obsidian space gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(ObsidianVoid, ObsidianSurface, ObsidianGlassBottom)
            )
        )

        // Subtle volumetric diagonal holographic light beam
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    RoyalSapphire.copy(alpha = 0.08f),
                    NeonCyan.copy(alpha = 0.045f),
                    Color.Transparent
                ),
                start = Offset(0f, size.height * (0.15f + 0.08f * drift)),
                end = Offset(size.width, size.height * (0.65f + 0.08f * drift))
            )
        )

        // Subtle high-tech HUD perspective grid lines
        val gridSpacing = 40.dp.toPx()
        val cols = (size.width / gridSpacing).toInt()
        val rows = (size.height / gridSpacing).toInt()
        for (c in 1..cols) {
            val x = c * gridSpacing
            drawLine(
                color = CyberBorderMuted.copy(alpha = if (c % 2 == 0) 0.11f else 0.06f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 0.7.dp.toPx()
            )
        }
        for (r in 1..rows) {
            val y = r * gridSpacing
            drawLine(
                color = CyberBorderMuted.copy(alpha = if (r % 2 == 0) 0.10f else 0.055f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.7.dp.toPx()
            )
        }

        // Faint precision orbital radar rings in backdrop
        val radarCenter = Offset(size.width * 0.5f, size.height * 0.34f)
        for (ringIdx in 1..3) {
            val ringR = size.minDimension * (0.28f * ringIdx)
            drawCircle(
                color = NeonCyan.copy(alpha = 0.055f / ringIdx),
                radius = ringR,
                center = radarCenter,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        rotate(degrees = radarSweep, pivot = radarCenter) {
            val sweepR = size.minDimension * 0.84f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(Color.Transparent, NeonCyan.copy(alpha = 0.07f), Color.Transparent),
                    center = radarCenter
                ),
                startAngle = 0f,
                sweepAngle = 72f,
                useCenter = true,
                topLeft = Offset(radarCenter.x - sweepR, radarCenter.y - sweepR),
                size = Size(sweepR * 2f, sweepR * 2f)
            )
        }

        // Animated Volumetric Aurora Glow Nebulas
        val topOrb = Offset(size.width * (0.16f + 0.10f * drift), size.height * (0.10f + 0.05f * drift))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(RoyalSapphire.copy(alpha = 0.30f), NeonCyan.copy(alpha = 0.10f), Color.Transparent),
                center = topOrb,
                radius = size.width * 0.85f
            ),
            radius = size.width * 0.85f,
            center = topOrb
        )
        val midOrb = Offset(size.width * (0.86f - 0.08f * drift), size.height * (0.58f - 0.05f * drift))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(HyperViolet.copy(alpha = 0.21f), ElectricIndigo.copy(alpha = 0.09f), Color.Transparent),
                center = midOrb,
                radius = size.width * 0.78f
            ),
            radius = size.width * 0.78f,
            center = midOrb
        )
        val botOrb = Offset(size.width * 0.48f, size.height * 0.92f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SolarAmber.copy(alpha = 0.12f), EmeraldPulse.copy(alpha = 0.05f), Color.Transparent),
                center = botOrb,
                radius = size.width * 0.68f
            ),
            radius = size.width * 0.68f,
            center = botOrb
        )

        // Luminous Telemetry Starfield Particles with Starburst Glint
        val particleNodes = listOf(
            Offset(0.12f, 0.18f) to NeonCyan,
            Offset(0.86f, 0.15f) to SolarAmber,
            Offset(0.24f, 0.44f) to HyperViolet,
            Offset(0.78f, 0.39f) to NeonCyan,
            Offset(0.15f, 0.74f) to EmeraldPulse,
            Offset(0.89f, 0.78f) to SolarAmber,
            Offset(0.52f, 0.26f) to ElectricCrimson,
            Offset(0.45f, 0.86f) to NeonCyan
        )
        particleNodes.forEachIndexed { idx, (pos, color) ->
            val phaseAlpha = if (idx % 2 == 0) twinkle else (1.35f - twinkle).coerceIn(0.3f, 1f)
            val center = Offset(size.width * pos.x, size.height * (pos.y + (drift - 0.5f) * 0.025f))
            drawCircle(
                color = color.copy(alpha = 0.32f * phaseAlpha),
                radius = 5.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.80f * phaseAlpha),
                radius = 1.4.dp.toPx(),
                center = center
            )
            if (idx % 2 == 0) {
                val glint = 5.5.dp.toPx() * phaseAlpha
                drawLine(
                    color = color.copy(alpha = 0.45f * phaseAlpha),
                    start = Offset(center.x - glint, center.y),
                    end = Offset(center.x + glint, center.y),
                    strokeWidth = 0.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color.copy(alpha = 0.45f * phaseAlpha),
                    start = Offset(center.x, center.y - glint),
                    end = Offset(center.x, center.y + glint),
                    strokeWidth = 0.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun CyberGlassCard(
    accent: Color,
    modifier: Modifier = Modifier,
    secondaryAccent: Color = HyperViolet,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ObsidianGlassTop.copy(alpha = 0.90f),
                        ObsidianCard,
                        ObsidianGlassBottom.copy(alpha = 0.96f)
                    )
                )
            )
            .border(
                width = 1.4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.88f),
                        CyberBorderMuted.copy(alpha = 0.55f),
                        secondaryAccent.copy(alpha = 0.62f)
                    )
                ),
                shape = shape
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Top glass specular catch line
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.24f),
                        accent.copy(alpha = 0.45f),
                        Color.Transparent
                    )
                ),
                start = Offset(size.width * 0.08f, 1.5.dp.toPx()),
                end = Offset(size.width * 0.92f, 1.5.dp.toPx()),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Soft corner ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.13f), Color.Transparent),
                    center = Offset(size.width * 0.5f, 0f),
                    radius = size.width * 0.65f
                ),
                radius = size.width * 0.65f,
                center = Offset(size.width * 0.5f, 0f)
            )
        }
        content()
    }
}

@Composable
fun AzrCyberEmblem(
    size: Dp,
    modifier: Modifier = Modifier,
    burstProgress: Float = 0f
) {
    val infinite = rememberInfiniteTransition(label = "azr_dustbin_anim")
    val spinCw by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6400, easing = LinearEasing)),
        label = "spin_cw"
    )
    val spinCcw by infinite.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(4800, easing = LinearEasing)),
        label = "spin_ccw"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = this.size.minDimension / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Aurora Sapphire + Gold ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.34f * pulse),
                        SolarAmber.copy(alpha = 0.22f * pulse),
                        HyperViolet.copy(alpha = 0.14f * pulse),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Prism burst shockwave rings during launch animation
            if (burstProgress > 0.01f) {
                val waveRadius = radius * (0.68f + burstProgress * 0.85f)
                drawCircle(
                    color = SolarAmber.copy(alpha = (1f - burstProgress) * 0.92f),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = (1f - burstProgress) * 0.80f),
                    radius = waveRadius * 0.86f,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = HyperViolet.copy(alpha = (1f - burstProgress) * 0.65f),
                    radius = waveRadius * 0.72f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Outer Turquoise + Gold sweeping orbital arc
            rotate(degrees = spinCw, pivot = center) {
                val arcRect = Size(radius * 1.84f, radius * 1.84f)
                val topLeft = Offset(center.x - arcRect.width / 2f, center.y - arcRect.height / 2f)
                drawArc(
                    color = SolarAmber.copy(alpha = 0.9f),
                    startAngle = 0f,
                    sweepAngle = 78f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcRect,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = NeonCyan.copy(alpha = 0.85f),
                    startAngle = 180f,
                    sweepAngle = 78f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcRect,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Inner Aurora Violet counter-rotating ring
            rotate(degrees = spinCcw, pivot = center) {
                val arcRect = Size(radius * 1.66f, radius * 1.66f)
                val topLeft = Offset(center.x - arcRect.width / 2f, center.y - arcRect.height / 2f)
                drawArc(
                    color = HyperViolet.copy(alpha = 0.85f),
                    startAngle = 60f,
                    sweepAngle = 80f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcRect,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = EmeraldPulse.copy(alpha = 0.7f),
                    startAngle = 240f,
                    sweepAngle = 80f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcRect,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Generated 3D Metallic Sapphire & Gold AZR Dustbin Logo
        Box(
            modifier = Modifier
                .fillMaxSize(0.76f)
                .clip(RoundedCornerShape(24))
                .border(
                    width = 1.6.dp,
                    brush = Brush.linearGradient(listOf(SolarAmber, NeonCyan, HyperViolet)),
                    shape = RoundedCornerShape(24)
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.azr_dustbin_icon_1794548659278),
                contentDescription = stringResource(R.string.azr_logo_cd),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ModuleArtworkCanvas(
    module: CleanerModule,
    accent: Color,
    progressRatio: Float,
    centerValue: String,
    centerUnit: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val infinite = rememberInfiniteTransition(label = "module_art_${module.name}")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6500, easing = LinearEasing)),
        label = "rot"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val wavePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "wave"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            val c = Offset(size.width / 2f, size.height / 2f)
            val ringStroke = if (compact) 6.dp.toPx() else 9.5.dp.toPx()
            val detailStroke = if (compact) 1.8.dp.toPx() else 2.5.dp.toPx()

            // Outer Dual-Tone Atmospheric Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.34f * pulse),
                        HyperViolet.copy(alpha = 0.11f * pulse),
                        Color.Transparent
                    ),
                    center = c,
                    radius = r
                ),
                radius = r,
                center = c
            )

            // Recessed obsidian glass core behind telemetry text
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ObsidianVoid.copy(alpha = 0.85f), ObsidianCard.copy(alpha = 0.65f)),
                    center = c,
                    radius = r * 0.64f
                ),
                radius = r * 0.64f,
                center = c
            )

            // Precision 24-tick perimeter bezel
            val tickInner = r * 0.90f
            val tickOuter = r * 0.98f
            for (i in 0 until 24) {
                val aRad = Math.toRadians((i * 15).toDouble())
                val isCardinal = i % 6 == 0
                val endR = if (isCardinal) tickOuter else tickInner + (tickOuter - tickInner) * 0.55f
                drawLine(
                    color = if (isCardinal) accent.copy(alpha = 0.85f) else CyberBorderMuted.copy(alpha = 0.45f),
                    start = Offset(c.x + (cos(aRad) * tickInner).toFloat(), c.y + (sin(aRad) * tickInner).toFloat()),
                    end = Offset(c.x + (cos(aRad) * endR).toFloat(), c.y + (sin(aRad) * endR).toFloat()),
                    strokeWidth = if (isCardinal) 1.6.dp.toPx() else 0.9.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            val ringDiameter = r * 1.66f
            val ringTopLeft = Offset(c.x - ringDiameter / 2f, c.y - ringDiameter / 2f)
            val ringSize = Size(ringDiameter, ringDiameter)

            // Precision track ring
            drawArc(
                color = CyberBorderMuted.copy(alpha = 0.58f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringSize,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )

            // Soft outer bloom behind progress arc
            val sweep = (progressRatio.coerceIn(0.08f, 1f)) * 360f
            drawArc(
                color = accent.copy(alpha = 0.24f * pulse),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringSize,
                style = Stroke(width = ringStroke * 1.85f, cap = StrokeCap.Round)
            )

            // Foreground luminous gradient arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(accent.copy(alpha = 0.45f), accent, Color.White.copy(alpha = 0.94f), accent),
                    center = c
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringSize,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )

            // Glowing leading endpoint bead on the progress arc
            val tipRad = Math.toRadians((-90f + sweep).toDouble())
            val arcR = ringDiameter / 2f
            val tipOffset = Offset(
                c.x + (cos(tipRad) * arcR).toFloat(),
                c.y + (sin(tipRad) * arcR).toFloat()
            )
            drawCircle(
                color = accent.copy(alpha = 0.45f * pulse),
                radius = ringStroke * 0.95f,
                center = tipOffset
            )
            drawCircle(
                color = Color.White,
                radius = ringStroke * 0.36f,
                center = tipOffset
            )

            when (module) {
                CleanerModule.CACHE -> {
                    rotate(rotation, c) {
                        val innerD = r * 1.28f
                        val tl = Offset(c.x - innerD / 2f, c.y - innerD / 2f)
                        for (k in 0..2) {
                            drawArc(
                                color = accent.copy(alpha = 0.78f),
                                startAngle = k * 120f,
                                sweepAngle = 64f,
                                useCenter = false,
                                topLeft = tl,
                                size = Size(innerD, innerD),
                                style = Stroke(width = detailStroke, cap = StrokeCap.Round)
                            )
                        }
                    }
                    rotate(-rotation * 0.65f, c) {
                        val subD = r * 1.06f
                        val tl = Offset(c.x - subD / 2f, c.y - subD / 2f)
                        for (k in 0..1) {
                            drawArc(
                                color = SolarAmber.copy(alpha = 0.55f),
                                startAngle = k * 180f + 20f,
                                sweepAngle = 55f,
                                useCenter = false,
                                topLeft = tl,
                                size = Size(subD, subD),
                                style = Stroke(width = detailStroke * 0.8f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                CleanerModule.RAM -> {
                    rotate(-rotation * 0.5f, c) {
                        for (i in 0 until 12) {
                            val angleRad = Math.toRadians((i * 30).toDouble())
                            val inner = r * 0.60f
                            val outer = r * 0.72f
                            drawLine(
                                color = if (i % 3 == 0) accent else accent.copy(alpha = 0.42f),
                                start = Offset(
                                    c.x + (cos(angleRad) * inner).toFloat(),
                                    c.y + (sin(angleRad) * inner).toFloat()
                                ),
                                end = Offset(
                                    c.x + (cos(angleRad) * outer).toFloat(),
                                    c.y + (sin(angleRad) * outer).toFloat()
                                ),
                                strokeWidth = detailStroke,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                CleanerModule.STORAGE -> {
                    val thrusterR = r * 0.66f * pulse
                    drawCircle(
                        color = accent.copy(alpha = 0.36f),
                        radius = thrusterR,
                        center = c,
                        style = Stroke(width = detailStroke)
                    )
                    rotate(rotation * 0.8f, c) {
                        val d = r * 1.30f
                        drawArc(
                            color = accent.copy(alpha = 0.85f),
                            startAngle = 200f,
                            sweepAngle = 140f,
                            useCenter = false,
                            topLeft = Offset(c.x - d / 2f, c.y - d / 2f),
                            size = Size(d, d),
                            style = Stroke(width = detailStroke, cap = StrokeCap.Round)
                        )
                    }
                }

                CleanerModule.VIRUS -> {
                    val hexR = r * 0.68f
                    val hexPath = Path().apply {
                        for (i in 0 until 6) {
                            val ang = Math.toRadians((i * 60 - 90).toDouble())
                            val x = c.x + (cos(ang) * hexR).toFloat()
                            val y = c.y + (sin(ang) * hexR).toFloat()
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        close()
                    }
                    drawPath(
                        path = hexPath,
                        color = accent.copy(alpha = 0.75f),
                        style = Stroke(width = detailStroke)
                    )
                    rotate(rotation * 1.3f, c) {
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.Transparent, accent.copy(alpha = 0.95f)),
                                start = c,
                                end = Offset(c.x, c.y - r * 0.72f)
                            ),
                            start = c,
                            end = Offset(c.x, c.y - r * 0.72f),
                            strokeWidth = detailStroke,
                            cap = StrokeCap.Round
                        )
                    }
                }

                CleanerModule.BATTERY -> {
                    val waveWidth = r * 1.15f
                    val wavePath = Path()
                    val steps = 24
                    for (i in 0..steps) {
                        val frac = i / steps.toFloat()
                        val x = c.x - waveWidth / 2f + frac * waveWidth
                        val y = c.y + r * 0.34f + sin(frac * 4f * PI + wavePhase).toFloat() * (if (compact) 3.dp.toPx() else 5.dp.toPx())
                        if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                    }
                    drawPath(
                        path = wavePath,
                        color = accent.copy(alpha = 0.85f),
                        style = Stroke(width = detailStroke, cap = StrokeCap.Round)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = moduleIcon(module),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(if (compact) 18.dp else 22.dp)
            )
            Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))
            Text(
                text = centerValue,
                style = if (compact) {
                    MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                color = TextPrimary,
                maxLines = 1
            )
            if (!compact && centerUnit.isNotEmpty()) {
                Text(
                    text = centerUnit,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SquareModuleSlideCard(
    module: CleanerModule,
    metricValue: String,
    metricUnit: String,
    statusLine: String,
    progressRatio: Float,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tileSize: Dp? = null,
    secondaryStatusLine: String? = null,
    secondaryStatusColor: Color = EmeraldPulse
) {
    val accent = moduleAccentColor(module)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_press_${module.name}"
    )

    val sizeModifier = if (tileSize != null) {
        modifier.size(tileSize)
    } else {
        modifier.fillMaxWidth().aspectRatio(1f)
    }

    Box(
        modifier = sizeModifier
            .scale(pressScale)
            .clip(RoundedCornerShape(26.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ObsidianGlassTop.copy(alpha = 0.92f),
                        ObsidianCard,
                        ObsidianGlassBottom
                    )
                )
            )
            .border(
                width = 1.6.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.95f),
                        CyberBorderMuted.copy(alpha = 0.50f),
                        SolarAmber.copy(alpha = 0.55f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = accent),
                onClick = onClick
            )
            .testTag("slide_${module.name.lowercase()}")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Subtle inner holographic glass aura + upper specular sheen
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.18f), HyperViolet.copy(alpha = 0.05f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.42f),
                    radius = size.minDimension * 0.62f
                ),
                radius = size.minDimension * 0.62f,
                center = Offset(size.width * 0.5f, size.height * 0.42f)
            )

            // Top specular highlight rim
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, CyberBevelHighlight, accent.copy(alpha = 0.6f), Color.Transparent)
                ),
                start = Offset(size.width * 0.14f, 1.5.dp.toPx()),
                end = Offset(size.width * 0.86f, 1.5.dp.toPx()),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )

            val bracketLen = 15.dp.toPx()
            val inset = 9.dp.toPx()
            val strokeW = 1.8.dp.toPx()
            val bracketColor = accent.copy(alpha = 0.92f)

            drawLine(bracketColor, Offset(inset, inset + bracketLen), Offset(inset, inset), strokeW)
            drawLine(bracketColor, Offset(inset, inset), Offset(inset + bracketLen, inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset - bracketLen, inset), Offset(size.width - inset, inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset, inset), Offset(size.width - inset, inset + bracketLen), strokeW)
            drawLine(bracketColor, Offset(inset, size.height - inset - bracketLen), Offset(inset, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(inset, size.height - inset), Offset(inset + bracketLen, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset - bracketLen, size.height - inset), Offset(size.width - inset, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset, size.height - inset - bracketLen), Offset(size.width - inset, size.height - inset), strokeW)

            // Precision corner vertex dots
            val dotR = 2.dp.toPx()
            drawCircle(accent, dotR, Offset(inset, inset))
            drawCircle(accent, dotR, Offset(size.width - inset, inset))
            drawCircle(accent, dotR, Offset(inset, size.height - inset))
            drawCircle(accent, dotR, Offset(size.width - inset, size.height - inset))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = accent.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.9.sp),
                        color = accent,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                }
            }

            ModuleArtworkCanvas(
                module = module,
                accent = accent,
                progressRatio = progressRatio,
                centerValue = metricValue,
                centerUnit = metricUnit,
                compact = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 3.dp)
                    .aspectRatio(1f)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = statusLine,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (secondaryStatusLine != null) {
                    Text(
                        text = secondaryStatusLine,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.2.sp
                        ),
                        color = secondaryStatusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                // Micro telemetry gauge bar at bottom of card
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(CyberBorderMuted.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressRatio.coerceIn(0.1f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(listOf(accent.copy(alpha = 0.65f), accent))
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun WideVirusShieldCard(
    metricValue: String,
    statusLine: String,
    progressRatio: Float,
    onClick: () -> Unit,
    cardHeight: Dp,
    modifier: Modifier = Modifier
) {
    val accent = ElectricCrimson
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "wide_virus_press"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .scale(pressScale)
            .clip(RoundedCornerShape(26.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        ObsidianGlassTop,
                        ObsidianCard,
                        ObsidianGlassBottom
                    )
                )
            )
            .border(
                width = 1.6.dp,
                brush = Brush.linearGradient(
                    colors = listOf(accent.copy(alpha = 0.95f), HyperViolet.copy(alpha = 0.68f), SolarAmber.copy(alpha = 0.58f))
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = accent),
                onClick = onClick
            )
            .testTag("slide_virus")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.18f), HyperViolet.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * 0.22f, size.height * 0.5f),
                    radius = size.height * 1.05f
                ),
                radius = size.height * 1.05f,
                center = Offset(size.width * 0.22f, size.height * 0.5f)
            )

            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, CyberBevelHighlight, accent.copy(alpha = 0.55f), Color.Transparent)
                ),
                start = Offset(size.width * 0.08f, 1.5.dp.toPx()),
                end = Offset(size.width * 0.92f, 1.5.dp.toPx()),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )

            val bracketLen = 15.dp.toPx()
            val inset = 9.dp.toPx()
            val strokeW = 1.8.dp.toPx()
            val bracketColor = accent.copy(alpha = 0.92f)

            drawLine(bracketColor, Offset(inset, inset + bracketLen), Offset(inset, inset), strokeW)
            drawLine(bracketColor, Offset(inset, inset), Offset(inset + bracketLen, inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset - bracketLen, inset), Offset(size.width - inset, inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset, inset), Offset(size.width - inset, inset + bracketLen), strokeW)
            drawLine(bracketColor, Offset(inset, size.height - inset - bracketLen), Offset(inset, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(inset, size.height - inset), Offset(inset + bracketLen, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset - bracketLen, size.height - inset), Offset(size.width - inset, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset, size.height - inset - bracketLen), Offset(size.width - inset, size.height - inset), strokeW)

            val dotR = 2.dp.toPx()
            drawCircle(accent, dotR, Offset(inset, inset))
            drawCircle(accent, dotR, Offset(size.width - inset, inset))
            drawCircle(accent, dotR, Offset(inset, size.height - inset))
            drawCircle(accent, dotR, Offset(size.width - inset, size.height - inset))
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModuleArtworkCanvas(
                module = CleanerModule.VIRUS,
                accent = accent,
                progressRatio = progressRatio,
                centerValue = metricValue,
                centerUnit = "SHIELD",
                compact = true,
                modifier = Modifier
                    .fillMaxHeight(0.88f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = accent.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = CleanerModule.VIRUS.tag,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = CleanerModule.VIRUS.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = statusLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                color = accent.copy(alpha = 0.24f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, Brush.linearGradient(listOf(accent, SolarAmber.copy(alpha = 0.7f))))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCAN",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModuleTopBar(
    title: String,
    tag: String,
    accent: Color,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianCard.copy(alpha = 0.85f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.1.dp,
            Brush.horizontalGradient(listOf(accent.copy(alpha = 0.55f), CyberBorderMuted, accent.copy(alpha = 0.35f)))
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .background(ObsidianVoid.copy(alpha = 0.85f))
                    .border(1.1.dp, accent.copy(alpha = 0.45f), CircleShape)
                    .testTag("module_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_cd),
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.5.sp),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AzrCyberEmblem(size = 40.dp)
        }
    }
}
