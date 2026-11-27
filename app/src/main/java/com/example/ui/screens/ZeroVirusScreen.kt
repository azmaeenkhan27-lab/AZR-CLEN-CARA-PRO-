package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CleanerModule
import com.example.model.RiskLevel
import com.example.model.VirusScanFinding
import com.example.ui.AzroiaUiState
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.ModuleTopBar
import com.example.ui.components.VirusDestructionReportCard
import com.example.ui.components.ZeroVirusDestroyCoreCanvas
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private enum class VirusFilterTab(val label: String) {
    ALL("All"),
    THREATS("Threats"),
    HIDDEN("Hidden"),
    SAFE("Safe")
}

@Composable
fun ZeroVirusScreen(
    uiState: AzroiaUiState,
    onStartScan: () -> Unit,
    onToggleAutoDeleteUnnecessary: (Boolean) -> Unit,
    onNeutralizeThreats: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val sec = uiState.securityPosture
    val totalFlagged = sec.criticalCount + sec.suspiciousCount
    val activeUnneutralized = sec.findings.count { it.riskLevel != RiskLevel.SAFE && !it.isNeutralized }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(VirusFilterTab.ALL) }

    val filteredFindings = remember(sec.findings, searchQuery, selectedFilter) {
        val q = searchQuery.trim().lowercase()
        sec.findings.filter { finding ->
            val matchesQ = q.isBlank() ||
                finding.appName.lowercase().contains(q) ||
                finding.packageName.lowercase().contains(q)
            val matchesTab = when (selectedFilter) {
                VirusFilterTab.ALL -> true
                VirusFilterTab.THREATS -> finding.riskLevel != RiskLevel.SAFE
                VirusFilterTab.HIDDEN -> finding.isHiddenPackage
                VirusFilterTab.SAFE -> finding.riskLevel == RiskLevel.SAFE
            }
            matchesQ && matchesTab
        }
    }

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
                // 1. Zero Virus Internal Destroyer Core + Auto-Destroy Controls
                item {
                    val virusHeroAccent = when {
                        uiState.isNeutralizingThreats -> SolarAmber
                        sec.lastScanCompleted && activeUnneutralized == 0 -> EmeraldPulse
                        else -> ElectricCrimson
                    }
                    CyberGlassCard(
                        accent = virusHeroAccent,
                        secondaryAccent = HyperViolet
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ZeroVirusDestroyCoreCanvas(
                                posture = sec,
                                isScanning = uiState.isScanningVirus,
                                isDestroying = uiState.isNeutralizingThreats,
                                scanProgress = uiState.virusScanProgress
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
                                    trackColor = CyberBorderMuted,
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
                                Surface(
                                    color = ObsidianVoid.copy(alpha = 0.72f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (activeUnneutralized == 0) EmeraldPulse.copy(alpha = 0.45f) else SolarAmber.copy(alpha = 0.45f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                                        color = if (activeUnneutralized == 0) EmeraldPulse else SolarAmber,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Auto-Destroy & Shred Toggle
                            Surface(
                                color = ObsidianVoid.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CyberBorderMuted),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoDelete,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Auto-Destroy Threats & Shred Junk",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "Auto-crushes threat processes & zero-fill shreds .apk, .dex, .tmp, .log, .wal & .dat on scan",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = uiState.autoDeleteUnnecessaryFiles,
                                        onCheckedChange = onToggleAutoDeleteUnnecessary,
                                        enabled = !uiState.isScanningVirus && !uiState.isNeutralizingThreats,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = ObsidianVoid,
                                            checkedTrackColor = NeonCyan
                                        ),
                                        modifier = Modifier.testTag("virus_auto_delete_switch")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = onStartScan,
                                enabled = !uiState.isScanningVirus && !uiState.isNeutralizingThreats,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCrimson,
                                    contentColor = TextPrimary
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
                                    text = if (uiState.isScanningVirus) {
                                        "CRUSHING THREATS & SHREDDING…"
                                    } else {
                                        "DEEP SCAN & AUTO-DESTROY"
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            if (sec.lastScanCompleted) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = onNeutralizeThreats,
                                    enabled = !uiState.isNeutralizingThreats && !uiState.isScanningVirus,
                                    border = BorderStroke(
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
                                        Text("CRUSHING FROM INSIDE…", color = SolarAmber)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Shield,
                                            contentDescription = null,
                                            tint = if (totalFlagged > 0) SolarAmber else EmeraldPulse
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (totalFlagged > 0) {
                                                "CRUSH & SHRED ALL THREATS"
                                            } else {
                                                "DEEP SHRED RESIDUAL PAYLOADS"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (totalFlagged > 0) SolarAmber else EmeraldPulse
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Internal Destruction & Payload Shredder Report Card
                if (sec.lastScanCompleted) {
                    item {
                        VirusDestructionReportCard(posture = sec)
                    }

                    // 3. 4-Column Security Metrics Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SecurityMetricBox("SCANNED", "${sec.scannedCount}", TextPrimary, Modifier.weight(1f))
                            SecurityMetricBox("HIDDEN", "${sec.hiddenCount}", HyperViolet, Modifier.weight(1f))
                            SecurityMetricBox(
                                "CRUSHED",
                                "${sec.autoDestroyedProcessesCount}",
                                SolarAmber,
                                Modifier.weight(1f)
                            )
                            SecurityMetricBox(
                                "SHREDDED",
                                "${sec.shreddedPayloadsCount}",
                                NeonCyan,
                                Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Surface(
                            color = ObsidianCard,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, CyberBorderMuted)
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

                    // 4. Filter & Search Results
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Search scanned apps or packages...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Search, contentDescription = null, tint = ElectricCrimson)
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Clear search", tint = TextSecondary)
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCrimson,
                                    unfocusedBorderColor = CyberBorderMuted,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = ObsidianVoid,
                                    unfocusedContainerColor = ObsidianVoid
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("virus_search_input")
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VirusFilterTab.entries.forEach { tab ->
                                    val isSelected = selectedFilter == tab
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedFilter = tab },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElectricCrimson.copy(alpha = 0.22f),
                                            selectedLabelColor = ElectricCrimson,
                                            containerColor = ObsidianVoid,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = CyberBorderMuted,
                                            selectedBorderColor = ElectricCrimson
                                        ),
                                        modifier = Modifier.testTag("virus_filter_${tab.name.lowercase()}")
                                    )
                                }
                            }

                            Text(
                                text = "DEEP PACKAGE & HIDDEN LAYER RESULTS (${filteredFindings.size}/${sec.findings.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCrimson,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    items(filteredFindings, key = { it.packageName }) { finding ->
                        VirusFindingRow(
                            finding = finding,
                            onUninstall = {
                                runCatching {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", finding.packageName, null)
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
        border = BorderStroke(1.dp, CyberBorderMuted),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp), color = TextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color, maxLines = 1)
        }
    }
}

@Composable
private fun VirusFindingRow(
    finding: VirusScanFinding,
    onUninstall: () -> Unit
) {
    val riskColor = when {
        finding.riskLevel == RiskLevel.SAFE -> EmeraldPulse
        finding.isNeutralized -> SolarAmber
        finding.riskLevel == RiskLevel.CRITICAL -> ElectricCrimson
        else -> SolarAmber
    }

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
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

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = if (finding.isHiddenPackage) "${finding.appName} (Hidden)" else finding.appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (finding.riskLevel != RiskLevel.SAFE) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (finding.isNeutralized) {
                            Surface(
                                color = EmeraldPulse.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.7.dp, EmeraldPulse.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = "NEUTRALIZED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = EmeraldPulse,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Surface(
                            color = riskColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "RISK ${finding.riskScore}/100",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = riskColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
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
                    border = BorderStroke(1.dp, riskColor),
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
                    Text("Uninstall", style = MaterialTheme.typography.labelLarge, color = riskColor)
                }
            }
        }
    }
}
