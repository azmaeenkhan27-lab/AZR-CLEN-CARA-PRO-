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
                        onStartSequentialClean = viewModel::startSequentialAppSettingsClean,
                        onLaunchNextGuidedApp = viewModel::launchNextGuidedApp,
                        onStopSequentialClean = viewModel::stopSequentialQueue,
                        onConsumePendingSettingsPkg = viewModel::consumePendingOpenSettingsPackage,
                        onSystemClearLaunched = viewModel::markAppCacheSystemActionLaunched,
                        onBack = viewModel::navigateBackToDashboard
                    )

                    CleanerModule.RAM -> RamOptimizerScreen(
                        uiState = uiState,
                        onToggleAppSelection = viewModel::toggleRamAppSelection,
                        onOptimizeRam = viewModel::runRamOptimization,
                        onBack = viewModel::navigateBackToDashboard
                    )

                    CleanerModule.STORAGE -> StorageBoostScreen(
                        uiState = uiState,
                        onBoostStorage = viewModel::runStorageBoost,
                        onScanTempFiles = viewModel::scanPhoneTempFiles,
                        onToggleTempFile = viewModel::toggleTempFileSelection,
                        onCleanSelectedTempFiles = viewModel::cleanSelectedTempFiles,
                        onBack = viewModel::navigateBackToDashboard
                    )

                    CleanerModule.VIRUS -> ZeroVirusScreen(
                        uiState = uiState,
                        onStartScan = viewModel::runZeroVirusScan,
                        onNeutralizeThreats = viewModel::neutralizeDetectedThreats,
                        onBack = viewModel::navigateBackToDashboard
                    )

                    CleanerModule.BATTERY -> BatteryGuardScreen(
                        uiState = uiState,
                        onSelectBatteryMode = viewModel::selectBatteryMode,
                        onOptimizeBattery = viewModel::runBatteryOptimization,
                        onBack = viewModel::navigateBackToDashboard
                    )
                }
            }
        }
    }
}
