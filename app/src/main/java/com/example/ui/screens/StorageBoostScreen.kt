package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.CleanerModule
import com.example.ui.AzroiaUiState
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.ModuleArtworkCanvas
import com.example.ui.components.ModuleTopBar
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.formatBytes
import com.example.util.formatGbShort

@Composable
fun StorageBoostScreen(
    uiState: AzroiaUiState,
    onBoostStorage: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val storage = uiState.storageTelemetry

    val animatedThrust by animateFloatAsState(
        targetValue = uiState.storageBoostProgress,
        animationSpec = tween(durationMillis = 140),
        label = "thrust_rise"
    )

    Box(modifier = modifier.fillMaxSize()) {
        CyberBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModuleTopBar(
                title = CleanerModule.STORAGE.title,
                tag = CleanerModule.STORAGE.tag,
                accent = SolarAmber,
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, SolarAmber.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ModuleArtworkCanvas(
                                    module = CleanerModule.STORAGE,
                                    accent = SolarAmber,
                                    progressRatio = (storage.usedPercent / 100f).coerceIn(0.1f, 1f),
                                    centerValue = "${storage.usedPercent}%",
                                    centerUnit = "USED",
                                    modifier = Modifier.size(156.dp)
                                )

                                // Ascending Vertical Plasma Rocket Thrust Chamber
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "TURBO BOOST",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SolarAmber
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(52.dp)
                                            .height(132.dp)
                                            .clip(RoundedCornerShape(26.dp))
                                            .background(ObsidianVoid)
                                            .border(1.2.dp, SolarAmber.copy(alpha = 0.55f), RoundedCornerShape(26.dp)),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                                            val fillHeight = size.height * animatedThrust.coerceIn(0.08f, 1f)
                                            drawRoundRect(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(EmeraldPulse, NeonCyan, SolarAmber)
                                                ),
                                                topLeft = Offset(0f, size.height - fillHeight),
                                                size = Size(size.width, fillHeight),
                                                cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${(animatedThrust * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (animatedThrust >= 0.99f) EmeraldPulse else SolarAmber
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            LinearProgressIndicator(
                                progress = { (storage.usedPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = SolarAmber,
                                trackColor = Color(0xFF1A243E),
                                strokeCap = StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Used: ${formatGbShort(storage.usedBytes)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Free: ${formatGbShort(storage.freeBytes)} / ${formatGbShort(storage.totalBytes)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            uiState.storageStatusMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldPulse
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Button(
                                onClick = onBoostStorage,
                                enabled = !uiState.isBoostingStorage,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SolarAmber,
                                    contentColor = ObsidianVoid
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("storage_boost_button")
                            ) {
                                Icon(Icons.Filled.RocketLaunch, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (uiState.isBoostingStorage) "BOOSTING STORAGE…" else "IGNITE STORAGE BOOST",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderMuted)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "STORAGE SECTOR DIAGNOSTICS",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolarAmber
                            )
                            StorageRow("App Cache Pool", formatBytes(storage.totalAppCacheBytes))
                            StorageRow("Cleanable Temp Buffers", formatBytes(storage.cleanableOwnCacheBytes))
                            StorageRow("Residual Temp & Logs", formatBytes(storage.residualTempBytes))
                            StorageRow("Last Boost Reclaimed", formatBytes(storage.lastBoostFreedBytes))

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    }
                                },
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("open_system_storage_button")
                            ) {
                                Icon(Icons.Filled.Storage, contentDescription = null, tint = NeonCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Android Storage Manager", color = NeonCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
    }
}
