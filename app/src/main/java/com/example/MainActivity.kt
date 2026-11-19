package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.CleanerModule
import com.example.model.ScreenRoute
import com.example.ui.CleanerViewModel
import com.example.ui.screens.BatteryGuardScreen
import com.example.ui.screens.CacheClearScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.RamOptimizerScreen
import com.example.ui.screens.SplashEntryScreen
import com.example.ui.screens.StorageBoostScreen
import com.example.ui.screens.ZeroVirusScreen
import com.example.ui.theme.AzroiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            AzroiaTheme {
                AzroiaAppRoot()
            }
        }
    }
}

@Composable
fun AzroiaAppRoot(
    viewModel: CleanerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.onAppResumed()
        onPauseOrDispose { }
    }

    AnimatedContent(
        targetState = uiState.route,
        transitionSpec = {
            fadeIn(animationSpec = tween(320)) togetherWith fadeOut(animationSpec = tween(240))
        },
        modifier = Modifier.fillMaxSize(),
        label = "azroia_route_transition"
    ) { route ->
        when (route) {
            is ScreenRoute.Splash -> {
                SplashEntryScreen(
                    isBurstActive = uiState.isLaunchBurstActive,
                    onTriggerLaunch = viewModel::triggerLaunchBurst
                )
            }

            is ScreenRoute.Dashboard -> {
                DashboardScreen(
                    uiState = uiState,
                    onModuleSelected = viewModel::openModule,
                    onReplayAzrLaunch = viewModel::replaySplashEntry
                )
            }

            is ScreenRoute.ModuleDetail -> {
                when (route.module) {
                    CleanerModule.CACHE -> CacheClearScreen(
                        uiState = uiState,
                        onRunCacheSweep = viewModel::runCacheSweepOnly,
                        onStartUsedAndCachedClean = viewModel::startAutoUsedAndCachedAppsClean,
                        onStartAutoAllAppsClean = viewModel::startAutoAllAppsCacheClean,
                        onStartAutoSelectedClean = viewModel::startAutoSelectedAppsCacheClean,
                        onStartOverThresholdClean = viewModel::startOverThresholdAutoClean,
                        onToggleAppSelection = viewModel::toggleCacheAppSelection,
                        onSelectAllApps = viewModel::selectAllCacheApps,
                        onApplySelectionPreset = viewModel::applyCacheSelectionPreset,
                        onSelectThreshold = viewModel::setCacheAlertThreshold,
                        onToggleSkipZeroCache = viewModel::setSkipZeroCacheAutoClean,
                        onToggleWhitelist = viewModel::toggleCacheWhitelist,
                        onInspectApp = viewModel::inspectCacheApp,
                        onClearCacheHistory = viewModel::clearCacheCleanHistory,
                        onCleanSingleApp = viewModel::cleanSingleAppAutoOrGuided,
                        onStartSequentialClean = viewModel::startSequentialAppSettingsClean,
                        onLaunchNextGuidedApp = viewModel::launchNextGuidedApp,
                        onStopSequentialClean = viewModel::stopSequentialQueue,
                        onDismissAccessibilityPrompt = viewModel::dismissAccessibilityPrompt,
                        onDismissUsageAccessDialog = viewModel::dismissUsageAccessDialog,
                        onConsumePendingSettingsPkg = viewModel::consumePendingOpenSettingsPackage,
                        onBack = viewModel::navigateBackToDashboard
                    )

                    CleanerModule.RAM -> RamOptimizerScreen(
                        uiState = uiState,
                        onToggleAppSelection = viewModel::toggleRamAppSelection,
                        onSelectBoostMode = viewModel::selectRamBoostMode,
                        onSelectThreshold = viewModel::selectRamPressureThreshold,
                        onToggleLiveMonitor = viewModel::toggleLiveRamMonitor,
                        onCompactHeap = viewModel::runHeapAndBufferCompact,
                        onStartDeepHibernate = viewModel::startDeepHibernateQueue,
                        onStartGuidedHibernateFallback = viewModel::startGuidedHibernateFallback,
                        onHibernateSingleApp = viewModel::hibernateSingleApp,
                        onLaunchNextHibernateApp = viewModel::launchNextHibernateApp,
                        onStopHibernateQueue = viewModel::stopRamHibernateQueue,
                        onDismissRamAccessibilityPrompt = viewModel::dismissRamAccessibilityPrompt,
                        onConsumePendingSettingsPkg = viewModel::consumePendingOpenSettingsPackage,
                        onInspectRamApp = viewModel::inspectRamApp,
                        onApplyPreset = viewModel::applyRamSelectionPreset,
                        onToggleWhitelist = viewModel::toggleRamWhitelist,
                        onRefreshProcesses = viewModel::refreshRamProcesses,
                        onClearHistory = viewModel::clearRamBoostHistory,
                        onOptimizeRam = viewModel::runRamOptimization,
                        onBack = viewModel::navigateBackToDashboard
                    )

                    CleanerModule.STORAGE -> StorageBoostScreen(
                        uiState = uiState,
                        onBoostStorage = viewModel::runStorageBoost,
                        onSelectBoostMode = viewModel::selectStorageBoostMode,
                        onToggleIncludeApksInBoost = viewModel::setIncludeApksInStorageBoost,
                        onToggleAutoSweepSafInBoost = viewModel::setAutoSweepSafInBoost,
                        onTriggerAutoAppCacheClean = {
                            viewModel.openModule(CleanerModule.CACHE)
                            viewModel.startAutoUsedAndCachedAppsClean()
                        },
                        onScanTempFiles = viewModel::scanPhoneTempFiles,
                        onSelectLargeThreshold = viewModel::setStorageLargeThreshold,
                        onApplyStoragePreset = viewModel::applyStorageSelectionPreset,
                        onClearStorageHistory = viewModel::clearStorageCleanHistory,
                        onToggleTempFile = viewModel::toggleTempFileSelection,
                        onCleanSelectedTempFiles = viewModel::cleanSelectedTempFiles,
                        onCleanSingleApp = viewModel::cleanSingleAppAutoOrGuided,
                        onConsumePendingSettingsPkg = viewModel::consumePendingOpenSettingsPackage,
                        onBack = viewModel::navigateBackToDashboard
                    )

                    CleanerModule.VIRUS -> ZeroVirusScreen(
                        uiState = uiState,
                        onStartScan = viewModel::runZeroVirusScan,
                        onToggleAutoDeleteUnnecessary = viewModel::setAutoDeleteUnnecessaryFiles,
                        onNeutralizeThreats = viewModel::neutralizeDetectedThreats,
                        onBack = viewModel::navigateBackToDashboard
                    )

                    CleanerModule.BATTERY -> BatteryGuardScreen(
                        uiState = uiState,
                        onSelectBatteryMode = viewModel::selectBatteryMode,
                        onToggleBatteryOptimization = viewModel::toggleBatteryOptimizationEnabled,
                        onToggleAutoCleanCacheForBattery = viewModel::toggleAutoCleanCacheForBattery,
                        onRunBatteryAutoCacheSweep = viewModel::runBatteryAutoCacheSweep,
                        onTriggerAutoAppCacheClean = {
                            viewModel.openModule(CleanerModule.CACHE)
                            viewModel.startAutoUsedAndCachedAppsClean()
                        },
                        onOptimizeBattery = viewModel::runBatteryOptimization,
                        onBack = viewModel::navigateBackToDashboard
                    )
                }
            }
        }
    }
}
