package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CleanerModule
import com.example.model.TempFileCategory
import com.example.model.TempFileEntry
import com.example.ui.AzroiaUiState
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.ModuleArtworkCanvas
import com.example.ui.components.ModuleTopBar
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.HyperViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import com.example.util.formatBytes
import com.example.util.formatGbShort

@Composable
fun StorageBoostScreen(
    uiState: AzroiaUiState,
    onBoostStorage: () -> Unit,
    onScanTempFiles: (Uri?) -> Unit,
    onToggleTempFile: (String) -> Unit,
    onCleanSelectedTempFiles: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val storage = uiState.storageTelemetry

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { pickedUri ->
        if (pickedUri != null) {
            onScanTempFiles(pickedUri)
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.tempFiles.isEmpty() && !uiState.isScanningTempFiles) {
            onScanTempFiles(uiState.scannedFolderUri)
        }
    }

    val selectedTempFiles = uiState.tempFiles.filter { it.isSelected && !it.isDeleted }
    val selectedTempBytes = selectedTempFiles.sumOf { it.sizeBytes }

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

                // Phone Temporary File Cleaner Control Card
                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.CleaningServices,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PHONE TEMP FILE CLEANER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan
                                    )
                                }

                                if (uiState.lastTempFreedBytes > 0L) {
                                    Surface(
                                        color = EmeraldPulse.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(50),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            EmeraldPulse.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Text(
                                            text = "Freed ${formatBytes(uiState.lastTempFreedBytes)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = EmeraldPulse,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Scans & removes .tmp, .log, .bak, .part, cache & 0-byte junk inside app buffers or any phone folder you pick (Downloads, Documents, etc.).",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            uiState.scannedFolderUri?.let { folderUri ->
                                val decodedSegment = folderUri.lastPathSegment
                                    ?.substringAfterLast(':')
                                    ?.ifBlank { "Selected Phone Folder" }
                                    ?: "Selected Phone Folder"
                                Surface(
                                    color = NeonCyan.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.FolderOpen,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Linked Phone Folder: $decodedSegment",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onScanTempFiles(null) },
                                    enabled = !uiState.isScanningTempFiles && !uiState.isCleaningTempFiles,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SolarAmber.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("scan_app_temp_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        tint = SolarAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Scan Temp",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = SolarAmber,
                                        maxLines = 1
                                    )
                                }

                                Button(
                                    onClick = { folderPickerLauncher.launch(uiState.scannedFolderUri) },
                                    enabled = !uiState.isScanningTempFiles && !uiState.isCleaningTempFiles,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonCyan,
                                        contentColor = ObsidianVoid
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("scan_phone_folder_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FolderOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pick Folder",
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1
                                    )
                                }
                            }

                            if (uiState.isScanningTempFiles) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = NeonCyan,
                                    trackColor = Color(0xFF1A243E),
                                    strokeCap = StrokeCap.Round
                                )
                            }

                            uiState.tempFilesStatusMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (uiState.tempFiles.isEmpty()) TextSecondary else EmeraldPulse
                                )
                            }

                            if (uiState.tempFiles.isEmpty() && !uiState.isScanningTempFiles) {
                                Surface(
                                    color = ObsidianVoid.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderMuted)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CleaningServices,
                                            contentDescription = null,
                                            tint = EmeraldPulse,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "No temporary junk detected in current scope. Tap \"Pick Folder\" to inspect Downloads or phone directories.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            } else if (uiState.tempFiles.isNotEmpty()) {
                                Button(
                                    onClick = onCleanSelectedTempFiles,
                                    enabled = !uiState.isCleaningTempFiles && selectedTempFiles.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPulse,
                                        contentColor = ObsidianVoid
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("clean_selected_temp_button")
                                ) {
                                    if (uiState.isCleaningTempFiles) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = ObsidianVoid,
                                            strokeWidth = 2.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "CLEANING TEMP FILES…",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    } else {
                                        Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "DELETE SELECTED (${selectedTempFiles.size} • ${formatBytes(selectedTempBytes)})",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.tempFiles.isNotEmpty()) {
                    item {
                        Text(
                            text = "DETECTED TEMPORARY FILES (${uiState.tempFiles.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    items(uiState.tempFiles, key = { it.id }) { entry ->
                        TempFileRow(
                            entry = entry,
                            onToggle = { onToggleTempFile(entry.id) }
                        )
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
                            StorageRow("Detected Temp Queue", formatBytes(uiState.tempFiles.sumOf { it.sizeBytes }))
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
private fun TempFileRow(
    entry: TempFileEntry,
    onToggle: () -> Unit
) {
    val categoryColor = when (entry.category) {
        TempFileCategory.TEMP_CACHE -> NeonCyan
        TempFileCategory.LOG_FILE -> SolarAmber
        TempFileCategory.RESIDUAL_PART -> WarningOrange
        TempFileCategory.EMPTY_FILE -> HyperViolet
    }

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (entry.isSelected) categoryColor.copy(alpha = 0.55f) else CyberBorderMuted
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
            .testTag("temp_file_row_${entry.id.hashCode()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = entry.category.label,
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = categoryColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = entry.category.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = entry.locationLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatBytes(entry.sizeBytes),
                style = MaterialTheme.typography.labelLarge,
                color = SolarAmber
            )

            Checkbox(
                checked = entry.isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = categoryColor,
                    uncheckedColor = TextSecondary
                ),
                modifier = Modifier.testTag("temp_file_checkbox_${entry.id.hashCode()}")
            )
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
