package com.example.model

import androidx.compose.ui.graphics.ImageBitmap

enum class CleanerModule(
    val title: String,
    val tag: String,
    val subtitle: String
) {
    CACHE(
        title = "Cache Clear",
        tag = "APP CACHE ENGINE",
        subtitle = "Inspect & sweep app cache buffers"
    ),
    RAM(
        title = "RAM Optimize",
        tag = "MEMORY TURBO",
        subtitle = "Release background RAM load"
    ),
    STORAGE(
        title = "Storage Clear",
        tag = "TURBO BOOST",
        subtitle = "Ignite boost & clean residual storage"
    ),
    VIRUS(
        title = "Zero Virus",
        tag = "DEEP THREAT SHIELD",
        subtitle = "Hunt hidden threats & destroy risks"
    ),
    BATTERY(
        title = "VoltCore Guard",
        tag = "BATTERY HEALTH PRO",
        subtitle = "Thermal diagnostics & power repair"
    )
}

sealed interface ScreenRoute {
    data object Splash : ScreenRoute
    data object Dashboard : ScreenRoute
    data class ModuleDetail(val module: CleanerModule) : ScreenRoute
}

enum class AppCacheCleanState {
    PENDING,
    CLEANING,
    CLEANED,
    SYSTEM_ACTION_READY
}

data class AppCacheEntry(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap?,
    val cacheBytes: Long,
    val appBytes: Long,
    val isPriorityMediaApp: Boolean,
    val cleanState: AppCacheCleanState = AppCacheCleanState.PENDING,
    val dataBytes: Long = 0L,
    val lastUsedTimeMs: Long = 0L,
    val foregroundTimeMs: Long = 0L,
    val wasUsedByUser: Boolean = false,
    val isSelectedForClean: Boolean = true,
    val isWhitelisted: Boolean = false,
    val versionName: String = "",
    val installerLabel: String = ""
) {
    val totalFootprintBytes: Long
        get() = (appBytes + dataBytes + cacheBytes).coerceAtLeast(0L)
}

data class CacheAreaBreakdown(
    val mediaSocialBytes: Long = 0L,
    val browserToolsBytes: Long = 0L,
    val otherAppsBytes: Long = 0L,
    val localBufferBytes: Long = 0L
) {
    val totalAreaBytes: Long
        get() = (mediaSocialBytes + browserToolsBytes + otherAppsBytes + localBufferBytes).coerceAtLeast(0L)
}

enum class CacheThresholdPreset(val label: String, val bytes: Long) {
    MB_50("50 MB", 50L * 1024L * 1024L),
    MB_150("150 MB", 150L * 1024L * 1024L),
    MB_300("300 MB", 300L * 1024L * 1024L),
    MB_500("500 MB", 500L * 1024L * 1024L)
}

enum class CacheSelectionPreset(val label: String) {
    OVER_THRESHOLD("Over Limit"),
    TOP_FIVE("Top 5 Hogs"),
    MEDIA_ONLY("Media Hogs")
}

data class CacheCleanHistoryRecord(
    val id: String,
    val timestampMs: Long,
    val appName: String,
    val packageName: String,
    val freedBytes: Long,
    val methodLabel: String
)

data class InternalBufferBreakdown(
    val cacheDirBytes: Long = 0L,
    val codeCacheDirBytes: Long = 0L,
    val externalCacheDirBytes: Long = 0L,
    val tempLogsBytes: Long = 0L
) {
    val totalBytes: Long
        get() = (cacheDirBytes + codeCacheDirBytes + externalCacheDirBytes + tempLogsBytes).coerceAtLeast(0L)
}

enum class RamBoostMode(val label: String, val detail: String) {
    BALANCED("Balanced", "Clears idle & cached background apps safely"),
    DEEP_TURBO("Deep Turbo", "Multi-pass reclaim on non-shielded apps + trims buffers"),
    GAMING_FOCUS("Gaming Focus", "Maximizes free RAM headroom & suppresses background tasks")
}

enum class RamPressureThresholdPreset(val label: String, val percent: Int) {
    PCT_75("75%", 75),
    PCT_80("80%", 80),
    PCT_85("85%", 85),
    PCT_90("90%", 90)
}

