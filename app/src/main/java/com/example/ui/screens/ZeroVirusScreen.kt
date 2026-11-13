package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CleanerModule
import com.example.model.RiskLevel
import com.example.model.VirusScanFinding
import com.example.ui.AzroiaUiState
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.ModuleArtworkCanvas
import com.example.ui.components.ModuleTopBar
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ZeroVirusScreen(
    uiState: AzroiaUiState,
    onStartScan: () -> Unit,
    onNeutralizeThreats: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val sec = uiState.securityPosture
    val totalFlagged = sec.criticalCount + sec.suspiciousCount

    Box(modifier = modifier.fillMaxSize()) {
        CyberBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModuleTopBar(
                title = CleanerModule.VIRUS.title,
                tag = CleanerModule.VIRUS.tag,
                accent = ElectricCrimson,
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
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, ElectricCrimson.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val centerVal = when {
                                uiState.isScanningVirus -> "${(uiState.virusScanProgress * 100).toInt()}%"
                                !sec.lastScanCompleted -> "HUNT"
                                totalFlagged == 0 -> "ZERO"
                                else -> "$totalFlagged"
                            }
                            val centerUnit = when {
                                uiState.isScanningVirus -> "DEEP AUDIT"
                                !sec.lastScanCompleted -> "ALL LAYERS"
                                totalFlagged == 0 -> "THREATS"
                                else -> "FLAGGED"
                            }

                            ModuleArtworkCanvas(
                                module = CleanerModule.VIRUS,
                                accent = if (sec.lastScanCompleted && totalFlagged == 0) EmeraldPulse else ElectricCrimson,
                                progressRatio = if (uiState.isScanningVirus) uiState.virusScanProgress else 1f,
                                centerValue = centerVal,
                                centerUnit = centerUnit,
                                modifier = Modifier.size(168.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            if (uiState.isScanningVirus) {
                                LinearProgressIndicator(
                                    progress = { uiState.virusScanProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = ElectricCrimson,
                                    trackColor = Color(0xFF1F162C),
                                    strokeCap = StrokeCap.Round
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${uiState.currentScanLayer}: ${uiState.currentScannedAppName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            uiState.virusStatusMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (totalFlagged == 0) EmeraldPulse else SolarAmber
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Button(
                                onClick = onStartScan,
                                enabled = !uiState.isScanningVirus,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCrimson,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("zero_virus_scan_button")
                            ) {
                                Icon(Icons.Filled.Security, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (uiState.isScanningVirus) "HUNTING HIDDEN THREATS…" else "DEEP HUNT & VIRUS SCAN",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            if (sec.lastScanCompleted) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = onNeutralizeThreats,
                                    enabled = !uiState.isNeutralizingThreats,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.2.dp,
                                        if (totalFlagged > 0) SolarAmber else EmeraldPulse
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("neutralize_threats_button")
                                ) {
                                    if (uiState.isNeutralizingThreats) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = SolarAmber,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("DESTROYING THREATS…", color = SolarAmber)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Shield,
                                            contentDescription = null,
                                            tint = if (totalFlagged > 0) SolarAmber else EmeraldPulse
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (totalFlagged > 0) "NEUTRALIZE & PURGE THREATS" else "PURGE RESIDUAL ARTIFACTS",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (totalFlagged > 0) SolarAmber else EmeraldPulse
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (sec.lastScanCompleted) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SecurityMetricBox("SCANNED", "${sec.scannedCount}", TextPrimary, Modifier.weight(1f))
                            SecurityMetricBox("HIDDEN", "${sec.hiddenCount}", HyperViolet, Modifier.weight(1f))
                            SecurityMetricBox("FLAGGED", "$totalFlagged", if (totalFlagged == 0) EmeraldPulse else ElectricCrimson, Modifier.weight(1f))
                        }
                    }

                    item {
                        Surface(
                            color = ObsidianCard,
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderMuted)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Android Security Patch", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                    Text(sec.securityPatch, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Device Lock Guard", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                    Text(
                                        text = if (sec.isDeviceLockSecure) "Protected" else "Unsecured",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (sec.isDeviceLockSecure) EmeraldPulse else SolarAmber
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "DEEP PACKAGE & HIDDEN LAYER RESULTS (${sec.findings.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCrimson,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(sec.findings, key = { it.packageName }) { finding ->
                        VirusFindingRow(
                            finding = finding,
                            onUninstall = {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_DELETE).apply {
                                        data = Uri.parse("package:${finding.packageName}")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityMetricBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderMuted),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = color)
        }
    }
}

@Composable
private fun VirusFindingRow(
    finding: VirusScanFinding,
    onUninstall: () -> Unit
) {
    val riskColor = when (finding.riskLevel) {
        RiskLevel.CRITICAL -> ElectricCrimson
        RiskLevel.SUSPICIOUS -> SolarAmber
        RiskLevel.SAFE -> EmeraldPulse
    }

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (finding.riskLevel == RiskLevel.SAFE) CyberBorderMuted else riskColor.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (finding.icon != null) {
                Image(
                    bitmap = finding.icon,
                    contentDescription = "${finding.appName} icon",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Android, contentDescription = null, tint = riskColor)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (finding.isHiddenPackage) "${finding.appName} (Hidden)" else finding.appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (finding.riskReasons.isEmpty()) {
                    Text(
                        text = "Verified Safe • ${finding.installerLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmeraldPulse,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = finding.riskReasons.joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = riskColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (finding.riskLevel == RiskLevel.SAFE) {
                Icon(
                    imageVector = Icons.Filled.GppGood,
                    contentDescription = "Safe",
                    tint = EmeraldPulse,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                OutlinedButton(
                    onClick = onUninstall,
                    border = androidx.compose.foundation.BorderStroke(1.dp, riskColor),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Destroy", style = MaterialTheme.typography.labelLarge, color = riskColor)
                }
            }
        }
    }
}
