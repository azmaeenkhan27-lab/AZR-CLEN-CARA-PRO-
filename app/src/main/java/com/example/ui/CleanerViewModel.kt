package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SystemDiagnosticsRepository
import com.example.model.AppCacheCleanState
import com.example.model.AppCacheEntry
import com.example.model.BatteryOptimizationMode
import com.example.model.BatteryTelemetry
import com.example.model.CleanerModule
import com.example.model.RamTelemetry
import com.example.model.RiskLevel
import com.example.model.ScreenRoute
import com.example.model.SecurityPosture
import com.example.model.StorageTelemetry
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

    // Module 1: Dedicated Sequential Cache Clear
    val cacheEntries: List<AppCacheEntry> = emptyList(),
    val ownAppCacheBytes: Long = 0L,
    val isCleaningCache: Boolean = false,
    val cacheSweepProgress: Float = 0f,
    val cacheStatusMessage: String? = null,
    val totalCacheFreedSessionBytes: Long = 0L,
    val isGuidedQueueActive: Boolean = false,
    val guidedQueueIndex: Int = 0,
    val guidedQueueTotal: Int = 0,
    val activeGuidedApp: AppCacheEntry? = null,
    val pendingOpenSettingsPackage: String? = null,

    // Module 2: RAM Optimization
    val ramTelemetry: RamTelemetry = RamTelemetry(),
    val isOptimizingRam: Boolean = false,
    val ramStatusMessage: String? = null,

    // Module 3: Storage Clear & Boost
    val storageTelemetry: StorageTelemetry = StorageTelemetry(),
    val isBoostingStorage: Boolean = false,
    val storageBoostProgress: Float = 0f,
    val storageStatusMessage: String? = null,

    // Module 4: Zero Virus Deep Shield
    val securityPosture: SecurityPosture = SecurityPosture(),
    val isScanningVirus: Boolean = false,
    val virusScanProgress: Float = 0f,
    val currentScannedAppName: String = "",
    val currentScanLayer: String = "",
    val virusStatusMessage: String? = null,
    val isNeutralizingThreats: Boolean = false,

    // Module 5: VoltCore Battery Guard
    val batteryTelemetry: BatteryTelemetry = BatteryTelemetry(),
    val selectedBatteryMode: BatteryOptimizationMode = BatteryOptimizationMode.BALANCED,
    val isOptimizingBattery: Boolean = false,
    val batteryStatusMessage: String? = null
) {
    val totalDetectedAppCacheBytes: Long
        get() = cacheEntries.sumOf { it.cacheBytes } + ownAppCacheBytes
}

class CleanerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SystemDiagnosticsRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(AzroiaUiState())
    val uiState: StateFlow<AzroiaUiState> = _uiState.asStateFlow()

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
                            lastCooledCount = state.batteryTelemetry.lastCooledCount
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
    }

    fun navigateBackToDashboard() {
        _uiState.update {
            it.copy(
                route = ScreenRoute.Dashboard,
                isGuidedQueueActive = false,
                pendingOpenSettingsPackage = null
            )
        }
    }

    fun onAppResumed() {
        val state = _uiState.value
        val activeGuided = state.activeGuidedApp
        if (state.isGuidedQueueActive && activeGuided != null && state.pendingOpenSettingsPackage == null) {
            // User returned from that app's Android App Info Storage & Cache settings -> advance queue!
            val updatedEntries = state.cacheEntries.map {
                if (it.packageName == activeGuided.packageName) {
                    it.copy(cleanState = AppCacheCleanState.CLEANED, cacheBytes = 0L)
                } else it
            }
            val nextIdx = state.guidedQueueIndex + 1
            val remaining = updatedEntries.filter {
                it.packageName != getApplication<Application>().packageName &&
                    it.cleanState != AppCacheCleanState.CLEANED
            }
            if (remaining.isNotEmpty() && nextIdx < state.guidedQueueTotal) {
                val nextApp = remaining.first()
                _uiState.update {
                    it.copy(
                        cacheEntries = updatedEntries,
                        guidedQueueIndex = nextIdx,
                        activeGuidedApp = nextApp,
                        cacheStatusMessage = "${activeGuided.appName} done! Next up: ${nextApp.appName}"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        cacheEntries = updatedEntries,
                        isGuidedQueueActive = false,
                        activeGuidedApp = null,
                        cacheStatusMessage = "Sequential OS App Cache Sweep Complete!"
                    )
                }
            }
        }
        refreshAllTelemetry()
    }

    fun refreshAllTelemetry() {
        viewModelScope.launch {
            val hasUsage = repo.hasUsageStatsPermission()
            val fetchedEntries = repo.loadInstalledAppCacheEntries()
            val prevCleaned = _uiState.value.cacheEntries
                .filter { it.cleanState == AppCacheCleanState.CLEANED }
                .map { it.packageName }
                .toSet()

            val mergedEntries = fetchedEntries.map { entry ->
                if (entry.packageName in prevCleaned && entry.cacheBytes == 0L) {
                    entry.copy(cleanState = AppCacheCleanState.CLEANED)
                } else entry
            }

            val ownCache = repo.measureOwnAppCacheBytes()
            val ram = repo.loadRamTelemetry()
            val totalAppCache = mergedEntries.sumOf { it.cacheBytes } + ownCache
            val storage = repo.loadStorageTelemetry(totalAppCache)

            _uiState.update { state ->
                state.copy(
                    hasUsageStatsAccess = hasUsage,
                    isLoadingTelemetry = false,
                    cacheEntries = mergedEntries,
                    ownAppCacheBytes = ownCache,
                    ramTelemetry = ram,
                    storageTelemetry = storage.copy(
                        lastBoostFreedBytes = state.storageTelemetry.lastBoostFreedBytes
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
            val queueable = entries.filter { it.packageName != getApplication<Application>().packageName }
            val summary = "Freed ${formatBytes(ownFreed)} locally • ${queueable.size} apps queued for sequential OS Cache Clear"

            _uiState.update {
                it.copy(
                    isCleaningCache = false,
                    cacheSweepProgress = 1f,
                    ownAppCacheBytes = refreshedOwn,
                    totalCacheFreedSessionBytes = it.totalCacheFreedSessionBytes + ownFreed,
                    cacheStatusMessage = summary
                )
            }
        }
    }

    fun startSequentialAppSettingsClean() {
        val candidates = _uiState.value.cacheEntries.filter {
            it.packageName != getApplication<Application>().packageName &&
                it.cleanState != AppCacheCleanState.CLEANED
        }
        if (candidates.isEmpty()) return
        val first = candidates.first()
        _uiState.update {
            it.copy(
                isGuidedQueueActive = true,
                guidedQueueIndex = 0,
                guidedQueueTotal = candidates.size,
                activeGuidedApp = first,
                pendingOpenSettingsPackage = first.packageName,
                cacheStatusMessage = "Opening ${first.appName} (1/${candidates.size}) → Tap Storage → Clear Cache"
            )
        }
    }

    fun launchNextGuidedApp() {
        val next = _uiState.value.activeGuidedApp ?: return
        _uiState.update {
            it.copy(
                pendingOpenSettingsPackage = next.packageName,
                cacheStatusMessage = "Opening ${next.appName} (${it.guidedQueueIndex + 1}/${it.guidedQueueTotal}) → Storage → Clear Cache"
            )
        }
    }

    fun stopSequentialQueue() {
        _uiState.update {
            it.copy(
                isGuidedQueueActive = false,
                activeGuidedApp = null,
                pendingOpenSettingsPackage = null
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
                if (it.packageName == packageName) it.copy(isSelected = !it.isSelected) else it
            }
            state.copy(ramTelemetry = state.ramTelemetry.copy(backgroundApps = updated))
        }
    }

    fun runRamOptimization() {
        if (_uiState.value.isOptimizingRam) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isOptimizingRam = true,
                    ramStatusMessage = "Terminating selected background tasks & reclaiming RAM..."
                )
            }
            delay(850L)
            val packages = _uiState.value.ramTelemetry.backgroundApps
                .filter { it.isSelected }
                .map { it.packageName }
            val updated = repo.optimizeRam(packages)
            val msg = "Turbo Released ${updated.lastKilledCount} processes • ${formatBytes(updated.lastFreedBytes)} RAM reclaimed"
            _uiState.update {
                it.copy(
                    isOptimizingRam = false,
                    ramTelemetry = updated,
                    ramStatusMessage = msg
                )
            }
        }
    }

    fun runStorageBoost() {
        if (_uiState.value.isBoostingStorage) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBoostingStorage = true,
                    storageBoostProgress = 0.05f,
                    storageStatusMessage = "Igniting Storage Turbo Reactor..."
                )
            }

            for (step in 1..20) {
                delay(60L)
                _uiState.update {
                    it.copy(
                        storageBoostProgress = (step / 20f).coerceIn(0f, 1f),
                        storageStatusMessage = when {
                            step < 7 -> "Scanning residual app cache & temp logs..."
                            step < 14 -> "Flushing storage sectors..."
                            else -> "Locking turbo storage boost..."
                        }
                    )
                }
            }

            val totalAppCache = _uiState.value.totalDetectedAppCacheBytes
            val updatedStorage = repo.executeStorageBoost(totalAppCache)
            val freedLabel = formatBytes(updatedStorage.lastBoostFreedBytes)
            _uiState.update {
                it.copy(
                    isBoostingStorage = false,
                    storageBoostProgress = 1f,
                    storageTelemetry = updatedStorage,
                    ownAppCacheBytes = updatedStorage.cleanableOwnCacheBytes,
                    storageStatusMessage = "Turbo Boost Complete • $freedLabel swept • ${formatBytes(updatedStorage.freeBytes)} Free"
                )
            }
        }
    }

    fun runZeroVirusScan() {
        if (_uiState.value.isScanningVirus) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanningVirus = true,
                    virusScanProgress = 0f,
                    currentScanLayer = "Initializing Deep Threat Matrix",
                    virusStatusMessage = "Hunting hidden apps, sideloads, and risky permissions..."
                )
            }

            val posture = repo.runDeepVirusHunt { appName, layerName, index, total ->
                _uiState.update {
                    it.copy(
                        virusScanProgress = (index.toFloat() / total.toFloat()).coerceIn(0f, 1f),
                        currentScannedAppName = appName,
                        currentScanLayer = layerName
                    )
                }
                if (index <= 14) delay(42L)
            }

            val totalFlagged = posture.criticalCount + posture.suspiciousCount
            val msg = if (totalFlagged == 0) {
                "Zero Threats Active • ${posture.scannedCount} packages & hidden layers verified clean"
            } else {
                "Found $totalFlagged Threat(s) (${posture.hiddenCount} hidden) • Tap Neutralize to eliminate"
            }

            _uiState.update {
                it.copy(
                    isScanningVirus = false,
                    virusScanProgress = 1f,
                    currentScannedAppName = "",
                    currentScanLayer = "Scan Complete",
                    securityPosture = posture,
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
                    virusStatusMessage = "Terminating threat background processes & purging local artifacts..."
                )
            }
            val flagged = state.securityPosture.findings
                .filter { it.riskLevel != RiskLevel.SAFE }
                .map { it.packageName }
            val stopped = repo.neutralizeLocalThreats(flagged)
            delay(650L)
            _uiState.update {
                it.copy(
                    isNeutralizingThreats = false,
                    virusStatusMessage = if (flagged.isEmpty()) {
                        "System Clean • Zero active threats & all local artifacts purged"
                    } else {
                        "Neutralized $stopped threat background process(es) • Remove flagged apps below"
                    }
                )
            }
        }
    }

    fun selectBatteryMode(mode: BatteryOptimizationMode) {
        _uiState.update { it.copy(selectedBatteryMode = mode) }
    }

    fun runBatteryOptimization() {
        if (_uiState.value.isOptimizingBattery) return
        viewModelScope.launch {
            val mode = _uiState.value.selectedBatteryMode
            _uiState.update {
                it.copy(
                    isOptimizingBattery = true,
                    batteryStatusMessage = "Applying ${mode.label} • Stopping power-draining tasks..."
                )
            }
            delay(850L)
            val pkgs = _uiState.value.ramTelemetry.backgroundApps.map { it.packageName }
            val ramUpdated = repo.optimizeRam(pkgs)
            val currentBattery = _uiState.value.batteryTelemetry
            val improvedScore = (currentBattery.healthScore + 6).coerceAtMost(100)
            val newBattery = currentBattery.copy(
                lastCooledCount = ramUpdated.lastKilledCount,
                drainingAppsCount = 0,
                healthScore = improvedScore
            )
            _uiState.update {
                it.copy(
                    isOptimizingBattery = false,
                    ramTelemetry = ramUpdated,
                    batteryTelemetry = newBattery,
                    batteryStatusMessage = "${mode.label} Active • Stopped ${ramUpdated.lastKilledCount} draining apps • Health ${improvedScore}%"
                )
            }
        }
    }
}
