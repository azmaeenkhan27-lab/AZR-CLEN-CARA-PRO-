package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
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
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(ObsidianVoid, ObsidianSurface, Color(0xFF040A1E))
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(RoyalSapphire.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.14f),
                radius = size.width * 0.72f
            ),
            radius = size.width * 0.72f,
            center = Offset(size.width * 0.2f, size.height * 0.14f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(HyperViolet.copy(alpha = 0.14f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.65f),
                radius = size.width * 0.7f
            ),
            radius = size.width * 0.7f,
            center = Offset(size.width * 0.85f, size.height * 0.65f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SolarAmber.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.92f),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(size.width * 0.5f, size.height * 0.92f)
        )
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
            val ringStroke = if (compact) 6.dp.toPx() else 9.dp.toPx()
            val detailStroke = if (compact) 1.8.dp.toPx() else 2.5.dp.toPx()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.28f * pulse), Color.Transparent),
                    center = c,
                    radius = r
                ),
                radius = r,
                center = c
            )

            val ringDiameter = r * 1.68f
            val ringTopLeft = Offset(c.x - ringDiameter / 2f, c.y - ringDiameter / 2f)
            val ringSize = Size(ringDiameter, ringDiameter)

            drawArc(
                color = CyberBorderMuted.copy(alpha = 0.65f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringSize,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )

            val sweep = (progressRatio.coerceIn(0.08f, 1f)) * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(accent.copy(alpha = 0.4f), accent, Color.White.copy(alpha = 0.92f), accent),
                    center = c
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringSize,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )

            when (module) {
                CleanerModule.CACHE -> {
                    rotate(rotation, c) {
                        val innerD = r * 1.28f
                        val tl = Offset(c.x - innerD / 2f, c.y - innerD / 2f)
                        for (k in 0..2) {
                            drawArc(
                                color = accent.copy(alpha = 0.72f),
                                startAngle = k * 120f,
                                sweepAngle = 62f,
                                useCenter = false,
                                topLeft = tl,
                                size = Size(innerD, innerD),
                                style = Stroke(width = detailStroke, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                CleanerModule.RAM -> {
                    rotate(-rotation * 0.5f, c) {
                        for (i in 0 until 12) {
                            val angleRad = Math.toRadians((i * 30).toDouble())
                            val inner = r * 0.60f
                            val outer = r * 0.70f
                            drawLine(
                                color = if (i % 3 == 0) accent else accent.copy(alpha = 0.4f),
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
                        color = accent.copy(alpha = 0.34f),
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
                        color = accent.copy(alpha = 0.82f),
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
    tileSize: Dp? = null
) {
    val accent = moduleAccentColor(module)
    val sizeModifier = if (tileSize != null) {
        modifier.size(tileSize)
    } else {
        modifier.fillMaxWidth().aspectRatio(1f)
    }

    Box(
        modifier = sizeModifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        ObsidianCard,
                        Color(0xFF0A1430),
                        Color(0xFF060C20)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.9f),
                        CyberBorderMuted.copy(alpha = 0.5f),
                        SolarAmber.copy(alpha = 0.45f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .testTag("slide_${module.name.lowercase()}")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bracketLen = 16.dp.toPx()
            val inset = 9.dp.toPx()
            val strokeW = 2.dp.toPx()
            val bracketColor = accent.copy(alpha = 0.9f)

            drawLine(bracketColor, Offset(inset, inset + bracketLen), Offset(inset, inset), strokeW)
            drawLine(bracketColor, Offset(inset, inset), Offset(inset + bracketLen, inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset - bracketLen, inset), Offset(size.width - inset, inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset, inset), Offset(size.width - inset, inset + bracketLen), strokeW)
            drawLine(bracketColor, Offset(inset, size.height - inset - bracketLen), Offset(inset, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(inset, size.height - inset), Offset(inset + bracketLen, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset - bracketLen, size.height - inset), Offset(size.width - inset, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset, size.height - inset - bracketLen), Offset(size.width - inset, size.height - inset), strokeW)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = accent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.4f))
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
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
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
                    .padding(vertical = 4.dp)
                    .aspectRatio(1f)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = statusLine,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1A0E2B),
                        ObsidianCard,
                        Color(0xFF0A1432)
                    )
                )
            )
            .border(
                width = 1.6.dp,
                brush = Brush.linearGradient(
                    colors = listOf(accent.copy(alpha = 0.9f), HyperViolet.copy(alpha = 0.65f), SolarAmber.copy(alpha = 0.55f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .testTag("slide_virus")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bracketLen = 16.dp.toPx()
            val inset = 9.dp.toPx()
            val strokeW = 2.dp.toPx()
            val bracketColor = accent.copy(alpha = 0.9f)

            drawLine(bracketColor, Offset(inset, inset + bracketLen), Offset(inset, inset), strokeW)
            drawLine(bracketColor, Offset(inset, inset), Offset(inset + bracketLen, inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset - bracketLen, inset), Offset(size.width - inset, inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset, inset), Offset(size.width - inset, inset + bracketLen), strokeW)
            drawLine(bracketColor, Offset(inset, size.height - inset - bracketLen), Offset(inset, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(inset, size.height - inset), Offset(inset + bracketLen, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset - bracketLen, size.height - inset), Offset(size.width - inset, size.height - inset), strokeW)
            drawLine(bracketColor, Offset(size.width - inset, size.height - inset - bracketLen), Offset(size.width - inset, size.height - inset), strokeW)
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
                    color = accent.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.45f))
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
                color = accent.copy(alpha = 0.2f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCAN",
                        style = MaterialTheme.typography.labelLarge,
                        color = accent
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .background(ObsidianCard)
                .border(1.dp, CyberBorderMuted, CircleShape)
                .testTag("module_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_cd),
                tint = TextPrimary
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        AzrCyberEmblem(size = 40.dp)
    }
}
