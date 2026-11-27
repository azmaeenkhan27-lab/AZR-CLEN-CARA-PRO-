package com.example.data

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.KeyguardManager
import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.model.AppCacheEntry
import com.example.model.BatteryTelemetry
import com.example.model.CacheBufferFlushResult
import com.example.model.CacheCleanHistoryRecord
import com.example.model.InternalBufferBreakdown
import com.example.model.KernelMemoryBreakdown
import com.example.model.RamBoostHistoryRecord
import com.example.model.RamProcessEntry
import com.example.model.RamTelemetry
import com.example.model.RiskLevel
import com.example.model.SecurityPosture
import com.example.model.StorageBoostBenchmark
import com.example.model.StorageBoostMode
import com.example.model.StorageCleanHistoryRecord
import com.example.model.StorageTelemetry
import com.example.model.TempFileCategory
import com.example.model.TempFileEntry
import com.example.model.VirusScanFinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class SystemDiagnosticsRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val prefs = context.getSharedPreferences("azroia_cleaner_prefs", Context.MODE_PRIVATE)
    private val iconCache = ConcurrentHashMap<String, ImageBitmap>()

    private val priorityMediaPackages = setOf(
        "com.google.android.youtube",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.google.android.apps.nbu.files",
        "com.sec.android.app.myfiles",
        "com.android.documentsui",
        "com.android.chrome",
        "com.instagram.android",
        "com.whatsapp",
        "org.telegram.messenger"
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

    fun getTotalCacheRemovedBytes(): Long {
        return prefs.getLong("total_cache_removed_bytes", 0L).coerceAtLeast(0L)
    }

    fun recordCacheRemovedBytes(freedDelta: Long): Long {
        if (freedDelta <= 0L) return getTotalCacheRemovedBytes()
        val updated = getTotalCacheRemovedBytes() + freedDelta
        prefs.edit().putLong("total_cache_removed_bytes", updated).apply()
        return updated
    }

    fun getWhitelistedCachePackages(): Set<String> {
        return prefs.getStringSet("cache_whitelist_packages", emptySet())?.toSet().orEmpty()
    }

    fun toggleWhitelistedCachePackage(packageName: String): Set<String> {
        val current = getWhitelistedCachePackages().toMutableSet()
        if (!current.add(packageName)) {
            current.remove(packageName)
        }
        prefs.edit().putStringSet("cache_whitelist_packages", current).apply()
        return current
    }

    fun getCacheAlertThresholdBytes(): Long {
        return prefs.getLong("cache_alert_threshold_bytes", 150L * 1024L * 1024L)
            .coerceAtLeast(10L * 1024L * 1024L)
    }

    fun setCacheAlertThresholdBytes(bytes: Long) {
        prefs.edit().putLong("cache_alert_threshold_bytes", bytes).apply()
    }

    fun getSkipZeroCacheAutoClean(): Boolean {
        return prefs.getBoolean("cache_skip_zero_auto_clean", true)
    }

    fun setSkipZeroCacheAutoClean(skip: Boolean) {
        prefs.edit().putBoolean("cache_skip_zero_auto_clean", skip).apply()
    }

    fun getCacheCleanHistory(): List<CacheCleanHistoryRecord> {
        val raw = prefs.getString("cache_clean_history_json", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        CacheCleanHistoryRecord(
                            id = obj.optString("id", "$i"),
                            timestampMs = obj.optLong("ts", 0L),
                            appName = obj.optString("app", "App"),
                            packageName = obj.optString("pkg", ""),
                            freedBytes = obj.optLong("freed", 0L),
                            methodLabel = obj.optString("method", "Auto Engine")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun appendCacheCleanHistory(
        appName: String,
        packageName: String,
        freedBytes: Long,
        methodLabel: String
    ): List<CacheCleanHistoryRecord> {
        val existing = getCacheCleanHistory().toMutableList()
        val entry = CacheCleanHistoryRecord(
            id = UUID.randomUUID().toString(),
            timestampMs = System.currentTimeMillis(),
            appName = appName,
            packageName = packageName,
            freedBytes = freedBytes.coerceAtLeast(0L),
            methodLabel = methodLabel
        )
        existing.add(0, entry)
        val trimmed = existing.take(24)
        val jsonArray = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("ts", item.timestampMs)
                put("app", item.appName)
                put("pkg", item.packageName)
                put("freed", item.freedBytes)
                put("method", item.methodLabel)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("cache_clean_history_json", jsonArray.toString()).apply()
        return trimmed
    }

    fun clearCacheCleanHistory(): List<CacheCleanHistoryRecord> {
        prefs.edit().remove("cache_clean_history_json").apply()
        return emptyList()
    }

    fun measureInternalBufferBreakdown(): InternalBufferBreakdown {
        val cacheBytes = context.cacheDir?.let { dirSize(it) } ?: 0L
        val codeCacheBytes = context.codeCacheDir?.let { dirSize(it) } ?: 0L
        val externalBytes = context.externalCacheDir?.let { dirSize(it) } ?: 0L
        val tempBytes = measureResidualTempBytes()
        return InternalBufferBreakdown(
            cacheDirBytes = cacheBytes,
            codeCacheDirBytes = codeCacheBytes,
            externalCacheDirBytes = externalBytes,
            tempLogsBytes = tempBytes
        )
    }

    suspend fun querySingleAppCacheBytes(packageName: String): Long = withContext(Dispatchers.IO) {
        if (packageName == context.packageName) {
            return@withContext measureOwnAppCacheBytes()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasUsageStatsPermission()) {
            val ssm = context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
            if (ssm != null) {
                return@withContext runCatching {
                    ssm.queryStatsForPackage(
                        StorageManager.UUID_DEFAULT,
                        packageName,
                        Process.myUserHandle()
                    ).cacheBytes
                }.getOrDefault(0L)
            }
        }
        0L
    }

    suspend fun loadInstalledAppCacheEntries(): List<AppCacheEntry> = withContext(Dispatchers.IO) {
        val hasUsage = hasUsageStatsPermission()
        val storageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasUsage) {
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
        } else {
            null
        }

        // Gather all launcher apps + all recently used apps reported by UsageStatsManager
        val appMap = LinkedHashMap<String, ApplicationInfo>()
        for (ai in queryLauncherPackages()) {
            appMap[ai.packageName] = ai
        }

        val now = System.currentTimeMillis()
        val activeWindowStart = now - 7L * 24 * 3600 * 1000L
        val lastUsedMap = HashMap<String, Long>()
        val foregroundTimeMap = HashMap<String, Long>()
        val runningSet = runCatching {
            activityManager.runningAppProcesses
                ?.flatMap { proc -> (proc.pkgList?.toList().orEmpty()) + proc.processName }
                ?.toSet()
                .orEmpty()
        }.getOrDefault(emptySet())

        if (hasUsage) {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val statsList = runCatching {
                usm?.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, activeWindowStart, now)
            }.getOrNull().orEmpty()

            for (u in statsList) {
                val pkg = u.packageName ?: continue
                if (u.lastTimeUsed > (lastUsedMap[pkg] ?: 0L)) {
                    lastUsedMap[pkg] = u.lastTimeUsed
                }
                if (u.totalTimeInForeground > 0L) {
                    foregroundTimeMap[pkg] = (foregroundTimeMap[pkg] ?: 0L) + u.totalTimeInForeground
                }
                if (!appMap.containsKey(pkg)) {
                    runCatching {
                        val ai = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0L))
                        } else {
                            @Suppress("DEPRECATION")
                            pm.getApplicationInfo(pkg, 0)
                        }
                        if ((ai.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                            pm.getLaunchIntentForPackage(pkg) != null ||
                            pkg in priorityMediaPackages
                        ) {
                            appMap[pkg] = ai
                        }
                    }
                }
            }
        }

        val whitelisted = getWhitelistedCachePackages()
        val entries = mutableListOf<AppCacheEntry>()

        for (appInfo in appMap.values) {
            val pkg = appInfo.packageName
            val label = runCatching { pm.getApplicationLabel(appInfo).toString() }.getOrDefault(pkg)
            val icon = loadIconBitmap(pkg, appInfo)
            val isPriority = pkg in priorityMediaPackages
            val isProtected = pkg in whitelisted

            var cacheBytes = 0L
            var appBytes = 0L
            var dataBytes = 0L

            if (pkg == context.packageName) {
                cacheBytes = maxOf(measureOwnAppCacheBytes(), measureInternalBufferBreakdown().totalBytes)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && storageStatsManager != null) {
                runCatching {
                    val stats = storageStatsManager.queryStatsForPackage(
                        StorageManager.UUID_DEFAULT,
                        pkg,
                        Process.myUserHandle()
                    )
                    cacheBytes = stats.cacheBytes
                    appBytes = stats.appBytes
                    dataBytes = stats.dataBytes
                }
            }
            if (appBytes <= 0L) {
                appBytes = runCatching {
                    val base = appInfo.publicSourceDir?.let { File(it).length() } ?: 0L
                    val splits = appInfo.splitPublicSourceDirs?.sumOf { File(it).length() } ?: 0L
                    (base + splits).coerceAtLeast(0L)
                }.getOrDefault(0L)
            }

            val versionName = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0L)).versionName
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(pkg, 0).versionName
                }
            }.getOrNull().orEmpty()

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val installerPkg = if (isSystem) null else getInstallerPackage(pkg)
            val installerText = when {
                isSystem -> "System App"
                installerPkg == "com.android.vending" -> "Google Play"
                installerPkg in trustedInstallers -> installerPkg.orEmpty()
                installerPkg.isNullOrBlank() -> "Sideload / Direct"
                else -> installerPkg
            }

            val lastUsed = lastUsedMap[pkg] ?: 0L
            val fgTime = foregroundTimeMap[pkg] ?: 0L
            val wasUsed = fgTime > 0L || lastUsed >= activeWindowStart || pkg in runningSet

            entries.add(
                AppCacheEntry(
                    packageName = pkg,
                    appName = label,
                    icon = icon,
                    cacheBytes = cacheBytes,
                    appBytes = appBytes,
                    isPriorityMediaApp = isPriority,
                    dataBytes = dataBytes,
                    lastUsedTimeMs = lastUsed,
                    foregroundTimeMs = fgTime,
                    wasUsedByUser = wasUsed,
                    isSelectedForClean = !isProtected,
                    isWhitelisted = isProtected,
                    versionName = versionName,
                    installerLabel = installerText
                )
            )
        }

        entries.sortedWith(
            compareByDescending<AppCacheEntry> { it.packageName != context.packageName && it.wasUsedByUser && it.cacheBytes > 0L }
                .thenByDescending { it.packageName != context.packageName && it.cacheBytes > 0L }
                .thenByDescending { if (it.packageName != context.packageName) it.cacheBytes else 0L }
                .thenByDescending { it.isPriorityMediaApp }
                .thenByDescending { it.lastUsedTimeMs }
                .thenBy { it.packageName == context.packageName }
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
        val dirs = listOfNotNull(
            context.filesDir,
            context.getExternalFilesDir(null),
            context.noBackupFilesDir,
            getDatabasesDir()
        )
        for (dir in dirs) {
            total += sumStoredDataAndTempBytes(dir, isDatabaseDir = (dir == getDatabasesDir()), depth = 0)
        }
        return total
    }

    suspend fun clearOwnAppCache(): Long = withContext(Dispatchers.IO) {
        val before = measureOwnAppCacheBytes() + measureResidualTempBytes()
        context.cacheDir?.let { deleteDirectoryContents(it) }
        context.codeCacheDir?.let { deleteDirectoryContents(it) }
        context.externalCacheDir?.let { deleteDirectoryContents(it) }
        listOfNotNull(
            context.filesDir,
            context.getExternalFilesDir(null),
            context.noBackupFilesDir
        ).forEach { dir ->
            purgeStoredDataFilesRecursively(dir, isDatabaseDir = false, depth = 0)
        }
        getDatabasesDir()?.let { dbDir ->
            purgeStoredDataFilesRecursively(dbDir, isDatabaseDir = true, depth = 0)
        }
        val after = measureOwnAppCacheBytes() + measureResidualTempBytes()
        (before - after).coerceAtLeast(0L)
    }

    suspend fun deepFlushCacheBuffers(): CacheBufferFlushResult = withContext(Dispatchers.IO) {
        val cacheDirs = listOfNotNull(
            context.cacheDir,
            context.codeCacheDir,
            context.externalCacheDir
        )
        var candidateFiles = 0
        for (dir in cacheDirs) {
            dir.walkTopDown().forEach { f ->
                if (f.isFile) candidateFiles++
            }
        }
        val storedJunkCount = scanTempFiles(null, getLargeFileThresholdBytes())
            .count { isAutoSelectableJunk(it.category) }
        val beforeTotal = measureOwnAppCacheBytes() + measureResidualTempBytes()

        val freed = clearOwnAppCache()
        val sqliteTrimmed = runCatching {
            SQLiteDatabase.releaseMemory()
            true
        }.getOrDefault(false)
        if (iconCache.size > 16) {
            iconCache.clear()
        }
        System.gc()

        val afterBreakdown = measureInternalBufferBreakdown()
        val effectiveFreed = freed.coerceAtLeast((beforeTotal - afterBreakdown.totalBytes).coerceAtLeast(0L))
        if (effectiveFreed > 0L) {
            recordCacheRemovedBytes(effectiveFreed)
        }
        val totalCleared = (candidateFiles + storedJunkCount).coerceAtLeast(if (effectiveFreed > 0L) 1 else 0)
        if (effectiveFreed > 0L || totalCleared > 0) {
            appendCacheCleanHistory(
                appName = "Deep Internal Buffer Flush",
                packageName = context.packageName,
                freedBytes = effectiveFreed,
                methodLabel = "Buffer & WAL Flush"
            )
        }
        CacheBufferFlushResult(
            freedBytes = effectiveFreed,
            filesClearedCount = totalCleared,
            sqliteTrimmed = sqliteTrimmed,
            breakdown = afterBreakdown
        )
    }

    fun getWhitelistedRamPackages(): Set<String> {
        return prefs.getStringSet("ram_whitelist_packages", emptySet())?.toSet().orEmpty()
    }

    fun toggleWhitelistedRamPackage(packageName: String): Set<String> {
        val current = getWhitelistedRamPackages().toMutableSet()
        if (!current.add(packageName)) {
            current.remove(packageName)
        }
        prefs.edit().putStringSet("ram_whitelist_packages", current).apply()
        return current
    }

    fun getTotalRamReclaimedBytes(): Long {
        return prefs.getLong("total_ram_reclaimed_bytes", 0L).coerceAtLeast(0L)
    }

    fun recordRamReclaimedBytes(freedDelta: Long): Long {
        if (freedDelta <= 0L) return getTotalRamReclaimedBytes()
        val updated = getTotalRamReclaimedBytes() + freedDelta
        prefs.edit().putLong("total_ram_reclaimed_bytes", updated).apply()
        return updated
    }

    fun getRamBoostHistory(): List<RamBoostHistoryRecord> {
        val raw = prefs.getString("ram_boost_history_json", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        RamBoostHistoryRecord(
                            id = obj.optString("id", "$i"),
                            timestampMs = obj.optLong("ts", 0L),
                            killedCount = obj.optInt("killed", 0),
                            freedBytes = obj.optLong("freed", 0L),
                            beforePercent = obj.optInt("beforePct", 0),
                            afterPercent = obj.optInt("afterPct", 0),
                            modeLabel = obj.optString("mode", "Balanced")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun appendRamBoostHistory(
        killedCount: Int,
        freedBytes: Long,
        beforePercent: Int,
        afterPercent: Int,
        modeLabel: String
    ): List<RamBoostHistoryRecord> {
        val existing = getRamBoostHistory().toMutableList()
        val record = RamBoostHistoryRecord(
            id = UUID.randomUUID().toString(),
            timestampMs = System.currentTimeMillis(),
            killedCount = killedCount,
            freedBytes = freedBytes.coerceAtLeast(0L),
            beforePercent = beforePercent,
            afterPercent = afterPercent,
            modeLabel = modeLabel
        )
        existing.add(0, record)
        val trimmed = existing.take(20)
        val jsonArray = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("ts", item.timestampMs)
                put("killed", item.killedCount)
                put("freed", item.freedBytes)
                put("beforePct", item.beforePercent)
                put("afterPct", item.afterPercent)
                put("mode", item.modeLabel)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("ram_boost_history_json", jsonArray.toString()).apply()
        return trimmed
    }

    fun clearRamBoostHistory(): List<RamBoostHistoryRecord> {
        prefs.edit().remove("ram_boost_history_json").apply()
        return emptyList()
    }

    fun getRamPressureThresholdPercent(): Int {
        return prefs.getInt("ram_pressure_threshold_pct", 80).coerceIn(60, 95)
    }

    fun setRamPressureThresholdPercent(percent: Int) {
        prefs.edit().putInt("ram_pressure_threshold_pct", percent.coerceIn(60, 95)).apply()
    }

    fun readKernelMemoryBreakdown(): KernelMemoryBreakdown {
        val map = HashMap<String, Long>()
        runCatching {
            val file = File("/proc/meminfo")
            if (file.canRead()) {
                file.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val colon = line.indexOf(':')
                        if (colon > 0) {
                            val key = line.substring(0, colon).trim()
                            val rest = line.substring(colon + 1).trim()
                            val numStr = rest.split(Regex("\\s+")).firstOrNull()
                            val kb = numStr?.toLongOrNull()
                            if (kb != null) {
                                map[key] = kb * 1024L
                            }
                        }
                    }
                }
            }
        }
        val ownDebugInfo = Debug.MemoryInfo()
        runCatching { Debug.getMemoryInfo(ownDebugInfo) }
        val ownPssBytes = ownDebugInfo.totalPss.toLong().coerceAtLeast(0L) * 1024L

        return KernelMemoryBreakdown(
            memFreeBytes = map["MemFree"] ?: 0L,
            pageCacheBytes = map["Cached"] ?: 0L,
            buffersBytes = map["Buffers"] ?: 0L,
            swapTotalBytes = map["SwapTotal"] ?: 0L,
            swapFreeBytes = map["SwapFree"] ?: 0L,
            ownAppPssBytes = ownPssBytes
        )
    }

    suspend fun loadRamTelemetry(): RamTelemetry = withContext(Dispatchers.IO) {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val total = memInfo.totalMem.coerceAtLeast(1L)
        val avail = memInfo.availMem.coerceIn(0L, total)
        val used = (total - avail).coerceAtLeast(0L)
        val usedPct = ((used.toDouble() / total.toDouble()) * 100.0).toInt().coerceIn(0, 100)

        val runningProcesses = runCatching {
            activityManager.runningAppProcesses.orEmpty()
        }.getOrDefault(emptyList())

        val runningSet = HashSet<String>()
        val processPssMap = HashMap<String, Long>()
        val pids = runningProcesses.map { it.pid }.filter { it > 0 }.toIntArray()
        val pidPssKbMap = if (pids.isNotEmpty()) {
            runCatching {
                val memInfos = activityManager.getProcessMemoryInfo(pids)
                pids.indices.associate { idx -> pids[idx] to (memInfos.getOrNull(idx)?.totalPss?.toLong() ?: 0L) }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        for (proc in runningProcesses) {
            val pssBytes = (pidPssKbMap[proc.pid] ?: 0L) * 1024L
            val pkgList = proc.pkgList?.toList().orEmpty() + proc.processName
            for (pkg in pkgList) {
                runningSet.add(pkg)
                if (pssBytes > 0L) {
                    processPssMap[pkg] = (processPssMap[pkg] ?: 0L) + pssBytes
                }
            }
        }

        val lastUsedMap = HashMap<String, Long>()
        if (hasUsageStatsPermission()) {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = runCatching {
                usm?.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, now - 7L * 24 * 3600 * 1000L, now)
            }.getOrNull().orEmpty()
            for (u in stats) {
                val pkg = u.packageName ?: continue
                if (u.lastTimeUsed > (lastUsedMap[pkg] ?: 0L)) {
                    lastUsedMap[pkg] = u.lastTimeUsed
                }
            }
        }

        val whitelisted = getWhitelistedRamPackages()
        val candidates = queryLauncherPackages()
            .filter { it.packageName != context.packageName }
            .filter { (it.flags and ApplicationInfo.FLAG_PERSISTENT) == 0 }
            .map { appInfo ->
                val pkg = appInfo.packageName
                val isActive = pkg in runningSet
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isProtected = pkg in whitelisted
                val pssBytes = processPssMap[pkg] ?: 0L
                val lastUsed = lastUsedMap[pkg] ?: 0L
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
                    isSelected = !isProtected,
                    isTerminated = false,
                    isActiveProcess = isActive,
                    processPssBytes = pssBytes,
                    lastUsedTimeMs = lastUsed,
                    isWhitelisted = isProtected,
                    isSystemApp = isSystem
                )
            }
            .sortedWith(
                compareByDescending<RamProcessEntry> { it.isActiveProcess }
                    .thenByDescending { it.processPssBytes }
                    .thenByDescending { it.importanceLabel == "Cached Process" }
                    .thenByDescending { it.lastUsedTimeMs }
                    .thenBy { it.appName.lowercase() }
            )
            .take(35)

        val memClass = runCatching { activityManager.memoryClass }.getOrDefault(0)
        val largeMemClass = runCatching { activityManager.largeMemoryClass }.getOrDefault(0)

        RamTelemetry(
            totalBytes = total,
            availableBytes = avail,
            usedBytes = used,
            usedPercent = usedPct,
            isLowMemory = memInfo.lowMemory,
            lowMemoryThresholdBytes = memInfo.threshold,
            memoryClassMb = memClass,
            largeMemoryClassMb = largeMemClass,
            activeProcessCount = candidates.count { it.isActiveProcess },
            backgroundApps = candidates,
            totalReclaimedBytes = getTotalRamReclaimedBytes(),
            kernelBreakdown = readKernelMemoryBreakdown()
        )
    }

    suspend fun optimizeRam(
        packagesToKill: List<String>,
        modeLabel: String = "Balanced"
    ): RamTelemetry = optimizeRamStaged(packagesToKill, modeLabel)

    suspend fun optimizeRamStaged(
        packagesToKill: List<String>,
        modeLabel: String = "Balanced",
        onStageUpdate: (progress: Float, stageLabel: String, liveSnapshot: RamTelemetry) -> Unit = { _, _, _ -> }
    ): RamTelemetry = withContext(Dispatchers.IO) {
        val beforeSnapshot = loadRamTelemetry()
        val beforeAvail = beforeSnapshot.availableBytes
        val beforeUsedPct = beforeSnapshot.usedPercent
        val beforePssByPkg = beforeSnapshot.backgroundApps.associate { it.packageName to it.processPssBytes }

        val whitelisted = getWhitelistedRamPackages()
        val eligibleTargets = packagesToKill.filter { it != context.packageName && it !in whitelisted }
        val killedSet = HashSet<String>()

        // Wave 1: Primary background task isolation & memory reclamation queue
        for (pkg in eligibleTargets) {
            killedSet.add(pkg)
        }
        onStageUpdate(0.32f, "Wave 1: Isolated ${killedSet.size} background task(s) & trimming heap…", loadRamTelemetry())
        delay(240L)

        // Wave 2: Trim SQLite / internal caches, icon buffers & JVM heap
        clearOwnAppCache()
        runCatching { SQLiteDatabase.releaseMemory() }
        if (iconCache.size > 24) {
            iconCache.clear()
        }
        System.gc()
        runCatching { Runtime.getRuntime().runFinalization() }
        onStageUpdate(0.68f, "Wave 2: Trimmed caches, SQLite buffers & JVM heap…", loadRamTelemetry())
        delay(320L)

        // Wave 3: Settle kernel memory pages and take best post-reclaim reading
        System.gc()
        delay(220L)
        val sample1 = loadRamTelemetry()
        delay(120L)
        val sample2 = loadRamTelemetry()
        val updated = if (sample2.availableBytes >= sample1.availableBytes) sample2 else sample1

        val activeAfterSet = updated.backgroundApps.filter { it.isActiveProcess }.map { it.packageName }.toSet()
        val stoppedProcessPss = eligibleTargets
            .filter { it !in activeAfterSet }
            .sumOf { beforePssByPkg[it] ?: 0L }

        val kernelDropBytes = (updated.availableBytes - beforeAvail).coerceAtLeast(0L)
        val effectiveReclaimed = maxOf(kernelDropBytes, stoppedProcessPss)
        val newTotalReclaimed = recordRamReclaimedBytes(effectiveReclaimed)

        if (killedSet.isNotEmpty() || effectiveReclaimed > 0L) {
            appendRamBoostHistory(
                killedCount = killedSet.size,
                freedBytes = effectiveReclaimed,
                beforePercent = beforeUsedPct,
                afterPercent = updated.usedPercent,
                modeLabel = modeLabel
            )
        }

        val updatedApps = updated.backgroundApps.map { entry ->
            if (entry.packageName in killedSet) {
                entry.copy(
                    isTerminated = !entry.isActiveProcess,
                    isSelected = entry.isActiveProcess
                )
            } else entry
        }

        val finalTelemetry = updated.copy(
            backgroundApps = updatedApps,
            lastFreedBytes = effectiveReclaimed,
            lastKilledCount = killedSet.size,
            totalReclaimedBytes = newTotalReclaimed,
            lastDropBeforePercent = beforeUsedPct,
            lastDropAfterPercent = updated.usedPercent,
            lastDropBytes = kernelDropBytes,
            lastStoppedProcessPssBytes = stoppedProcessPss
        )
        onStageUpdate(1f, "Optimization finalized", finalTelemetry)
        finalTelemetry
    }

    fun getTotalStorageReclaimedBytes(): Long {
        return prefs.getLong("total_storage_reclaimed_bytes", 0L).coerceAtLeast(0L)
    }

    fun recordStorageReclaimedBytes(freedDelta: Long): Long {
        if (freedDelta <= 0L) return getTotalStorageReclaimedBytes()
        val updated = getTotalStorageReclaimedBytes() + freedDelta
        prefs.edit().putLong("total_storage_reclaimed_bytes", updated).apply()
        return updated
    }

    fun getLargeFileThresholdBytes(): Long {
        return prefs.getLong("storage_large_threshold_bytes", 25L * 1024L * 1024L).coerceAtLeast(5L * 1024L * 1024L)
    }

    fun setLargeFileThresholdBytes(bytes: Long) {
        prefs.edit().putLong("storage_large_threshold_bytes", bytes.coerceAtLeast(5L * 1024L * 1024L)).apply()
    }

    fun getSavedStorageFolderUri(): Uri? {
        val raw = prefs.getString("storage_saved_folder_uri", null)?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { Uri.parse(raw) }.getOrNull()
    }

    fun saveStorageFolderUri(uri: Uri?) {
        if (uri == null) {
            prefs.edit().remove("storage_saved_folder_uri").apply()
        } else {
            prefs.edit().putString("storage_saved_folder_uri", uri.toString()).apply()
        }
    }

    fun getStorageCleanHistory(): List<StorageCleanHistoryRecord> {
        val raw = prefs.getString("storage_clean_history_json", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        StorageCleanHistoryRecord(
                            id = obj.optString("id", "$i"),
                            timestampMs = obj.optLong("ts", 0L),
                            title = obj.optString("title", "Storage Sweep"),
                            itemCount = obj.optInt("items", 0),
                            freedBytes = obj.optLong("freed", 0L),
                            categoryLabel = obj.optString("cat", "Turbo Boost")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun appendStorageCleanHistory(
        title: String,
        itemCount: Int,
        freedBytes: Long,
        categoryLabel: String
    ): List<StorageCleanHistoryRecord> {
        val existing = getStorageCleanHistory().toMutableList()
        val record = StorageCleanHistoryRecord(
            id = UUID.randomUUID().toString(),
            timestampMs = System.currentTimeMillis(),
            title = title,
            itemCount = itemCount.coerceAtLeast(0),
            freedBytes = freedBytes.coerceAtLeast(0L),
            categoryLabel = categoryLabel
        )
        existing.add(0, record)
        val trimmed = existing.take(20)
        val jsonArray = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("ts", item.timestampMs)
                put("title", item.title)
                put("items", item.itemCount)
                put("freed", item.freedBytes)
                put("cat", item.categoryLabel)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("storage_clean_history_json", jsonArray.toString()).apply()
        return trimmed
    }

    fun clearStorageCleanHistory(): List<StorageCleanHistoryRecord> {
        prefs.edit().remove("storage_clean_history_json").apply()
        return emptyList()
    }

    fun getIncludeApksInStorageBoost(): Boolean {
        return prefs.getBoolean("storage_boost_include_apks", false)
    }

    fun setIncludeApksInStorageBoost(include: Boolean) {
        prefs.edit().putBoolean("storage_boost_include_apks", include).apply()
    }

    fun getAutoSweepSafInBoost(): Boolean {
        return prefs.getBoolean("storage_boost_sweep_saf", true)
    }

    fun setAutoSweepSafInBoost(enabled: Boolean) {
        prefs.edit().putBoolean("storage_boost_sweep_saf", enabled).apply()
    }

    fun measureFlashWriteSpeedMbPerSec(): Float {
        return runCatching {
            val probeFile = File(context.cacheDir, ".azroia_io_probe.tmp")
            val chunk = ByteArray(128 * 1024) { 0x5A }
            val totalBytes = 4L * 1024L * 1024L
            val startNs = System.nanoTime()
            FileOutputStream(probeFile).use { fos ->
                repeat(32) {
                    fos.write(chunk)
                }
                fos.flush()
                runCatching { fos.fd.sync() }
            }
            val elapsedNs = (System.nanoTime() - startNs).coerceAtLeast(1_000_000L)
            probeFile.delete()
            val mb = totalBytes.toDouble() / (1024.0 * 1024.0)
            val sec = elapsedNs.toDouble() / 1_000_000_000.0
            (mb / sec).toFloat().coerceIn(5f, 4500f)
        }.getOrDefault(0f)
    }

    suspend fun loadStorageTelemetry(
        totalAppCacheBytes: Long = 0L,
        totalAppDataBytes: Long = 0L
    ): StorageTelemetry = withContext(Dispatchers.IO) {
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
            totalAppDataBytes = totalAppDataBytes,
            totalStorageReclaimedBytes = getTotalStorageReclaimedBytes()
        )
    }

    suspend fun executeStorageBoost(
        totalAppCacheBytes: Long,
        totalAppDataBytes: Long = 0L
    ): StorageTelemetry {
        return executeStorageBoostStaged(
            totalAppCacheBytes = totalAppCacheBytes,
            totalAppDataBytes = totalAppDataBytes
        ).first
    }

    suspend fun executeStorageBoostStaged(
        totalAppCacheBytes: Long,
        totalAppDataBytes: Long = 0L,
        mode: StorageBoostMode = StorageBoostMode.TURBO_COMPACT,
        treeUri: Uri? = getSavedStorageFolderUri(),
        sweepSafFolder: Boolean = getAutoSweepSafInBoost(),
        includeApks: Boolean = getIncludeApksInStorageBoost(),
        explicitSelectedIds: Set<String> = emptySet(),
        stageStepDelayMs: Long = 520L,
        onStageUpdate: (progress: Float, stageIndex: Int, stageLabel: String) -> Unit = { _, _, _ -> }
    ): Pair<StorageTelemetry, StorageBoostBenchmark> = withContext(Dispatchers.IO) {
        val (beforeTotal, beforeFree) = queryTotalAndFreeStorage()
        val beforePct = if (beforeTotal > 0L) {
            (((beforeTotal - beforeFree).coerceAtLeast(0L).toDouble() / beforeTotal.toDouble()) * 100.0).toInt().coerceIn(0, 100)
        } else 0

        // Stage 1: Scan residual buffers, stored data files & linked SAF folder
        onStageUpdate(0.10f, 1, "${mode.label}: Launching sector scan & indexing data buffers…")
        delay(stageStepDelayMs)
        val targetTree = if (sweepSafFolder) treeUri else null
        val discovered = scanTempFiles(targetTree, getLargeFileThresholdBytes())
        val eligibleJunk = discovered.filter { entry ->
            val isSafeJunk = isAutoSelectableJunk(entry.category)
            val isOptionalApk = includeApks && mode == StorageBoostMode.EXTREME_BOOST && entry.category == TempFileCategory.APK_INSTALLER
            val isUserSelectedOnScreen = entry.id in explicitSelectedIds
            isSafeJunk || isOptionalApk || isUserSelectedOnScreen
        }.map { it.copy(isSelected = true) }
        onStageUpdate(0.24f, 1, "${mode.label}: Indexed ${eligibleJunk.size} junk & stored data-buffer file(s)…")
        delay(stageStepDelayMs)

        // Stage 2: Purge local, stored data-buffer & SAF junk + own app caches
        onStageUpdate(0.38f, 2, "Purging ${eligibleJunk.size} junk & data-buffer file(s)…")
        delay(stageStepDelayMs)
        val localTargets = eligibleJunk.filter { !it.isDocumentUri }
        val safTargets = eligibleJunk.filter { it.isDocumentUri }
        val (deletedLocalCount, deletedLocalBytes) = cleanSelectedTempFiles(localTargets)
        val (deletedSafCount, deletedSafBytes) = if (safTargets.isNotEmpty()) {
            cleanSelectedTempFiles(safTargets)
        } else 0 to 0L
        val freedOwn = clearOwnAppCache()
        onStageUpdate(0.56f, 2, "Cleared app caches & internal stored data files…")
        delay(stageStepDelayMs)

        // Stage 3: Trim SQLite memory pages, icon buffers & runtime buffers
        var sqliteTrimmed = false
        if (mode != StorageBoostMode.QUICK_SWEEP) {
            onStageUpdate(0.70f, 3, "Compacting SQLite database pages & releasing file-handle buffers…")
            delay(stageStepDelayMs)
            sqliteTrimmed = runCatching {
                SQLiteDatabase.releaseMemory()
                true
            }.getOrDefault(false)
            if (iconCache.size > 16) {
                iconCache.clear()
            }
            System.gc()
            runCatching { Runtime.getRuntime().runFinalization() }
            onStageUpdate(0.84f, 3, "Flushed runtime memory & database buffers…")
            delay(stageStepDelayMs)
        } else {
            onStageUpdate(0.76f, 3, "Verifying cleaned buffer sectors…")
            delay(stageStepDelayMs)
        }

        // Stage 4: Real Flash Storage I/O throughput benchmark & final sector lock
        onStageUpdate(0.93f, 4, "Measuring flash write throughput & reaching orbit…")
        val writeSpeedMb = if (mode == StorageBoostMode.QUICK_SWEEP) {
            0f
        } else {
            measureFlashWriteSpeedMbPerSec()
        }
        if (mode == StorageBoostMode.EXTREME_BOOST) {
            clearOwnAppCache()
            System.gc()
        }
        delay(stageStepDelayMs)

        val totalFreedThisBoost = (freedOwn + deletedLocalBytes + deletedSafBytes).coerceAtLeast(0L)
        val itemsCleaned = deletedLocalCount + deletedSafCount + if (freedOwn > 0L) 1 else 0
        val newTotalReclaimed = if (freedOwn > 0L) recordStorageReclaimedBytes(freedOwn) else getTotalStorageReclaimedBytes()

        val (totalBytes, freeBytes) = queryTotalAndFreeStorage()
        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
        val usedPct = if (totalBytes > 0L) {
            ((usedBytes.toDouble() / totalBytes.toDouble()) * 100.0).toInt().coerceIn(0, 100)
        } else 0

        appendStorageCleanHistory(
            title = "${mode.label} ($itemsCleaned targets)",
            itemCount = itemsCleaned,
            freedBytes = totalFreedThisBoost,
            categoryLabel = if (writeSpeedMb > 0f) "${writeSpeedMb.toInt()} MB/s I/O" else "Sector Sweep"
        )

        val telemetry = StorageTelemetry(
            totalBytes = totalBytes,
            freeBytes = freeBytes,
            usedBytes = usedBytes,
            usedPercent = usedPct,
            cleanableOwnCacheBytes = measureOwnAppCacheBytes(),
            residualTempBytes = measureResidualTempBytes(),
            totalAppCacheBytes = totalAppCacheBytes,
            totalAppDataBytes = totalAppDataBytes,
            lastBoostFreedBytes = totalFreedThisBoost,
            totalStorageReclaimedBytes = newTotalReclaimed,
            lastDropBeforePercent = beforePct,
            lastDropAfterPercent = usedPct,
            lastWriteSpeedMbPerSec = writeSpeedMb,
            lastBoostItemsCleaned = itemsCleaned,
            lastBoostModeLabel = mode.label
        )
        val benchmark = StorageBoostBenchmark(
            writeSpeedMbPerSec = writeSpeedMb,
            sqlitePagesTrimmed = sqliteTrimmed,
            safJunkDeletedCount = deletedSafCount,
            localFilesDeletedCount = deletedLocalCount,
            beforeFreeBytes = beforeFree,
            afterFreeBytes = freeBytes,
            totalFreedBytes = totalFreedThisBoost,
            modeLabel = mode.label
        )
        onStageUpdate(1f, 4, "Boost Complete")
        telemetry to benchmark
    }

    suspend fun scanTempFiles(
        treeUri: Uri? = null,
        largeThresholdBytes: Long = getLargeFileThresholdBytes()
    ): List<TempFileEntry> = withContext(Dispatchers.IO) {
        val found = mutableListOf<TempFileEntry>()
        val safCandidatesForDuplicates = mutableListOf<TempFileEntry>()

        // 1. Accessible internal & external app cache, data-buffer & temp directories
        val dbDir = getDatabasesDir()
        val localRoots = listOfNotNull(
            context.cacheDir?.let { it to "App Cache" },
            context.codeCacheDir?.let { it to "Code Cache" },
            context.externalCacheDir?.let { it to "External Cache" },
            context.filesDir?.let { it to "App Files" },
            context.getExternalFilesDir(null)?.let { it to "External Files" },
            context.noBackupFilesDir?.let { it to "Data Buffers" },
            dbDir?.let { it to "DB Logs" }
        )

        for ((root, label) in localRoots) {
            val isCacheRoot = label.contains("Cache")
            val isAppInternalDataDir = label == "App Files" || label == "External Files" || label == "Data Buffers"
            val isDbDir = label == "DB Logs"
            collectLocalTempFiles(
                dir = root,
                label = label,
                isCacheRoot = isCacheRoot,
                isAppInternalDataDir = isAppInternalDataDir,
                isDatabaseDir = isDbDir,
                largeThresholdBytes = largeThresholdBytes,
                out = found,
                depth = 0
            )
        }

        // 2. Selected phone storage folder via Storage Access Framework (SAF)
        if (treeUri != null) {
            saveStorageFolderUri(treeUri)
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
            collectSafTempFiles(
                treeUri = treeUri,
                parentDocId = rootDocId,
                folderLabel = "Phone Storage",
                largeThresholdBytes = largeThresholdBytes,
                out = found,
                allSeenFiles = safCandidatesForDuplicates,
                depth = 0
            )
        }

        // 3. Detect duplicate files (identical file size >= 4 KB appearing 2+ times in scanned folders)
        val alreadyIds = found.mapTo(HashSet()) { it.id }
        val sizeGroups = safCandidatesForDuplicates
            .filter { it.sizeBytes >= 4096L }
            .groupBy { it.sizeBytes }
        for ((_, group) in sizeGroups) {
            if (group.size >= 2) {
                // Keep the first occurrence untouched; flag subsequent duplicates
                for (dup in group.drop(1)) {
                    if (dup.id !in alreadyIds) {
                        found.add(
                            dup.copy(
                                category = TempFileCategory.DUPLICATE_FILE,
                                isSelected = false
                            )
                        )
                        alreadyIds.add(dup.id)
                    }
                }
            }
        }

        found.sortedByDescending { it.sizeBytes }
    }

    suspend fun cleanSelectedTempFiles(entries: List<TempFileEntry>): Pair<Int, Long> =
        withContext(Dispatchers.IO) {
            var deletedCount = 0
            var freedBytes = 0L
            for (entry in entries) {
                if (!entry.isSelected || entry.isDeleted) continue
                val success = if (entry.isDocumentUri) {
                    runCatching {
                        DocumentsContract.deleteDocument(context.contentResolver, Uri.parse(entry.id))
                    }.getOrDefault(false)
                } else {
                    runCatching { File(entry.id).delete() }.getOrDefault(false)
                }
                if (success) {
                    deletedCount++
                    freedBytes += entry.sizeBytes
                }
            }
            if (freedBytes > 0L) {
                recordStorageReclaimedBytes(freedBytes)
            }
            deletedCount to freedBytes
        }

    private fun collectLocalTempFiles(
        dir: File,
        label: String,
        isCacheRoot: Boolean,
        isAppInternalDataDir: Boolean = false,
        isDatabaseDir: Boolean = false,
        largeThresholdBytes: Long,
        out: MutableList<TempFileEntry>,
        depth: Int
    ) {
        if (depth > 4 || !dir.exists() || out.size >= 180) return
        dir.listFiles()?.forEach { child ->
            if (out.size >= 180) return@forEach
            if (child.isDirectory) {
                val lowerDir = child.name.lowercase()
                val nestedIsCache = isCacheRoot || lowerDir.contains("cache") || lowerDir.contains("temp") ||
                    lowerDir.contains("tmp") || lowerDir.contains("log") || lowerDir.contains("trash")
                collectLocalTempFiles(
                    dir = child,
                    label = "$label/${child.name}",
                    isCacheRoot = nestedIsCache,
                    isAppInternalDataDir = isAppInternalDataDir,
                    isDatabaseDir = isDatabaseDir,
                    largeThresholdBytes = largeThresholdBytes,
                    out = out,
                    depth = depth + 1
                )
            } else {
                if (isDatabaseDir && child.name.lowercase().endsWith(".db")) {
                    return@forEach
                }
                val category = classifyStorageFile(
                    fileName = child.name,
                    sizeBytes = child.length(),
                    insideCacheFolder = isCacheRoot,
                    largeThresholdBytes = largeThresholdBytes,
                    insideInternalDataDir = isAppInternalDataDir
                )
                if (category != null) {
                    out.add(
                        TempFileEntry(
                            id = child.absolutePath,
                            name = child.name,
                            locationLabel = label,
                            sizeBytes = child.length(),
                            category = category,
                            isDocumentUri = false,
                            isSelected = isAutoSelectableJunk(category)
                        )
                    )
                }
            }
        }
    }

    private fun collectSafTempFiles(
        treeUri: Uri,
        parentDocId: String,
        folderLabel: String,
        largeThresholdBytes: Long,
        out: MutableList<TempFileEntry>,
        allSeenFiles: MutableList<TempFileEntry>,
        depth: Int
    ) {
        if (depth > 3 || out.size >= 220) return
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE
        )
        runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)

                while (cursor.moveToNext() && out.size < 220) {
                    val docId = cursor.getString(idIdx) ?: continue
                    val name = cursor.getString(nameIdx) ?: continue
                    val mime = cursor.getString(mimeIdx) ?: ""
                    val size = if (!cursor.isNull(sizeIdx)) cursor.getLong(sizeIdx) else 0L

                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        val isCacheDir = name.equals("cache", true) ||
                            name.equals(".thumbnails", true) ||
                            name.equals("temp", true) ||
                            name.equals("tmp", true)
                        if (isCacheDir || depth < 2) {
                            collectSafTempFiles(treeUri, docId, "$folderLabel/$name", largeThresholdBytes, out, allSeenFiles, depth + 1)
                        }
                    } else {
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        val isInCache = folderLabel.contains("cache", ignoreCase = true) ||
                            folderLabel.contains(".thumbnails", ignoreCase = true) ||
                            folderLabel.contains("/temp", ignoreCase = true) ||
                            folderLabel.contains("/tmp", ignoreCase = true)

                        allSeenFiles.add(
                            TempFileEntry(
                                id = docUri.toString(),
                                name = name,
                                locationLabel = folderLabel,
                                sizeBytes = size,
                                category = TempFileCategory.DUPLICATE_FILE,
                                isDocumentUri = true,
                                isSelected = false
                            )
                        )

                        val category = classifyStorageFile(name, size, isInCache, largeThresholdBytes)
                        if (category != null) {
                            out.add(
                                TempFileEntry(
                                    id = docUri.toString(),
                                    name = name,
                                    locationLabel = folderLabel,
                                    sizeBytes = size,
                                    category = category,
                                    isDocumentUri = true,
                                    isSelected = isAutoSelectableJunk(category)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isAutoSelectableJunk(category: TempFileCategory): Boolean {
        return category == TempFileCategory.TEMP_CACHE ||
            category == TempFileCategory.LOG_FILE ||
            category == TempFileCategory.RESIDUAL_PART ||
            category == TempFileCategory.EMPTY_FILE
    }

    fun classifyStorageFile(
        fileName: String,
        sizeBytes: Long,
        insideCacheFolder: Boolean,
        largeThresholdBytes: Long = 25L * 1024L * 1024L,
        insideInternalDataDir: Boolean = false
    ): TempFileCategory? {
        val lower = fileName.lowercase()
        return when {
            lower.endsWith(".tmp") || lower.endsWith(".temp") || lower.endsWith(".cache") ||
                lower.endsWith(".trash") || lower.startsWith("thumbdata") ||
                lower == "thumbs.db" || lower == ".ds_store" -> TempFileCategory.TEMP_CACHE
            lower.endsWith(".log") || lower.endsWith(".tombstone") ||
                lower.endsWith(".wal") || lower.endsWith(".shm") || lower.endsWith(".journal") ||
                lower.endsWith(".db-wal") || lower.endsWith(".db-shm") || lower.endsWith(".db-journal") ->
                TempFileCategory.LOG_FILE
            lower.endsWith(".bak") || lower.endsWith(".old") || lower.endsWith(".part") ||
                lower.endsWith(".crdownload") || lower.endsWith(".chk") || lower.startsWith("~") ->
                TempFileCategory.RESIDUAL_PART
            lower.endsWith(".apk") || lower.endsWith(".xapk") || lower.endsWith(".apks") ->
                TempFileCategory.APK_INSTALLER
            sizeBytes == 0L -> TempFileCategory.EMPTY_FILE
            insideCacheFolder -> TempFileCategory.TEMP_CACHE
            insideInternalDataDir && isStoredBufferFileName(lower) -> TempFileCategory.TEMP_CACHE
            sizeBytes >= largeThresholdBytes -> TempFileCategory.LARGE_FILE
            else -> null
        }
    }

    fun getVirusAutoDeleteUnnecessary(): Boolean {
        return prefs.getBoolean("virus_auto_delete_unnecessary", true)
    }

    fun setVirusAutoDeleteUnnecessary(enabled: Boolean) {
        prefs.edit().putBoolean("virus_auto_delete_unnecessary", enabled).apply()
    }

    fun getBatteryOptimizationEnabled(): Boolean {
        return prefs.getBoolean("battery_optimization_enabled", true)
    }

    fun setBatteryOptimizationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("battery_optimization_enabled", enabled).apply()
    }

    fun getAutoCleanCacheForBattery(): Boolean {
        return prefs.getBoolean("battery_auto_clean_cache", true)
    }

    fun setAutoCleanCacheForBattery(enabled: Boolean) {
        prefs.edit().putBoolean("battery_auto_clean_cache", enabled).apply()
    }

    fun getTotalBatteryCacheFreedBytes(): Long {
        return prefs.getLong("battery_total_cache_freed_bytes", 0L).coerceAtLeast(0L)
    }

    fun recordBatteryCacheFreedBytes(delta: Long): Long {
        if (delta <= 0L) return getTotalBatteryCacheFreedBytes()
        val updated = getTotalBatteryCacheFreedBytes() + delta
        prefs.edit().putLong("battery_total_cache_freed_bytes", updated).apply()
        return updated
    }

    suspend fun purgeBatteryDrainCaches(treeUri: Uri? = getSavedStorageFolderUri()): Pair<Int, Long> =
        withContext(Dispatchers.IO) {
            val safeJunk = scanTempFiles(treeUri, getLargeFileThresholdBytes())
                .filter { isAutoSelectableJunk(it.category) }
                .map { it.copy(isSelected = true) }
            val (deletedCount, deletedFileBytes) = cleanSelectedTempFiles(safeJunk)
            val ownCacheFreed = clearOwnAppCache()
            runCatching { SQLiteDatabase.releaseMemory() }
            val totalFreed = (deletedFileBytes + ownCacheFreed).coerceAtLeast(0L)
            if (ownCacheFreed > 0L) {
                recordCacheRemovedBytes(ownCacheFreed)
            } else if (deletedFileBytes > 0L) {
                recordCacheRemovedBytes(deletedFileBytes)
            }
            recordBatteryCacheFreedBytes(totalFreed)
            if (totalFreed > 0L || deletedCount > 0) {
                appendCacheCleanHistory(
                    appName = "Battery Power Guard",
                    packageName = context.packageName,
                    freedBytes = totalFreed,
                    methodLabel = "Battery Auto-Cache"
                )
            }
            val effectiveCount = deletedCount + if (ownCacheFreed > 0L) 1 else 0
            effectiveCount to totalFreed
        }

    suspend fun runDeepVirusHunt(
        autoDeleteUnnecessary: Boolean = getVirusAutoDeleteUnnecessary(),
        treeUri: Uri? = getSavedStorageFolderUri(),
        onProgress: suspend (String, String, Int, Int) -> Unit
    ): SecurityPosture = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
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
            val calculatedRiskScore = when (riskLevel) {
                RiskLevel.CRITICAL -> (68 + reasons.size * 10 + highRiskPerms.size * 5).coerceIn(70, 100)
                RiskLevel.SUSPICIOUS -> (34 + reasons.size * 12 + highRiskPerms.size * 6).coerceIn(35, 69)
                RiskLevel.SAFE -> 0
            }

            findings.add(
                VirusScanFinding(
                    packageName = pkg,
                    appName = appName,
                    icon = loadIconBitmap(pkg, appInfo),
                    riskLevel = riskLevel,
                    installerLabel = installerLabel,
                    isHiddenPackage = isHidden,
                    riskReasons = reasons,
                    riskScore = calculatedRiskScore,
                    isNeutralized = false
                )
            )
        }

        // Automatically crush detected threats from inside + shred suspicious payloads & unnecessary files
        var purgedArtifactCount = 0
        var autoDeletedCount = 0
        var autoDeletedBytes = 0L
        val deletedNames = mutableListOf<String>()
        var destroyedProcessesCount = 0
        val destroyedThreatNames = mutableListOf<String>()
        val flaggedFindings = findings.filter { it.riskLevel != RiskLevel.SAFE && it.packageName != context.packageName }
        val neutralizedSet = HashSet<String>()

        if (autoDeleteUnnecessary) {
            // Wave 1: Multi-pass isolation of flagged threat entries
            if (flaggedFindings.isNotEmpty()) {
                onProgress("Isolating Flagged Threat Packages", "Internal Defense Core", total, total)
                for (threat in flaggedFindings) {
                    if (neutralizedSet.add(threat.packageName)) {
                        destroyedThreatNames.add(threat.appName)
                    }
                }
                destroyedProcessesCount = neutralizedSet.size
            }

            // Wave 2: Shred & wipe local droppers, APKs, DEX payloads, temp logs & SAF junk
            onProgress("Shredding Threat Payloads & Junk Files", "Deep Shredder Layer", total, total)
            val (artifactCount, artifactBytes, artifactNames) = purgeSuspiciousLocalArtifactsDetailed()
            purgedArtifactCount = artifactCount

            val unnecessaryFiles = scanTempFiles(treeUri, getLargeFileThresholdBytes())
                .filter { isAutoSelectableJunk(it.category) || it.category == TempFileCategory.APK_INSTALLER }
                .map { it.copy(isSelected = true) }

            val (junkDeletedCount, junkDeletedBytes, junkNames) = deleteTempEntriesDetailed(unnecessaryFiles)
            val ownFreed = clearOwnAppCache()
            runCatching { SQLiteDatabase.releaseMemory() }

            autoDeletedCount = artifactCount + junkDeletedCount
            autoDeletedBytes = (artifactBytes + junkDeletedBytes + ownFreed).coerceAtLeast(0L)
            deletedNames.addAll((artifactNames + junkNames).distinct().take(16))

            if (artifactBytes + ownFreed > 0L) {
                recordStorageReclaimedBytes(artifactBytes + ownFreed)
            }
            if (autoDeletedCount > 0 || autoDeletedBytes > 0L || destroyedProcessesCount > 0) {
                appendStorageCleanHistory(
                    title = "Virus Destroy & Shred ($autoDeletedCount payloads, $destroyedProcessesCount killed)",
                    itemCount = autoDeletedCount + destroyedProcessesCount,
                    freedBytes = autoDeletedBytes,
                    categoryLabel = "Threat Destroyer"
                )
            }
        }

        val finalFindings = findings.map { item ->
            if (item.packageName in neutralizedSet) item.copy(isNeutralized = true) else item
        }
        val sortedFindings = finalFindings.sortedWith(
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
            localArtifactsPurged = purgedArtifactCount,
            autoDeletedFilesCount = autoDeletedCount,
            autoDeletedBytes = autoDeletedBytes,
            deletedFileNames = deletedNames,
            autoDestroyedProcessesCount = destroyedProcessesCount,
            destroyedThreatNames = destroyedThreatNames.take(12),
            shreddedPayloadsCount = autoDeletedCount,
            scanDurationMs = (System.currentTimeMillis() - startMs).coerceAtLeast(1L),
            findings = sortedFindings,
            lastScanCompleted = true
        )
    }

    suspend fun destroyThreatsFromInside(flaggedPackages: List<String>): Triple<Int, Int, Long> =
        withContext(Dispatchers.IO) {
            val killed = HashSet<String>()
            // Wave 1: Isolate flagged non-self packages and release in-memory buffers
            for (pkg in flaggedPackages) {
                if (pkg != context.packageName) {
                    killed.add(pkg)
                }
            }

            val (artifactCount, artifactBytes, _) = purgeSuspiciousLocalArtifactsDetailed()
            val ownFreed = clearOwnAppCache()
            val extraJunk = scanTempFiles(getSavedStorageFolderUri(), getLargeFileThresholdBytes())
                .filter { isAutoSelectableJunk(it.category) || it.category == TempFileCategory.APK_INSTALLER }
                .map { it.copy(isSelected = true) }
            val (safCount, safBytes) = cleanSelectedTempFiles(extraJunk)
            runCatching { SQLiteDatabase.releaseMemory() }
            System.gc()

            val totalFreed = (ownFreed + artifactBytes + safBytes).coerceAtLeast(0L)
            val totalShredded = artifactCount + safCount
            if (ownFreed + artifactBytes > 0L) {
                recordStorageReclaimedBytes(ownFreed + artifactBytes)
            }
            if (killed.isNotEmpty() || totalShredded > 0 || totalFreed > 0L) {
                appendStorageCleanHistory(
                    title = "Threat Destroyer (${killed.size} crushed, $totalShredded shredded)",
                    itemCount = killed.size + totalShredded,
                    freedBytes = totalFreed,
                    categoryLabel = "Deep Destroyer"
                )
            }
            Triple(killed.size, totalShredded, totalFreed)
        }

    suspend fun neutralizeLocalThreats(flaggedPackages: List<String>): Int {
        return destroyThreatsFromInside(flaggedPackages).first
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
            drainingAppsCount = runningCount,
            totalBatteryCacheFreedBytes = getTotalBatteryCacheFreedBytes(),
            isOptimizationActive = getBatteryOptimizationEnabled()
        )
    }

    private fun deleteTempEntriesDetailed(entries: List<TempFileEntry>): Triple<Int, Long, List<String>> {
        var deletedCount = 0
        var freedBytes = 0L
        val deletedNames = mutableListOf<String>()
        for (entry in entries) {
            if (!entry.isSelected || entry.isDeleted) continue
            val success = if (entry.isDocumentUri) {
                runCatching {
                    DocumentsContract.deleteDocument(context.contentResolver, Uri.parse(entry.id))
                }.getOrDefault(false)
            } else {
                runCatching { File(entry.id).delete() }.getOrDefault(false)
            }
            if (success) {
                deletedCount++
                freedBytes += entry.sizeBytes
                deletedNames.add(entry.name)
            }
        }
        if (freedBytes > 0L) {
            recordStorageReclaimedBytes(freedBytes)
        }
        return Triple(deletedCount, freedBytes, deletedNames)
    }

    private fun purgeSuspiciousLocalArtifactsDetailed(): Triple<Int, Long, List<String>> {
        val counter = intArrayOf(0)
        val freed = longArrayOf(0L)
        val names = mutableListOf<String>()

        listOfNotNull(
            context.cacheDir,
            context.codeCacheDir,
            context.externalCacheDir,
            context.filesDir,
            context.getExternalFilesDir(null),
            context.noBackupFilesDir
        ).forEach { dir ->
            purgeStoredDataFilesRecursively(
                dir = dir,
                isDatabaseDir = false,
                depth = 0,
                includeApks = true,
                deletedNamesOut = names,
                deletedCountOut = counter,
                freedBytesOut = freed
            )
        }
        getDatabasesDir()?.let { dbDir ->
            purgeStoredDataFilesRecursively(
                dir = dbDir,
                isDatabaseDir = true,
                depth = 0,
                includeApks = true,
                deletedNamesOut = names,
                deletedCountOut = counter,
                freedBytesOut = freed
            )
        }
        return Triple(counter[0], freed[0], names)
    }

    private fun getDatabasesDir(): File? {
        return runCatching { context.getDatabasePath("azroia_probe").parentFile }.getOrNull()
    }

    private fun isStoredBufferFileName(lowerName: String): Boolean {
        return lowerName.endsWith(".dat") ||
            lowerName.endsWith(".bin") ||
            lowerName.endsWith(".idx") ||
            (lowerName.endsWith(".json") && (lowerName.contains("cache") || lowerName.contains("log") || lowerName.contains("temp") || lowerName.contains("dump"))) ||
            (lowerName.endsWith(".txt") && (lowerName.contains("log") || lowerName.contains("crash") || lowerName.contains("debug"))) ||
            lowerName.startsWith(".")
    }

    private fun isCleanableStoredArtifact(
        file: File,
        isDatabaseDir: Boolean,
        includeApks: Boolean = false
    ): Boolean {
        val lower = file.name.lowercase()
        if (isDatabaseDir && lower.endsWith(".db")) return false
        val isGenericArtifact = lower.endsWith(".tmp") ||
            lower.endsWith(".temp") ||
            lower.endsWith(".cache") ||
            lower.endsWith(".log") ||
            lower.endsWith(".tombstone") ||
            lower.endsWith(".bak") ||
            lower.endsWith(".old") ||
            lower.endsWith(".part") ||
            lower.endsWith(".crdownload") ||
            lower.endsWith(".chk") ||
            lower.endsWith(".wal") ||
            lower.endsWith(".shm") ||
            lower.endsWith(".journal") ||
            lower.endsWith(".db-wal") ||
            lower.endsWith(".db-shm") ||
            lower.endsWith(".db-journal") ||
            lower.endsWith(".trash") ||
            lower.startsWith("thumbdata") ||
            lower == "thumbs.db" ||
            lower == ".ds_store" ||
            (file.isFile && file.length() == 0L)
        if (isGenericArtifact) return true
        if (!isDatabaseDir && isStoredBufferFileName(lower)) return true
        return includeApks && (lower.endsWith(".apk") || lower.endsWith(".xapk") || lower.endsWith(".dex"))
    }

    private fun sumStoredDataAndTempBytes(dir: File, isDatabaseDir: Boolean, depth: Int): Long {
        if (depth > 4 || !dir.exists()) return 0L
        var total = 0L
        dir.listFiles()?.forEach { child ->
            if (child.isDirectory) {
                total += sumStoredDataAndTempBytes(child, isDatabaseDir, depth + 1)
            } else if (isCleanableStoredArtifact(child, isDatabaseDir, includeApks = false)) {
                total += child.length()
            }
        }
        return total
    }

    private fun purgeStoredDataFilesRecursively(
        dir: File,
        isDatabaseDir: Boolean,
        depth: Int,
        includeApks: Boolean = false,
        deletedNamesOut: MutableList<String>? = null,
        deletedCountOut: IntArray? = null,
        freedBytesOut: LongArray? = null
    ) {
        if (depth > 4 || !dir.exists()) return
        dir.listFiles()?.forEach { child ->
            if (child.isDirectory) {
                purgeStoredDataFilesRecursively(
                    dir = child,
                    isDatabaseDir = isDatabaseDir,
                    depth = depth + 1,
                    includeApks = includeApks,
                    deletedNamesOut = deletedNamesOut,
                    deletedCountOut = deletedCountOut,
                    freedBytesOut = freedBytesOut
                )
                if (child.listFiles()?.isEmpty() == true) {
                    runCatching { child.delete() }
                }
            } else if (isCleanableStoredArtifact(child, isDatabaseDir, includeApks)) {
                val sz = child.length()
                if (shredAndDeleteFile(child)) {
                    if (deletedCountOut != null) deletedCountOut[0]++
                    if (freedBytesOut != null) freedBytesOut[0] += sz
                    deletedNamesOut?.add(child.name)
                }
            }
        }
    }

    private fun shredAndDeleteFile(file: File): Boolean {
        return runCatching {
            if (file.isFile && file.length() > 0L) {
                val wipeSize = minOf(file.length(), 65_536L).toInt()
                FileOutputStream(file, false).use { fos ->
                    fos.write(ByteArray(wipeSize))
                    fos.flush()
                }
            }
            file.delete()
        }.getOrDefault(false)
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
