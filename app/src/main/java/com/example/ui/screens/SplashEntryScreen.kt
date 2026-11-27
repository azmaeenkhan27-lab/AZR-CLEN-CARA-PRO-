package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AzrCyberEmblem
import com.example.ui.components.CyberBackdrop
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.RoyalSapphire
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.sin

@Composable
fun SplashEntryScreen(
    isBurstActive: Boolean,
    onTriggerLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val autoProgress = remember { Animatable(0f) }
    val burstAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        autoProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
        )
        onTriggerLaunch()
    }

    LaunchedEffect(isBurstActive) {
        if (isBurstActive) {
            burstAnim.snapTo(0f)
            burstAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1050, easing = FastOutSlowInEasing)
            )
        } else {
            burstAnim.snapTo(0f)
        }
    }

    val infinite = rememberInfiniteTransition(label = "splash_prism")
    val radarSpin by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
        label = "radar"
    )
    val particleDrop by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label = "drop"
    )
    val pulseScale by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val currentScale = if (isBurstActive) {
        1f + burstAnim.value * 1.25f
    } else {
        pulseScale
    }
    val screenFadeOut = if (isBurstActive && burstAnim.value > 0.78f) {
        1f - ((burstAnim.value - 0.78f) / 0.22f).coerceIn(0f, 1f)
    } else 1f

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(screenFadeOut),
        contentAlignment = Alignment.Center
    ) {
        CyberBackdrop()

        // Luxury Sapphire-Gold-Turquoise Aurora Vortex, Geometric Reticle & Dropping Digital Junk Particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val baseR = size.minDimension * 0.25f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        RoyalSapphire.copy(alpha = 0.42f + burstAnim.value * 0.28f),
                        NeonCyan.copy(alpha = 0.26f + burstAnim.value * 0.24f),
                        SolarAmber.copy(alpha = 0.16f + burstAnim.value * 0.22f),
                        Color.Transparent
                    ),
                    center = c,
                    radius = baseR * (2.1f + burstAnim.value * 2.4f)
                ),
                radius = baseR * (2.1f + burstAnim.value * 2.4f),
                center = c
            )

            // Rotating Geometric Hexagonal Aegis Reticle
            rotate(-radarSpin * 0.6f, c) {
                val hexR = baseR * (1.28f + burstAnim.value * 0.9f)
                val hexPath = androidx.compose.ui.graphics.Path().apply {
                    for (k in 0 until 6) {
                        val ang = Math.toRadians((k * 60 - 90).toDouble())
                        val hx = c.x + (kotlin.math.cos(ang) * hexR).toFloat()
                        val hy = c.y + (kotlin.math.sin(ang) * hexR).toFloat()
                        if (k == 0) moveTo(hx, hy) else lineTo(hx, hy)
                    }
                    close()
                }
                drawPath(
                    path = hexPath,
                    color = NeonCyan.copy(alpha = 0.28f * (1f - burstAnim.value * 0.4f)),
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }

            // Animated digital junk sparks dropping into the AZR Dustbin
            for (i in 0 until 12) {
                val frac = (particleDrop + i * 0.083f) % 1f
                val startY = c.y - baseR * 1.75f
                val endY = c.y - baseR * 0.22f
                val y = startY + (endY - startY) * frac
                val xSpread = (1f - frac) * baseR * 0.80f * sin(i * 1.7f + frac * 4.5f)
                val alpha = (1f - frac) * 0.95f
                val pColor = when (i % 3) {
                    0 -> NeonCyan
                    1 -> SolarAmber
                    else -> EmeraldPulse
                }
                drawCircle(
                    color = pColor.copy(alpha = alpha * 0.35f),
                    radius = (8f - frac * 3f).dp.toPx(),
                    center = Offset(c.x + xSpread, y)
                )
                drawCircle(
                    color = pColor.copy(alpha = alpha),
                    radius = (4.2f - frac * 1.8f).dp.toPx(),
                    center = Offset(c.x + xSpread, y)
                )
            }

            rotate(radarSpin, c) {
                for (i in 0 until 4) {
                    val r = baseR * (0.96f + i * 0.34f + burstAnim.value * 1.15f)
                    val ringColor = when (i) {
                        0 -> SolarAmber.copy(alpha = 0.48f)
                        1 -> NeonCyan.copy(alpha = 0.38f)
                        2 -> HyperViolet.copy(alpha = 0.30f)
                        else -> EmeraldPulse.copy(alpha = 0.22f)
                    }
                    drawCircle(
                        color = ringColor,
                        radius = r,
                        center = c,
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Compact AZR Dustbin Emblem -> expands dramatically on tap/launch
            Box(
                modifier = Modifier
                    .scale(currentScale)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTriggerLaunch
                    )
                    .testTag("azr_splash_logo"),
                contentAlignment = Alignment.Center
            ) {
                AzrCyberEmblem(
                    size = 122.dp,
                    burstProgress = burstAnim.value
                )
            }

            Spacer(modifier = Modifier.height(if (isBurstActive) 48.dp else 28.dp))

            // Before tap: ONLY "AZR" badge on dustbin screen; Upon tap/burst: "AZR CLEAN CARE" emerges!
            AnimatedVisibility(
                visible = !isBurstActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("A", style = MaterialTheme.typography.displayLarge, color = NeonCyan)
                        Text("Z", style = MaterialTheme.typography.displayLarge, color = SolarAmber)
                        Text("R", style = MaterialTheme.typography.displayLarge, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "TAP DUSTBIN TO ENTER",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 2.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = isBurstActive,
                enter = fadeIn(tween(240)) + scaleIn(initialScale = 0.85f),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AZR CLEAN CARE",
                        style = MaterialTheme.typography.displayMedium.copy(
                            letterSpacing = 2.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = SolarAmber,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ULTRA OPTIMIZATION ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                color = ObsidianCard.copy(alpha = 0.88f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .border(
                        1.2.dp,
                        Brush.linearGradient(listOf(SolarAmber.copy(alpha = 0.7f), NeonCyan.copy(alpha = 0.7f))),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(onClick = onTriggerLaunch)
                    .testTag("launch_enter_pill")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBurstActive) "AZR CLEAN CARE LAUNCHING…" else "AZR • LAUNCH ENGINE",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isBurstActive) EmeraldPulse else SolarAmber,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (isBurstActive) burstAnim.value else autoProgress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (isBurstActive) EmeraldPulse else NeonCyan,
                        trackColor = Color(0xFF152042),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
