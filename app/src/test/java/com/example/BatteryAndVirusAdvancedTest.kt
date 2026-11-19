package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SystemDiagnosticsRepository
import com.example.model.BatteryOptimizationMode
import com.example.model.RiskLevel
import com.example.model.SecurityPosture
import com.example.model.VirusScanFinding
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
            assertTrue(postureAuto.shreddedPayloadsCount >= 3)
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
    fun `destroyThreatsFromInside crushes threat processes and shreds nested payloads cleanly`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repo = SystemDiagnosticsRepository(context)

            val nestedDir = File(context.filesDir, "threat_dropper_box").apply { mkdirs() }
            val dropperApk = File(nestedDir, "stealth_payload.apk").apply {
                writeBytes(ByteArray(16 * 1024) { 6 })
            }
            val bufferDat = File(nestedDir, "stored_trace.dat").apply {
                writeBytes(ByteArray(6 * 1024) { 4 })
            }
            assertTrue(dropperApk.exists())
            assertTrue(bufferDat.exists())

            val (crushed, shredded, freedBytes) = repo.destroyThreatsFromInside(
                listOf("com.fake.spyware", "com.fake.adware", context.packageName)
            )

            assertFalse(dropperApk.exists())
            assertFalse(bufferDat.exists())
            assertEquals(2, crushed)
            assertTrue(shredded >= 2)
            assertTrue(freedBytes >= 22L * 1024L)
        }
    }

    @Test
    fun `securityPosture threatDefenseScore rewards neutralized threats and uiState defaults stay accurate`() {
        val cleanPosture = SecurityPosture(
            isDeviceLockSecure = true,
            criticalCount = 0,
            suspiciousCount = 0,
            autoDeletedFilesCount = 4,
            lastScanCompleted = true
        )
        val unneutralizedRisky = SecurityPosture(
            isDeviceLockSecure = true,
            criticalCount = 2,
            suspiciousCount = 2,
            findings = listOf(
                VirusScanFinding("com.threat.one", "Threat One", null, RiskLevel.CRITICAL, "Unknown", false, listOf("Dropper"), 88, isNeutralized = false),
                VirusScanFinding("com.threat.two", "Threat Two", null, RiskLevel.CRITICAL, "Unknown", true, listOf("Hidden"), 92, isNeutralized = false)
            ),
            lastScanCompleted = true
        )
        val neutralizedPosture = unneutralizedRisky.copy(
            findings = unneutralizedRisky.findings.map { it.copy(isNeutralized = true) }
        )
        assertEquals(100, cleanPosture.threatDefenseScore)
        assertTrue(unneutralizedRisky.threatDefenseScore < cleanPosture.threatDefenseScore)
        assertTrue(neutralizedPosture.threatDefenseScore > unneutralizedRisky.threatDefenseScore)

        val state = AzroiaUiState()
        assertTrue(state.autoDeleteUnnecessaryFiles)
        assertTrue(state.isBatteryOptimizationEnabled)
        assertTrue(state.autoCleanCacheForBattery)
        assertEquals(BatteryOptimizationMode.BALANCED, state.selectedBatteryMode)
    }
}