data class KernelMemoryBreakdown(
    val memFreeBytes: Long = 0L,
    val pageCacheBytes: Long = 0L,
    val buffersBytes: Long = 0L,
    val swapTotalBytes: Long = 0L,
    val swapFreeBytes: Long = 0L,
    val ownAppPssBytes: Long = 0L
) {
    val swapUsedBytes: Long
        get() = (swapTotalBytes - swapFreeBytes).coerceAtLeast(0L)

    val swapUsedPercent: Int
        get() = if (swapTotalBytes > 0L) {
            ((swapUsedBytes.toDouble() / swapTotalBytes.toDouble()) * 100.0).toInt().coerceIn(0, 100)
        } else 0
}

data class RamProcessEntry(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap?,
    val importanceLabel: String,
    val isSelected: Boolean = true,
    val isTerminated: Boolean = false,
    val isActiveProcess: Boolean = false,
    val processPssBytes: Long = 0L,
    val lastUsedTimeMs: Long = 0L,
    val isWhitelisted: Boolean = false,
    val isSystemApp: Boolean = false
)

data class RamBoostHistoryRecord(
    val id: String,
    val timestampMs: Long,
    val killedCount: Int,
    val freedBytes: Long,
    val beforePercent: Int,
    val afterPercent: Int,
    val modeLabel: String
)

data class RamTelemetry(
    val totalBytes: Long = 0L,
    val availableBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercent: Int = 0,
    val isLowMemory: Boolean = false,
    val lowMemoryThresholdBytes: Long = 0L,
    val memoryClassMb: Int = 0,
    val largeMemoryClassMb: Int = 0,
    val activeProcessCount: Int = 0,
    val backgroundApps: List<RamProcessEntry> = emptyList(),
    val lastFreedBytes: Long = 0L,
    val lastKilledCount: Int = 0,
    val totalReclaimedBytes: Long = 0L,
    val lastDropBeforePercent: Int = 0,
    val lastDropAfterPercent: Int = 0,
    val lastDropBytes: Long = 0L,
    val lastStoppedProcessPssBytes: Long = 0L,
    val kernelBreakdown: KernelMemoryBreakdown = KernelMemoryBreakdown()
) {
    val healthScore: Int
        get() {
            val base = (100 - (usedPercent * 0.65).toInt()).coerceIn(20, 100)
            val lowPenalty = if (isLowMemory) 18 else 0
            val reclaimedBonus = if (lastFreedBytes > 0L || lastStoppedProcessPssBytes > 0L) 6 else 0
            return (base - lowPenalty + reclaimedBonus).coerceIn(15, 100)
        }
}

enum class StorageBoostMode(val label: String, val detail: String) {
    QUICK_SWEEP("Quick Sweep", "Purges app temp caches, .tmp/.log files & empty stubs"),
    TURBO_COMPACT("Turbo Compact", "Purges junk + trims SQLite pages, VM buffers & linked folder junk"),
    EXTREME_BOOST("Extreme Boost", "Multi-pass sector flush + flash I/O sync & deep cleanup")
}

data class StorageBoostBenchmark(
    val writeSpeedMbPerSec: Float = 0f,
    val sqlitePagesTrimmed: Boolean = false,
    val safJunkDeletedCount: Int = 0,
    val localFilesDeletedCount: Int = 0,
    val beforeFreeBytes: Long = 0L,
    val afterFreeBytes: Long = 0L,
    val totalFreedBytes: Long = 0L,
    val modeLabel: String = ""
)

data class StorageTelemetry(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercent: Int = 0,
    val cleanableOwnCacheBytes: Long = 0L,
    val residualTempBytes: Long = 0L,
    val totalAppCacheBytes: Long = 0L,
    val totalAppDataBytes: Long = 0L,
    val lastBoostFreedBytes: Long = 0L,
    val totalStorageReclaimedBytes: Long = 0L,
    val lastDropBeforePercent: Int = 0,
    val lastDropAfterPercent: Int = 0,
    val lastWriteSpeedMbPerSec: Float = 0f,
    val lastBoostItemsCleaned: Int = 0,
    val lastBoostModeLabel: String = ""
) {
    val healthScore: Int
        get() {
            val base = (100 - (usedPercent * 0.6).toInt()).coerceIn(20, 100)
            val cachePenalty = if (totalAppCacheBytes >= 1024L * 1024L * 1024L) 12 else 0
            val boostBonus = if (lastBoostFreedBytes > 0L || totalStorageReclaimedBytes > 0L) 6 else 0
            return (base - cachePenalty + boostBonus).coerceIn(15, 100)
        }
}

