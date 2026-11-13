package com.example.data

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.KeyguardManager
import android.app.usage.StorageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.storage.StorageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.model.AppCacheEntry
import com.example.model.BatteryTelemetry
import com.example.model.RamProcessEntry
import com.example.model.RamTelemetry
import com.example.model.RiskLevel
import com.example.model.SecurityPosture
import com.example.model.StorageTelemetry
import com.example.model.VirusScanFinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class SystemDiagnosticsRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val iconCache = ConcurrentHashMap<String, ImageBitmap>()

    private val priorityMediaPackages = setOf(
        "com.google.android.youtube",
        "com.facebook.katana",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.google.android.apps.nbu.files",
        "com.sec.android.app.myfiles",
        "com.android.documentsui",
        "com.android.chrome",
        "com.instagram.android",
        "com.whatsapp"
    )

    private val trustedInstallers = setOf(
        "com.android.vending",
        "com.google.android.feedback",
        "com.sec.android.app.samsungapps",
        "com.amazon.venezia",
        "com.huawei.appmarket"
    )

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    suspend fun loadInstalledAppCacheEntries(): List<AppCacheEntry> = withContext(Dispatchers.IO) {
        val hasUsage = hasUsageStatsPermission()
        val storageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasUsage) {
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
        } else {
            null
        }

        val launcherPackages = queryLauncherPackages()
        val entries = mutableListOf<AppCacheEntry>()

        for (appInfo in launcherPackages) {
            val pkg = appInfo.packageName
            val label = runCatching { pm.getApplicationLabel(appInfo).toString() }.getOrDefault(pkg)
            val icon = loadIconBitmap(pkg, appInfo)
            val isPriority = pkg in priorityMediaPackages

            var cacheBytes = 0L
            var appBytes = 0L

            if (pkg == context.packageName) {
                cacheBytes = measureOwnAppCacheBytes()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && storageStatsManager != null) {
                runCatching {
                    val stats = storageStatsManager.queryStatsForPackage(
                        StorageManager.UUID_DEFAULT,
                        pkg,
                        Process.myUserHandle()
                    )
                    cacheBytes = stats.cacheBytes
                    appBytes = stats.appBytes
                }
            }

            entries.add(
                AppCacheEntry(
                    packageName = pkg,
                    appName = label,
                    icon = icon,
                    cacheBytes = cacheBytes,
                    appBytes = appBytes,
                    isPriorityMediaApp = isPriority
                )
            )
        }

        entries.sortedWith(
            compareByDescending<AppCacheEntry> { it.isPriorityMediaApp }
                .thenByDescending { it.cacheBytes }
                .thenBy { it.appName.lowercase() }
        )
    }

    fun measureOwnAppCacheBytes(): Long {
        var total = 0L
        context.cacheDir?.let { total += dirSize(it) }
        context.codeCacheDir?.let { total += dirSize(it) }
        context.externalCacheDir?.let { total += dirSize(it) }
        return total
    }

    fun measureResidualTempBytes(): Long {
        var total = 0L
        context.filesDir?.listFiles()?.forEach { file ->
            if (file.name.endsWith(".tmp") || file.name.endsWith(".log") || file.name.startsWith(".")) {
                total += if (file.isDirectory) dirSize(file) else file.length()
            }
        }
        return total
    }

    suspend fun clearOwnAppCache(): Long = withContext(Dispatchers.IO) {
        val before = measureOwnAppCacheBytes() + measureResidualTempBytes()
        context.cacheDir?.let { deleteDirectoryContents(it) }
        context.codeCacheDir?.let { deleteDirectoryContents(it) }
        context.externalCacheDir?.let { deleteDirectoryContents(it) }
        context.filesDir?.listFiles()?.forEach { file ->
            if (file.name.endsWith(".tmp") || file.name.endsWith(".log")) {
                runCatching { file.deleteRecursively() }
            }
        }
        val after = measureOwnAppCacheBytes() + measureResidualTempBytes()
        (before - after).coerceAtLeast(0L)
    }

    suspend fun loadRamTelemetry(): RamTelemetry = withContext(Dispatchers.IO) {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val total = memInfo.totalMem.coerceAtLeast(1L)
        val avail = memInfo.availMem.coerceIn(0L, total)
        val used = (total - avail).coerceAtLeast(0L)
        val usedPct = ((used.toDouble() / total.toDouble()) * 100.0).toInt().coerceIn(0, 100)

        val runningSet = runCatching {
            activityManager.runningAppProcesses?.map { it.processName }?.toSet().orEmpty()
        }.getOrDefault(emptySet())

        val candidates = queryLauncherPackages()
            .filter { it.packageName != context.packageName }
            .filter { (it.flags and ApplicationInfo.FLAG_PERSISTENT) == 0 }
            .map { appInfo ->
                val pkg = appInfo.packageName
                val isActive = pkg in runningSet
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val importance = when {
                    isActive -> "Active Background"
                    !isSystem -> "Cached Process"
                    else -> "System Service"
                }
                RamProcessEntry(
                    packageName = pkg,
                    appName = runCatching { pm.getApplicationLabel(appInfo).toString() }.getOrDefault(pkg),
                    icon = loadIconBitmap(pkg, appInfo),
                    importanceLabel = importance,
                    isSelected = true,
                    isTerminated = false
                )
            }
            .sortedWith(
                compareByDescending<RamProcessEntry> { it.importanceLabel == "Active Background" }
                    .thenByDescending { it.importanceLabel == "Cached Process" }
                    .thenBy { it.appName.lowercase() }
            )
            .take(20)

        RamTelemetry(
            totalBytes = total,
            availableBytes = avail,
            usedBytes = used,
            usedPercent = usedPct,
            isLowMemory = memInfo.lowMemory,
            backgroundApps = candidates
        )
    }

    suspend fun optimizeRam(packagesToKill: List<String>): RamTelemetry = withContext(Dispatchers.IO) {
        val beforeInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(beforeInfo)

        var killed = 0
        for (pkg in packagesToKill) {
            if (pkg != context.packageName) {
                runCatching {
                    activityManager.killBackgroundProcesses(pkg)
                    killed++
                }
            }
        }
        clearOwnAppCache()
        System.gc()

        val updated = loadRamTelemetry()
        val freed = (updated.availableBytes - beforeInfo.availMem).coerceAtLeast(0L)
        val updatedApps = updated.backgroundApps.map { entry ->
            if (entry.packageName in packagesToKill) entry.copy(isTerminated = true) else entry
        }
        updated.copy(
            backgroundApps = updatedApps,
            lastFreedBytes = freed,
            lastKilledCount = killed
        )
    }

    suspend fun loadStorageTelemetry(totalAppCacheBytes: Long = 0L): StorageTelemetry = withContext(Dispatchers.IO) {
        val (totalBytes, freeBytes) = queryTotalAndFreeStorage()
        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
        val usedPct = if (totalBytes > 0L) {
            ((usedBytes.toDouble() / totalBytes.toDouble()) * 100.0).toInt().coerceIn(0, 100)
        } else 0

        StorageTelemetry(
            totalBytes = totalBytes,
            freeBytes = freeBytes,
            usedBytes = usedBytes,
            usedPercent = usedPct,
            cleanableOwnCacheBytes = measureOwnAppCacheBytes(),
            residualTempBytes = measureResidualTempBytes(),
            totalAppCacheBytes = totalAppCacheBytes
        )
    }

    suspend fun executeStorageBoost(totalAppCacheBytes: Long): StorageTelemetry = withContext(Dispatchers.IO) {
        val freedOwn = clearOwnAppCache()
        val (totalBytes, freeBytes) = queryTotalAndFreeStorage()
        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
        val usedPct = if (totalBytes > 0L) {
            ((usedBytes.toDouble() / totalBytes.toDouble()) * 100.0).toInt().coerceIn(0, 100)
        } else 0

        StorageTelemetry(
            totalBytes = totalBytes,
            freeBytes = freeBytes,
            usedBytes = usedBytes,
            usedPercent = usedPct,
            cleanableOwnCacheBytes = measureOwnAppCacheBytes(),
            residualTempBytes = measureResidualTempBytes(),
            totalAppCacheBytes = totalAppCacheBytes,
            lastBoostFreedBytes = freedOwn
        )
    }

    suspend fun runDeepVirusHunt(onProgress: suspend (String, String, Int, Int) -> Unit): SecurityPosture =
        withContext(Dispatchers.IO) {
            val combinedPackages = LinkedHashMap<String, ApplicationInfo>()
            for (ai in queryLauncherPackages()) {
                combinedPackages[ai.packageName] = ai
            }
            val installedApps = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledApplications(PackageManager.GET_META_DATA)
                }
            }.getOrDefault(emptyList())
            for (ai in installedApps) {
                combinedPackages.putIfAbsent(ai.packageName, ai)
            }

            val allPackages = combinedPackages.values.toList()
            val total = allPackages.size.coerceAtLeast(1)
            val findings = mutableListOf<VirusScanFinding>()
            var hiddenCount = 0

            allPackages.forEachIndexed { index, appInfo ->
                val pkg = appInfo.packageName
                val appName = runCatching { pm.getApplicationLabel(appInfo).toString() }.getOrDefault(pkg)
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                val hasLauncher = pm.getLaunchIntentForPackage(pkg) != null
                val isHidden = !isSystemApp && !isUpdatedSystem && !hasLauncher && pkg != context.packageName
                if (isHidden) hiddenCount++

                val layerName = when {
                    isHidden -> "Hidden Background Layer"
                    !isSystemApp -> "Third-Party Package Layer"
                    else -> "System Integrity Layer"
                }
                onProgress(appName, layerName, index + 1, total)

                val reasons = mutableListOf<String>()
                val installerPkg = getInstallerPackage(pkg)
                val installerLabel = when {
                    isSystemApp || isUpdatedSystem -> "System Preload"
                    installerPkg == "com.android.vending" -> "Google Play"
                    installerPkg != null && installerPkg in trustedInstallers -> installerPkg
                    installerPkg == null -> "Unknown / Sideloaded"
                    else -> installerPkg
                }

                if (!isSystemApp && !isUpdatedSystem && pkg != context.packageName) {
                    if (installerPkg == null || installerPkg !in trustedInstallers) {
                        reasons.add("Sideloaded / Unverified source ($installerLabel)")
                    }
                    if (isHidden) {
                        reasons.add("Hidden package with no launcher icon")
                    }
                    if ((appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                        reasons.add("Debuggable flag active on installed app")
                    }
                }

                val pkgDetails = getDetailedPackageInfo(pkg)
                val requestedPerms = pkgDetails?.requestedPermissions?.toSet().orEmpty()
                val highRiskPerms = mutableListOf<String>()
                if ("android.permission.REQUEST_INSTALL_PACKAGES" in requestedPerms) highRiskPerms.add("APK Dropper")
                if ("android.permission.SYSTEM_ALERT_WINDOW" in requestedPerms) highRiskPerms.add("Screen Overlay")
                if ("android.permission.READ_SMS" in requestedPerms || "android.permission.SEND_SMS" in requestedPerms) {
                    highRiskPerms.add("SMS Hijack Risk")
                }
                if ("android.permission.BIND_ACCESSIBILITY_SERVICE" in requestedPerms) highRiskPerms.add("Accessibility Hook")
                if ("android.permission.RECEIVE_BOOT_COMPLETED" in requestedPerms && isHidden) {
                    highRiskPerms.add("Auto-Boot Hidden Service")
                }

                if (!isSystemApp && highRiskPerms.size >= 2) {
                    reasons.add("Risk cluster: ${highRiskPerms.joinToString(", ")}")
                }

                val serviceCount = pkgDetails?.services?.size ?: 0
                if (isHidden && serviceCount > 0) {
                    reasons.add("$serviceCount stealth background service(s) detected")
                }

                val riskLevel = when {
                    reasons.size >= 2 -> RiskLevel.CRITICAL
                    reasons.size == 1 -> RiskLevel.SUSPICIOUS
                    else -> RiskLevel.SAFE
                }

                findings.add(
                    VirusScanFinding(
                        packageName = pkg,
                        appName = appName,
                        icon = loadIconBitmap(pkg, appInfo),
                        riskLevel = riskLevel,
                        installerLabel = installerLabel,
                        isHiddenPackage = isHidden,
                        riskReasons = reasons
                    )
                )
            }

            // Purge any suspicious local temp artifacts
            val purgedCount = purgeSuspiciousLocalArtifacts()

            val sortedFindings = findings.sortedWith(
                compareByDescending<VirusScanFinding> { it.riskLevel.ordinal }
                    .thenByDescending { it.isHiddenPackage }
                    .thenBy { it.appName.lowercase() }
            )
            val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Build.VERSION.SECURITY_PATCH
            } else {
                "Legacy"
            }

            SecurityPosture(
                securityPatch = securityPatch,
                isDeviceLockSecure = keyguard.isDeviceSecure,
                scannedCount = sortedFindings.size,
                hiddenCount = hiddenCount,
                suspiciousCount = sortedFindings.count { it.riskLevel == RiskLevel.SUSPICIOUS },
                criticalCount = sortedFindings.count { it.riskLevel == RiskLevel.CRITICAL },
                localArtifactsPurged = purgedCount,
                findings = sortedFindings,
                lastScanCompleted = true
            )
        }

    suspend fun neutralizeLocalThreats(flaggedPackages: List<String>): Int = withContext(Dispatchers.IO) {
        var stopped = 0
        for (pkg in flaggedPackages) {
            if (pkg != context.packageName) {
                runCatching {
                    activityManager.killBackgroundProcesses(pkg)
                    stopped++
                }
            }
        }
        clearOwnAppCache()
        purgeSuspiciousLocalArtifacts()
        stopped
    }

    fun observeBatteryTelemetry(): Flow<BatteryTelemetry> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent != null) {
                    trySend(parseBatteryIntent(intent))
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialSticky = context.registerReceiver(receiver, filter)
        if (initialSticky != null) {
            trySend(parseBatteryIntent(initialSticky))
        }
        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    private fun parseBatteryIntent(intent: Intent): BatteryTelemetry {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
        val percent = if (level >= 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else 100

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val plugSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast Charge"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Power"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Pad"
            else -> if (isCharging) "Charging" else "Battery Power"
        }

        val healthCode = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        val (healthLabel, isHealthy) = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Optimal" to true
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat Alert" to false
            BatteryManager.BATTERY_HEALTH_DEAD -> "Cell Degraded" to false
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage" to false
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold Temp" to false
            else -> "Nominal" to true
        }

        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280)
        val tempC = tempTenths / 10f
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4100)
        val voltageV = if (voltageMv > 100) voltageMv / 1000f else voltageMv.toFloat()
        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.ifBlank { "Li-ion" } ?: "Li-ion"

        val currentRaw = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentMa = if (currentRaw != Int.MIN_VALUE && currentRaw != 0) {
            val absVal = kotlin.math.abs(currentRaw)
            if (absVal > 10_000) currentRaw / 1000 else currentRaw
        } else null

        val runningCount = runCatching {
            activityManager.runningAppProcesses?.count { it.processName != context.packageName } ?: 0
        }.getOrDefault(0)

        var score = 100
        if (!isHealthy) score -= 28
        if (tempC >= 39.5f) score -= 18 else if (tempC >= 36.0f) score -= 6
        score -= (runningCount.coerceAtMost(8) * 1)
        score = score.coerceIn(35, 100)

        return BatteryTelemetry(
            levelPercent = percent,
            isCharging = isCharging,
            plugSource = plugSource,
            healthLabel = healthLabel,
            isHealthy = isHealthy,
            healthScore = score,
            temperatureCelsius = tempC,
            voltageVolts = voltageV,
            technology = tech,
            currentMilliamps = currentMa,
            thermalWarning = tempC >= 39.5f || !isHealthy,
            drainingAppsCount = runningCount
        )
    }

    private fun purgeSuspiciousLocalArtifacts(): Int {
        var count = 0
        listOf(context.cacheDir, context.externalCacheDir, context.filesDir).forEach { dir ->
            dir?.listFiles()?.forEach { f ->
                if (f.name.endsWith(".apk") || f.name.endsWith(".tmp") || f.name.endsWith(".dex")) {
                    if (runCatching { f.deleteRecursively() }.getOrDefault(false)) count++
                }
            }
        }
        return count
    }

    private fun queryTotalAndFreeStorage(): Pair<Long, Long> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val stats = runCatching {
                val ssm = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                ssm.getTotalBytes(StorageManager.UUID_DEFAULT) to ssm.getFreeBytes(StorageManager.UUID_DEFAULT)
            }.getOrNull()
            if (stats != null && stats.first > 0L) return stats
        }
        val statFs = StatFs(Environment.getDataDirectory().absolutePath)
        return statFs.totalBytes to statFs.availableBytes
    }

    private fun queryLauncherPackages(): List<ApplicationInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }

        val map = LinkedHashMap<String, ApplicationInfo>()
        for (ri in resolveInfos) {
            val ai = ri.activityInfo?.applicationInfo ?: continue
            map.putIfAbsent(ai.packageName, ai)
        }

        for (pkg in priorityMediaPackages) {
            if (!map.containsKey(pkg)) {
                runCatching {
                    val ai = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0L))
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getApplicationInfo(pkg, 0)
                    }
                    map[pkg] = ai
                }
            }
        }
        return map.values.toList()
    }

    private fun getInstallerPackage(packageName: String): String? {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }
        }.getOrNull()
    }

    private fun getDetailedPackageInfo(packageName: String): PackageInfo? {
        val flags = PackageManager.GET_PERMISSIONS or PackageManager.GET_SERVICES
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(flags.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, flags)
            }
        }.getOrNull()
    }

    private fun loadIconBitmap(pkg: String, appInfo: ApplicationInfo): ImageBitmap? {
        iconCache[pkg]?.let { return it }
        return runCatching {
            val drawable = pm.getApplicationIcon(appInfo)
            val bmp = drawableToBitmap(drawable, 96)
            bmp.asImageBitmap().also { iconCache[pkg] = it }
        }.getOrNull()
    }

    private fun drawableToBitmap(drawable: Drawable, sizePx: Int): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return Bitmap.createScaledBitmap(drawable.bitmap, sizePx, sizePx, true)
        }
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun dirSize(dir: File): Long {
        if (!dir.exists()) return 0L
        var size = 0L
        dir.listFiles()?.forEach { child ->
            size += if (child.isDirectory) dirSize(child) else child.length()
        }
        return size
    }

    private fun deleteDirectoryContents(dir: File) {
        if (!dir.exists()) return
        dir.listFiles()?.forEach { child ->
            if (child.isDirectory) {
                deleteDirectoryContents(child)
            }
            runCatching { child.delete() }
        }
    }
}
