package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SystemDiagnosticsRepository
import com.example.model.AppCacheCleanState
import com.example.model.AppCacheEntry
import com.example.model.BatteryOptimizationMode
import com.example.model.BatteryTelemetry
import com.example.model.CacheAreaBreakdown
import com.example.model.CacheCleanHistoryRecord
import com.example.model.CacheSelectionPreset
import com.example.model.CleanerModule
import com.example.model.InternalBufferBreakdown
import com.example.model.RamBoostHistoryRecord
import com.example.model.RamBoostMode
import com.example.model.RamProcessEntry
import com.example.model.RamTelemetry
import com.example.model.RiskLevel
import com.example.model.ScreenRoute
import com.example.model.SecurityPosture
import com.example.model.StorageBoostBenchmark
import com.example.model.StorageBoostMode
import com.example.model.StorageCleanHistoryRecord
import com.example.model.StorageTelemetry
import com.example.model.TempFileCategory
import com.example.model.TempFileEntry
import com.example.service.AutoAutomationMode
import com.example.service.AutoCacheCoordinator
import com.example.util.formatBytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AzroiaUiState(
    val route: ScreenRoute = ScreenRoute.Splash,
    val isLaunchBurstActive: Boolean = false,
    val hasUsageStatsAccess: Boolean = false,
    val isLoadingTelemetry: Boolean = true,

    // Module 1: Dedicated Sequential & Auto Accessibility Cache Clear
    val cacheEntries: List<AppCacheEntry> = emptyList(),
    val ownAppCacheBytes: Long = 0L,
    val isCleaningCache: Boolean = false,
    val cacheSweepProgress: Float = 0f,
    val cacheStatusMessage: String? = null,
    val totalCacheFreedSessionBytes: Long = 0L,
    val isAccessibilityServiceEnabled: Boolean = false,
    val showAccessibilityPrompt: Boolean = false,
    val showUsageAccessRequiredDialog: Boolean = false,
    val isAutoCleanMode: Boolean = false,
    val isGuidedQueueActive: Boolean = false,
    val guidedQueueIndex: Int = 0,
    val guidedQueueTotal: Int = 0,
    val guidedQueuePackages: List<String> = emptyList(),
    val guidedQueueModeLabel: String = "",
    val activeGuidedApp: AppCacheEntry? = null,
    val pendingOpenSettingsPackage: String? = null,
    val cacheHistory: List<CacheCleanHistoryRecord> = emptyList(),
    val whitelistedCachePackages: Set<String> = emptySet(),
    val cacheAlertThresholdBytes: Long = 150L * 1024L * 1024L,
    val skipZeroCacheAutoClean: Boolean = true,
    val internalBufferBreakdown: InternalBufferBreakdown = InternalBufferBreakdown(),
    val inspectedCacheApp: AppCacheEntry? = null,

    // Module 2: RAM Optimization
    val ramTelemetry: RamTelemetry = RamTelemetry(),
    val selectedRamBoostMode: RamBoostMode = RamBoostMode.BALANCED,
    val ramPressureThresholdPercent: Int = 80,
    val isOptimizingRam: Boolean = false,
    val ramOptimizationProgress: Float = 0f,
    val ramStatusMessage: String? = null,
    val ramBoostHistory: List<RamBoostHistoryRecord> = emptyList(),
    val whitelistedRamPackages: Set<String> = emptySet(),
    val isRamHibernateQueueActive: Boolean = false,
    val ramHibernateQueueIndex: Int = 0,
    val ramHibernateQueueTotal: Int = 0,
    val ramHibernateQueuePackages: List<String> = emptyList(),
    val activeHibernateApp: RamProcessEntry? = null,
    val showRamAccessibilityPrompt: Boolean = false,
    val ramHibernateBeforeAvailBytes: Long = 0L,
    val ramHibernateBeforePercent: Int = 0,
    val isLiveRamMonitorEnabled: Boolean = false,
    val inspectedRamApp: RamProcessEntry? = null,

    // Module 3: Storage Clear, Boost & Deep Junk / Large File Cleaner
    val storageTelemetry: StorageTelemetry = StorageTelemetry(),
    val selectedStorageBoostMode: StorageBoostMode = StorageBoostMode.TURBO_COMPACT,
    val includeApksInStorageBoost: Boolean = false,
    val autoSweepSafInBoost: Boolean = true,
    val isBoostingStorage: Boolean = false,
    val storageBoostProgress: Float = 0f,
    val storageBoostStageIndex: Int = 0,
    val storageStatusMessage: String? = null,
    val storageBoostResultSummary: StorageBoostBenchmark? = null,
    val tempFiles: List<TempFileEntry> = emptyList(),
    val scannedFolderUri: Uri? = null,
    val storageLargeThresholdBytes: Long = 25L * 1024L * 1024L,
    val storageCleanHistory: List<StorageCleanHistoryRecord> = emptyList(),
    val isScanningTempFiles: Boolean = false,
    val isCleaningTempFiles: Boolean = false,
    val tempFilesStatusMessage: String? = null,
    val lastTempFreedBytes: Long = 0L,

    // Module 4: Zero Virus Deep Shield
    val securityPosture: SecurityPosture = SecurityPosture(),
    val autoDeleteUnnecessaryFiles: Boolean = true,
    val isScanningVirus: Boolean = false,
    val virusScanProgress: Float = 0f,
    val currentScannedAppName: String = "",
    val currentScanLayer: String = "",
    val virusStatusMessage: String? = null,
    val isNeutralizingThreats: Boolean = false,

    // Module 5: VoltCore Battery Guard
    val batteryTelemetry: BatteryTelemetry = BatteryTelemetry(),
    val selectedBatteryMode: BatteryOptimizationMode = BatteryOptimizationMode.BALANCED,
    val isBatteryOptimizationEnabled: Boolean = true,
    val autoCleanCacheForBattery: Boolean = true,
    val isOptimizingBattery: Boolean = false,
    val batteryStatusMessage: String? = null
) {
    val totalDetectedAppCacheBytes: Long
        get() = cacheEntries.sumOf { it.cacheBytes }.coerceAtLeast(ownAppCacheBytes)

    val cacheAreaBreakdown: CacheAreaBreakdown
        get() {
            var media = 0L
            var browser = 0L
            var other = 0L
            var local = ownAppCacheBytes
            for (entry in cacheEntries) {
                if (entry.cacheBytes <= 0L) continue
                val pkg = entry.packageName.lowercase()
                when {
                    pkg == "com.aistudio.azroiacare.qxmkvp" -> {
                        local = maxOf(local, entry.cacheBytes)
                    }
                    pkg.contains("youtube") || pkg.contains("facebook") || pkg.contains("instagram") ||
                        pkg.contains("whatsapp") || pkg.contains("tiktok") || pkg.contains("musically") ||
                        pkg.contains("ugc.trill") || pkg.contains("telegram") || pkg.contains("snapchat") ||
                        pkg.contains("spotify") || pkg.contains("netflix") -> {
                        media += entry.cacheBytes
                    }
                    pkg.contains("chrome") || pkg.contains("browser") || pkg.contains("opera") ||
                        pkg.contains("firefox") || pkg.contains("files") || pkg.contains("documents") -> {
                        browser += entry.cacheBytes
                    }
                    else -> {
                        other += entry.cacheBytes
                    }
                }
            }
            return CacheAreaBreakdown(
                mediaSocialBytes = media,
                browserToolsBytes = browser,
                otherAppsBytes = other,
                localBufferBytes = local
            )
        }

    val overThresholdApps: List<AppCacheEntry>
        get() = cacheEntries.filter {
            !it.isWhitelisted &&
                it.cleanState != AppCacheCleanState.CLEANED &&
                it.cacheBytes >= cacheAlertThresholdBytes
        }

    val usedAndCachedApps: List<AppCacheEntry>
        get() = cacheEntries.filter {
            it.packageName != "com.aistudio.azroiacare.qxmkvp" &&
                !it.isWhitelisted &&
                it.cleanState != AppCacheCleanState.CLEANED &&
                it.wasUsedByUser &&
                it.cacheBytes > 0L
        }

    val cacheHealthScore: Int
        get() {
            val totalCache = totalDetectedAppCacheBytes
            val hogPenalty = (overThresholdApps.size * 8).coerceAtMost(35)
            val sizePenalty = when {
                totalCache >= 2L * 1024L * 1024L * 1024L -> 35
                totalCache >= 1024L * 1024L * 1024L -> 24
                totalCache >= 500L * 1024L * 1024L -> 15
                totalCache >= 150L * 1024L * 1024L -> 8
                else -> 0
            }
            val cleanedBonus = if (totalCacheFreedSessionBytes > 0L) 5 else 0
            return (100 - hogPenalty - sizePenalty + cleanedBonus).coerceIn(25, 100)
        }
}

class CleanerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SystemDiagnosticsRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(AzroiaUiState())
    val uiState: StateFlow<AzroiaUiState> = _uiState.asStateFlow()

    private var liveRamMonitorJob: kotlinx.coroutines.Job? = null
    private var lastBatteryAutoCleanMs: Long = 0L

    init {
        observeBattery()
        refreshAllTelemetry()
    }

    private fun observeBattery() {
        viewModelScope.launch {
            repo.observeBatteryTelemetry().collect { battery ->
                _uiState.update { state ->
                    state.copy(
                        batteryTelemetry = battery.copy(
                            lastCooledCount = state.batteryTelemetry.lastCooledCount,
                            lastCacheFreedBytes = state.batteryTelemetry.lastCacheFreedBytes,
                            lastCleanedFilesCount = state.batteryTelemetry.lastCleanedFilesCount,
                            totalBatteryCacheFreedBytes = maxOf(
                                battery.totalBatteryCacheFreedBytes,
                                state.batteryTelemetry.totalBatteryCacheFreedBytes
                            ),
                            isOptimizationActive = state.isBatteryOptimizationEnabled
                        )
                    )
                }
            }
        }
    }

    fun triggerLaunchBurst() {
        if (_uiState.value.isLaunchBurstActive) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLaunchBurstActive = true) }
            delay(1150L)
            _uiState.update {
                it.copy(
                    route = ScreenRoute.Dashboard,
                    isLaunchBurstActive = false
                )
            }
        }
    }

    fun replaySplashEntry() {
        _uiState.update {
            it.copy(
                route = ScreenRoute.Splash,
                isLaunchBurstActive = false
            )
        }
    }

    fun openModule(module: CleanerModule) {
        _uiState.update { it.copy(route = ScreenRoute.ModuleDetail(module)) }
        refreshAllTelemetry()
        when (module) {
            CleanerModule.VIRUS -> {
                if (!_uiState.value.isScanningVirus) {
                    runZeroVirusScan()
                }
            }
            CleanerModule.BATTERY -> {
                val now = System.currentTimeMillis()
                if (_uiState.value.isBatteryOptimizationEnabled &&
                    _uiState.value.autoCleanCacheForBattery &&
                    now - lastBatteryAutoCleanMs > 25_000L
                ) {
                    runBatteryAutoCacheSweep()
                }
            }
            else -> Unit
        }
    }

    fun navigateBackToDashboard() {
        AutoCacheCoordinator.stopAll()
        liveRamMonitorJob?.cancel()
        liveRamMonitorJob = null
        _uiState.update {
            it.copy(
                route = ScreenRoute.Dashboard,
                isGuidedQueueActive = false,
                isAutoCleanMode = false,
                guidedQueuePackages = emptyList(),
                guidedQueueModeLabel = "",
                activeGuidedApp = null,
                pendingOpenSettingsPackage = null,
                isRamHibernateQueueActive = false,
                ramHibernateQueuePackages = emptyList(),
                activeHibernateApp = null,
                isLiveRamMonitorEnabled = false,
                inspectedRamApp = null
            )
        }
    }

    fun onAppResumed() {
        val context = getApplication<Application>()
        val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(context)
        val state = _uiState.value
        val activeGuided = state.activeGuidedApp
        val activeHibernate = state.activeHibernateApp

        _uiState.update { it.copy(isAccessibilityServiceEnabled = accessibilityEnabled) }

        if (state.isRamHibernateQueueActive && activeHibernate != null && state.pendingOpenSettingsPackage == null) {
            viewModelScope.launch {
                delay(220L)
                val refreshedRam = repo.loadRamTelemetry()
                val remainingPkgs = state.ramHibernateQueuePackages.filter { it != activeHibernate.packageName }
                val remainingApps = remainingPkgs.mapNotNull { pkg ->
                    refreshedRam.backgroundApps.find { it.packageName == pkg }
                }.filter { !it.isWhitelisted }
                val nextIdx = state.ramHibernateQueueIndex + 1

                if (remainingApps.isNotEmpty() && nextIdx < state.ramHibernateQueueTotal) {
                    val nextApp = remainingApps.first()
                    if (accessibilityEnabled) {
                        AutoCacheCoordinator.beginAppStep(
                            nextApp.packageName,
                            nextApp.appName,
                            AutoAutomationMode.FORCE_STOP_RAM
                        )
                    }
                    _uiState.update {
                        it.copy(
                            ramTelemetry = refreshedRam,
                            ramHibernateQueueIndex = nextIdx,
                            ramHibernateQueuePackages = remainingApps.map { app -> app.packageName },
                            activeHibernateApp = nextApp,
                            pendingOpenSettingsPackage = if (accessibilityEnabled) nextApp.packageName else null,
                            ramStatusMessage = "Hibernated ${activeHibernate.appName} • Next: ${nextApp.appName} (${nextIdx + 1}/${state.ramHibernateQueueTotal})"
                        )
                    }
                } else {
                    AutoCacheCoordinator.stopAll()
                    val beforeAvail = state.ramHibernateBeforeAvailBytes
                    val beforePct = state.ramHibernateBeforePercent.takeIf { it > 0 } ?: refreshedRam.usedPercent
                    val dropBytes = (refreshedRam.availableBytes - beforeAvail).coerceAtLeast(0L)
                    val totalReclaimed = repo.recordRamReclaimedBytes(dropBytes)
                    val updatedHistory = repo.appendRamBoostHistory(
                        killedCount = state.ramHibernateQueueTotal.coerceAtLeast(1),
                        freedBytes = dropBytes,
                        beforePercent = beforePct,
                        afterPercent = refreshedRam.usedPercent,
                        modeLabel = "Deep Hibernate"
                    )
                    val pctDrop = (beforePct - refreshedRam.usedPercent).coerceAtLeast(0)
                    _uiState.update {
                        it.copy(
                            ramTelemetry = refreshedRam.copy(
                                lastFreedBytes = dropBytes,
                                lastKilledCount = state.ramHibernateQueueTotal.coerceAtLeast(1),
                                totalReclaimedBytes = totalReclaimed,
                                lastDropBeforePercent = beforePct,
                                lastDropAfterPercent = refreshedRam.usedPercent,
                                lastDropBytes = dropBytes
                            ),
                            ramBoostHistory = updatedHistory,
                            isRamHibernateQueueActive = false,
                            ramHibernateQueuePackages = emptyList(),
                            activeHibernateApp = null,
                            pendingOpenSettingsPackage = null,
                            ramStatusMessage = if (dropBytes > 0L) {
                                "Deep Hibernate Complete & Stopped • RAM ${beforePct}% → ${refreshedRam.usedPercent}% (-${pctDrop}% • ${formatBytes(dropBytes)} freed)"
                            } else {
                                "Deep Hibernate Complete & Stopped • Stopped ${state.ramHibernateQueueTotal} heavy app(s)"
                            }
                        )
                    }
                }
            }
            return
        }

        if (state.isGuidedQueueActive && activeGuided != null && state.pendingOpenSettingsPackage == null) {
            viewModelScope.launch {
                // Small settle delay so Android StorageStatsManager reflects the cleared cache
                delay(180L)
                val afterCache = repo.querySingleAppCacheBytes(activeGuided.packageName)
                val freedDelta = (activeGuided.cacheBytes - afterCache).coerceAtLeast(0L)
                val newRemovedTotal = repo.recordCacheRemovedBytes(freedDelta)
                val updatedHistory = if (freedDelta > 0L || state.isAutoCleanMode) {
                    repo.appendCacheCleanHistory(
                        appName = activeGuided.appName,
                        packageName = activeGuided.packageName,
                        freedBytes = freedDelta,
                        methodLabel = if (state.isAutoCleanMode) "Auto Engine" else "Guided Queue"
                    )
                } else {
                    _uiState.value.cacheHistory
                }

                val updatedEntries = _uiState.value.cacheEntries.map {
                    if (it.packageName == activeGuided.packageName) {
                        it.copy(cleanState = AppCacheCleanState.CLEANED, cacheBytes = afterCache)
                    } else it
                }

                // Strictly advance ONLY within the snapshot queue (and stop immediately once finished)
                val nextPackageQueue = state.guidedQueuePackages.filter { it != activeGuided.packageName }
                val remaining = if (nextPackageQueue.isNotEmpty()) {
                    nextPackageQueue.mapNotNull { pkg -> updatedEntries.find { it.packageName == pkg } }
                } else {
                    emptyList()
                }.filter {
                    it.packageName != context.packageName &&
                        !it.isWhitelisted &&
                        it.cleanState != AppCacheCleanState.CLEANED &&
                        (!state.isAutoCleanMode || !state.skipZeroCacheAutoClean || !state.hasUsageStatsAccess || it.cacheBytes > 0L)
                }
                val nextIdx = state.guidedQueueIndex + 1

                if (remaining.isNotEmpty() && nextIdx < state.guidedQueueTotal) {
                    val nextApp = remaining.first()
                    val autoContinue = state.isAutoCleanMode && accessibilityEnabled
                    if (autoContinue) {
                        AutoCacheCoordinator.beginAppStep(nextApp.packageName, nextApp.appName)
                    }
                    _uiState.update {
                        it.copy(
                            cacheEntries = updatedEntries,
                            cacheHistory = updatedHistory,
                            totalCacheFreedSessionBytes = newRemovedTotal,
                            guidedQueueIndex = nextIdx,
                            guidedQueuePackages = remaining.map { entry -> entry.packageName },
                            activeGuidedApp = nextApp,
                            pendingOpenSettingsPackage = if (autoContinue) nextApp.packageName else null,
                            cacheStatusMessage = if (freedDelta > 0L) {
                                "${activeGuided.appName}: Freed ${formatBytes(freedDelta)} • Next: ${nextApp.appName}"
                            } else {
                                "${activeGuided.appName} checked • Next: ${nextApp.appName}"
                            }
                        )
                    }
                } else {
                    AutoCacheCoordinator.stopAll()
                    val modeDoneLabel = state.guidedQueueModeLabel.ifBlank { "Cache Sweep" }
                    _uiState.update {
                        it.copy(
                            cacheEntries = updatedEntries,
                            cacheHistory = updatedHistory,
                            totalCacheFreedSessionBytes = newRemovedTotal,
                            isGuidedQueueActive = false,
                            isAutoCleanMode = false,
                            guidedQueuePackages = emptyList(),
                            guidedQueueModeLabel = "",
                            activeGuidedApp = null,
                            pendingOpenSettingsPackage = null,
                            cacheStatusMessage = if (newRemovedTotal > 0L) {
                                "$modeDoneLabel Complete & Stopped • Total Removed: ${formatBytes(newRemovedTotal)}"
                            } else {
                                "$modeDoneLabel Complete & Stopped!"
                            }
                        )
                    }
                }
                refreshAllTelemetry()
            }
            return
        }
        refreshAllTelemetry()
    }

    fun refreshAllTelemetry() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val hasUsage = repo.hasUsageStatsPermission()
            val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(context)
            val removedTotal = repo.getTotalCacheRemovedBytes()
            val whitelisted = repo.getWhitelistedCachePackages()
            val thresholdBytes = repo.getCacheAlertThresholdBytes()
            val skipZero = repo.getSkipZeroCacheAutoClean()
            val history = repo.getCacheCleanHistory()
            val internalBreakdown = repo.measureInternalBufferBreakdown()
            val fetchedEntries = repo.loadInstalledAppCacheEntries()
            val prevCleaned = _uiState.value.cacheEntries
                .filter { it.cleanState == AppCacheCleanState.CLEANED }
                .map { it.packageName }
                .toSet()

            val mergedEntries = fetchedEntries.map { entry ->
                val wasCleaned = entry.packageName in prevCleaned && entry.cacheBytes == 0L
                val isProtected = entry.packageName in whitelisted
                entry.copy(
                    cleanState = if (wasCleaned) AppCacheCleanState.CLEANED else entry.cleanState,
                    isWhitelisted = isProtected,
                    isSelectedForClean = if (isProtected) false else entry.isSelectedForClean
                )
            }

            val ownCache = repo.measureOwnAppCacheBytes()
            val ram = repo.loadRamTelemetry()
            val ramHistory = repo.getRamBoostHistory()
            val ramWhitelist = repo.getWhitelistedRamPackages()
            val ramThreshold = repo.getRamPressureThresholdPercent()
            val totalAppCache = mergedEntries.filter { it.packageName != context.packageName }.sumOf { it.cacheBytes } + ownCache
            val totalAppData = mergedEntries.sumOf { it.appBytes + it.dataBytes }
            val storageLargeLimit = repo.getLargeFileThresholdBytes()
            val storageHistory = repo.getStorageCleanHistory()
            val savedFolder = _uiState.value.scannedFolderUri ?: repo.getSavedStorageFolderUri()
            val includeApksBoost = repo.getIncludeApksInStorageBoost()
            val sweepSafBoost = repo.getAutoSweepSafInBoost()
            val virusAutoDelete = repo.getVirusAutoDeleteUnnecessary()
            val batteryOptEnabled = repo.getBatteryOptimizationEnabled()
            val batteryAutoCache = repo.getAutoCleanCacheForBattery()
            val storage = repo.loadStorageTelemetry(totalAppCache, totalAppData)

            _uiState.update { state ->
                val updatedInspected = state.inspectedCacheApp?.let { curr ->
                    mergedEntries.find { it.packageName == curr.packageName }
                }
                state.copy(
                    hasUsageStatsAccess = hasUsage,
                    isAccessibilityServiceEnabled = accessibilityEnabled,
                    totalCacheFreedSessionBytes = removedTotal,
                    whitelistedCachePackages = whitelisted,
                    cacheAlertThresholdBytes = thresholdBytes,
                    skipZeroCacheAutoClean = skipZero,
                    cacheHistory = history,
                    internalBufferBreakdown = internalBreakdown,
                    isLoadingTelemetry = false,
                    cacheEntries = mergedEntries,
                    ownAppCacheBytes = ownCache,
                    inspectedCacheApp = updatedInspected,
                    ramTelemetry = ram,
                    ramBoostHistory = ramHistory,
                    whitelistedRamPackages = ramWhitelist,
                    ramPressureThresholdPercent = ramThreshold,
                    scannedFolderUri = savedFolder,
                    storageLargeThresholdBytes = storageLargeLimit,
                    storageCleanHistory = storageHistory,
                    includeApksInStorageBoost = includeApksBoost,
                    autoSweepSafInBoost = sweepSafBoost,
                    autoDeleteUnnecessaryFiles = virusAutoDelete,
                    isBatteryOptimizationEnabled = batteryOptEnabled,
                    autoCleanCacheForBattery = batteryAutoCache,
                    storageTelemetry = storage.copy(
                        lastBoostFreedBytes = state.storageTelemetry.lastBoostFreedBytes,
                        lastDropBeforePercent = state.storageTelemetry.lastDropBeforePercent,
                        lastDropAfterPercent = state.storageTelemetry.lastDropAfterPercent,
                        lastWriteSpeedMbPerSec = state.storageTelemetry.lastWriteSpeedMbPerSec,
                        lastBoostItemsCleaned = state.storageTelemetry.lastBoostItemsCleaned,
                        lastBoostModeLabel = state.storageTelemetry.lastBoostModeLabel
                    )
                )
            }
        }
    }

    fun runCacheSweepOnly() {
        if (_uiState.value.isCleaningCache) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCleaningCache = true,
                    cacheSweepProgress = 0.05f,
                    cacheStatusMessage = "Sweeping AZR internal cache & preparing app queue..."
                )
            }

            val ownFreed = repo.clearOwnAppCache()
            val entries = _uiState.value.cacheEntries.toMutableList()
            val totalEntries = entries.size.coerceAtLeast(1)

            for (i in entries.indices) {
                val entry = entries[i]
                entries[i] = entry.copy(cleanState = AppCacheCleanState.CLEANING)
                val progress = (i + 1).toFloat() / totalEntries.toFloat()
                _uiState.update {
                    it.copy(
                        cacheEntries = entries.toList(),
                        cacheSweepProgress = progress,
                        cacheStatusMessage = "Inspecting & sweeping ${entry.appName}..."
                    )
                }
                delay(if (i < 8) 85L else 24L)

                val isSelf = entry.packageName == getApplication<Application>().packageName
                val nextState = if (isSelf) {
                    AppCacheCleanState.CLEANED
                } else {
                    AppCacheCleanState.SYSTEM_ACTION_READY
                }
                entries[i] = entry.copy(
                    cleanState = nextState,
                    cacheBytes = if (isSelf) 0L else entry.cacheBytes
                )
                _uiState.update { it.copy(cacheEntries = entries.toList()) }
            }

            val refreshedOwn = repo.measureOwnAppCacheBytes()
            val internalBreakdown = repo.measureInternalBufferBreakdown()
            val newRemovedTotal = repo.recordCacheRemovedBytes(ownFreed)
            val updatedHistory = if (ownFreed > 0L) {
                repo.appendCacheCleanHistory(
                    appName = "Azroia Internal Buffer",
                    packageName = getApplication<Application>().packageName,
                    freedBytes = ownFreed,
                    methodLabel = "Local Sweep"
                )
            } else {
                _uiState.value.cacheHistory
            }
            val queueable = entries.filter { it.packageName != getApplication<Application>().packageName && !it.isWhitelisted }
            val summary = "Freed ${formatBytes(ownFreed)} locally • ${queueable.size} apps indexed for Cache Clear"

            _uiState.update {
                it.copy(
                    isCleaningCache = false,
                    cacheSweepProgress = 1f,
                    ownAppCacheBytes = refreshedOwn,
                    internalBufferBreakdown = internalBreakdown,
                    totalCacheFreedSessionBytes = newRemovedTotal,
                    cacheHistory = updatedHistory,
                    cacheStatusMessage = summary
                )
            }
        }
    }

    fun startAutoAllAppsCacheClean() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCleaningCache = true,
                    cacheSweepProgress = 0.2f,
                    cacheStatusMessage = "Scanning YouTube, Facebook, Chrome & all used apps cache..."
                )
            }
            val ownFreed = repo.clearOwnAppCache()
            val newRemovedTotal = repo.recordCacheRemovedBytes(ownFreed)
            val refreshedOwn = repo.measureOwnAppCacheBytes()
            val internalBreakdown = repo.measureInternalBufferBreakdown()
            val updatedHistory = if (ownFreed > 0L) {
                repo.appendCacheCleanHistory(
                    appName = "Azroia Internal Buffer",
                    packageName = context.packageName,
                    freedBytes = ownFreed,
                    methodLabel = "Local Sweep"
                )
            } else {
                _uiState.value.cacheHistory
            }
            val hasUsage = repo.hasUsageStatsPermission()
            val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(context)
            val freshEntries = repo.loadInstalledAppCacheEntries()

            _uiState.update {
                it.copy(
                    isCleaningCache = false,
                    cacheSweepProgress = 1f,
                    hasUsageStatsAccess = hasUsage,
                    cacheEntries = freshEntries,
                    ownAppCacheBytes = refreshedOwn,
                    internalBufferBreakdown = internalBreakdown,
                    totalCacheFreedSessionBytes = newRemovedTotal,
                    cacheHistory = updatedHistory,
                    isAccessibilityServiceEnabled = accessibilityEnabled
                )
            }

            if (!accessibilityEnabled) {
                _uiState.update {
                    it.copy(
                        showAccessibilityPrompt = true,
                        cacheStatusMessage = "Enable Azroia Auto Cache Cleaner in Accessibility to auto-clear every app"
                    )
                }
                return@launch
            }

            val state = _uiState.value
            val cachedCandidates = state.cacheEntries.filter {
                it.packageName != context.packageName &&
                    !it.isWhitelisted &&
                    it.cleanState != AppCacheCleanState.CLEANED &&
                    (!state.skipZeroCacheAutoClean || !state.hasUsageStatsAccess || it.cacheBytes > 0L)
            }
            val queue = if (state.hasUsageStatsAccess && state.skipZeroCacheAutoClean) {
                cachedCandidates
            } else if (cachedCandidates.isNotEmpty()) {
                cachedCandidates
            } else {
                state.cacheEntries.filter {
                    it.packageName != context.packageName &&
                        !it.isWhitelisted &&
                        it.cleanState != AppCacheCleanState.CLEANED
                }
            }
            if (queue.isEmpty()) {
                _uiState.update {
                    it.copy(cacheStatusMessage = "All non-shielded apps already have 0 B cache! Stopped.")
                }
                return@launch
            }

            val first = queue.first()
            AutoCacheCoordinator.beginAppStep(first.packageName, first.appName)
            _uiState.update {
                it.copy(
                    isGuidedQueueActive = true,
                    isAutoCleanMode = true,
                    guidedQueueIndex = 0,
                    guidedQueueTotal = queue.size,
                    guidedQueuePackages = queue.map { entry -> entry.packageName },
                    guidedQueueModeLabel = "Auto Cache Clean",
                    activeGuidedApp = first,
                    pendingOpenSettingsPackage = first.packageName,
                    cacheStatusMessage = "Auto-cleaning ${first.appName} (1/${queue.size})…"
                )
            }
        }
    }

    fun startAutoUsedAndCachedAppsClean() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCleaningCache = true,
                    cacheSweepProgress = 0.25f,
                    cacheStatusMessage = "Detecting apps you used that have cache > 0 B..."
                )
            }
            val ownFreed = repo.clearOwnAppCache()
            val newRemovedTotal = repo.recordCacheRemovedBytes(ownFreed)
            val refreshedOwn = repo.measureOwnAppCacheBytes()
            val internalBreakdown = repo.measureInternalBufferBreakdown()
            val updatedHistory = if (ownFreed > 0L) {
                repo.appendCacheCleanHistory(
                    appName = "Azroia Internal Buffer",
                    packageName = context.packageName,
                    freedBytes = ownFreed,
                    methodLabel = "Local Sweep"
                )
            } else {
                _uiState.value.cacheHistory
            }
            val hasUsage = repo.hasUsageStatsPermission()
            val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(context)
            val freshEntries = repo.loadInstalledAppCacheEntries()

            _uiState.update {
                it.copy(
                    isCleaningCache = false,
                    cacheSweepProgress = 1f,
                    hasUsageStatsAccess = hasUsage,
                    cacheEntries = freshEntries,
                    ownAppCacheBytes = refreshedOwn,
                    internalBufferBreakdown = internalBreakdown,
                    totalCacheFreedSessionBytes = newRemovedTotal,
                    cacheHistory = updatedHistory,
                    isAccessibilityServiceEnabled = accessibilityEnabled
                )
            }

            if (!hasUsage) {
                _uiState.update {
                    it.copy(
                        showUsageAccessRequiredDialog = true,
                        cacheStatusMessage = "Grant Usage Access so Azroia only cleans apps you used with cache > 0 B"
                    )
                }
                return@launch
            }

            val queue = _uiState.value.usedAndCachedApps.filter { it.packageName != context.packageName }
            if (queue.isEmpty()) {
                AutoCacheCoordinator.stopAll()
                _uiState.update {
                    it.copy(
                        isGuidedQueueActive = false,
                        isAutoCleanMode = false,
                        guidedQueuePackages = emptyList(),
                        guidedQueueModeLabel = "",
                        activeGuidedApp = null,
                        cacheStatusMessage = "All used apps already have 0 B cache • Stopped!"
                    )
                }
                return@launch
            }

            if (!accessibilityEnabled) {
                _uiState.update {
                    it.copy(
                        guidedQueuePackages = queue.map { entry -> entry.packageName },
                        guidedQueueModeLabel = "Used & Cached Clean",
                        showAccessibilityPrompt = true,
                        cacheStatusMessage = "Ready to clean ${queue.size} used & cached app(s) — enable Auto Cleaner or tap Manual Queue"
                    )
                }
                return@launch
            }

            val first = queue.first()
            AutoCacheCoordinator.beginAppStep(first.packageName, first.appName)
            _uiState.update {
                it.copy(
                    isGuidedQueueActive = true,
                    isAutoCleanMode = true,
                    guidedQueueIndex = 0,
                    guidedQueueTotal = queue.size,
                    guidedQueuePackages = queue.map { entry -> entry.packageName },
                    guidedQueueModeLabel = "Used & Cached Clean",
                    activeGuidedApp = first,
                    pendingOpenSettingsPackage = first.packageName,
                    cacheStatusMessage = "Cleaning used app ${first.appName} (1/${queue.size}) • Auto-stops when done"
                )
            }
        }
    }

    fun dismissUsageAccessDialog() {
        _uiState.update { it.copy(showUsageAccessRequiredDialog = false) }
    }

    fun cleanSingleAppAutoOrGuided(entry: AppCacheEntry) {
        val context = getApplication<Application>()
        if (entry.packageName == context.packageName) {
            runCacheSweepOnly()
            return
        }
        val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(context)
        if (accessibilityEnabled) {
            AutoCacheCoordinator.beginAppStep(entry.packageName, entry.appName)
        } else {
            AutoCacheCoordinator.stopAll()
        }
        _uiState.update {
            it.copy(
                isAccessibilityServiceEnabled = accessibilityEnabled,
                isGuidedQueueActive = true,
                isAutoCleanMode = accessibilityEnabled,
                guidedQueueIndex = 0,
                guidedQueueTotal = 1,
                guidedQueuePackages = listOf(entry.packageName),
                guidedQueueModeLabel = entry.appName,
                activeGuidedApp = entry,
                pendingOpenSettingsPackage = entry.packageName,
                cacheStatusMessage = if (accessibilityEnabled) {
                    "Auto-cleaning ${entry.appName} → Storage → Clear Cache…"
                } else {
                    "Opening ${entry.appName} → Tap Storage → Clear Cache"
                }
            )
        }
    }

    fun toggleCacheAppSelection(packageName: String) {
        _uiState.update { state ->
            state.copy(
                cacheEntries = state.cacheEntries.map { entry ->
                    if (entry.packageName == packageName && !entry.isWhitelisted) {
                        entry.copy(isSelectedForClean = !entry.isSelectedForClean)
                    } else entry
                }
            )
        }
    }

    fun selectAllCacheApps(selectAll: Boolean) {
        _uiState.update { state ->
            state.copy(
                cacheEntries = state.cacheEntries.map { entry ->
                    if (entry.isWhitelisted) {
                        entry.copy(isSelectedForClean = false)
                    } else {
                        entry.copy(isSelectedForClean = selectAll)
                    }
                }
            )
        }
    }

    fun applyCacheSelectionPreset(preset: CacheSelectionPreset) {
        val state = _uiState.value
        val threshold = state.cacheAlertThresholdBytes
        val topFivePkgs = state.cacheEntries
            .filter { !it.isWhitelisted && it.cacheBytes > 0L }
            .sortedByDescending { it.cacheBytes }
            .take(5)
            .map { it.packageName }
            .toSet()

        val updated = state.cacheEntries.map { entry ->
            if (entry.isWhitelisted) {
                entry.copy(isSelectedForClean = false)
            } else {
                val match = when (preset) {
                    CacheSelectionPreset.OVER_THRESHOLD -> entry.cacheBytes >= threshold
                    CacheSelectionPreset.TOP_FIVE -> entry.packageName in topFivePkgs
                    CacheSelectionPreset.MEDIA_ONLY -> entry.isPriorityMediaApp && entry.cacheBytes > 0L
                }
                entry.copy(isSelectedForClean = match)
            }
        }
        val selectedCount = updated.count { it.isSelectedForClean }
        val selectedBytes = updated.filter { it.isSelectedForClean }.sumOf { it.cacheBytes }
        _uiState.update {
            it.copy(
                cacheEntries = updated,
                cacheStatusMessage = "Preset '${preset.label}': Selected $selectedCount app(s) (${formatBytes(selectedBytes)})"
            )
        }
    }

    fun toggleCacheWhitelist(packageName: String) {
        val updatedSet = repo.toggleWhitelistedCachePackage(packageName)
        _uiState.update { state ->
            val updatedEntries = state.cacheEntries.map { entry ->
                if (entry.packageName == packageName) {
                    val isProtected = packageName in updatedSet
                    entry.copy(
                        isWhitelisted = isProtected,
                        isSelectedForClean = !isProtected
                    )
                } else entry
            }
            val updatedInspected = state.inspectedCacheApp?.let { curr ->
                updatedEntries.find { it.packageName == curr.packageName }
            }
            val isNowProtected = packageName in updatedSet
            state.copy(
                whitelistedCachePackages = updatedSet,
                cacheEntries = updatedEntries,
                inspectedCacheApp = updatedInspected,
                cacheStatusMessage = if (isNowProtected) {
                    "App shielded & protected from Auto Clear"
                } else {
                    "Shield removed • App unlocked for Cache Clear"
                }
            )
        }
    }

    fun setCacheAlertThreshold(thresholdBytes: Long) {
        repo.setCacheAlertThresholdBytes(thresholdBytes)
        _uiState.update {
            it.copy(
                cacheAlertThresholdBytes = thresholdBytes,
                cacheStatusMessage = "Cache Guard Alert set to ${formatBytes(thresholdBytes)}"
            )
        }
    }

    fun setSkipZeroCacheAutoClean(skip: Boolean) {
        repo.setSkipZeroCacheAutoClean(skip)
        _uiState.update {
            it.copy(skipZeroCacheAutoClean = skip)
        }
    }

    fun inspectCacheApp(entry: AppCacheEntry?) {
        _uiState.update { it.copy(inspectedCacheApp = entry) }
    }

    fun clearCacheCleanHistory() {
        val cleared = repo.clearCacheCleanHistory()
        _uiState.update {
            it.copy(
                cacheHistory = cleared,
                cacheStatusMessage = "Cache clean history log cleared"
            )
        }
    }

    fun startOverThresholdAutoClean() {
        val context = getApplication<Application>()
        val overLimit = _uiState.value.overThresholdApps.filter { it.packageName != context.packageName }
        if (overLimit.isEmpty()) {
            _uiState.update {
                it.copy(cacheStatusMessage = "No apps exceed the ${formatBytes(it.cacheAlertThresholdBytes)} limit")
            }
            return
        }
        val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(context)
        if (!accessibilityEnabled) {
            _uiState.update {
                it.copy(
                    showAccessibilityPrompt = true,
                    cacheStatusMessage = "Enable Auto Cache Cleaner to clean ${overLimit.size} over-limit app(s)"
                )
            }
            return
        }
        val first = overLimit.first()
        AutoCacheCoordinator.beginAppStep(first.packageName, first.appName)
        _uiState.update {
            it.copy(
                isGuidedQueueActive = true,
                isAutoCleanMode = true,
                guidedQueueIndex = 0,
                guidedQueueTotal = overLimit.size,
                guidedQueuePackages = overLimit.map { entry -> entry.packageName },
                guidedQueueModeLabel = "Over-Limit Clean",
                activeGuidedApp = first,
                pendingOpenSettingsPackage = first.packageName,
                cacheStatusMessage = "Cleaning Over-Limit: ${first.appName} (1/${overLimit.size})…"
            )
        }
    }

    fun startAutoSelectedAppsCacheClean() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCleaningCache = true,
                    cacheSweepProgress = 0.25f,
                    cacheStatusMessage = "Preparing selected apps cache queue..."
                )
            }
            val ownFreed = repo.clearOwnAppCache()
            val newRemovedTotal = repo.recordCacheRemovedBytes(ownFreed)
            val refreshedOwn = repo.measureOwnAppCacheBytes()
            val internalBreakdown = repo.measureInternalBufferBreakdown()
            val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(context)

            _uiState.update {
                it.copy(
                    isCleaningCache = false,
                    cacheSweepProgress = 1f,
                    ownAppCacheBytes = refreshedOwn,
                    internalBufferBreakdown = internalBreakdown,
                    totalCacheFreedSessionBytes = newRemovedTotal,
                    isAccessibilityServiceEnabled = accessibilityEnabled
                )
            }

            if (!accessibilityEnabled) {
                _uiState.update {
                    it.copy(
                        showAccessibilityPrompt = true,
                        cacheStatusMessage = "Enable Azroia Auto Cache Cleaner to clean selected apps automatically"
                    )
                }
                return@launch
            }

            val queue = _uiState.value.cacheEntries.filter {
                it.packageName != context.packageName &&
                    !it.isWhitelisted &&
                    it.isSelectedForClean &&
                    it.cleanState != AppCacheCleanState.CLEANED
            }
            if (queue.isEmpty()) {
                _uiState.update {
                    it.copy(cacheStatusMessage = "Select at least one non-shielded app below to auto-clean!")
                }
                return@launch
            }

            val first = queue.first()
            AutoCacheCoordinator.beginAppStep(first.packageName, first.appName)
            _uiState.update {
                it.copy(
                    isGuidedQueueActive = true,
                    isAutoCleanMode = true,
                    guidedQueueIndex = 0,
                    guidedQueueTotal = queue.size,
                    guidedQueuePackages = queue.map { entry -> entry.packageName },
                    guidedQueueModeLabel = "Selected Apps Clean",
                    activeGuidedApp = first,
                    pendingOpenSettingsPackage = first.packageName,
                    cacheStatusMessage = "Auto-cleaning ${first.appName} (1/${queue.size})…"
                )
            }
        }
    }

    fun dismissAccessibilityPrompt() {
        _uiState.update {
            it.copy(
                showAccessibilityPrompt = false,
                guidedQueuePackages = if (it.isGuidedQueueActive) it.guidedQueuePackages else emptyList(),
                guidedQueueModeLabel = if (it.isGuidedQueueActive) it.guidedQueueModeLabel else ""
            )
        }
    }

    fun startSequentialAppSettingsClean() {
        val state = _uiState.value
        val context = getApplication<Application>()
        val candidates = if (state.guidedQueuePackages.isNotEmpty()) {
            state.guidedQueuePackages.mapNotNull { pkg ->
                state.cacheEntries.find { it.packageName == pkg }
            }.filter {
                it.packageName != context.packageName &&
                    !it.isWhitelisted &&
                    it.cleanState != AppCacheCleanState.CLEANED
            }
        } else {
            state.cacheEntries.filter {
                it.packageName != context.packageName &&
                    !it.isWhitelisted &&
                    it.cleanState != AppCacheCleanState.CLEANED &&
                    (!state.hasUsageStatsAccess || !state.skipZeroCacheAutoClean || it.cacheBytes > 0L)
            }
        }
        if (candidates.isEmpty()) {
            _uiState.update {
                it.copy(
                    showAccessibilityPrompt = false,
                    cacheStatusMessage = "No cached apps left to clean • Stopped!"
                )
            }
            return
        }
        val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(context)
        val first = candidates.first()
        if (accessibilityEnabled) {
            AutoCacheCoordinator.beginAppStep(first.packageName, first.appName)
        } else {
            AutoCacheCoordinator.stopAll()
        }
        _uiState.update {
            it.copy(
                showAccessibilityPrompt = false,
                isAccessibilityServiceEnabled = accessibilityEnabled,
                isGuidedQueueActive = true,
                isAutoCleanMode = accessibilityEnabled,
                guidedQueueIndex = 0,
                guidedQueueTotal = candidates.size,
                guidedQueuePackages = candidates.map { entry -> entry.packageName },
                guidedQueueModeLabel = state.guidedQueueModeLabel.ifBlank { "Guided Queue Clean" },
                activeGuidedApp = first,
                pendingOpenSettingsPackage = first.packageName,
                cacheStatusMessage = if (accessibilityEnabled) {
                    "Auto-cleaning ${first.appName} (1/${candidates.size})…"
                } else {
                    "Opening ${first.appName} (1/${candidates.size}) → Tap Storage → Clear Cache"
                }
            )
        }
    }

    fun launchNextGuidedApp() {
        val next = _uiState.value.activeGuidedApp ?: return
        val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(getApplication())
        if (accessibilityEnabled) {
            AutoCacheCoordinator.beginAppStep(next.packageName, next.appName)
        }
        _uiState.update {
            it.copy(
                pendingOpenSettingsPackage = next.packageName,
                cacheStatusMessage = "Opening ${next.appName} (${it.guidedQueueIndex + 1}/${it.guidedQueueTotal}) → Storage → Clear Cache"
            )
        }
    }

    fun stopSequentialQueue() {
        AutoCacheCoordinator.stopAll()
        _uiState.update {
            it.copy(
                isGuidedQueueActive = false,
                isAutoCleanMode = false,
                guidedQueuePackages = emptyList(),
                guidedQueueModeLabel = "",
                activeGuidedApp = null,
                pendingOpenSettingsPackage = null,
                cacheStatusMessage = "Cache clean queue stopped."
            )
        }
    }

    fun consumePendingOpenSettingsPackage() {
        _uiState.update { it.copy(pendingOpenSettingsPackage = null) }
    }

    fun markAppCacheSystemActionLaunched(packageName: String) {
        _uiState.update { state ->
            state.copy(
                cacheEntries = state.cacheEntries.map {
                    if (it.packageName == packageName) {
                        it.copy(cleanState = AppCacheCleanState.CLEANED, cacheBytes = 0L)
                    } else it
                }
            )
        }
    }

    fun toggleRamAppSelection(packageName: String) {
        _uiState.update { state ->
            val updated = state.ramTelemetry.backgroundApps.map {
                if (it.packageName == packageName && !it.isWhitelisted && !it.isTerminated) {
                    it.copy(isSelected = !it.isSelected)
                } else it
            }
            state.copy(ramTelemetry = state.ramTelemetry.copy(backgroundApps = updated))
        }
    }

    fun selectRamBoostMode(mode: RamBoostMode) {
        _uiState.update { state ->
            val updatedApps = state.ramTelemetry.backgroundApps.map { entry ->
                if (entry.isWhitelisted || entry.isTerminated) {
                    entry.copy(isSelected = false)
                } else {
                    val autoSelect = when (mode) {
                        RamBoostMode.BALANCED -> !entry.isSystemApp || entry.isActiveProcess
                        RamBoostMode.DEEP_TURBO,
                        RamBoostMode.GAMING_FOCUS -> true
                    }
                    entry.copy(isSelected = autoSelect)
                }
            }
            state.copy(
                selectedRamBoostMode = mode,
                ramTelemetry = state.ramTelemetry.copy(backgroundApps = updatedApps),
                ramStatusMessage = "${mode.label} Mode: ${mode.detail}"
            )
        }
    }

    fun selectRamPressureThreshold(percent: Int) {
        repo.setRamPressureThresholdPercent(percent)
        _uiState.update {
            it.copy(
                ramPressureThresholdPercent = percent,
                ramStatusMessage = "RAM Pressure Guard threshold set to $percent%"
            )
        }
    }

    fun applyRamSelectionPreset(preset: String) {
        _uiState.update { state ->
            val updatedApps = state.ramTelemetry.backgroundApps.map { entry ->
                if (entry.isWhitelisted || entry.isTerminated) {
                    entry.copy(isSelected = false)
                } else {
                    val select = when (preset) {
                        "ACTIVE" -> entry.isActiveProcess
                        "HIGH_PSS" -> entry.processPssBytes >= 40L * 1024L * 1024L || entry.isActiveProcess
                        "ALL" -> true
                        else -> false
                    }
                    entry.copy(isSelected = select)
                }
            }
            val count = updatedApps.count { it.isSelected }
            state.copy(
                ramTelemetry = state.ramTelemetry.copy(backgroundApps = updatedApps),
                ramStatusMessage = when (preset) {
                    "ACTIVE" -> "Selected $count active running process(es)"
                    "HIGH_PSS" -> "Selected $count high-memory process(es)"
                    "ALL" -> "Selected $count cleanable background process(es)"
                    else -> "Cleared RAM process selection"
                }
            )
        }
    }

    fun toggleRamWhitelist(packageName: String) {
        val updatedSet = repo.toggleWhitelistedRamPackage(packageName)
        _uiState.update { state ->
            val updatedApps = state.ramTelemetry.backgroundApps.map { entry ->
                if (entry.packageName == packageName) {
                    val isProtected = packageName in updatedSet
                    entry.copy(
                        isWhitelisted = isProtected,
                        isSelected = if (isProtected) false else entry.isSelected
                    )
                } else entry
            }
            val nowProtected = packageName in updatedSet
            state.copy(
                whitelistedRamPackages = updatedSet,
                ramTelemetry = state.ramTelemetry.copy(backgroundApps = updatedApps),
                ramStatusMessage = if (nowProtected) {
                    "App shielded • Protected from RAM Turbo Release & Deep Hibernate"
                } else {
                    "Shield removed • App unlocked for RAM boost"
                }
            )
        }
    }

    fun refreshRamProcesses() {
        viewModelScope.launch {
            val ram = repo.loadRamTelemetry()
            val history = repo.getRamBoostHistory()
            val whitelist = repo.getWhitelistedRamPackages()
            _uiState.update {
                it.copy(
                    ramTelemetry = ram,
                    ramBoostHistory = history,
                    whitelistedRamPackages = whitelist,
                    ramStatusMessage = "Live telemetry refreshed • ${ram.activeProcessCount} active process(es)"
                )
            }
        }
    }

    fun clearRamBoostHistory() {
        val cleared = repo.clearRamBoostHistory()
        _uiState.update {
            it.copy(
                ramBoostHistory = cleared,
                ramStatusMessage = "RAM optimization history log cleared"
            )
        }
    }

    fun runRamOptimization() {
        if (_uiState.value.isOptimizingRam) return
        viewModelScope.launch {
            val mode = _uiState.value.selectedRamBoostMode
            val packages = _uiState.value.ramTelemetry.backgroundApps
                .filter { it.isSelected && !it.isWhitelisted }
                .map { it.packageName }

            _uiState.update {
                it.copy(
                    isOptimizingRam = true,
                    ramOptimizationProgress = 0.08f,
                    ramStatusMessage = "${mode.label}: Scanning active heaps & background processes…"
                )
            }

            val updated = repo.optimizeRamStaged(packages, mode.label) { progress, stageLabel, snapshot ->
                _uiState.update { state ->
                    state.copy(
                        ramOptimizationProgress = progress.coerceIn(0.08f, 0.96f),
                        ramTelemetry = snapshot,
                        ramStatusMessage = stageLabel
                    )
                }
            }
            val refreshedHistory = repo.getRamBoostHistory()
            val dropPct = (updated.lastDropBeforePercent - updated.lastDropAfterPercent).coerceAtLeast(0)
            val msg = buildString {
                append("Turbo Released ${updated.lastKilledCount} processes")
                if (updated.lastFreedBytes > 0L) {
                    append(" • Reclaimed ${formatBytes(updated.lastFreedBytes)}")
                }
                if (dropPct > 0) {
                    append(" • RAM ${updated.lastDropBeforePercent}% → ${updated.lastDropAfterPercent}%")
                } else if (updated.activeProcessCount > 0) {
                    append(" • Use Deep Hibernate for ${updated.activeProcessCount} active service(s)")
                }
            }
            _uiState.update {
                it.copy(
                    isOptimizingRam = false,
                    ramOptimizationProgress = 1f,
                    ramTelemetry = updated,
                    ramBoostHistory = refreshedHistory,
                    ramStatusMessage = msg
                )
            }
        }
    }

    fun startDeepHibernateQueue() {
        val context = getApplication<Application>()
        val state = _uiState.value
        val activeTargets = state.ramTelemetry.backgroundApps
            .filter {
                it.packageName != context.packageName &&
                    !it.isWhitelisted &&
                    (it.isActiveProcess || it.isSelected)
            }
            .sortedByDescending { it.isActiveProcess }
            .take(12)

        if (activeTargets.isEmpty()) {
            _uiState.update {
                it.copy(ramStatusMessage = "No active non-shielded apps need Deep Hibernate!")
            }
            return
        }

        val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(context)
        if (!accessibilityEnabled) {
            _uiState.update {
                it.copy(
                    ramHibernateQueuePackages = activeTargets.map { entry -> entry.packageName },
                    showRamAccessibilityPrompt = true,
                    ramStatusMessage = "Enable Azroia Auto Engine or use Guided Force-Stop to drop stubborn RAM!"
                )
            }
            return
        }

        val first = activeTargets.first()
        AutoCacheCoordinator.beginAppStep(
            packageName = first.packageName,
            appName = first.appName,
            automationMode = AutoAutomationMode.FORCE_STOP_RAM
        )
        _uiState.update {
            it.copy(
                isRamHibernateQueueActive = true,
                ramHibernateQueueIndex = 0,
                ramHibernateQueueTotal = activeTargets.size,
                ramHibernateQueuePackages = activeTargets.map { entry -> entry.packageName },
                activeHibernateApp = first,
                ramHibernateBeforeAvailBytes = state.ramTelemetry.availableBytes,
                ramHibernateBeforePercent = state.ramTelemetry.usedPercent,
                pendingOpenSettingsPackage = first.packageName,
                ramStatusMessage = "Deep Hibernating ${first.appName} (1/${activeTargets.size})…"
            )
        }
    }

    fun startGuidedHibernateFallback() {
        val context = getApplication<Application>()
        val state = _uiState.value
        val candidates = if (state.ramHibernateQueuePackages.isNotEmpty()) {
            state.ramHibernateQueuePackages.mapNotNull { pkg ->
                state.ramTelemetry.backgroundApps.find { it.packageName == pkg }
            }
        } else {
            state.ramTelemetry.backgroundApps.filter { !it.isWhitelisted && it.isActiveProcess }
        }.filter { it.packageName != context.packageName && !it.isWhitelisted }

        if (candidates.isEmpty()) {
            _uiState.update {
                it.copy(
                    showRamAccessibilityPrompt = false,
                    ramStatusMessage = "No active non-shielded apps left to hibernate!"
                )
            }
            return
        }

        val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(context)
        val first = candidates.first()
        if (accessibilityEnabled) {
            AutoCacheCoordinator.beginAppStep(first.packageName, first.appName, AutoAutomationMode.FORCE_STOP_RAM)
        } else {
            AutoCacheCoordinator.stopAll()
        }
        _uiState.update {
            it.copy(
                showRamAccessibilityPrompt = false,
                isRamHibernateQueueActive = true,
                ramHibernateQueueIndex = 0,
                ramHibernateQueueTotal = candidates.size,
                ramHibernateQueuePackages = candidates.map { entry -> entry.packageName },
                activeHibernateApp = first,
                ramHibernateBeforeAvailBytes = state.ramTelemetry.availableBytes,
                ramHibernateBeforePercent = state.ramTelemetry.usedPercent,
                pendingOpenSettingsPackage = first.packageName,
                ramStatusMessage = if (accessibilityEnabled) {
                    "Deep Hibernating ${first.appName} (1/${candidates.size})…"
                } else {
                    "Opening ${first.appName} (1/${candidates.size}) → Tap Force Stop → OK"
                }
            )
        }
    }

    fun hibernateSingleApp(entry: RamProcessEntry) {
        val context = getApplication<Application>()
        if (entry.packageName == context.packageName || entry.isWhitelisted) return
        val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(context)
        if (accessibilityEnabled) {
            AutoCacheCoordinator.beginAppStep(entry.packageName, entry.appName, AutoAutomationMode.FORCE_STOP_RAM)
        } else {
            AutoCacheCoordinator.stopAll()
        }
        val state = _uiState.value
        _uiState.update {
            it.copy(
                isRamHibernateQueueActive = true,
                ramHibernateQueueIndex = 0,
                ramHibernateQueueTotal = 1,
                ramHibernateQueuePackages = listOf(entry.packageName),
                activeHibernateApp = entry,
                ramHibernateBeforeAvailBytes = state.ramTelemetry.availableBytes,
                ramHibernateBeforePercent = state.ramTelemetry.usedPercent,
                pendingOpenSettingsPackage = entry.packageName,
                ramStatusMessage = if (accessibilityEnabled) {
                    "Auto Force-Stopping ${entry.appName}…"
                } else {
                    "Opening ${entry.appName} → Tap Force Stop → OK to release RAM"
                }
            )
        }
    }

    fun launchNextHibernateApp() {
        val next = _uiState.value.activeHibernateApp ?: return
        val accessibilityEnabled = AutoCacheCoordinator.isAccessibilityServiceEnabled(getApplication())
        if (accessibilityEnabled) {
            AutoCacheCoordinator.beginAppStep(next.packageName, next.appName, AutoAutomationMode.FORCE_STOP_RAM)
        }
        _uiState.update {
            it.copy(
                pendingOpenSettingsPackage = next.packageName,
                ramStatusMessage = "Opening ${next.appName} (${it.ramHibernateQueueIndex + 1}/${it.ramHibernateQueueTotal}) → Force Stop → OK"
            )
        }
    }

    fun stopRamHibernateQueue() {
        AutoCacheCoordinator.stopAll()
        _uiState.update {
            it.copy(
                isRamHibernateQueueActive = false,
                ramHibernateQueuePackages = emptyList(),
                activeHibernateApp = null,
                pendingOpenSettingsPackage = null,
                ramStatusMessage = "Deep Hibernate queue stopped."
            )
        }
    }

    fun dismissRamAccessibilityPrompt() {
        _uiState.update {
            it.copy(
                showRamAccessibilityPrompt = false,
                ramHibernateQueuePackages = if (it.isRamHibernateQueueActive) it.ramHibernateQueuePackages else emptyList()
            )
        }
    }

    fun inspectRamApp(entry: RamProcessEntry?) {
        _uiState.update { it.copy(inspectedRamApp = entry) }
    }

    fun toggleLiveRamMonitor(enabled: Boolean) {
        liveRamMonitorJob?.cancel()
        liveRamMonitorJob = null
        _uiState.update {
            it.copy(
                isLiveRamMonitorEnabled = enabled,
                ramStatusMessage = if (enabled) {
                    "Live RAM Pulse Monitor active (sampling every 3s)"
                } else {
                    "Live RAM Pulse Monitor paused"
                }
            )
        }
        if (enabled) {
            liveRamMonitorJob = viewModelScope.launch {
                while (true) {
                    delay(3000L)
                    if (_uiState.value.route != ScreenRoute.ModuleDetail(CleanerModule.RAM)) {
                        _uiState.update { it.copy(isLiveRamMonitorEnabled = false) }
                        break
                    }
                    if (!_uiState.value.isOptimizingRam && !_uiState.value.isRamHibernateQueueActive) {
                        val live = repo.loadRamTelemetry()
                        _uiState.update { state ->
                            val updatedInspected = state.inspectedRamApp?.let { curr ->
                                live.backgroundApps.find { it.packageName == curr.packageName }
                            }
                            state.copy(
                                ramTelemetry = live.copy(
                                    lastFreedBytes = state.ramTelemetry.lastFreedBytes,
                                    lastKilledCount = state.ramTelemetry.lastKilledCount,
                                    lastDropBeforePercent = state.ramTelemetry.lastDropBeforePercent,
                                    lastDropAfterPercent = state.ramTelemetry.lastDropAfterPercent,
                                    lastDropBytes = state.ramTelemetry.lastDropBytes,
                                    lastStoppedProcessPssBytes = state.ramTelemetry.lastStoppedProcessPssBytes
                                ),
                                inspectedRamApp = updatedInspected
                            )
                        }
                    }
                }
            }
        }
    }

    fun runHeapAndBufferCompact() {
        if (_uiState.value.isOptimizingRam) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isOptimizingRam = true,
                    ramOptimizationProgress = 0.25f,
                    ramStatusMessage = "Compacting ART heap, SQLite pages & internal buffers…"
                )
            }
            val updated = repo.optimizeRamStaged(emptyList(), "Heap Compact") { progress, label, snapshot ->
                _uiState.update {
                    it.copy(
                        ramOptimizationProgress = progress.coerceIn(0.25f, 0.96f),
                        ramTelemetry = snapshot,
                        ramStatusMessage = label
                    )
                }
            }
            val refreshedHistory = repo.getRamBoostHistory()
            _uiState.update {
                it.copy(
                    isOptimizingRam = false,
                    ramOptimizationProgress = 1f,
                    ramTelemetry = updated,
                    ramBoostHistory = refreshedHistory,
                    ramStatusMessage = if (updated.lastFreedBytes > 0L) {
                        "Memory Compacted • Reclaimed ${formatBytes(updated.lastFreedBytes)}"
                    } else {
                        "Heap & internal buffers compacted cleanly"
                    }
                )
            }
        }
    }

    fun selectStorageBoostMode(mode: StorageBoostMode) {
        _uiState.update {
            it.copy(
                selectedStorageBoostMode = mode,
                storageStatusMessage = "${mode.label} selected • ${mode.detail}"
            )
        }
    }

    fun setIncludeApksInStorageBoost(include: Boolean) {
        repo.setIncludeApksInStorageBoost(include)
        _uiState.update {
            it.copy(
                includeApksInStorageBoost = include,
                storageStatusMessage = if (include) {
                    "Extreme Boost will also purge old .apk installers"
                } else {
                    "Installer .apk packages kept safe during Storage Boost"
                }
            )
        }
    }

    fun setAutoSweepSafInBoost(enabled: Boolean) {
        repo.setAutoSweepSafInBoost(enabled)
        _uiState.update {
            it.copy(
                autoSweepSafInBoost = enabled,
                storageStatusMessage = if (enabled) {
                    "Boost will auto-sweep safe junk in your linked phone folder"
                } else {
                    "Boost scoped to internal app buffers only"
                }
            )
        }
    }

    fun runStorageBoost() {
        if (_uiState.value.isBoostingStorage) return
        viewModelScope.launch {
            val snapshot = _uiState.value
            val mode = snapshot.selectedStorageBoostMode
            _uiState.update {
                it.copy(
                    isBoostingStorage = true,
                    storageBoostProgress = 0.06f,
                    storageBoostStageIndex = 1,
                    storageStatusMessage = "Igniting ${mode.label} Reactor…"
                )
            }

            val totalAppCache = snapshot.totalDetectedAppCacheBytes
            val totalAppData = snapshot.cacheEntries.sumOf { it.appBytes + it.dataBytes }
            val screenSelectedIds = snapshot.tempFiles
                .filter { it.isSelected && !it.isDeleted }
                .map { it.id }
                .toSet()
            val (updatedStorage, benchmark) = repo.executeStorageBoostStaged(
                totalAppCacheBytes = totalAppCache,
                totalAppDataBytes = totalAppData,
                mode = mode,
                treeUri = snapshot.scannedFolderUri,
                sweepSafFolder = snapshot.autoSweepSafInBoost,
                includeApks = snapshot.includeApksInStorageBoost,
                explicitSelectedIds = screenSelectedIds
            ) { progress, stageIndex, stageLabel ->
                _uiState.update { state ->
                    state.copy(
                        storageBoostProgress = progress.coerceIn(0.06f, 0.96f),
                        storageBoostStageIndex = stageIndex,
                        storageStatusMessage = stageLabel
                    )
                }
            }

            val refreshedTemps = repo.scanTempFiles(snapshot.scannedFolderUri, snapshot.storageLargeThresholdBytes)
            val refreshedHistory = repo.getStorageCleanHistory()
            val refreshedBreakdown = repo.measureInternalBufferBreakdown()
            val freedLabel = formatBytes(updatedStorage.lastBoostFreedBytes)
            val speedPart = if (benchmark.writeSpeedMbPerSec > 0f) {
                " • Flash I/O ${benchmark.writeSpeedMbPerSec.toInt()} MB/s"
            } else ""

            _uiState.update {
                it.copy(
                    isBoostingStorage = false,
                    storageBoostProgress = 1f,
                    storageBoostStageIndex = 4,
                    storageTelemetry = updatedStorage,
                    storageBoostResultSummary = benchmark,
                    storageCleanHistory = refreshedHistory,
                    internalBufferBreakdown = refreshedBreakdown,
                    ownAppCacheBytes = updatedStorage.cleanableOwnCacheBytes,
                    tempFiles = refreshedTemps,
                    storageStatusMessage = "${mode.label} Complete • $freedLabel swept (${updatedStorage.lastBoostItemsCleaned} items)$speedPart"
                )
            }
        }
    }

    fun scanPhoneTempFiles(treeUri: Uri? = _uiState.value.scannedFolderUri) {
        if (_uiState.value.isScanningTempFiles) return
        viewModelScope.launch {
            val targetUri = treeUri ?: _uiState.value.scannedFolderUri ?: repo.getSavedStorageFolderUri()
            val limit = _uiState.value.storageLargeThresholdBytes
            _uiState.update {
                it.copy(
                    isScanningTempFiles = true,
                    scannedFolderUri = targetUri,
                    tempFilesStatusMessage = "Deep scanning temp junk, APKs, duplicates & large files ≥ ${formatBytes(limit)}..."
                )
            }
            val files = repo.scanTempFiles(targetUri, limit)
            val totalSize = files.sumOf { it.sizeBytes }
            val msg = if (files.isEmpty()) {
                "No temp junk, APKs, duplicates or large files found in current scope"
            } else {
                "Found ${files.size} item(s) (${formatBytes(totalSize)}) across scanned storage"
            }
            _uiState.update {
                it.copy(
                    isScanningTempFiles = false,
                    tempFiles = files,
                    tempFilesStatusMessage = msg
                )
            }
        }
    }

    fun setStorageLargeThreshold(bytes: Long) {
        repo.setLargeFileThresholdBytes(bytes)
        _uiState.update {
            it.copy(
                storageLargeThresholdBytes = bytes,
                tempFilesStatusMessage = "Large file detector threshold set to ${formatBytes(bytes)}"
            )
        }
        scanPhoneTempFiles(_uiState.value.scannedFolderUri)
    }

    fun applyStorageSelectionPreset(preset: String) {
        _uiState.update { state ->
            val updated = state.tempFiles.map { entry ->
                val select = when (preset) {
                    "JUNK_ONLY" -> entry.category == TempFileCategory.TEMP_CACHE ||
                        entry.category == TempFileCategory.LOG_FILE ||
                        entry.category == TempFileCategory.RESIDUAL_PART ||
                        entry.category == TempFileCategory.EMPTY_FILE
                    "APKS" -> entry.category == TempFileCategory.APK_INSTALLER
                    "DUPLICATES" -> entry.category == TempFileCategory.DUPLICATE_FILE
                    "ALL" -> true
                    else -> false
                }
                entry.copy(isSelected = select)
            }
            val count = updated.count { it.isSelected }
            val size = updated.filter { it.isSelected }.sumOf { it.sizeBytes }
            state.copy(
                tempFiles = updated,
                tempFilesStatusMessage = when (preset) {
                    "JUNK_ONLY" -> "Selected $count safe junk file(s) (${formatBytes(size)})"
                    "APKS" -> "Selected $count installer APK(s) (${formatBytes(size)})"
                    "DUPLICATES" -> "Selected $count duplicate file(s) (${formatBytes(size)})"
                    "ALL" -> "Selected all $count item(s) (${formatBytes(size)})"
                    else -> "Cleared file selection"
                }
            )
        }
    }

    fun clearStorageCleanHistory() {
        val cleared = repo.clearStorageCleanHistory()
        _uiState.update {
            it.copy(
                storageCleanHistory = cleared,
                storageStatusMessage = "Storage clean history log cleared"
            )
        }
    }

    fun toggleTempFileSelection(id: String) {
        _uiState.update { state ->
            state.copy(
                tempFiles = state.tempFiles.map {
                    if (it.id == id) it.copy(isSelected = !it.isSelected) else it
                }
            )
        }
    }

    fun cleanSelectedTempFiles() {
        val state = _uiState.value
        if (state.isCleaningTempFiles) return
        val selected = state.tempFiles.filter { it.isSelected && !it.isDeleted }
        if (selected.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCleaningTempFiles = true,
                    tempFilesStatusMessage = "Deleting ${selected.size} selected file(s)..."
                )
            }
            val (deletedCount, freedBytes) = repo.cleanSelectedTempFiles(selected)
            val updatedHistory = if (deletedCount > 0 || freedBytes > 0L) {
                repo.appendStorageCleanHistory(
                    title = "Junk & File Sweep ($deletedCount files)",
                    itemCount = deletedCount,
                    freedBytes = freedBytes,
                    categoryLabel = "Deep Cleaner"
                )
            } else {
                state.storageCleanHistory
            }
            val remaining = repo.scanTempFiles(state.scannedFolderUri, state.storageLargeThresholdBytes)
            val totalAppData = state.cacheEntries.sumOf { it.appBytes + it.dataBytes }
            val updatedStorage = repo.loadStorageTelemetry(state.totalDetectedAppCacheBytes, totalAppData)
            val refreshedBreakdown = repo.measureInternalBufferBreakdown()
            _uiState.update {
                it.copy(
                    isCleaningTempFiles = false,
                    tempFiles = remaining,
                    storageTelemetry = updatedStorage.copy(
                        lastBoostFreedBytes = freedBytes.coerceAtLeast(state.storageTelemetry.lastBoostFreedBytes)
                    ),
                    storageCleanHistory = updatedHistory,
                    internalBufferBreakdown = refreshedBreakdown,
                    ownAppCacheBytes = updatedStorage.cleanableOwnCacheBytes,
                    lastTempFreedBytes = it.lastTempFreedBytes + freedBytes,
                    tempFilesStatusMessage = "Cleaned $deletedCount file(s) • Freed ${formatBytes(freedBytes)}"
                )
            }
        }
    }

    fun setAutoDeleteUnnecessaryFiles(enabled: Boolean) {
        repo.setVirusAutoDeleteUnnecessary(enabled)
        _uiState.update {
            it.copy(
                autoDeleteUnnecessaryFiles = enabled,
                virusStatusMessage = if (enabled) {
                    "Auto-Clean active: Unnecessary junk & temp files are auto-deleted on scan"
                } else {
                    "Auto-Delete paused: Only threat scanning active"
                }
            )
        }
    }

    fun runZeroVirusScan() {
        if (_uiState.value.isScanningVirus) return
        viewModelScope.launch {
            val autoDelete = _uiState.value.autoDeleteUnnecessaryFiles
            val savedTree = _uiState.value.scannedFolderUri ?: repo.getSavedStorageFolderUri()
            _uiState.update {
                it.copy(
                    isScanningVirus = true,
                    virusScanProgress = 0.04f,
                    currentScanLayer = "Initializing Deep Threat & Auto-Clean Matrix",
                    virusStatusMessage = if (autoDelete) {
                        "Scanning apps & auto-deleting unnecessary junk files..."
                    } else {
                        "Hunting hidden apps, sideloads, and risky permissions..."
                    }
                )
            }

            val posture = repo.runDeepVirusHunt(
                autoDeleteUnnecessary = autoDelete,
                treeUri = savedTree
            ) { appName, layerName, index, total ->
                _uiState.update {
                    it.copy(
                        virusScanProgress = (index.toFloat() / total.toFloat()).coerceIn(0.04f, 0.98f),
                        currentScannedAppName = appName,
                        currentScanLayer = layerName
                    )
                }
                if (index <= 10) delay(28L)
            }

            val refreshedTemps = repo.scanTempFiles(savedTree, _uiState.value.storageLargeThresholdBytes)
            val refreshedHistory = repo.getStorageCleanHistory()
            val refreshedOwnCache = repo.measureOwnAppCacheBytes()
            val refreshedBreakdown = repo.measureInternalBufferBreakdown()
            val totalFlagged = posture.criticalCount + posture.suspiciousCount

            val autoCleanSummary = if (posture.autoDeletedFilesCount > 0 || posture.autoDeletedBytes > 0L) {
                "Auto-deleted ${posture.autoDeletedFilesCount} unnecessary files (${formatBytes(posture.autoDeletedBytes)})"
            } else {
                "Unnecessary junk buffers already clean"
            }
            val msg = if (totalFlagged == 0) {
                "Zero Threats • ${posture.scannedCount} apps safe • $autoCleanSummary"
            } else {
                "Found $totalFlagged Threat(s) (${posture.hiddenCount} hidden) • $autoCleanSummary"
            }

            _uiState.update {
                it.copy(
                    isScanningVirus = false,
                    virusScanProgress = 1f,
                    currentScannedAppName = "",
                    currentScanLayer = "Scan & Cleanup Complete",
                    securityPosture = posture,
                    tempFiles = refreshedTemps,
                    storageCleanHistory = refreshedHistory,
                    internalBufferBreakdown = refreshedBreakdown,
                    ownAppCacheBytes = refreshedOwnCache,
                    virusStatusMessage = msg
                )
            }
        }
    }

    fun neutralizeDetectedThreats() {
        val state = _uiState.value
        if (state.isNeutralizingThreats) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isNeutralizingThreats = true,
                    virusStatusMessage = "Terminating threat background processes & deleting unnecessary files..."
                )
            }
            val flagged = state.securityPosture.findings
                .filter { it.riskLevel != RiskLevel.SAFE }
                .map { it.packageName }
            val stopped = repo.neutralizeLocalThreats(flagged)
            val refreshedTemps = repo.scanTempFiles(state.scannedFolderUri, state.storageLargeThresholdBytes)
            delay(450L)
            _uiState.update {
                it.copy(
                    isNeutralizingThreats = false,
                    tempFiles = refreshedTemps,
                    ownAppCacheBytes = repo.measureOwnAppCacheBytes(),
                    virusStatusMessage = if (flagged.isEmpty()) {
                        "System Clean • Zero active threats & all unnecessary files deleted"
                    } else {
                        "Neutralized $stopped threat background process(es) & purged artifacts • Remove flagged apps below"
                    }
                )
            }
        }
    }

    fun selectBatteryMode(mode: BatteryOptimizationMode) {
        _uiState.update {
            it.copy(
                selectedBatteryMode = mode,
                batteryStatusMessage = "${mode.label} ready • ${mode.detail}"
            )
        }
    }

    fun toggleBatteryOptimizationEnabled(enabled: Boolean) {
        repo.setBatteryOptimizationEnabled(enabled)
        _uiState.update { state ->
            state.copy(
                isBatteryOptimizationEnabled = enabled,
                batteryTelemetry = state.batteryTelemetry.copy(isOptimizationActive = enabled),
                batteryStatusMessage = if (enabled) {
                    "Battery Optimization ENABLED • Power guard & auto-cache active"
                } else {
                    "Battery Optimization paused"
                }
            )
        }
        if (enabled && _uiState.value.autoCleanCacheForBattery) {
            runBatteryAutoCacheSweep()
        }
    }

    fun toggleAutoCleanCacheForBattery(enabled: Boolean) {
        repo.setAutoCleanCacheForBattery(enabled)
        _uiState.update {
            it.copy(
                autoCleanCacheForBattery = enabled,
                batteryStatusMessage = if (enabled) {
                    "Automatic Battery Cache Cleaner enabled"
                } else {
                    "Automatic Battery Cache Cleaner disabled"
                }
            )
        }
        if (enabled && _uiState.value.isBatteryOptimizationEnabled) {
            runBatteryAutoCacheSweep()
        }
    }

    fun runBatteryAutoCacheSweep() {
        viewModelScope.launch {
            lastBatteryAutoCleanMs = System.currentTimeMillis()
            val savedTree = _uiState.value.scannedFolderUri ?: repo.getSavedStorageFolderUri()
            val (cleanedFiles, freedBytes) = repo.purgeBatteryDrainCaches(savedTree)
            val totalBatteryFreed = repo.getTotalBatteryCacheFreedBytes()
            val refreshedHistory = repo.getCacheCleanHistory()
            val refreshedOwn = repo.measureOwnAppCacheBytes()
            val totalCacheRemoved = repo.getTotalCacheRemovedBytes()

            _uiState.update { state ->
                val boostedScore = if (freedBytes > 0L) (state.batteryTelemetry.healthScore + 2).coerceAtMost(100) else state.batteryTelemetry.healthScore
                state.copy(
                    ownAppCacheBytes = refreshedOwn,
                    cacheHistory = refreshedHistory,
                    totalCacheFreedSessionBytes = totalCacheRemoved,
                    batteryTelemetry = state.batteryTelemetry.copy(
                        lastCacheFreedBytes = freedBytes,
                        lastCleanedFilesCount = cleanedFiles,
                        totalBatteryCacheFreedBytes = totalBatteryFreed,
                        healthScore = boostedScore
                    ),
                    batteryStatusMessage = if (freedBytes > 0L || cleanedFiles > 0) {
                        "Auto-Cleaned $cleanedFiles cache file(s) (${formatBytes(freedBytes)}) for Battery Power"
                    } else {
                        "Battery Guard Active • Cache buffers already clean"
                    }
                )
            }
        }
    }

    fun runBatteryOptimization() {
        if (_uiState.value.isOptimizingBattery) return
        viewModelScope.launch {
            repo.setBatteryOptimizationEnabled(true)
            val mode = _uiState.value.selectedBatteryMode
            _uiState.update {
                it.copy(
                    isBatteryOptimizationEnabled = true,
                    isOptimizingBattery = true,
                    batteryStatusMessage = "Applying ${mode.label} • Auto-cleaning cache files & draining tasks..."
                )
            }
            delay(500L)

            val savedTree = _uiState.value.scannedFolderUri ?: repo.getSavedStorageFolderUri()
            val (cleanedFiles, cacheFreed) = if (_uiState.value.autoCleanCacheForBattery) {
                lastBatteryAutoCleanMs = System.currentTimeMillis()
                repo.purgeBatteryDrainCaches(savedTree)
            } else {
                0 to 0L
            }

            val pkgs = _uiState.value.ramTelemetry.backgroundApps.map { it.packageName }
            val ramUpdated = repo.optimizeRam(pkgs, "Battery ${mode.label}")
            val currentBattery = _uiState.value.batteryTelemetry
            val improvedScore = (currentBattery.healthScore + 6).coerceAtMost(100)
            val totalBatteryCacheFreed = repo.getTotalBatteryCacheFreedBytes()
            val newBattery = currentBattery.copy(
                lastCooledCount = ramUpdated.lastKilledCount,
                drainingAppsCount = 0,
                healthScore = improvedScore,
                lastCacheFreedBytes = cacheFreed,
                lastCleanedFilesCount = cleanedFiles,
                totalBatteryCacheFreedBytes = totalBatteryCacheFreed,
                isOptimizationActive = true
            )
            _uiState.update {
                it.copy(
                    isOptimizingBattery = false,
                    ramTelemetry = ramUpdated,
                    cacheHistory = repo.getCacheCleanHistory(),
                    totalCacheFreedSessionBytes = repo.getTotalCacheRemovedBytes(),
                    ownAppCacheBytes = repo.measureOwnAppCacheBytes(),
                    batteryTelemetry = newBattery,
                    batteryStatusMessage = buildString {
                        append("${mode.label} Enabled • Stopped ${ramUpdated.lastKilledCount} tasks")
                        if (cacheFreed > 0L || cleanedFiles > 0) {
                            append(" • Auto-cleaned $cleanedFiles cache (${formatBytes(cacheFreed)})")
                        }
                        append(" • Health ${improvedScore}%")
                    }
                )
            }
        }
    }
}
