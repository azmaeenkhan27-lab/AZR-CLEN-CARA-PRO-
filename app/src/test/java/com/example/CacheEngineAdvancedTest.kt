package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SystemDiagnosticsRepository
import com.example.model.AppCacheCleanState
import com.example.model.AppCacheEntry
import com.example.model.CacheCleanMode
import com.example.ui.AzroiaUiState
import com.example.util.formatRelativeTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CacheEngineAdvancedTest {

    @Test
    fun `whitelist threshold and history persist accurately`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SystemDiagnosticsRepository(context)

        val set1 = repo.toggleWhitelistedCachePackage("com.google.android.youtube")
        assertTrue("com.google.android.youtube" in set1)
        val set2 = repo.toggleWhitelistedCachePackage("com.google.android.youtube")
        assertFalse("com.google.android.youtube" in set2)

        val limit = 300L * 1024L * 1024L
        repo.setCacheAlertThresholdBytes(limit)
        assertEquals(limit, repo.getCacheAlertThresholdBytes())

        val history = repo.appendCacheCleanHistory(
            appName = "Chrome",
            packageName = "com.android.chrome",
            freedBytes = 128L * 1024L * 1024L,
            methodLabel = "Auto Engine"
        )
        assertEquals(1, history.size)
        assertEquals("Chrome", history.first().appName)

        val cleared = repo.clearCacheCleanHistory()
        assertTrue(cleared.isEmpty())
    }

    @Test
    fun `deepFlushCacheBuffers clears internal cache directories and stored logs and appends history`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repo = SystemDiagnosticsRepository(context)

            val tempCacheFile = File(context.cacheDir, "render_shader.cache").apply {
                writeBytes(ByteArray(14 * 1024) { 2 })
            }
            val tempStoredLog = File(context.filesDir, "cache_trace.wal").apply {
                writeBytes(ByteArray(6 * 1024) { 5 })
            }
            assertTrue(tempCacheFile.exists())
            assertTrue(tempStoredLog.exists())

            val result = repo.deepFlushCacheBuffers()

            assertFalse(tempCacheFile.exists())
            assertFalse(tempStoredLog.exists())
            assertTrue(result.freedBytes >= 20L * 1024L)
            assertTrue(result.filesClearedCount >= 2)
            assertTrue(repo.getCacheCleanHistory().any { it.methodLabel == "Buffer & WAL Flush" })
        }
    }

    @Test
    fun `uiState computes overThresholdApps dormantCachedApps and cacheHealthScore`() {
        val heavyApp = AppCacheEntry(
            packageName = "com.google.android.youtube",
            appName = "YouTube",
            icon = null,
            cacheBytes = 400L * 1024L * 1024L,
            appBytes = 120L * 1024L * 1024L,
            isPriorityMediaApp = true,
            isWhitelisted = false,
            wasUsedByUser = true
        )
        val dormantApp = AppCacheEntry(
            packageName = "com.example.dormant",
            appName = "Dormant App",
            icon = null,
            cacheBytes = 60L * 1024L * 1024L,
            appBytes = 50L * 1024L * 1024L,
            isPriorityMediaApp = false,
            isWhitelisted = false,
            wasUsedByUser = false
        )
        val shieldedApp = AppCacheEntry(
            packageName = "com.android.chrome",
            appName = "Chrome",
            icon = null,
            cacheBytes = 500L * 1024L * 1024L,
            appBytes = 100L * 1024L * 1024L,
            isPriorityMediaApp = false,
            isWhitelisted = true
        )
        val cleanedApp = AppCacheEntry(
            packageName = "com.facebook.katana",
            appName = "Facebook",
            icon = null,
            cacheBytes = 250L * 1024L * 1024L,
            appBytes = 90L * 1024L * 1024L,
            isPriorityMediaApp = true,
            cleanState = AppCacheCleanState.CLEANED
        )

        val state = AzroiaUiState(
            cacheEntries = listOf(heavyApp, dormantApp, shieldedApp, cleanedApp),
            cacheAlertThresholdBytes = 150L * 1024L * 1024L
        )

        assertEquals(1, state.overThresholdApps.size)
        assertEquals("com.google.android.youtube", state.overThresholdApps.first().packageName)
        assertEquals(1, state.dormantCachedApps.size)
        assertEquals("com.example.dormant", state.dormantCachedApps.first().packageName)
        assertEquals(CacheCleanMode.SMART_USED, state.selectedCacheMode)
        assertTrue(state.cacheHealthScore in 25..100)
    }

    @Test
    fun `usedAndCachedApps selects only used non-shielded apps with cache greater than zero`() {
        val usedCached = AppCacheEntry(
            packageName = "com.google.android.youtube",
            appName = "YouTube",
            icon = null,
            cacheBytes = 120L * 1024L * 1024L,
            appBytes = 90L * 1024L * 1024L,
            isPriorityMediaApp = true,
            wasUsedByUser = true
        )
        val unusedCached = AppCacheEntry(
            packageName = "com.example.unused",
            appName = "Unused App",
            icon = null,
            cacheBytes = 80L * 1024L * 1024L,
            appBytes = 50L * 1024L * 1024L,
            isPriorityMediaApp = false,
            wasUsedByUser = false
        )
        val usedZeroCache = AppCacheEntry(
            packageName = "com.example.zerocache",
            appName = "Zero Cache",
            icon = null,
            cacheBytes = 0L,
            appBytes = 40L * 1024L * 1024L,
            isPriorityMediaApp = false,
            wasUsedByUser = true
        )
        val usedShielded = AppCacheEntry(
            packageName = "com.example.shielded",
            appName = "Shielded",
            icon = null,
            cacheBytes = 200L * 1024L * 1024L,
            appBytes = 40L * 1024L * 1024L,
            isPriorityMediaApp = false,
            wasUsedByUser = true,
            isWhitelisted = true
        )

        val state = AzroiaUiState(
            cacheEntries = listOf(usedCached, unusedCached, usedZeroCache, usedShielded)
        )

        assertEquals(1, state.usedAndCachedApps.size)
        assertEquals("com.google.android.youtube", state.usedAndCachedApps.first().packageName)
    }

    @Test
    fun `formatRelativeTime returns readable labels`() {
        val now = 1_700_000_000_000L
        assertEquals("Usage untracked", formatRelativeTime(0L, now))
        assertEquals("Just now", formatRelativeTime(now - 30_000L, now))
        assertEquals("15m ago", formatRelativeTime(now - 15 * 60_000L, now))
        assertEquals("4h ago", formatRelativeTime(now - 4 * 3_600_000L, now))
        assertEquals("3d ago", formatRelativeTime(now - 3 * 86_400_000L, now))
    }
}
