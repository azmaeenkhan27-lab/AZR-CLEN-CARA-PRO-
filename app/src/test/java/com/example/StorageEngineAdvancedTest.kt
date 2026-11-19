package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.SystemDiagnosticsRepository
import com.example.model.LargeFileThresholdPreset
import com.example.model.StorageBoostMode
import com.example.model.StorageTelemetry
import com.example.model.TempFileCategory
import com.example.ui.AzroiaUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StorageEngineAdvancedTest {

    @Test
    fun `storageTelemetry computes healthScore accurately across load levels`() {
        val healthyStorage = StorageTelemetry(
            totalBytes = 128L * 1024L * 1024L * 1024L,
            freeBytes = 76L * 1024L * 1024L * 1024L,
            usedBytes = 52L * 1024L * 1024L * 1024L,
            usedPercent = 40,
            totalAppCacheBytes = 180L * 1024L * 1024L,
            lastBoostFreedBytes = 64L * 1024L * 1024L
        )
        val strainedStorage = StorageTelemetry(
            totalBytes = 64L * 1024L * 1024L * 1024L,
            freeBytes = 4L * 1024L * 1024L * 1024L,
            usedBytes = 60L * 1024L * 1024L * 1024L,
            usedPercent = 94,
            totalAppCacheBytes = 2L * 1024L * 1024L * 1024L,
            lastBoostFreedBytes = 0L,
            totalStorageReclaimedBytes = 0L
        )

        assertTrue(healthyStorage.healthScore > strainedStorage.healthScore)
        assertTrue(healthyStorage.healthScore in 15..100)
        assertTrue(strainedStorage.healthScore in 15..100)
    }

    @Test
    fun `classifyStorageFile detects temp log partial apk empty and large files`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SystemDiagnosticsRepository(context)
        val threshold50Mb = LargeFileThresholdPreset.MB_50.bytes

        assertEquals(
            TempFileCategory.TEMP_CACHE,
            repo.classifyStorageFile("render.tmp", 1024L, insideCacheFolder = false, largeThresholdBytes = threshold50Mb)
        )
        assertEquals(
            TempFileCategory.LOG_FILE,
            repo.classifyStorageFile("crash_01.tombstone", 4096L, insideCacheFolder = false, largeThresholdBytes = threshold50Mb)
        )
        assertEquals(
            TempFileCategory.RESIDUAL_PART,
            repo.classifyStorageFile("video.crdownload", 2048L, insideCacheFolder = false, largeThresholdBytes = threshold50Mb)
        )
        assertEquals(
            TempFileCategory.APK_INSTALLER,
            repo.classifyStorageFile("installer_v2.apk", 18L * 1024L * 1024L, insideCacheFolder = false, largeThresholdBytes = threshold50Mb)
        )
        assertEquals(
            TempFileCategory.EMPTY_FILE,
            repo.classifyStorageFile("orphan_marker.dat", 0L, insideCacheFolder = false, largeThresholdBytes = threshold50Mb)
        )
        assertEquals(
            TempFileCategory.TEMP_CACHE,
            repo.classifyStorageFile("thumb_99.jpg", 8192L, insideCacheFolder = true, largeThresholdBytes = threshold50Mb)
        )
        assertEquals(
            TempFileCategory.LARGE_FILE,
            repo.classifyStorageFile("archive_backup.zip", 60L * 1024L * 1024L, insideCacheFolder = false, largeThresholdBytes = threshold50Mb)
        )
        assertNull(
            repo.classifyStorageFile("notes.pdf", 2L * 1024L * 1024L, insideCacheFolder = false, largeThresholdBytes = threshold50Mb)
        )
    }

    @Test
    fun `storage reclaimed threshold folder uri and history persist cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SystemDiagnosticsRepository(context)

        val total = repo.recordStorageReclaimedBytes(120L * 1024L * 1024L)
        assertTrue(total >= 120L * 1024L * 1024L)
        assertEquals(total, repo.getTotalStorageReclaimedBytes())

        repo.setLargeFileThresholdBytes(LargeFileThresholdPreset.MB_100.bytes)
        assertEquals(LargeFileThresholdPreset.MB_100.bytes, repo.getLargeFileThresholdBytes())

        val sampleUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADownload")
        repo.saveStorageFolderUri(sampleUri)
        assertEquals(sampleUri, repo.getSavedStorageFolderUri())
        repo.saveStorageFolderUri(null)
        assertNull(repo.getSavedStorageFolderUri())

        val history = repo.appendStorageCleanHistory(
            title = "SAF Deep Sweep",
            itemCount = 9,
            freedBytes = 48L * 1024L * 1024L,
            categoryLabel = "APK & Junk"
        )
        assertEquals(1, history.size)
        assertEquals("SAF Deep Sweep", history.first().title)
        assertEquals(9, history.first().itemCount)

        val cleared = repo.clearStorageCleanHistory()
        assertTrue(cleared.isEmpty())
    }

    @Test
    fun `boost prefs flash write probe and staged boost engine work end to end`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repo = SystemDiagnosticsRepository(context)

            assertFalse(repo.getIncludeApksInStorageBoost())
            repo.setIncludeApksInStorageBoost(true)
            assertTrue(repo.getIncludeApksInStorageBoost())
            repo.setIncludeApksInStorageBoost(false)

            assertTrue(repo.getAutoSweepSafInBoost())
            repo.setAutoSweepSafInBoost(false)
            assertFalse(repo.getAutoSweepSafInBoost())
            repo.setAutoSweepSafInBoost(true)

            val probeFile = File(context.cacheDir, ".azroia_io_probe.tmp")
            val junkFile = File(context.filesDir, "stale_residual.tmp").apply {
                writeBytes(ByteArray(16 * 1024) { 1 })
            }
            val nestedBufferDir = File(context.filesDir, "telemetry_store").apply { mkdirs() }
            val storedDataFile = File(nestedBufferDir, "session_cache.dat").apply {
                writeBytes(ByteArray(12 * 1024) { 2 })
            }
            val dbDir = context.getDatabasePath("azroia_test.db").parentFile?.apply { mkdirs() }
            val mainDbFile = dbDir?.let { File(it, "azroia_test.db").apply { writeBytes(ByteArray(4 * 1024) { 5 }) } }
            val walFile = dbDir?.let { File(it, "azroia_test.db-wal").apply { writeBytes(ByteArray(8 * 1024) { 6 }) } }

            assertTrue(junkFile.exists())
            assertTrue(storedDataFile.exists())
            assertTrue(walFile?.exists() == true)

            val stagesSeen = mutableListOf<Int>()
            val (telemetry, benchmark) = repo.executeStorageBoostStaged(
                totalAppCacheBytes = 128L * 1024L * 1024L,
                totalAppDataBytes = 512L * 1024L * 1024L,
                mode = StorageBoostMode.TURBO_COMPACT,
                treeUri = null,
                sweepSafFolder = false,
                includeApks = false,
                stageStepDelayMs = 10L
            ) { _, stageIdx, _ ->
                stagesSeen.add(stageIdx)
            }

            assertFalse(junkFile.exists())
            assertFalse(storedDataFile.exists())
            assertFalse(walFile?.exists() == true)
            assertTrue(mainDbFile?.exists() == true)
            mainDbFile?.delete()
            assertFalse(probeFile.exists())
            assertTrue(stagesSeen.containsAll(listOf(1, 2, 3, 4)))
            assertTrue(benchmark.writeSpeedMbPerSec > 0f)
            assertTrue(benchmark.totalFreedBytes >= 36L * 1024L)
            assertEquals(StorageBoostMode.TURBO_COMPACT.label, telemetry.lastBoostModeLabel)
            assertTrue(telemetry.lastBoostItemsCleaned >= 1)
        }
    }

    @Test
    fun `uiState tracks boost mode large threshold default and storage history`() {
        val state = AzroiaUiState()
        assertEquals(StorageBoostMode.TURBO_COMPACT, state.selectedStorageBoostMode)
        assertEquals(25L * 1024L * 1024L, state.storageLargeThresholdBytes)
        assertTrue(state.storageCleanHistory.isEmpty())
    }
}
