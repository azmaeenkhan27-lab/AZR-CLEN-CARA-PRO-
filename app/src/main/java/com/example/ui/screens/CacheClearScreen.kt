package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCacheCleanState
import com.example.model.AppCacheEntry
import com.example.model.CacheCleanMode
import com.example.model.CacheSelectionPreset
import com.example.model.CleanerModule
import com.example.ui.AzroiaUiState
import com.example.ui.components.AppCacheInspectorSheet
import com.example.ui.components.AppsWithCacheSummaryCard
import com.example.ui.components.CacheAreaDistributionPanel
import com.example.ui.components.CacheHealthAndHistoryCard
import com.example.ui.components.CacheReactorHeroCanvas
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.GrandTotalCleanedBanner
import com.example.ui.components.ModuleTopBar
import com.example.ui.components.SmartCacheGuardPanel
import com.example.ui.components.TopCacheHogsVisualCard
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
import com.example.util.formatRelativeTime

private enum class CacheFilterTab(val label: String) {
    ALL("All Apps"),
    USED_CACHED("Used & Cached"),
    CACHED_ONLY("Cached > 0"),
    OVER_LIMIT("Over Limit"),
    DORMANT("Dormant"),
    MEDIA_SOCIAL("Media & Social"),
    BROWSERS("Browsers & Files"),
    SHIELDED("Shielded")
}

private enum class CacheSortOption(val label: String) {
    CACHE_DESC("Cache ↓"),
    FOOTPRINT_DESC("Total Size ↓"),
    RECENT("Recent"),
    NAME_ASC("A–Z")
}