enum class TempFileCategory(val label: String) {
    TEMP_CACHE("Temp / Cache"),
    LOG_FILE("System Log"),
    RESIDUAL_PART("Partial / Bak"),
    EMPTY_FILE("0-Byte Junk"),
    APK_INSTALLER("APK Installer"),
    LARGE_FILE("Large File"),
    DUPLICATE_FILE("Duplicate")
}

enum class LargeFileThresholdPreset(val label: String, val bytes: Long) {
    MB_10("10 MB", 10L * 1024L * 1024L),
    MB_25("25 MB", 25L * 1024L * 1024L),
    MB_50("50 MB", 50L * 1024L * 1024L),
    MB_100("100 MB", 100L * 1024L * 1024L)
}

data class StorageCleanHistoryRecord(
    val id: String,
    val timestampMs: Long,
    val title: String,
    val itemCount: Int,
    val freedBytes: Long,
    val categoryLabel: String
)

data class TempFileEntry(
    val id: String,
    val name: String,
    val locationLabel: String,
    val sizeBytes: Long,
    val category: TempFileCategory,
    val isDocumentUri: Boolean,
    val isSelected: Boolean = true,
    val isDeleted: Boolean = false
)

enum class RiskLevel {
    SAFE,
    SUSPICIOUS,
    CRITICAL
}

data class VirusScanFinding(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap?,
    val riskLevel: RiskLevel,
    val installerLabel: String,
    val isHiddenPackage: Boolean,
    val riskReasons: List<String>
)

data class SecurityPosture(
    val securityPatch: String = "Unknown",
    val isDeviceLockSecure: Boolean = false,
    val scannedCount: Int = 0,
    val hiddenCount: Int = 0,
    val suspiciousCount: Int = 0,
    val criticalCount: Int = 0,
    val localArtifactsPurged: Int = 0,
    val autoDeletedFilesCount: Int = 0,
    val autoDeletedBytes: Long = 0L,
    val deletedFileNames: List<String> = emptyList(),
    val scanDurationMs: Long = 0L,
    val findings: List<VirusScanFinding> = emptyList(),
    val lastScanCompleted: Boolean = false
) {
    val threatDefenseScore: Int
        get() {
            if (!lastScanCompleted) return 88
            val critPenalty = (criticalCount * 16).coerceAtMost(50)
            val suspPenalty = (suspiciousCount * 5).coerceAtMost(25)
            val lockBonus = if (isDeviceLockSecure) 4 else -6
            val purgeBonus = if (autoDeletedFilesCount > 0 || localArtifactsPurged > 0) 4 else 0
            return (96 - critPenalty - suspPenalty + lockBonus + purgeBonus).coerceIn(20, 100)
        }
}

enum class BatteryOptimizationMode(val label: String, val detail: String) {
    BALANCED("Balanced Care", "Stops high-drain background tasks + auto cache sweep"),
    TURBO_COOL("Turbo Cool", "Aggressive thermal relief + deep buffer cache purge"),
    ULTRA_SAVER("Ultra Saver", "Max endurance + system Battery Saver + auto cache clean")
}

data class BatteryTelemetry(
    val levelPercent: Int = 100,
    val isCharging: Boolean = false,
    val plugSource: String = "Battery",
    val healthLabel: String = "Optimal",
    val isHealthy: Boolean = true,
    val healthScore: Int = 96,
    val temperatureCelsius: Float = 28.0f,
    val voltageVolts: Float = 4.1f,
    val technology: String = "Li-ion",
    val currentMilliamps: Int? = null,
    val thermalWarning: Boolean = false,
    val drainingAppsCount: Int = 0,
    val lastCooledCount: Int = 0,
    val lastCacheFreedBytes: Long = 0L,
    val totalBatteryCacheFreedBytes: Long = 0L,
    val lastCleanedFilesCount: Int = 0,
    val isOptimizationActive: Boolean = true
)
