package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CleanerModule
import com.example.model.RamProcessEntry
import com.example.ui.AzroiaUiState
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.ModuleArtworkCanvas
import com.example.ui.components.ModuleTopBar
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.formatGbShort

@Composable
fun RamOptimizerScreen(
    uiState: AzroiaUiState,
    onToggleAppSelection: (String) -> Unit,
    onOptimizeRam: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val ram = uiState.ramTelemetry
    val selectedCount = ram.backgroundApps.count { it.isSelected }

    Box(modifier = modifier.fillMaxSize()) {
        CyberBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModuleTopBar(
                title = CleanerModule.RAM.title,
                tag = CleanerModule.RAM.tag,
                accent = HyperViolet,
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, HyperViolet.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ModuleArtworkCanvas(
                                module = CleanerModule.RAM,
                                accent = HyperViolet,
                                progressRatio = (ram.usedPercent / 100f).coerceIn(0.1f, 1f),
                                centerValue = "${ram.usedPercent}%",
                                centerUnit = "RAM LOAD",
                                modifier = Modifier.size(164.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = { (ram.usedPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = HyperViolet,
                                trackColor = EmeraldPulse.copy(alpha = 0.25f),
                                strokeCap = StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                RamStatChip("ACTIVE", formatGbShort(ram.usedBytes), HyperViolet)
                                RamStatChip("FREE", formatGbShort(ram.availableBytes), EmeraldPulse)
                                RamStatChip("TOTAL", formatGbShort(ram.totalBytes), TextPrimary)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            uiState.ramStatusMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldPulse
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Button(
                                onClick = onOptimizeRam,
                                enabled = !uiState.isOptimizingRam && selectedCount > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HyperViolet,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("optimize_ram_button")
                            ) {
                                if (uiState.isOptimizingRam) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("RELEASING MEMORY…", style = MaterialTheme.typography.labelLarge)
                                } else {
                                    Icon(Icons.Filled.Memory, contentDescription = null)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("TURBO RELEASE ($selectedCount APPS)", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "SELECT BACKGROUND PROCESSES (${ram.backgroundApps.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = HyperViolet,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(ram.backgroundApps, key = { it.packageName }) { app ->
                    RamAppRow(
                        entry = app,
                        onToggle = { onToggleAppSelection(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RamStatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

@Composable
private fun RamAppRow(
    entry: RamProcessEntry,
    onToggle: () -> Unit
) {
    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                entry.isTerminated -> EmeraldPulse.copy(alpha = 0.6f)
                entry.isSelected -> HyperViolet.copy(alpha = 0.55f)
                else -> CyberBorderMuted
            }
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entry.icon != null) {
                Image(
                    bitmap = entry.icon,
                    contentDescription = "${entry.appName} icon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Android, contentDescription = null, tint = HyperViolet)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (entry.isTerminated) "Memory released" else entry.importanceLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (entry.isTerminated) EmeraldPulse else TextSecondary
                )
            }

            if (entry.isTerminated) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Stopped",
                    tint = EmeraldPulse,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Checkbox(
                    checked = entry.isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = HyperViolet,
                        uncheckedColor = TextSecondary
                    )
                )
            }
        }
    }
}
