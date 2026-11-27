package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CleanerModule
import com.example.model.RamBoostMode
import com.example.model.RamProcessEntry
import com.example.ui.AzroiaUiState
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.KernelZramTelemetryCard
import com.example.ui.components.ModuleTopBar
import com.example.ui.components.RamAnalyticsAndHistoryCard
import com.example.ui.components.RamMemoryAllocationPanel
import com.example.ui.components.RamNeuralReactorCanvas
import com.example.ui.components.RamProcessInspectorSheet
import com.example.ui.components.SmartRamGuardCard
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
import com.example.util.formatBytes
import com.example.util.formatRelativeTime

private enum class RamFilterTab(val label: String) {
    ALL("All"),
    ACTIVE("Active Running"),
    CACHED("Cached"),
    SHIELDED("Shielded")
}

private enum class RamSortOption(val label: String) {
    PRIORITY("Priority"),
    PSS_DESC("PSS ↓"),
    RECENT("Recent"),
    NAME_ASC("A–Z")
}

@Composable
fun RamOptimizerScreen(
    uiState: AzroiaUiState,
    onToggleAppSelection: (String) -> Unit,
    onSelectBoostMode: (RamBoostMode) -> Unit,
    onSelectThreshold: (Int) -> Unit,
    onToggleLiveMonitor: (Boolean) -> Unit,
    onCompactHeap: () -> Unit,
    onStartDeepHibernate: () -> Unit,
    onStartGuidedHibernateFallback: () -> Unit,
    onHibernateSingleApp: (RamProcessEntry) -> Unit,
    onLaunchNextHibernateApp: () -> Unit,
    onStopHibernateQueue: () -> Unit,
    onDismissRamAccessibilityPrompt: () -> Unit,
    onConsumePendingSettingsPkg: () -> Unit,
    onInspectRamApp: (RamProcessEntry?) -> Unit,
    onApplyPreset: (String) -> Unit,
    onToggleWhitelist: (String) -> Unit,
    onRefreshProcesses: () -> Unit,
    onClearHistory: () -> Unit,
    onOptimizeRam: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val ram = uiState.ramTelemetry
    val selectedMode = uiState.selectedRamBoostMode
    val selectedCount = ram.backgroundApps.count { it.isSelected && !it.isWhitelisted && !it.isTerminated }
    val selectedPssBytes = ram.backgroundApps
        .filter { it.isSelected && !it.isWhitelisted && !it.isTerminated }
        .sumOf { it.processPssBytes }

    LaunchedEffect(uiState.pendingOpenSettingsPackage, uiState.isRamHibernateQueueActive) {
        val pkg = uiState.pendingOpenSettingsPackage
        if (pkg != null && uiState.isRamHibernateQueueActive) {
            onConsumePendingSettingsPkg()
            runCatching {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", pkg, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(RamFilterTab.ALL) }
    var selectedSort by rememberSaveable { mutableStateOf(RamSortOption.PRIORITY) }

    val animatedProgress by animateFloatAsState(
        targetValue = uiState.ramOptimizationProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 220),
        label = "ram_opt_progress"
    )

    val activeCount = remember(ram.backgroundApps) { ram.backgroundApps.count { it.isActiveProcess } }
    val shieldedCount = remember(ram.backgroundApps) { ram.backgroundApps.count { it.isWhitelisted } }

    val filteredApps = remember(ram.backgroundApps, searchQuery, selectedFilter, selectedSort) {
        val query = searchQuery.trim().lowercase()
        val filtered = ram.backgroundApps.filter { entry ->
            val matchesQuery = query.isBlank() ||
                entry.appName.lowercase().contains(query) ||
                entry.packageName.lowercase().contains(query)
            val matchesTab = when (selectedFilter) {
                RamFilterTab.ALL -> true
                RamFilterTab.ACTIVE -> entry.isActiveProcess
                RamFilterTab.CACHED -> !entry.isActiveProcess && !entry.isWhitelisted
                RamFilterTab.SHIELDED -> entry.isWhitelisted
            }
            matchesQuery && matchesTab
        }

        when (selectedSort) {
            RamSortOption.PRIORITY -> filtered.sortedWith(
                compareByDescending<RamProcessEntry> { it.isActiveProcess }
                    .thenByDescending { it.processPssBytes }
                    .thenByDescending { it.importanceLabel == "Cached Process" }
                    .thenBy { it.appName.lowercase() }
            )
            RamSortOption.PSS_DESC -> filtered.sortedWith(
                compareByDescending<RamProcessEntry> { it.processPssBytes }
                    .thenByDescending { it.isActiveProcess }
                    .thenBy { it.appName.lowercase() }
            )
            RamSortOption.RECENT -> filtered.sortedWith(
                compareByDescending<RamProcessEntry> { it.lastUsedTimeMs }
                    .thenByDescending { it.isActiveProcess }
                    .thenBy { it.appName.lowercase() }
            )
            RamSortOption.NAME_ASC -> filtered.sortedBy { it.appName.lowercase() }
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
                // 1. Neural Reactor Hero Card + Allocation Matrix + Boost Mode + Turbo Release CTA
                item {
                    val ramHeroAccent = if (ram.isLowMemory || ram.usedPercent >= uiState.ramPressureThresholdPercent) {
                        ElectricCrimson
                    } else {
                        HyperViolet
                    }
                    CyberGlassCard(
                        accent = ramHeroAccent,
                        secondaryAccent = NeonCyan,
                        modifier = Modifier.testTag("ram_hero_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            RamNeuralReactorCanvas(
                                usedPercent = ram.usedPercent,
                                totalBytes = ram.totalBytes,
                                usedBytes = ram.usedBytes,
                                availableBytes = ram.availableBytes,
                                thresholdBytes = ram.lowMemoryThresholdBytes,
                                isLowMemory = ram.isLowMemory,
                                isOptimizing = uiState.isOptimizingRam || uiState.isRamHibernateQueueActive
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            RamMemoryAllocationPanel(ram = ram)

                            Spacer(modifier = Modifier.height(14.dp))

                            // Boost Mode Selector
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Tune,
                                            contentDescription = null,
                                            tint = HyperViolet,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "BOOST ENGINE PROFILE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HyperViolet
                                        )
                                    }
                                    Text(
                                        text = selectedMode.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RamBoostMode.entries.forEach { mode ->
                                        val isSelected = selectedMode == mode
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onSelectBoostMode(mode) },
                                            label = {
                                                Text(
                                                    text = mode.label,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = HyperViolet.copy(alpha = 0.25f),
                                                selectedLabelColor = TextPrimary,
                                                containerColor = ObsidianVoid,
                                                labelColor = TextSecondary
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = CyberBorderMuted,
                                                selectedBorderColor = HyperViolet
                                            ),
                                            modifier = Modifier.testTag("ram_mode_${mode.name.lowercase()}")
                                        )
                                    }
                                }

                                Text(
                                    text = selectedMode.detail,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = TextSecondary
                                )
                            }

                            if (uiState.isOptimizingRam) {
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = NeonCyan,
                                    trackColor = HyperViolet.copy(alpha = 0.25f),
                                    strokeCap = StrokeCap.Round
                                )
                            }

                            uiState.ramStatusMessage?.let { msg ->
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
                                onClick = onOptimizeRam,
                                enabled = !uiState.isOptimizingRam && selectedCount > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HyperViolet,
                                    contentColor = Color.White,
                                    disabledContainerColor = CyberBorderMuted,
                                    disabledContentColor = TextSecondary
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
                                    Icon(Icons.Filled.Bolt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val pssSuffix = if (selectedPssBytes > 0L) " • ${formatBytes(selectedPssBytes)}" else ""
                                    Text(
                                        text = "TURBO RELEASE ($selectedCount APPS$pssSuffix)",
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Deep Hibernate Queue Stepper
                if (uiState.isRamHibernateQueueActive && uiState.activeHibernateApp != null) {
                    item {
                        val activeApp = uiState.activeHibernateApp
                        Surface(
                            color = ObsidianCardElevated,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.3.dp, ElectricCrimson.copy(alpha = 0.72f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ram_hibernate_queue_card")
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
                                        text = "DEEP HIBERNATE (${uiState.ramHibernateQueueIndex + 1}/${uiState.ramHibernateQueueTotal})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCrimson
                                    )
                                    IconButton(
                                        onClick = onStopHibernateQueue,
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("stop_hibernate_queue_button")
                                    ) {
                                        Icon(Icons.Filled.Stop, contentDescription = "Stop Deep Hibernate", tint = TextSecondary)
                                    }
                                }
                                Text(
                                    text = "Force Stopping: ${activeApp.appName}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Button(
                                    onClick = onLaunchNextHibernateApp,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricCrimson,
                                        contentColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("next_hibernate_app_button")
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open App Info → Force Stop", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }

                // 2. Smart RAM Guard, Compact Heap & Deep Hibernate Panel
                item {
                    SmartRamGuardCard(
                        ram = ram,
                        pressureThresholdPercent = uiState.ramPressureThresholdPercent,
                        isLiveMonitorEnabled = uiState.isLiveRamMonitorEnabled,
                        isOptimizing = uiState.isOptimizingRam || uiState.isRamHibernateQueueActive,
                        onSelectThreshold = onSelectThreshold,
                        onToggleLiveMonitor = onToggleLiveMonitor,
                        onCompactHeap = onCompactHeap,
                        onStartDeepHibernate = onStartDeepHibernate
                    )
                }

                // 3. Kernel Page Cache & ZRAM Engine
                item {
                    KernelZramTelemetryCard(kernel = ram.kernelBreakdown)
                }

                // 4. Memory Health Score, VM Heap Class & Boost History Timeline
                item {
                    RamAnalyticsAndHistoryCard(
                        ram = ram,
                        history = uiState.ramBoostHistory,
                        onClearHistory = onClearHistory
                    )
                }

                // 5. Background Process Inspector: Search, Filters, Sort & Presets
                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CyberBorderMuted),
                        modifier = Modifier.fillMaxWidth()
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Memory,
                                        contentDescription = null,
                                        tint = HyperViolet,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PROCESS INSPECTOR (${filteredApps.size}/${ram.backgroundApps.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HyperViolet
                                    )
                                }
                                IconButton(
                                    onClick = onRefreshProcesses,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("ram_refresh_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Refresh live background processes",
                                        tint = NeonCyan
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Search background app or package...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Search, contentDescription = null, tint = HyperViolet)
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.testTag("ram_search_clear")
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
                                    focusedBorderColor = HyperViolet,
                                    unfocusedBorderColor = CyberBorderMuted,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = ObsidianVoid,
                                    unfocusedContainerColor = ObsidianVoid
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ram_search_input")
                            )

                            // Filter Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RamFilterTab.entries.forEach { tab ->
                                    val countBadge = when (tab) {
                                        RamFilterTab.ALL -> "${ram.backgroundApps.size}"
                                        RamFilterTab.ACTIVE -> "$activeCount"
                                        RamFilterTab.CACHED -> "${ram.backgroundApps.count { !it.isActiveProcess && !it.isWhitelisted }}"
                                        RamFilterTab.SHIELDED -> "$shieldedCount"
                                    }
                                    val isSelected = selectedFilter == tab
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedFilter = tab },
                                        label = {
                                            Text(
                                                text = "${tab.label} ($countBadge)",
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
                                        modifier = Modifier.testTag("ram_filter_${tab.name.lowercase()}")
                                    )
                                }
                            }

                            // Sort + Quick Selection Presets
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RamSortOption.entries.forEach { sort ->
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
                                        modifier = Modifier.testTag("ram_sort_${sort.name.lowercase()}")
                                    )
                                }

                                QuickPresetChip("Active", "ram_preset_active") { onApplyPreset("ACTIVE") }
                                QuickPresetChip("High PSS", "ram_preset_high_pss") { onApplyPreset("HIGH_PSS") }
                                QuickPresetChip("All", "ram_preset_all") { onApplyPreset("ALL") }
                                QuickPresetChip("Clear", "ram_preset_none") { onApplyPreset("NONE") }
                            }
                        }
                    }
                }

                if (filteredApps.isEmpty()) {
                    item {
                        Surface(
                            color = ObsidianCard,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, CyberBorderMuted),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Memory,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "No matching background processes",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Try switching the filter tab or clearing your search query.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(filteredApps, key = { _, app -> app.packageName }) { idx, app ->
                        RamAppRow(
                            rank = idx + 1,
                            entry = app,
                            onInspect = { onInspectRamApp(app) },
                            onForceStopSingle = { onHibernateSingleApp(app) },
                            onToggleSelection = { onToggleAppSelection(app.packageName) },
                            onToggleShield = { onToggleWhitelist(app.packageName) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // Guided Force-Stop opens Android App Info directly without Accessibility permissions

    uiState.inspectedRamApp?.let { inspected ->
        RamProcessInspectorSheet(
            entry = inspected,
            onDismiss = { onInspectRamApp(null) },
            onToggleWhitelist = { onToggleWhitelist(inspected.packageName) },
            onForceStopApp = { onHibernateSingleApp(inspected) }
        )
    }
}

@Composable
private fun QuickPresetChip(
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        color = ObsidianCardElevated,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, HyperViolet.copy(alpha = 0.55f)),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = HyperViolet,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun RamAppRow(
    rank: Int,
    entry: RamProcessEntry,
    onInspect: () -> Unit,
    onForceStopSingle: () -> Unit,
    onToggleSelection: () -> Unit,
    onToggleShield: () -> Unit
) {
    val borderColor = when {
        entry.isWhitelisted -> EmeraldPulse.copy(alpha = 0.6f)
        entry.isTerminated -> EmeraldPulse.copy(alpha = 0.55f)
        entry.isActiveProcess && entry.isSelected -> ElectricCrimson.copy(alpha = 0.65f)
        entry.isSelected -> HyperViolet.copy(alpha = 0.55f)
        else -> CyberBorderMuted
    }

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onInspect)
            .testTag("ram_row_${entry.packageName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = if (entry.isActiveProcess) ElectricCrimson else TextMuted,
                modifier = Modifier.width(26.dp)
            )

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

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = entry.appName,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    val (badgeText, badgeColor) = when {
                        entry.isWhitelisted -> "SHIELDED" to EmeraldPulse
                        entry.isTerminated -> "RELEASED" to EmeraldPulse
                        entry.isActiveProcess -> "ACTIVE" to ElectricCrimson
                        entry.isSystemApp -> "SYSTEM" to SolarAmber
                        else -> "CACHED" to NeonCyan
                    }
                    Surface(
                        color = badgeColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.6.dp, badgeColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val metaLine = buildString {
                    if (entry.processPssBytes > 0L) {
                        append(formatBytes(entry.processPssBytes))
                        append(" PSS • ")
                    }
                    append(if (entry.isTerminated) "Memory released" else entry.importanceLabel)
                    if (entry.lastUsedTimeMs > 0L) {
                        append(" • ")
                        append(formatRelativeTime(entry.lastUsedTimeMs))
                    }
                }
                Text(
                    text = metaLine,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                    color = if (entry.isTerminated) EmeraldPulse else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!entry.isWhitelisted) {
                IconButton(
                    onClick = onForceStopSingle,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("ram_force_stop_${entry.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = "Force stop ${entry.appName}",
                        tint = if (entry.isActiveProcess) ElectricCrimson else SolarAmber,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            IconButton(
                onClick = onToggleShield,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("ram_shield_${entry.packageName}")
            ) {
                Icon(
                    imageVector = if (entry.isWhitelisted) Icons.Filled.Shield else Icons.Filled.Security,
                    contentDescription = if (entry.isWhitelisted) "Remove shield for ${entry.appName}" else "Shield ${entry.appName}",
                    tint = if (entry.isWhitelisted) EmeraldPulse else TextMuted,
                    modifier = Modifier.size(19.dp)
                )
            }

            if (entry.isTerminated) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Stopped",
                        tint = EmeraldPulse,
                        modifier = Modifier.size(21.dp)
                    )
                }
            } else {
                Checkbox(
                    checked = entry.isSelected && !entry.isWhitelisted,
                    enabled = !entry.isWhitelisted,
                    onCheckedChange = { onToggleSelection() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = HyperViolet,
                        uncheckedColor = TextSecondary,
                        disabledCheckedColor = EmeraldPulse.copy(alpha = 0.4f),
                        disabledUncheckedColor = TextMuted
                    ),
                    modifier = Modifier.testTag("ram_select_${entry.packageName}")
                )
            }
        }
    }
}