@Composable
fun CacheClearScreen(
    uiState: AzroiaUiState,
    onRunCacheSweep: () -> Unit,
    onStartUsedAndCachedClean: () -> Unit,
    onStartAutoAllAppsClean: () -> Unit,
    onStartAutoSelectedClean: () -> Unit,
    onStartOverThresholdClean: () -> Unit,
    onToggleAppSelection: (String) -> Unit,
    onSelectAllApps: (Boolean) -> Unit,
    onApplySelectionPreset: (CacheSelectionPreset) -> Unit,
    onSelectThreshold: (Long) -> Unit,
    onToggleSkipZeroCache: (Boolean) -> Unit,
    onToggleWhitelist: (String) -> Unit,
    onInspectApp: (AppCacheEntry?) -> Unit,
    onClearCacheHistory: () -> Unit,
    onCleanSingleApp: (AppCacheEntry) -> Unit,
    onStartSequentialClean: () -> Unit,
    onLaunchNextGuidedApp: () -> Unit,
    onStopSequentialClean: () -> Unit,
    onDismissAccessibilityPrompt: () -> Unit,
    onDismissUsageAccessDialog: () -> Unit,
    onConsumePendingSettingsPkg: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onSelectCacheMode: (CacheCleanMode) -> Unit = {},
    onExecuteCacheMode: () -> Unit = onStartUsedAndCachedClean,
    onFlushInternalBuffers: () -> Unit = onRunCacheSweep
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val storage = uiState.storageTelemetry

    var isAppsExpanded by rememberSaveable { mutableStateOf(true) }
    var selectedFilter by rememberSaveable { mutableStateOf(CacheFilterTab.ALL) }
    var selectedSort by rememberSaveable { mutableStateOf(CacheSortOption.CACHE_DESC) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isAppsExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "apps_expand_arrow"
    )

    val filteredEntries = remember(
        uiState.cacheEntries,
        selectedFilter,
        selectedSort,
        searchQuery,
        uiState.cacheAlertThresholdBytes
    ) {
        val byTab = when (selectedFilter) {
            CacheFilterTab.ALL -> uiState.cacheEntries
            CacheFilterTab.USED_CACHED -> uiState.cacheEntries.filter {
                it.packageName != context.packageName && it.wasUsedByUser && it.cacheBytes > 0L
            }
            CacheFilterTab.CACHED_ONLY -> uiState.cacheEntries.filter { it.cacheBytes > 0L }
            CacheFilterTab.OVER_LIMIT -> uiState.cacheEntries.filter { it.cacheBytes >= uiState.cacheAlertThresholdBytes }
            CacheFilterTab.DORMANT -> uiState.cacheEntries.filter {
                it.packageName != context.packageName && !it.wasUsedByUser && it.cacheBytes > 0L
            }
            CacheFilterTab.MEDIA_SOCIAL -> uiState.cacheEntries.filter {
                val p = it.packageName.lowercase()
                p.contains("youtube") || p.contains("facebook") || p.contains("instagram") ||
                    p.contains("whatsapp") || p.contains("tiktok") || p.contains("musically") ||
                    p.contains("ugc.trill") || p.contains("telegram") || it.isPriorityMediaApp
            }
            CacheFilterTab.BROWSERS -> uiState.cacheEntries.filter {
                val p = it.packageName.lowercase()
                p.contains("chrome") || p.contains("browser") || p.contains("files") ||
                    p.contains("documents") || p.contains("opera") || p.contains("firefox")
            }
            CacheFilterTab.SHIELDED -> uiState.cacheEntries.filter { it.isWhitelisted }
        }
        val query = searchQuery.trim().lowercase()
        val byQuery = if (query.isEmpty()) {
            byTab
        } else {
            byTab.filter {
                it.appName.lowercase().contains(query) || it.packageName.lowercase().contains(query)
            }
        }
        when (selectedSort) {
            CacheSortOption.CACHE_DESC -> byQuery
            CacheSortOption.FOOTPRINT_DESC -> byQuery.sortedByDescending { it.totalFootprintBytes }
            CacheSortOption.RECENT -> byQuery.sortedByDescending { it.lastUsedTimeMs }
            CacheSortOption.NAME_ASC -> byQuery.sortedBy { it.appName.lowercase() }
        }
    }

    val maxAppCacheBytes = remember(uiState.cacheEntries) {
        uiState.cacheEntries.maxOfOrNull { it.cacheBytes }?.coerceAtLeast(1L) ?: 1L
    }
    val totalCachePool = uiState.totalDetectedAppCacheBytes.coerceAtLeast(1L)
    val usedCachedQueue = remember(uiState.usedAndCachedApps, context.packageName) {
        uiState.usedAndCachedApps.filter { it.packageName != context.packageName }
    }
    val usedCachedBytes = usedCachedQueue.sumOf { it.cacheBytes }
    val selectedQueue = remember(uiState.cacheEntries) {
        uiState.cacheEntries.filter {
            it.packageName != context.packageName &&
                !it.isWhitelisted &&
                it.isSelectedForClean &&
                it.cleanState != AppCacheCleanState.CLEANED
        }
    }
    val selectedBytes = selectedQueue.sumOf { it.cacheBytes }

    // Automatically open the queued app's Android App Info Settings page when requested
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

    // Guided App Info queue launches directly without Accessibility permissions

    if (uiState.showUsageAccessRequiredDialog) {
        AlertDialog(
            onDismissRequest = onDismissUsageAccessDialog,
            containerColor = ObsidianCardElevated,
            titleContentColor = SolarAmber,
            textContentColor = TextPrimary,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Storage,
                    contentDescription = null,
                    tint = SolarAmber,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Usage Access Needed",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Grant Usage Access for Azroia Clean Care Pro so it can detect ONLY the apps you actually used that have cache > 0 B, clean those apps cleanly, and stop automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissUsageAccessDialog()
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarAmber,
                        contentColor = ObsidianVoid
                    ),
                    modifier = Modifier.testTag("dialog_grant_usage_access_button")
                ) {
                    Text("Grant Usage Access")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissUsageAccessDialog,
                    modifier = Modifier.testTag("dialog_cancel_usage_access_button")
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    uiState.inspectedCacheApp?.let { inspected ->
        AppCacheInspectorSheet(
            entry = inspected,
            thresholdBytes = uiState.cacheAlertThresholdBytes,
            onDismiss = { onInspectApp(null) },
            onToggleWhitelist = { onToggleWhitelist(inspected.packageName) },
            onAutoCleanNow = { onCleanSingleApp(inspected) },
            onOpenSystemAppInfo = {
                onInspectApp(null)
                runCatching {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", inspected.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
        )
    }

    val previewNames = uiState.cacheEntries
        .take(3)
        .joinToString(", ") { it.appName }
        .ifBlank { "YouTube, Facebook, Chrome" }

    Box(modifier = modifier.fillMaxSize()) {
        CyberBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModuleTopBar(
                title = CleanerModule.CACHE.title,
                tag = CleanerModule.CACHE.tag,
                accent = NeonCyan,
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CyberGlassCard(
                        accent = NeonCyan,
                        secondaryAccent = SolarAmber
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Advanced Multi-Segment Cache Reactor Graphics
                            CacheReactorHeroCanvas(
                                totalCacheBytes = uiState.totalDetectedAppCacheBytes,
                                removedBytes = uiState.totalCacheFreedSessionBytes,
                                breakdown = uiState.cacheAreaBreakdown,
                                isScanningOrCleaning = uiState.isCleaningCache || uiState.isGuidedQueueActive || uiState.isFlushingInternalBuffers
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Engine Mode Selector Pills (Smart Used / Deep Buffer / Turbo Sweep)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CacheCleanMode.entries.forEach { mode ->
                                    val selected = uiState.selectedCacheMode == mode
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSelectCacheMode(mode) },
                                        label = {
                                            Text(
                                                text = mode.label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonCyan.copy(alpha = 0.22f),
                                            selectedLabelColor = NeonCyan,
                                            containerColor = ObsidianVoid,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selected,
                                            borderColor = CyberBorderMuted,
                                            selectedBorderColor = NeonCyan
                                        ),
                                        modifier = Modifier.testTag("cache_mode_${mode.name.lowercase()}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Accumulated Cache vs Apps With Cache vs Total Cleaned
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CacheStatChip(
                                    label = "TOTAL CACHE",
                                    value = formatBytes(uiState.totalDetectedAppCacheBytes),
                                    color = NeonCyan
                                )
                                CacheStatChip(
                                    label = "APPS W/ CACHE",
                                    value = "${uiState.appsWithCache.size} Apps",
                                    color = SolarAmber
                                )
                                CacheStatChip(
                                    label = "TOTAL CLEANED",
                                    value = formatBytes(uiState.totalCacheFreedSessionBytes),
                                    color = EmeraldPulse
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            GrandTotalCleanedBanner(
                                sessionCleanedBytes = uiState.sessionCleanedCacheBytes,
                                sessionCleanedCount = uiState.sessionCleanedCount,
                                grandTotalCleanedBytes = uiState.totalCacheFreedSessionBytes,
                                totalHistoryCount = uiState.cacheHistory.size
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Total Cache Area Breakdown Visualization
                            CacheAreaDistributionPanel(
                                breakdown = uiState.cacheAreaBreakdown,
                                removedBytes = uiState.totalCacheFreedSessionBytes
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Phone Storage Capacity & Free Bar
                            Surface(
                                color = ObsidianVoid.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderMuted)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Storage,
                                                contentDescription = null,
                                                tint = SolarAmber,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "PHONE STORAGE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SolarAmber
                                            )
                                        }
                                        Text(
                                            text = "${storage.usedPercent}% USED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextPrimary
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { (storage.usedPercent / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color = SolarAmber,
                                        trackColor = Color(0xFF1A243E),
                                        strokeCap = StrokeCap.Round
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Used: ${formatGbShort(storage.usedBytes)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "Free: ${formatGbShort(storage.freeBytes)} / ${formatGbShort(storage.totalBytes)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                            color = EmeraldPulse
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (uiState.isCleaningCache) {
                                LinearProgressIndicator(
                                    progress = { uiState.cacheSweepProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = NeonCyan,
                                    trackColor = Color(0xFF15203E),
                                    strokeCap = StrokeCap.Round
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            uiState.cacheStatusMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldPulse
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Dedicated Primary CTA: Mode-Aware Engine Clean
                            val primaryTitle = when (uiState.selectedCacheMode) {
                                CacheCleanMode.SMART_USED -> if (uiState.hasUsageStatsAccess) {
                                    "CLEAN USED & CACHED (${usedCachedQueue.size} • ${formatBytes(usedCachedBytes)})"
                                } else {
                                    "CLEAN USED & CACHED APPS ONLY"
                                }
                                CacheCleanMode.DEEP_BUFFER -> "FLUSH BUFFERS & WAL (${formatBytes(uiState.internalBufferBreakdown.totalBytes)})"
                                CacheCleanMode.TURBO_SWEEP -> "TURBO SWEEP ALL NON-SHIELDED APPS"
                            }
                            Button(
                                onClick = onExecuteCacheMode,
                                enabled = !uiState.isCleaningCache && !uiState.isGuidedQueueActive && !uiState.isFlushingInternalBuffers,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = ObsidianVoid
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("clean_used_cached_apps_button")
                            ) {
                                Icon(Icons.Filled.Bolt, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = primaryTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = uiState.selectedCacheMode.detail,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.5.sp),
                                        color = ObsidianVoid.copy(alpha = 0.82f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Secondary Auto Clear All Cached Apps
                            Button(
                                onClick = onStartAutoAllAppsClean,
                                enabled = !uiState.isCleaningCache && uiState.cacheEntries.isNotEmpty() && !uiState.isGuidedQueueActive,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ObsidianCardElevated,
                                    contentColor = NeonCyan
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("auto_cache_clear_button")
                            ) {
                                Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.isAccessibilityServiceEnabled) {
                                        "AUTO CLEAR ALL CACHED APPS"
                                    } else {
                                        "AUTO CLEAR CACHE (FAST)"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Selective Batch Auto Clean CTA
                            Button(
                                onClick = onStartAutoSelectedClean,
                                enabled = !uiState.isCleaningCache && selectedQueue.isNotEmpty() && !uiState.isGuidedQueueActive,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPulse,
                                    contentColor = ObsidianVoid
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("clean_selected_apps_cache_button")
                            ) {
                                Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CLEAN SELECTED (${selectedQueue.size} • ${formatBytes(selectedBytes)})",
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onRunCacheSweep,
                                    enabled = !uiState.isCleaningCache,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SolarAmber.copy(alpha = 0.75f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("sweep_local_cache_button")
                                ) {
                                    if (uiState.isCleaningCache) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = SolarAmber,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Sweeping", color = SolarAmber, maxLines = 1)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = null,
                                            tint = SolarAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Deep Scan", color = SolarAmber, maxLines = 1)
                                    }
                                }

                                OutlinedButton(
                                    onClick = onStartSequentialClean,
                                    enabled = uiState.cacheEntries.isNotEmpty() && !uiState.isCleaningCache,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, HyperViolet.copy(alpha = 0.75f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("start_sequential_os_clean_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = HyperViolet,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Queue All", color = HyperViolet, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // CLEAN METHOD STATUS Card (Usage Stats Reader + Accessibility Auto-Clean Method)
                item {
                    val methodSummary = when {
                        uiState.isCleaningCache -> "SCANNING APPS"
                        uiState.isGuidedQueueActive && uiState.isAutoCleanMode ->
                            "AUTO CLEANING ${uiState.guidedQueueIndex + 1}/${uiState.guidedQueueTotal}"
                        uiState.hasUsageStatsAccess && uiState.isAccessibilityServiceEnabled -> "ALL METHODS ACTIVE"
                        uiState.isAccessibilityServiceEnabled -> "AUTO READY"
                        else -> "METHOD SETUP"
                    }
                    val methodBadgeColor = when {
                        uiState.hasUsageStatsAccess && uiState.isAccessibilityServiceEnabled -> EmeraldPulse
                        uiState.isAccessibilityServiceEnabled || uiState.isGuidedQueueActive -> NeonCyan
                        else -> SolarAmber
                    }

                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, methodBadgeColor.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clean_method_status_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CLEAN METHOD STATUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = methodBadgeColor
                                )
                                Surface(
                                    color = methodBadgeColor.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(50),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        methodBadgeColor.copy(alpha = 0.55f)
                                    )
                                ) {
                                    Text(
                                        text = methodSummary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = methodBadgeColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MethodStatusTile(
                                    title = "Cache Size Method",
                                    statusText = if (uiState.hasUsageStatsAccess) "ACTIVE" else "GRANT",
                                    isActive = uiState.hasUsageStatsAccess,
                                    accent = SolarAmber,
                                    onClick = {
                                        if (!uiState.hasUsageStatsAccess) {
                                            runCatching {
                                                context.startActivity(
                                                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("method_status_usage_tile")
                                )

                                MethodStatusTile(
                                    title = "Clean Engine",
                                    statusText = "PLAY-SAFE",
                                    isActive = true,
                                    accent = NeonCyan,
                                    onClick = onFlushInternalBuffers,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("method_status_auto_tile")
                                )
                            }
                        }
                    }
                }

                // Smart Cache Guard & Quick Presets Panel
                item {
                    val overLimitApps = uiState.overThresholdApps
                    SmartCacheGuardPanel(
                        selectedThresholdBytes = uiState.cacheAlertThresholdBytes,
                        overThresholdCount = overLimitApps.size,
                        overThresholdTotalBytes = overLimitApps.sumOf { it.cacheBytes },
                        skipZeroCacheAutoClean = uiState.skipZeroCacheAutoClean,
                        internalBuffer = uiState.internalBufferBreakdown,
                        onSelectThreshold = onSelectThreshold,
                        onApplyPreset = onApplySelectionPreset,
                        onToggleSkipZero = onToggleSkipZeroCache,
                        onCleanOverThreshold = onStartOverThresholdClean,
                        isFlushingBuffers = uiState.isFlushingInternalBuffers,
                        lastBufferFlushFreedBytes = uiState.lastBufferFlushFreedBytes,
                        onFlushBuffersNow = onFlushInternalBuffers
                    )
                }

                // Live Apps With Cache & Usage Access Unlock Summary
                item {
                    AppsWithCacheSummaryCard(
                        appsWithCache = uiState.appsWithCache,
                        totalDetectedCacheBytes = uiState.totalDetectedAppCacheBytes,
                        internalBufferBytes = uiState.internalBufferBreakdown.totalBytes,
                        hasUsageStatsAccess = uiState.hasUsageStatsAccess,
                        onGrantUsageAccess = {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                )
                            }
                        },
                        onOpenRestrictedSettingsHelp = {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                )
                            }
                        },
                        onCleanSingleApp = onCleanSingleApp,
                        onInspectApp = onInspectApp
                    )
                }

                // Top Cache Hogs Radar Visualization
                item {
                    TopCacheHogsVisualCard(
                        entries = uiState.cacheEntries,
                        thresholdBytes = uiState.cacheAlertThresholdBytes,
                        onInspectApp = { onInspectApp(it) },
                        onQuickCleanApp = { onCleanSingleApp(it) }
                    )
                }

                // Cache Health Score, Savings Chart & Clean History Log
                item {
                    CacheHealthAndHistoryCard(
                        healthScore = uiState.cacheHealthScore,
                        history = uiState.cacheHistory,
                        totalRemovedBytes = uiState.totalCacheFreedSessionBytes,
                        onClearHistory = onClearCacheHistory
                    )
                }

                // Expandable Storage App Cache Breakdown Row with Arrow (YouTube, Facebook, Chrome & all apps)
                item {
                    Surface(
                        color = ObsidianCardElevated,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.3.dp,
                            if (isAppsExpanded) NeonCyan else SolarAmber.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { isAppsExpanded = !isAppsExpanded }
                            .testTag("app_cache_expand_arrow_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NeonCyan.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FolderCopy,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Ranked Apps Cache",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Surface(
                                        color = SolarAmber.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = formatBytes(uiState.totalDetectedAppCacheBytes),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = SolarAmber,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (uiState.whitelistedCachePackages.isNotEmpty()) {
                                        Surface(
                                            color = EmeraldPulse.copy(alpha = 0.16f),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = "${uiState.whitelistedCachePackages.size} Shielded",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                                color = EmeraldPulse,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isAppsExpanded) {
                                        "Search, sort, inspect telemetry & clean per-app cache"
                                    } else {
                                        "Tap arrow to see $previewNames (${uiState.cacheEntries.size} apps)"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = if (isAppsExpanded) "Collapse app cache list" else "Expand app cache list",
                                    tint = NeonCyan,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .rotate(arrowRotation)
                                )
                            }
                        }
                    }
                }

                // Expanded Apps Cache Search, Filter Chips, Sort Chips & Ranked List
                if (isAppsExpanded) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Search app name or package (e.g. YouTube, Chrome)...",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Search apps",
                                        tint = NeonCyan
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.testTag("clear_cache_search_button")
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
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberBorderMuted,
                                    focusedContainerColor = ObsidianCard,
                                    unfocusedContainerColor = ObsidianCard,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = NeonCyan
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cache_search_input")
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CacheFilterTab.entries.forEach { tab ->
                                    val selected = selectedFilter == tab
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedFilter = tab },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonCyan.copy(alpha = 0.22f),
                                            selectedLabelColor = NeonCyan,
                                            containerColor = ObsidianCard,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selected,
                                            borderColor = CyberBorderMuted,
                                            selectedBorderColor = NeonCyan
                                        ),
                                        modifier = Modifier.testTag("cache_filter_${tab.name.lowercase()}")
                                    )
                                }
                            }

                            // Sort Mode Selector Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SORT:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = SolarAmber
                                )
                                CacheSortOption.entries.forEach { sortOpt ->
                                    val selected = selectedSort == sortOpt
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedSort = sortOpt },
                                        label = {
                                            Text(
                                                text = sortOpt.label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp)
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
                                            selected = selected,
                                            borderColor = CyberBorderMuted,
                                            selectedBorderColor = SolarAmber
                                        ),
                                        modifier = Modifier.testTag("cache_sort_${sortOpt.name.lowercase()}")
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SHOWING ${filteredEntries.size} APPS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan
                                )
                                val cleanableEntries = uiState.cacheEntries.filter { !it.isWhitelisted }
                                val allChecked = cleanableEntries.isNotEmpty() &&
                                    cleanableEntries.all { it.isSelectedForClean }
                                TextButton(
                                    onClick = { onSelectAllApps(!allChecked) },
                                    modifier = Modifier.testTag("toggle_select_all_cache_apps")
                                ) {
                                    Text(
                                        text = if (allChecked) "Deselect All" else "Select All",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = SolarAmber
                                    )
                                }
                            }
                        }
                    }

                    if (!uiState.hasUsageStatsAccess) {
                        item {
                            Surface(
                                color = ObsidianCardElevated,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SolarAmber.copy(alpha = 0.65f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Show Exact App Cache Sizes",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = SolarAmber
                                        )
                                        Text(
                                            text = "Grant Usage Access to view MB/GB cache inside Chrome, YouTube, Facebook & all apps.",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                            color = TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            runCatching {
                                                context.startActivity(
                                                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                )
                                            }
                                        },
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SolarAmber),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("grant_usage_access_button")
                                    ) {
                                        Text("Grant", color = SolarAmber)
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(filteredEntries, key = { _, item -> item.packageName }) { index, entry ->
                        AppCacheRow(
                            rank = index + 1,
                            entry = entry,
                            maxCacheBytes = maxAppCacheBytes,
                            totalCachePoolBytes = totalCachePool,
                            thresholdBytes = uiState.cacheAlertThresholdBytes,
                            hasUsageAccess = uiState.hasUsageStatsAccess,
                            isAutoCleanEnabled = uiState.isAccessibilityServiceEnabled,
                            onToggleSelect = { onToggleAppSelection(entry.packageName) },
                            onInspectApp = { onInspectApp(entry) },
                            onClearAppCache = { onCleanSingleApp(entry) }
                        )
                    }
                }

                // Active Sequential / Auto Queue Controller Card
                if (uiState.isGuidedQueueActive && uiState.activeGuidedApp != null) {
                    val guidedApp = uiState.activeGuidedApp
                    item {
                        Surface(
                            color = ObsidianCardElevated,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SolarAmber)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (uiState.isAutoCleanMode) {
                                        "AUTO CACHE CLEAN (${uiState.guidedQueueIndex + 1} / ${uiState.guidedQueueTotal})"
                                    } else {
                                        "SEQUENTIAL APP CLEAN (${uiState.guidedQueueIndex + 1} / ${uiState.guidedQueueTotal})"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SolarAmber
                                )
                                Text(
                                    text = "Target: ${guidedApp.appName} (${formatBytes(guidedApp.cacheBytes)})",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (uiState.isAutoCleanMode) {
                                        "Azroia Auto Cache Cleaner opens App Info → Storage & cache → Clear cache automatically."
                                    } else {
                                        "1. Open App Info  →  2. Tap Storage & Cache  →  3. Clear Cache  →  4. Press Back to auto-advance"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = onLaunchNextGuidedApp,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SolarAmber,
                                            contentColor = ObsidianVoid
                                        ),
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text("Open ${guidedApp.appName}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    OutlinedButton(
                                        onClick = onStopSequentialClean,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderMuted),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Icon(Icons.Filled.Stop, contentDescription = null, tint = TextSecondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Stop", color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Play-Safe Direct Buffer & Guided App Info Status Banner
                item {
                    Surface(
                        color = EmeraldPulse.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            EmeraldPulse.copy(alpha = 0.55f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = EmeraldPulse,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Play-Safe Cache Engine: ACTIVE",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EmeraldPulse
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Internal buffers & logs flush automatically; third-party apps open safely via standard Android App Info without Accessibility hooks.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodStatusTile(
    title: String,
    statusText: String,
    isActive: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = if (isActive) EmeraldPulse else accent
    Surface(
        color = activeColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.55f)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = activeColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CacheStatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Composable
private fun AppCacheRow(
    rank: Int,
    entry: AppCacheEntry,
    maxCacheBytes: Long,
    totalCachePoolBytes: Long,
    thresholdBytes: Long,
    hasUsageAccess: Boolean,
    isAutoCleanEnabled: Boolean,
    onToggleSelect: () -> Unit,
    onInspectApp: () -> Unit,
    onClearAppCache: () -> Unit
) {
    val isOverThreshold = !entry.isWhitelisted &&
        entry.cleanState != AppCacheCleanState.CLEANED &&
        entry.cacheBytes >= thresholdBytes

    val heatColor = when {
        entry.isWhitelisted -> EmeraldPulse
        entry.cleanState == AppCacheCleanState.CLEANED -> EmeraldPulse
        isOverThreshold || entry.cacheBytes >= 150L * 1024L * 1024L -> ElectricCrimson
        entry.cacheBytes >= 30L * 1024L * 1024L -> WarningOrange
        entry.cacheBytes > 0L -> NeonCyan
        else -> TextSecondary
    }
    val heatTag = when {
        entry.isWhitelisted -> "SHIELDED"
        entry.cleanState == AppCacheCleanState.CLEANED -> "CLEANED"
        isOverThreshold -> "OVER LIMIT"
        entry.cacheBytes >= 150L * 1024L * 1024L -> "HEAVY HOG"
        entry.cacheBytes >= 30L * 1024L * 1024L -> "HIGH CACHE"
        entry.isPriorityMediaApp -> "MEDIA / APP"
        else -> "APP"
    }
    val sharePercent = ((entry.cacheBytes.toDouble() / totalCachePoolBytes.toDouble()) * 100.0)
        .toInt()
        .coerceIn(0, 100)
    val barProgress = if (entry.cacheBytes > 0L) {
        (entry.cacheBytes.toFloat() / maxCacheBytes.toFloat()).coerceIn(0.06f, 1f)
    } else 0f

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = when {
                entry.isWhitelisted -> EmeraldPulse.copy(alpha = 0.55f)
                entry.cleanState == AppCacheCleanState.CLEANING -> NeonCyan
                entry.cleanState == AppCacheCleanState.CLEANED -> EmeraldPulse.copy(alpha = 0.6f)
                entry.cleanState == AppCacheCleanState.SYSTEM_ACTION_READY -> SolarAmber.copy(alpha = 0.55f)
                entry.isSelectedForClean -> heatColor.copy(alpha = 0.45f)
                else -> CyberBorderMuted
            }
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onInspectApp)
            .testTag("cache_row_${entry.packageName}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.isWhitelisted) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(onClick = onInspectApp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Shielded app",
                            tint = EmeraldPulse,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    Checkbox(
                        checked = entry.isSelectedForClean,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonCyan,
                            uncheckedColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("cache_select_${entry.packageName}")
                    )
                }

                if (entry.icon != null) {
                    Image(
                        bitmap = entry.icon,
                        contentDescription = "${entry.appName} icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Android, contentDescription = null, tint = NeonCyan)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Surface(
                            color = if (rank <= 3) SolarAmber.copy(alpha = 0.2f) else CyberBorderMuted.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "#$rank",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (rank <= 3) SolarAmber else TextSecondary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = entry.appName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Surface(
                            color = heatColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = heatTag,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = heatColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (entry.wasUsedByUser && entry.cacheBytes > 0L && !entry.isWhitelisted) {
                            Surface(
                                color = NeonCyan.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "USED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                    color = NeonCyan,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        val sizeSubtext = when {
                            entry.cleanState == AppCacheCleanState.CLEANED -> "Cleaned • ${formatBytes(entry.cacheBytes)} left"
                            entry.cacheBytes > 0L -> "${formatBytes(entry.cacheBytes)} ($sharePercent%)"
                            hasUsageAccess -> "0 B cache"
                            else -> "Grant Usage"
                        }
                        Text(
                            text = sizeSubtext,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                            color = heatColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onInspectApp,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("inspect_app_button_${entry.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = "Inspect ${entry.appName} cache details",
                        tint = TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                when (entry.cleanState) {
                    AppCacheCleanState.CLEANING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NeonCyan,
                            strokeWidth = 2.5.dp
                        )
                    }
                    AppCacheCleanState.CLEANED -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Cleaned",
                            tint = EmeraldPulse,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    AppCacheCleanState.PENDING,
                    AppCacheCleanState.SYSTEM_ACTION_READY -> {
                        OutlinedButton(
                            onClick = onClearAppCache,
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAutoCleanEnabled || entry.cacheBytes > 0L) NeonCyan else SolarAmber
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("clear_app_button_${entry.packageName}")
                        ) {
                            Icon(
                                imageVector = if (isAutoCleanEnabled) Icons.Filled.Bolt else Icons.Filled.Tune,
                                contentDescription = null,
                                tint = if (isAutoCleanEnabled || entry.cacheBytes > 0L) NeonCyan else SolarAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isAutoCleanEnabled) "Auto" else "Clean",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
                                color = if (isAutoCleanEnabled || entry.cacheBytes > 0L) NeonCyan else SolarAmber
                            )
                        }
                    }
                }
            }

            // Directly under the app: App/Data/LastUsed telemetry + proportional Cache Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (entry.appBytes > 0L || entry.dataBytes > 0L || entry.lastUsedTimeMs > 0L) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "App: ${formatBytes(entry.appBytes)} • Data: ${formatBytes(entry.dataBytes)} • ${formatRelativeTime(entry.lastUsedTimeMs)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.5.sp),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cache: ${formatBytes(entry.cacheBytes)}",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                            color = heatColor
                        )
                    }
                }
                if (barProgress > 0f) {
                    LinearProgressIndicator(
                        progress = { barProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = heatColor,
                        trackColor = ObsidianVoid,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
