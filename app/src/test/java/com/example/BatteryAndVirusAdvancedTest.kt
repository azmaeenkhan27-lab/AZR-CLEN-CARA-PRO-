package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SystemDiagnosticsRepository
import com.example.model.BatteryOptimizationMode
import com.example.model.SecurityPosture
import com.example.ui.AzroiaUiState
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
class BatteryAndVirusAdvancedTest {

    @Test
    fun `battery optimization flags and purgeBatteryDrainCaches clean files and record savings`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repo = SystemDiagnosticsRepository(context)

            assertTrue(repo.getBatteryOptimizationEnabled())
            repo.setBatteryOptimizationEnabled(false)
            assertFalse(repo.getBatteryOptimizationEnabled())
            repo.setBatteryOptimizationEnabled(true)

            assertTrue(repo.getAutoCleanCacheForBattery())
            repo.setAutoCleanCacheForBattery(false)
            assertFalse(repo.getAutoCleanCacheForBattery())
            repo.setAutoCleanCacheForBattery(true)

            val tempJunk = File(context.filesDir, "battery_wake.tmp").apply {
                writeBytes(ByteArray(12 * 1024) { 7 })
            }
            val cacheJunk = File(context.cacheDir, "drain_cache.bin").apply {
                writeBytes(ByteArray(8 * 1024) { 3 })
            }
            assertTrue(tempJunk.exists())
            assertTrue(cacheJunk.exists())

            val (cleanedCount, freedBytes) = repo.purgeBatteryDrainCaches(treeUri = null)

            assertFalse(tempJunk.exists())
            assertFalse(cacheJunk.exists())
            assertTrue(cleanedCount >= 1)
            assertTrue(freedBytes >= 20L * 1024L)
            assertTrue(repo.getTotalBatteryCacheFreedBytes() >= freedBytes)
            assertTrue(repo.getCacheCleanHistory().any { it.methodLabel == "Battery Auto-Cache" })
        }
    }

    @Test
    fun `runDeepVirusHunt auto deletes unnecessary files when enabled and respects disabled toggle`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repo = SystemDiagnosticsRepository(context)

            val residualTmp = File(context.filesDir, "virus_junk_scan.tmp").apply {
                writeBytes(ByteArray(10 * 1024) { 2 })
            }
            val staleLog = File(context.filesDir, "crash_dump.log").apply {
                writeBytes(ByteArray(4 * 1024) { 5 })
            }
            val staleApk = File(context.filesDir, "dropper_temp.apk").apply {
                writeBytes(ByteArray(6 * 1024) { 9 })
            }

            val postureAuto = repo.runDeepVirusHunt(autoDeleteUnnecessary = true, treeUri = null) { _, _, _, _ -> }
            assertTrue(postureAuto.lastScanCompleted)
            assertFalse(residualTmp.exists())
            assertFalse(staleLog.exists())
            assertFalse(staleApk.exists())
            assertTrue(postureAuto.autoDeletedFilesCount >= 3)
            assertTrue(postureAuto.autoDeletedBytes >= 20L * 1024L)
            assertTrue(postureAuto.deletedFileNames.contains("virus_junk_scan.tmp"))

            val preservedFile = File(context.filesDir, "keep_when_off.tmp").apply {
                writeBytes(ByteArray(4 * 1024) { 1 })
            }
            val postureManual = repo.runDeepVirusHunt(autoDeleteUnnecessary = false, treeUri = null) { _, _, _, _ -> }
            assertTrue(preservedFile.exists())
            assertEquals(0, postureManual.autoDeletedFilesCount)
            preservedFile.delete()
        }
    }

    @Test
    fun `securityPosture threatDefenseScore and uiState battery virus defaults stay accurate`() {
        val cleanPosture = SecurityPosture(
            isDeviceLockSecure = true,
            criticalCount = 0,
            suspiciousCount = 0,
            autoDeletedFilesCount = 4,
            lastScanCompleted = true
        )
        val riskyPosture = SecurityPosture(
            isDeviceLockSecure = false,
            criticalCount = 2,
            suspiciousCount = 2,
            lastScanCompleted = true
        )
        assertEquals(100, cleanPosture.threatDefenseScore)
        assertTrue(riskyPosture.threatDefenseScore < cleanPosture.threatDefenseScore)

        val state = AzroiaUiState()
        assertTrue(state.autoDeleteUnnecessaryFiles)
        assertTrue(state.isBatteryOptimizationEnabled)
        assertTrue(state.autoCleanCacheForBattery)
        assertEquals(BatteryOptimizationMode.BALANCED, state.selectedBatteryMode)
    }
}
