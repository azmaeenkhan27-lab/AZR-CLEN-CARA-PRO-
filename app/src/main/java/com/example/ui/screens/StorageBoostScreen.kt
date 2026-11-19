package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCacheCleanState
import com.example.model.AppCacheEntry
import com.example.model.CleanerModule
import com.example.model.StorageBoostMode
import com.example.model.TempFileCategory
import com.example.model.TempFileEntry
import com.example.ui.AzroiaUiState
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.ModuleTopBar
import com.example.ui.components.SmartStorageGuardCard
import com.example.ui.components.StorageBoostEngineCard
import com.example.ui.components.StorageHealthAndHistoryCard
import com.example.ui.components.StorageReactorHeroCanvas
import com.example.ui.components.StorageRocketBoosterStage
import com.example.ui.components.StorageSectorDistributionPanel
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
import com.example.ui.theme.WarningOrange
import com.example.util.formatBytes
import com.example.util.formatGbShort

private enum class StorageFileFilterTab(val label: String) {
    ALL("All"),
    JUNK_LOGS("Temp & Logs"),
    APKS("APKs"),
    LARGE("Large Files"),
    DUPLICATES("Duplicates")
}

private enum class StorageFileSortOption(val label: String) {
    SIZE_DESC("Size ↓"),
    NAME_ASC("A–Z"),
    TYPE("Category")
}

