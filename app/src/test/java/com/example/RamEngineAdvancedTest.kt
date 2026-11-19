package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SystemDiagnosticsRepository
import com.example.model.KernelMemoryBreakdown
import com.example.model.RamBoostMode
import com.example.model.RamPressureThresholdPreset
import com.example.model.RamProcessEntry
import com.example.model.RamTelemetry
import com.example.service.AutoAutomationMode
import com.example.service.AutoCacheCoordinator
import com.example.service.AutoCleanStep
import com.example.ui.AzroiaUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RamEngineAdvancedTest {

    @Test
    fun `ram whitelist reclaimed bytes pressure threshold and history persist accurately`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SystemDiagnosticsRepository(context)

        val set1 = repo.toggleWhitelistedRamPackage("com.spotify.music")
        assertTrue("com.spotify.music" in set1)
        val set2 = repo.toggleWhitelistedRamPackage("com.spotify.music")
        assertFalse("com.spotify.music" in set2)

        repo.setRamPressureThresholdPercent(RamPressureThresholdPreset.PCT_85.percent)
        assertEquals(85, repo.getRamPressureThresholdPercent())

        val reclaimed1 = repo.recordRamReclaimedBytes(64L * 1024L * 1024L)
        assertTrue(reclaimed1 >= 64L * 1024L * 1024L)
        assertEquals(reclaimed1, repo.getTotalRamReclaimedBytes())

        val history = repo.appendRamBoostHistory(
            killedCount = 6,
            freedBytes = 96L * 1024L * 1024L,
            beforePercent = 82,
            afterPercent = 64,
            modeLabel = RamBoostMode.DEEP_TURBO.label
        )
        assertEquals(1, history.size)
        assertEquals(6, history.first().killedCount)
        assertEquals(RamBoostMode.DEEP_TURBO.label, history.first().modeLabel)
        assertEquals(82, history.first().beforePercent)
        assertEquals(64, history.first().afterPercent)

        val cleared = repo.clearRamBoostHistory()
        assertTrue(cleared.isEmpty())
    }

    @Test
    fun `kernel memory breakdown computes swap percent and reads own pss cleanly`() {
        val kernel = KernelMemoryBreakdown(
            memFreeBytes = 256L * 1024L * 1024L,
            pageCacheBytes = 1024L * 1024L * 1024L,
            buffersBytes = 64L * 1024L * 1024L,
            swapTotalBytes = 2L * 1024L * 1024L * 1024L,
            swapFreeBytes = 1L * 1024L * 1024L * 1024L,
            ownAppPssBytes = 48L * 1024L * 1024L
        )
        assertEquals(1L * 1024L * 1024L * 1024L, kernel.swapUsedBytes)
        assertEquals(50, kernel.swapUsedPercent)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SystemDiagnosticsRepository(context)
        val liveKernel = repo.readKernelMemoryBreakdown()
        assertTrue(liveKernel.memFreeBytes >= 0L)
        assertTrue(liveKernel.swapTotalBytes >= 0L)
    }

    @Test
    fun `ramTelemetry computes healthScore and delta drop stats`() {
        val healthyRam = RamTelemetry(
            totalBytes = 8L * 1024L * 1024L * 1024L,
            availableBytes = 4L * 1024L * 1024L * 1024L,
            usedBytes = 4L * 1024L * 1024L * 1024L,
            usedPercent = 50,
            isLowMemory = false,
            lastFreedBytes = 128L * 1024L * 1024L,
            lastDropBeforePercent = 66,
            lastDropAfterPercent = 50,
            lastDropBytes = 128L * 1024L * 1024L
        )
        val strainedRam = RamTelemetry(
            totalBytes = 4L * 1024L * 1024L * 1024L,
            availableBytes = 300L * 1024L * 1024L,
            usedBytes = 3800L * 1024L * 1024L,
            usedPercent = 92,
            isLowMemory = true,
            lastFreedBytes = 0L
        )

        assertTrue(healthyRam.healthScore > strainedRam.healthScore)
        assertEquals(16, healthyRam.lastDropBeforePercent - healthyRam.lastDropAfterPercent)
        assertTrue(healthyRam.healthScore in 15..100)
        assertTrue(strainedRam.healthScore in 15..100)
    }

    @Test
    fun `coordinator isolates cache clear mode and ram force stop mode`() {
        AutoCacheCoordinator.beginAppStep("com.google.android.youtube", "YouTube", AutoAutomationMode.FORCE_STOP_RAM)
        assertEquals(AutoAutomationMode.FORCE_STOP_RAM, AutoCacheCoordinator.mode)
        assertTrue(AutoCacheCoordinator.isAutoSequenceActive)
        AutoCacheCoordinator.moveToForceStopConfirm()
        assertEquals(AutoCleanStep.WAITING_FORCE_STOP_CONFIRM, AutoCacheCoordinator.step)
        AutoCacheCoordinator.markReturningToAzroia(true)
        assertTrue(AutoCacheCoordinator.didForceStopThisStep)
        AutoCacheCoordinator.stopAll()
        assertFalse(AutoCacheCoordinator.isAutoSequenceActive)
        assertEquals(AutoAutomationMode.CACHE_CLEAR, AutoCacheCoordinator.mode)
    }

    @Test
    fun `uiState tracks selectedRamBoostMode and process metadata`() {
        val activeProc = RamProcessEntry(
            packageName = "com.example.heavy",
            appName = "Heavy App",
            icon = null,
            importanceLabel = "Active Background",
            isSelected = true,
            isActiveProcess = true,
            processPssBytes = 180L * 1024L * 1024L
        )
        val state = AzroiaUiState(
            selectedRamBoostMode = RamBoostMode.GAMING_FOCUS,
            ramPressureThresholdPercent = 75,
            ramTelemetry = RamTelemetry(
                activeProcessCount = 1,
                backgroundApps = listOf(activeProc)
            )
        )
        assertEquals(RamBoostMode.GAMING_FOCUS, state.selectedRamBoostMode)
        assertEquals(75, state.ramPressureThresholdPercent)
        assertEquals(1, state.ramTelemetry.activeProcessCount)
        assertEquals(180L * 1024L * 1024L, state.ramTelemetry.backgroundApps.first().processPssBytes)
    }
}
