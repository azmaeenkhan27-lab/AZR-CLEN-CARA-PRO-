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
    val cleanState: AppCacheCleanState = AppCacheCleanState.PENDING
)

data class RamProcessEntry(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap?,
    val importanceLabel: String,
    val isSelected: Boolean = true,
    val isTerminated: Boolean = false
)

data class RamTelemetry(
    val totalBytes: Long = 0L,
    val availableBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercent: Int = 0,
    val isLowMemory: Boolean = false,
    val backgroundApps: List<RamProcessEntry> = emptyList(),
    val lastFreedBytes: Long = 0L,
    val lastKilledCount: Int = 0
)

data class StorageTelemetry(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercent: Int = 0,
    val cleanableOwnCacheBytes: Long = 0L,
    val residualTempBytes: Long = 0L,
    val totalAppCacheBytes: Long = 0L,
    val lastBoostFreedBytes: Long = 0L
)

enum class TempFileCategory(val label: String) {
    TEMP_CACHE("Temp / Cache"),
    LOG_FILE("System Log"),
    RESIDUAL_PART("Partial / Bak"),
    EMPTY_FILE("0-Byte Junk")
}

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
    val findings: List<VirusScanFinding> = emptyList(),
    val lastScanCompleted: Boolean = false
)

enum class BatteryOptimizationMode(val label: String, val detail: String) {
    BALANCED("Balanced Care", "Stops high-drain background tasks"),
    TURBO_COOL("Turbo Cool", "Aggressive thermal & process relief"),
    ULTRA_SAVER("Ultra Saver", "Max endurance + system Battery Saver")
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
    val lastCooledCount: Int = 0
)
