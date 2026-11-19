package com.example.service

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

enum class AutoAutomationMode {
    CACHE_CLEAR,
    FORCE_STOP_RAM
}

enum class AutoCleanStep {
    IDLE,
    WAITING_APP_INFO,
    WAITING_STORAGE_SCREEN,
    WAITING_FORCE_STOP_CONFIRM,
    RETURNING_TO_APP
}

object AutoCacheCoordinator {
    @Volatile
    var mode: AutoAutomationMode = AutoAutomationMode.CACHE_CLEAR
        private set

    @Volatile
    var isAutoSequenceActive: Boolean = false
        private set

    @Volatile
    var currentPackageName: String? = null
        private set

    @Volatile
    var currentAppName: String? = null
        private set

    @Volatile
    var step: AutoCleanStep = AutoCleanStep.IDLE
        private set

    @Volatile
    var stepStartedAtMs: Long = 0L
        private set

    @Volatile
    var scrollAttempts: Int = 0

    @Volatile
    var lastTapTimestampMs: Long = 0L

    @Volatile
    var didClearCacheThisStep: Boolean = false

    @Volatile
    var didForceStopThisStep: Boolean = false

    fun beginAppStep(
        packageName: String,
        appName: String,
        automationMode: AutoAutomationMode = AutoAutomationMode.CACHE_CLEAR
    ) {
        mode = automationMode
        isAutoSequenceActive = true
        currentPackageName = packageName
        currentAppName = appName
        step = AutoCleanStep.WAITING_APP_INFO
        stepStartedAtMs = System.currentTimeMillis()
        scrollAttempts = 0
        lastTapTimestampMs = 0L
        didClearCacheThisStep = false
        didForceStopThisStep = false
    }

    fun moveToStorageScreen() {
        step = AutoCleanStep.WAITING_STORAGE_SCREEN
        stepStartedAtMs = System.currentTimeMillis()
        lastTapTimestampMs = System.currentTimeMillis()
    }

    fun moveToForceStopConfirm() {
        step = AutoCleanStep.WAITING_FORCE_STOP_CONFIRM
        stepStartedAtMs = System.currentTimeMillis()
        lastTapTimestampMs = System.currentTimeMillis()
    }

    fun markReturningToAzroia(cleared: Boolean) {
        if (mode == AutoAutomationMode.FORCE_STOP_RAM) {
            didForceStopThisStep = cleared
        } else {
            didClearCacheThisStep = cleared
        }
        step = AutoCleanStep.RETURNING_TO_APP
        stepStartedAtMs = System.currentTimeMillis()
        lastTapTimestampMs = System.currentTimeMillis()
    }

    fun finishStepAndWaitNext() {
        step = AutoCleanStep.IDLE
        currentPackageName = null
        currentAppName = null
        scrollAttempts = 0
    }

    fun stopAll() {
        mode = AutoAutomationMode.CACHE_CLEAR
        isAutoSequenceActive = false
        currentPackageName = null
        currentAppName = null
        step = AutoCleanStep.IDLE
        stepStartedAtMs = 0L
        scrollAttempts = 0
        didClearCacheThisStep = false
        didForceStopThisStep = false
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponent = ComponentName(context, AutoCacheAccessibilityService::class.java)
        val expectedFull = expectedComponent.flattenToString()
        val expectedShort = expectedComponent.flattenToShortString()

        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val enabledList = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).orEmpty()
        if (enabledList.any { info ->
                val id = info.id ?: ""
                id.equals(expectedFull, ignoreCase = true) ||
                    id.equals(expectedShort, ignoreCase = true) ||
                    id.contains(AutoCacheAccessibilityService::class.java.simpleName)
            }
        ) {
            return true
        }

        val enabledSetting = runCatching {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
        }.getOrNull().orEmpty()

        return enabledSetting.split(':').any { entry ->
            entry.equals(expectedFull, ignoreCase = true) ||
                entry.equals(expectedShort, ignoreCase = true) ||
                entry.contains(AutoCacheAccessibilityService::class.java.simpleName)
        }
    }
}