@Composable
fun StorageBoostScreen(
    uiState: AzroiaUiState,
    onBoostStorage: () -> Unit,
    onSelectBoostMode: (StorageBoostMode) -> Unit,
    onToggleIncludeApksInBoost: (Boolean) -> Unit,
    onToggleAutoSweepSafInBoost: (Boolean) -> Unit,
    onTriggerAutoAppCacheClean: () -> Unit,
    onScanTempFiles: (Uri?) -> Unit,
    onSelectLargeThreshold: (Long) -> Unit,
    onApplyStoragePreset: (String) -> Unit,
    onClearStorageHistory: () -> Unit,
    onToggleTempFile: (String) -> Unit,
    onCleanSelectedTempFiles: () -> Unit,
    onCleanSingleApp: (AppCacheEntry) -> Unit,
    onConsumePendingSettingsPkg: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val storage = uiState.storageTelemetry

    var isCacheAppsExpanded by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(StorageFileFilterTab.ALL) }
    var selectedSort by rememberSaveable { mutableStateOf(StorageFileSortOption.SIZE_DESC) }
    var showConfirmDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val cacheArrowRotation by animateFloatAsState(
        targetValue = if (isCacheAppsExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "storage_cache_expand_arrow"
    )

    LaunchedEffect(uiState.pendingOpenSettingsPackage) {
        val pkg = uiState.pendingOpenSettingsPackage ?: return@LaunchedEffect
        onConsumePendingSettingsPkg()
        runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", pkg, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

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

    val detectedJunkBytes = remember(uiState.tempFiles) {
        uiState.tempFiles.filter { !it.isDeleted }.sumOf { it.sizeBytes }
    }
    val selectedTempFiles = remember(uiState.tempFiles) {
        uiState.tempFiles.filter { it.isSelected && !it.isDeleted }
    }
    val selectedTempBytes = selectedTempFiles.sumOf { it.sizeBytes }
    val containsSensitiveSelection = remember(selectedTempFiles) {
        selectedTempFiles.any {
            it.category == TempFileCategory.LARGE_FILE ||
                it.category == TempFileCategory.APK_INSTALLER ||
                it.category == TempFileCategory.DUPLICATE_FILE
        }
    }

    val filteredFiles = remember(uiState.tempFiles, searchQuery, selectedFilter, selectedSort) {
        val q = searchQuery.trim().lowercase()
        val matching = uiState.tempFiles.filter { entry ->
            val matchesQ = q.isBlank() ||
                entry.name.lowercase().contains(q) ||
                entry.locationLabel.lowercase().contains(q)
            val matchesCat = when (selectedFilter) {
                StorageFileFilterTab.ALL -> true
                StorageFileFilterTab.JUNK_LOGS -> entry.category == TempFileCategory.TEMP_CACHE ||
                    entry.category == TempFileCategory.LOG_FILE ||
                    entry.category == TempFileCategory.RESIDUAL_PART ||
                    entry.category == TempFileCategory.EMPTY_FILE
                StorageFileFilterTab.APKS -> entry.category == TempFileCategory.APK_INSTALLER
                StorageFileFilterTab.LARGE -> entry.category == TempFileCategory.LARGE_FILE
                StorageFileFilterTab.DUPLICATES -> entry.category == TempFileCategory.DUPLICATE_FILE
            }
            matchesQ && matchesCat
        }

        when (selectedSort) {
            StorageFileSortOption.SIZE_DESC -> matching.sortedByDescending { it.sizeBytes }
            StorageFileSortOption.NAME_ASC -> matching.sortedBy { it.name.lowercase() }
            StorageFileSortOption.TYPE -> matching.sortedWith(
                compareBy<TempFileEntry> { it.category.ordinal }.thenByDescending { it.sizeBytes }
            )
        }
    }

    val topStorageApps = remember(uiState.cacheEntries) {
        uiState.cacheEntries.sortedByDescending { it.totalFootprintBytes }
    }

    val animatedThrust by animateFloatAsState(
        targetValue = uiState.storageBoostProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 0. Quick-Glance Storage KPI Strip
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("storage_quick_glance_strip"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StorageQuickPill("FREE", formatGbShort(storage.freeBytes), EmeraldPulse, Modifier.weight(1f))
                        StorageQuickPill("CACHE", formatBytes(uiState.totalDetectedAppCacheBytes), NeonCyan, Modifier.weight(1f))
                        StorageQuickPill("JUNK", formatBytes(detectedJunkBytes), HyperViolet, Modifier.weight(1f))
                        StorageQuickPill("SAVED", formatBytes(storage.totalStorageReclaimedBytes), SolarAmber, Modifier.weight(1f))
                    }
                }

                // 1. Storage Sector Reactor + Rocket Launchpad Hero Card
                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(
                            1.3.dp,
                            if (storage.usedPercent >= 90) ElectricCrimson.copy(alpha = 0.72f)
                            else SolarAmber.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier.testTag("storage_hero_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            StorageReactorHeroCanvas(
                                storage = storage,
                                detectedJunkBytes = detectedJunkBytes,
                                isBoostingOrScanning = uiState.isBoostingStorage || uiState.isScanningTempFiles || uiState.isCleaningTempFiles
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            StorageRocketBoosterStage(
                                isBoosting = uiState.isBoostingStorage,
                                boostProgress = animatedThrust,
                                modeLabel = uiState.selectedStorageBoostMode.label,
                                lastWriteSpeedMbPerSec = storage.lastWriteSpeedMbPerSec
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            StorageSectorDistributionPanel(
                                storage = storage,
                                detectedJunkBytes = detectedJunkBytes
                            )

                            if (uiState.isBoostingStorage) {
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { animatedThrust },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = EmeraldPulse,
                                    trackColor = CyberBorderMuted,
                                    strokeCap = StrokeCap.Round
                                )
                            }

                            uiState.storageStatusMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = ObsidianVoid.copy(alpha = 0.72f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(0.8.dp, EmeraldPulse.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                                        color = EmeraldPulse,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = onBoostStorage,
                                enabled = !uiState.isBoostingStorage,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SolarAmber,
                                    contentColor = ObsidianVoid,
                                    disabledContainerColor = CyberBorderMuted,
                                    disabledContentColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("storage_boost_button")
                            ) {
                                if (uiState.isBoostingStorage) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = ObsidianVoid,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "RUNNING ${uiState.selectedStorageBoostMode.label.uppercase()}…",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                } else {
                                    Icon(Icons.Filled.RocketLaunch, contentDescription = null)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "IGNITE ${uiState.selectedStorageBoostMode.label.uppercase()}",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }

                // 1b. High-Quality Turbo Boost Modes, 4-Stage Pipeline & Flash Benchmark
                item {
                    StorageBoostEngineCard(
                        selectedMode = uiState.selectedStorageBoostMode,
                        includeApksInBoost = uiState.includeApksInStorageBoost,
                        autoSweepSafInBoost = uiState.autoSweepSafInBoost,
                        hasLinkedFolder = uiState.scannedFolderUri != null,
                        isBoosting = uiState.isBoostingStorage,
                        activeStageIndex = uiState.storageBoostStageIndex,
                        benchmark = uiState.storageBoostResultSummary,
                        totalAppCacheBytes = uiState.totalDetectedAppCacheBytes,
                        onSelectMode = onSelectBoostMode,
                        onToggleIncludeApks = onToggleIncludeApksInBoost,
                        onToggleAutoSweepSaf = onToggleAutoSweepSafInBoost,
                        onTriggerAutoAppCacheBoost = onTriggerAutoAppCacheClean
                    )
                }

                // 2. Smart Storage Analyzer & Presets
                item {
                    SmartStorageGuardCard(
                        files = uiState.tempFiles,
                        largeThresholdBytes = uiState.storageLargeThresholdBytes,
                        onSelectLargeThreshold = onSelectLargeThreshold,
                        onApplyPreset = onApplyStoragePreset
                    )
                }

                // 3. Storage Health Score & Sweep Log Timeline
                item {
                    StorageHealthAndHistoryCard(
                        storage = storage,
                        history = uiState.storageCleanHistory,
                        onClearHistory = onClearStorageHistory
                    )
                }

                // 4. Expandable Top Storage Hogs & App Cache
                item {
                    Surface(
                        color = ObsidianCardElevated,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            1.2.dp,
                            if (isCacheAppsExpanded) NeonCyan else SolarAmber.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { isCacheAppsExpanded = !isCacheAppsExpanded }
                            .testTag("storage_app_cache_arrow_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Top Storage Hogs & App Cache (${formatBytes(uiState.totalDetectedAppCacheBytes)})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeonCyan
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isCacheAppsExpanded) {
                                        "Sorted by total storage footprint • Tap an app to auto-clean cache"
                                    } else {
                                        "Tap arrow ▼ to inspect Chrome, YouTube & ${topStorageApps.size} apps"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = if (isCacheAppsExpanded) "Collapse apps storage" else "Expand apps storage",
                                tint = NeonCyan,
                                modifier = Modifier
                                    .size(28.dp)
                                    .rotate(cacheArrowRotation)
                            )
                        }
                    }
                }

                if (isCacheAppsExpanded) {
                    items(topStorageApps, key = { "storage_${it.packageName}" }) { entry ->
                        Surface(
                            color = ObsidianCard,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CyberBorderMuted),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onCleanSingleApp(entry) }
                                .testTag("storage_cache_app_${entry.packageName}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 54.dp)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (entry.icon != null) {
                                    Image(
                                        bitmap = entry.icon,
                                        contentDescription = "${entry.appName} icon",
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ObsidianCardElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Android, contentDescription = null, tint = NeonCyan)
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
                                        text = if (entry.cleanState == AppCacheCleanState.CLEANED) {
                                            "Cache Cleaned • Total: ${formatBytes(entry.totalFootprintBytes)}"
                                        } else {
                                            "Cache: ${formatBytes(entry.cacheBytes)} • Total: ${formatBytes(entry.totalFootprintBytes)}"
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                        color = if (entry.cleanState == AppCacheCleanState.CLEANED) EmeraldPulse else NeonCyan,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (entry.cleanState == AppCacheCleanState.CLEANED) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Cleaned",
                                        tint = EmeraldPulse,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = "Clean ${entry.appName} cache",
                                        tint = SolarAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Deep Phone Junk, Temp, APK, Duplicate & Large File Cleaner Card
                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                                        text = "DEEP JUNK, APK & LARGE FILE CLEANER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan
                                    )
                                }

                                if (uiState.lastTempFreedBytes > 0L) {
                                    Surface(
                                        color = EmeraldPulse.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, EmeraldPulse.copy(alpha = 0.5f))
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
                                text = "Detects .tmp, .log, .bak, .wal, .dat, 0-byte files, installer .apk packages, duplicate files, and large files ≥ ${formatBytes(uiState.storageLargeThresholdBytes)} in app data buffers & any phone folder you pick.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                                color = TextSecondary
                            )

                            uiState.scannedFolderUri?.let { folderUri ->
                                val decodedSegment = folderUri.lastPathSegment
                                    ?.substringAfterLast(':')
                                    ?.ifBlank { "Linked Phone Folder" }
                                    ?: "Linked Phone Folder"
                                Surface(
                                    color = NeonCyan.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f))
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
                                            text = "Linked Folder: $decodedSegment",
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
                                    border = BorderStroke(1.dp, SolarAmber.copy(alpha = 0.7f)),
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
                                        text = "Deep Scan",
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
                                    trackColor = CyberBorderMuted,
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

                            if (uiState.tempFiles.isNotEmpty()) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            text = "Search file, .apk, log, or folder...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMuted
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Search, contentDescription = null, tint = NeonCyan)
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = { searchQuery = "" },
                                                modifier = Modifier.testTag("storage_search_clear")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = "Clear search",
                                                    tint = TextSecondary
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = CyberBorderMuted,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedContainerColor = ObsidianVoid,
                                        unfocusedContainerColor = ObsidianVoid
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("storage_search_input")
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StorageFileFilterTab.entries.forEach { tab ->
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
                                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                                selectedLabelColor = NeonCyan,
                                                containerColor = ObsidianVoid,
                                                labelColor = TextSecondary
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = CyberBorderMuted,
                                                selectedBorderColor = NeonCyan
                                            ),
                                            modifier = Modifier.testTag("storage_filter_${tab.name.lowercase()}")
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StorageFileSortOption.entries.forEach { sort ->
                                        val isSelected = selectedSort == sort
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedSort = sort },
                                            label = {
                                                Text(
                                                    text = sort.label,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = SolarAmber.copy(alpha = 0.2f),
                                                selectedLabelColor = SolarAmber,
                                                containerColor = ObsidianVoid,
                                                labelColor = TextSecondary
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = CyberBorderMuted,
                                                selectedBorderColor = SolarAmber
                                            ),
                                            modifier = Modifier.testTag("storage_sort_${sort.name.lowercase()}")
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (containsSensitiveSelection) {
                                            showConfirmDeleteDialog = true
                                        } else {
                                            onCleanSelectedTempFiles()
                                        }
                                    },
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
                                            text = "CLEANING SELECTED FILES…",
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
                            } else if (!uiState.isScanningTempFiles) {
                                Surface(
                                    color = ObsidianVoid.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, CyberBorderMuted)
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
                                            text = "No junk or temp files detected in current scope. Tap \"Pick Folder\" to deep-scan Downloads, Documents or Media folders for junk, APKs, duplicates & large files.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (filteredFiles.isNotEmpty()) {
                    item {
                        Text(
                            text = "DETECTED FILES (${filteredFiles.size}/${uiState.tempFiles.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    items(filteredFiles, key = { it.id }) { entry ->
                        TempFileRow(
                            entry = entry,
                            onToggle = { onToggleTempFile(entry.id) }
                        )
                    }
                }

                // 6. System Storage Diagnostics & System Manager Shortcut
                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CyberBorderMuted)
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
                            StorageRow("Apps Binary & User Data", formatBytes(storage.totalAppDataBytes))
                            StorageRow("App Cache Pool", formatBytes(storage.totalAppCacheBytes))
                            StorageRow("Cleanable Internal Buffers", formatBytes(storage.cleanableOwnCacheBytes))
                            StorageRow("Residual Temp & Logs", formatBytes(storage.residualTempBytes))
                            StorageRow("Detected Scan Queue", formatBytes(detectedJunkBytes))
                            StorageRow("Lifetime Storage Reclaimed", formatBytes(storage.totalStorageReclaimedBytes))

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
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
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

                item { Spacer(modifier = Modifier.height(14.dp)) }
            }
        }
    }

    if (showConfirmDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            containerColor = ObsidianCardElevated,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            icon = {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = SolarAmber,
                    modifier = Modifier.size(30.dp)
                )
            },
            title = {
                Text(
                    text = "Confirm Permanent File Deletion",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "You selected ${selectedTempFiles.size} file(s) totaling ${formatBytes(selectedTempBytes)}, including Large Files, APKs, or Duplicates. Deleted files cannot be undone. Proceed?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteDialog = false
                        onCleanSelectedTempFiles()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCrimson, contentColor = TextPrimary),
                    modifier = Modifier.testTag("confirm_delete_storage_files_button")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDeleteDialog = false },
                    modifier = Modifier.testTag("cancel_delete_storage_files_button")
                ) {
                    Text("Cancel", color = NeonCyan)
                }
            }
        )
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
        TempFileCategory.APK_INSTALLER -> EmeraldPulse
        TempFileCategory.LARGE_FILE -> ElectricCrimson
        TempFileCategory.DUPLICATE_FILE -> HyperViolet
    }
    val categoryIcon = when (entry.category) {
        TempFileCategory.APK_INSTALLER -> Icons.Filled.Android
        TempFileCategory.LARGE_FILE -> Icons.Filled.FolderOpen
        TempFileCategory.DUPLICATE_FILE -> Icons.Filled.ContentCopy
        else -> Icons.Filled.Description
    }

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
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
                    imageVector = categoryIcon,
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
private fun StorageQuickPill(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = TextSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
