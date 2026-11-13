package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppCacheCleanState
import com.example.model.AppCacheEntry
import com.example.model.CleanerModule
import com.example.ui.AzroiaUiState
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.ModuleArtworkCanvas
import com.example.ui.components.ModuleTopBar
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.formatBytes

@Composable
fun CacheClearScreen(
    uiState: AzroiaUiState,
    onRunCacheSweep: () -> Unit,
    onStartSequentialClean: () -> Unit,
    onLaunchNextGuidedApp: () -> Unit,
    onStopSequentialClean: () -> Unit,
    onConsumePendingSettingsPkg: () -> Unit,
    onSystemClearLaunched: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    // Automatically open the queued app's Android App Info Settings page when requested
    LaunchedEffect(uiState.pendingOpenSettingsPackage) {
        val pkg = uiState.pendingOpenSettingsPackage ?: return@LaunchedEffect
        onConsumePendingSettingsPkg()
        runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", pkg, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        CyberBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModuleTopBar(
                title = CleanerModule.CACHE.title,
                tag = CleanerModule.CACHE.tag,
                accent = NeonCyan,
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.4.dp,
                            Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.8f), SolarAmber.copy(alpha = 0.45f)))
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ModuleArtworkCanvas(
                                module = CleanerModule.CACHE,
                                accent = NeonCyan,
                                progressRatio = if (uiState.isCleaningCache) uiState.cacheSweepProgress else 0.76f,
                                centerValue = formatBytes(uiState.totalDetectedAppCacheBytes),
                                centerUnit = "CACHE POOL",
                                modifier = Modifier.size(156.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (uiState.isCleaningCache) {
                                LinearProgressIndicator(
                                    progress = { uiState.cacheSweepProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = NeonCyan,
                                    trackColor = Color(0xFF15203E),
                                    strokeCap = StrokeCap.Round
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            uiState.cacheStatusMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldPulse
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Button(
                                onClick = onRunCacheSweep,
                                enabled = !uiState.isCleaningCache,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = ObsidianVoid
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("auto_cache_clear_button")
                            ) {
                                if (uiState.isCleaningCache) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = ObsidianVoid,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("SWEEPING APP CACHE…", style = MaterialTheme.typography.labelLarge)
                                } else {
                                    Icon(Icons.Filled.CleaningServices, contentDescription = null)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("SWEEP CACHE & INDEX APPS", style = MaterialTheme.typography.labelLarge)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Sequential App-by-App OS Settings Queue Launcher
                            OutlinedButton(
                                onClick = onStartSequentialClean,
                                enabled = uiState.cacheEntries.isNotEmpty() && !uiState.isCleaningCache,
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, SolarAmber.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_sequential_os_clean_button")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = SolarAmber)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "APP-BY-APP OS CACHE CLEANER",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SolarAmber
                                )
                            }
                        }
                    }
                }

                // Active Sequential Queue Controller Card
                if (uiState.isGuidedQueueActive && uiState.activeGuidedApp != null) {
                    val guidedApp = uiState.activeGuidedApp
                    item {
                        Surface(
                            color = ObsidianCardElevated,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SolarAmber)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "SEQUENTIAL APP CLEAN (${uiState.guidedQueueIndex + 1} / ${uiState.guidedQueueTotal})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SolarAmber
                                )
                                Text(
                                    text = "Next: ${guidedApp.appName}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "1. Open App Info  →  2. Tap Storage & Cache  →  3. Clear Cache  →  4. Press Back to auto-advance",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = onLaunchNextGuidedApp,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SolarAmber,
                                            contentColor = ObsidianVoid
                                        ),
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text("Open ${guidedApp.appName}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    OutlinedButton(
                                        onClick = onStopSequentialClean,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderMuted),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Icon(Icons.Filled.Stop, contentDescription = null, tint = TextSecondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Stop", color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                if (!uiState.hasUsageStatsAccess) {
                    item {
                        Surface(
                            color = ObsidianCardElevated,
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SolarAmber.copy(alpha = 0.55f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Live App Cache Telemetry",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SolarAmber
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Grant Usage Access so Android reports exact cache sizes for YouTube, Facebook, TikTok & Files.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        runCatching {
                                            context.startActivity(
                                                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                            )
                                        }
                                    },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SolarAmber),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("grant_usage_access_button")
                                ) {
                                    Text("Grant", color = SolarAmber)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "INSTALLED APPS CACHE QUEUE (${uiState.cacheEntries.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(uiState.cacheEntries, key = { it.packageName }) { entry ->
                    AppCacheRow(
                        entry = entry,
                        hasUsageAccess = uiState.hasUsageStatsAccess,
                        onOpenAppClearCache = {
                            runCatching {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", entry.packageName, null)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                onSystemClearLaunched(entry.packageName)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppCacheRow(
    entry: AppCacheEntry,
    hasUsageAccess: Boolean,
    onOpenAppClearCache: () -> Unit
) {
    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = when (entry.cleanState) {
                AppCacheCleanState.CLEANING -> NeonCyan
                AppCacheCleanState.CLEANED -> EmeraldPulse.copy(alpha = 0.6f)
                AppCacheCleanState.SYSTEM_ACTION_READY -> SolarAmber.copy(alpha = 0.55f)
                AppCacheCleanState.PENDING -> CyberBorderMuted
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entry.icon != null) {
                Image(
                    bitmap = entry.icon,
                    contentDescription = "${entry.appName} icon",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Android, contentDescription = null, tint = NeonCyan)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val sizeLabel = when {
                    entry.cleanState == AppCacheCleanState.CLEANED -> "Cache Cleaned"
                    entry.cacheBytes > 0L -> "Cache: ${formatBytes(entry.cacheBytes)}"
                    hasUsageAccess -> "0 B Cache"
                    else -> "Tap Clear → Storage → Clear Cache"
                }
                Text(
                    text = sizeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (entry.cleanState == AppCacheCleanState.CLEANED) EmeraldPulse else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            when (entry.cleanState) {
                AppCacheCleanState.CLEANING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = NeonCyan,
                        strokeWidth = 2.5.dp
                    )
                }
                AppCacheCleanState.CLEANED -> {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Cleaned",
                        tint = EmeraldPulse,
                        modifier = Modifier.size(26.dp)
                    )
                }
                AppCacheCleanState.PENDING,
                AppCacheCleanState.SYSTEM_ACTION_READY -> {
                    OutlinedButton(
                        onClick = onOpenAppClearCache,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (entry.cleanState == AppCacheCleanState.SYSTEM_ACTION_READY) SolarAmber else NeonCyan
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = if (entry.cleanState == AppCacheCleanState.SYSTEM_ACTION_READY) SolarAmber else NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (entry.cleanState == AppCacheCleanState.SYSTEM_ACTION_READY) SolarAmber else NeonCyan
                        )
                    }
                }
            }
        }
    }
}
