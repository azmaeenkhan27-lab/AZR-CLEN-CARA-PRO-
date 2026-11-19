package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.MainActivity

class AutoCacheAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var backSequenceScheduled = false
    private var watchdogRunnable: Runnable? = null

    private val storageMenuLabels = setOf(
        "storage & cache",
        "storage and cache",
        "storage",
        "storage usage",
        "app storage",
        "internal storage",
        "স্টোরেজ এবং ক্যাশ",
        "স্টোরেজ ও ক্যাশ",
        "স্টোরেজ"
    )

    private val clearCacheButtonLabels = setOf(
        "clear cache",
        "ক্যাশ সাফ করুন",
        "ক্যাশ মুছুন",
        "ক্যাশে সাফ করুন",
        "ক্যাশ পরিষ্কার করুন"
    )

    private val forceStopButtonLabels = setOf(
        "force stop",
        "force close",
        "জোরপূর্বক বন্ধ করুন",
        "জোর করে বন্ধ করুন",
        "বাধ্যতামূলক বন্ধ"
    )

    private val forceStopConfirmLabels = setOf(
        "ok",
        "force stop",
        "stop",
        "ঠিক আছে",
        "জোরপূর্বক বন্ধ করুন",
        "বন্ধ করুন"
    )

    private val cacheClearForbiddenLabels = listOf(
        "clear storage",
        "clear data",
        "clear all data",
        "manage space",
        "manage storage",
        "uninstall",
        "force stop",
        "ডেটা সাফ",
        "ডেটা মুছুন",
        "স্টোরেজ মুছুন"
    )

    private val ramHibernateForbiddenLabels = listOf(
        "clear storage",
        "clear data",
        "clear all data",
        "manage space",
        "manage storage",
        "uninstall",
        "আনইনস্টল",
        "ডেটা সাফ",
        "ডেটা মুছুন",
        "স্টোরেজ মুছুন"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!AutoCacheCoordinator.isAutoSequenceActive) return
        val pkg = event?.packageName?.toString()
            ?: rootInActiveWindow?.packageName?.toString()
            ?: return

        // Only operate inside Android Settings / Security Center screens during an active user sweep
        if (pkg == packageName || !isSystemSettingsPackage(pkg)) return

        scheduleWatchdog()

        val root = rootInActiveWindow ?: return
        val now = System.currentTimeMillis()

        // Debounce rapid consecutive clicks
        if (now - AutoCacheCoordinator.lastTapTimestampMs < 240L) return

        when (AutoCacheCoordinator.mode) {
            AutoAutomationMode.CACHE_CLEAR -> when (AutoCacheCoordinator.step) {
                AutoCleanStep.WAITING_APP_INFO -> handleAppInfoCacheScreen(root, now)
                AutoCleanStep.WAITING_STORAGE_SCREEN -> handleStorageScreen(root, now)
                AutoCleanStep.WAITING_FORCE_STOP_CONFIRM,
                AutoCleanStep.RETURNING_TO_APP,
                AutoCleanStep.IDLE -> Unit
            }

            AutoAutomationMode.FORCE_STOP_RAM -> when (AutoCacheCoordinator.step) {
                AutoCleanStep.WAITING_APP_INFO -> handleAppInfoForceStopScreen(root, now)
                AutoCleanStep.WAITING_FORCE_STOP_CONFIRM -> handleForceStopConfirmDialog(root, now)
                AutoCleanStep.WAITING_STORAGE_SCREEN,
                AutoCleanStep.RETURNING_TO_APP,
                AutoCleanStep.IDLE -> Unit
            }
        }
    }

    private fun scheduleWatchdog() {
        watchdogRunnable?.let { handler.removeCallbacks(it) }
        val runnable = Runnable {
            if (!AutoCacheCoordinator.isAutoSequenceActive || backSequenceScheduled) return@Runnable
            val fgPkg = rootInActiveWindow?.packageName?.toString()
            if (fgPkg != null && fgPkg != packageName && isSystemSettingsPackage(fgPkg)) {
                val elapsed = System.currentTimeMillis() - AutoCacheCoordinator.stepStartedAtMs
                if (elapsed >= 4500L) {
                    val backs = if (AutoCacheCoordinator.step == AutoCleanStep.WAITING_STORAGE_SCREEN) 2 else 1
                    scheduleReturnBackToAzroia(success = false, backCount = backs)
                }
            }
        }
        watchdogRunnable = runnable
        handler.postDelayed(runnable, 4600L)
    }

    private fun cancelWatchdog() {
        watchdogRunnable?.let { handler.removeCallbacks(it) }
        watchdogRunnable = null
    }

    private fun handleAppInfoCacheScreen(root: AccessibilityNodeInfo, now: Long) {
        if (findMatchingNode(root, clearCacheButtonLabels, allowDisabled = true, forbidden = cacheClearForbiddenLabels) != null) {
            AutoCacheCoordinator.moveToStorageScreen()
            handleStorageScreen(root, now)
            return
        }

        val storageNode = findMatchingNode(root, storageMenuLabels, allowDisabled = false, forbidden = cacheClearForbiddenLabels)
        if (storageNode != null) {
            val clickable = findClickableAncestor(storageNode)
            if (clickable != null && clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                AutoCacheCoordinator.moveToStorageScreen()
                return
            }
        }

        if (AutoCacheCoordinator.scrollAttempts < 2 &&
            now - AutoCacheCoordinator.stepStartedAtMs > 750L
        ) {
            val scrollable = findScrollableNode(root)
            if (scrollable != null) {
                AutoCacheCoordinator.scrollAttempts += 1
                AutoCacheCoordinator.lastTapTimestampMs = now
                scrollable.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                return
            }
        }

        if (now - AutoCacheCoordinator.stepStartedAtMs > 4500L) {
            scheduleReturnBackToAzroia(success = false, backCount = 1)
        }
    }

    private fun handleStorageScreen(root: AccessibilityNodeInfo, now: Long) {
        val clearCacheNode = findMatchingNode(root, clearCacheButtonLabels, allowDisabled = true, forbidden = cacheClearForbiddenLabels)
        if (clearCacheNode != null) {
            val target = findClickableAncestor(clearCacheNode) ?: clearCacheNode
            if (target.isEnabled) {
                val clicked = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                scheduleReturnBackToAzroia(success = clicked, backCount = 2)
            } else {
                scheduleReturnBackToAzroia(success = false, backCount = 2)
            }
            return
        }

        if (now - AutoCacheCoordinator.stepStartedAtMs > 3600L) {
            scheduleReturnBackToAzroia(success = false, backCount = 2)
        }
    }

    private fun handleAppInfoForceStopScreen(root: AccessibilityNodeInfo, now: Long) {
        // First check if a confirmation dialog is already visible ("If you force stop an app...")
        if (isForceStopDialogOpen(root)) {
            AutoCacheCoordinator.moveToForceStopConfirm()
            handleForceStopConfirmDialog(root, now)
            return
        }

        val forceStopNode = findMatchingNode(
            root = root,
            targets = forceStopButtonLabels,
            allowDisabled = true,
            forbidden = ramHibernateForbiddenLabels
        )
        if (forceStopNode != null) {
            val target = findClickableAncestor(forceStopNode) ?: forceStopNode
            if (target.isEnabled) {
                if (target.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    AutoCacheCoordinator.moveToForceStopConfirm()
                    return
                }
            } else {
                // Force Stop disabled means the app is already completely stopped in RAM!
                scheduleReturnBackToAzroia(success = true, backCount = 1)
                return
            }
        }

        if (now - AutoCacheCoordinator.stepStartedAtMs > 4000L) {
            scheduleReturnBackToAzroia(success = false, backCount = 1)
        }
    }

    private fun handleForceStopConfirmDialog(root: AccessibilityNodeInfo, now: Long) {
        val confirmNode = findMatchingNode(
            root = root,
            targets = forceStopConfirmLabels,
            allowDisabled = false,
            forbidden = ramHibernateForbiddenLabels + listOf("cancel", "বাতিল")
        )
        if (confirmNode != null) {
            val target = findClickableAncestor(confirmNode) ?: confirmNode
            if (target.isEnabled) {
                val clicked = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                scheduleReturnBackToAzroia(success = clicked, backCount = 1)
                return
            }
        }

        // If Force stop button already became disabled without a dialog, it's stopped
        val forceStopNode = findMatchingNode(
            root = root,
            targets = forceStopButtonLabels,
            allowDisabled = true,
            forbidden = ramHibernateForbiddenLabels
        )
        if (forceStopNode != null) {
            val target = findClickableAncestor(forceStopNode) ?: forceStopNode
            if (!target.isEnabled) {
                scheduleReturnBackToAzroia(success = true, backCount = 1)
                return
            }
        }

        if (now - AutoCacheCoordinator.stepStartedAtMs > 3200L) {
            scheduleReturnBackToAzroia(success = false, backCount = 1)
        }
    }

    private fun isForceStopDialogOpen(root: AccessibilityNodeInfo): Boolean {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var hasCancel = false
        var hasOkOrForce = false
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val text = node.text?.toString()?.trim()?.lowercase().orEmpty()
            val desc = node.contentDescription?.toString()?.trim()?.lowercase().orEmpty()
            val combined = "$text $desc"
            if (combined.contains("cancel") || combined.contains("বাতিল")) hasCancel = true
            if (text == "ok" || text == "ঠিক আছে") hasOkOrForce = true
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.addLast(it) }
            }
        }
        return hasCancel && hasOkOrForce
    }

    private fun scheduleReturnBackToAzroia(success: Boolean, backCount: Int) {
        if (backSequenceScheduled) return
        backSequenceScheduled = true
        cancelWatchdog()
        AutoCacheCoordinator.markReturningToAzroia(success)

        handler.postDelayed({
            performGlobalAction(GLOBAL_ACTION_BACK)
            if (backCount >= 2) {
                handler.postDelayed({
                    if (AutoCacheCoordinator.isAutoSequenceActive) {
                        val fgPkg = rootInActiveWindow?.packageName?.toString()
                        if (fgPkg != packageName) {
                            performGlobalAction(GLOBAL_ACTION_BACK)
                        }
                    }
                    handler.postDelayed({
                        ensureBackInAzroia()
                        AutoCacheCoordinator.finishStepAndWaitNext()
                        backSequenceScheduled = false
                    }, 360L)
                }, 480L)
            } else {
                handler.postDelayed({
                    ensureBackInAzroia()
                    AutoCacheCoordinator.finishStepAndWaitNext()
                    backSequenceScheduled = false
                }, 360L)
            }
        }, 320L)
    }

    private fun ensureBackInAzroia() {
        if (!AutoCacheCoordinator.isAutoSequenceActive) return
        val fgPkg = rootInActiveWindow?.packageName?.toString()
        if (fgPkg != null && fgPkg != packageName) {
            runCatching {
                val resumeIntent = Intent(this, MainActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                }
                startActivity(resumeIntent)
            }
        }
    }

    private fun findMatchingNode(
        root: AccessibilityNodeInfo,
        targets: Set<String>,
        allowDisabled: Boolean,
        forbidden: List<String>
    ): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val text = node.text?.toString()?.trim()?.lowercase().orEmpty()
            val desc = node.contentDescription?.toString()?.trim()?.lowercase().orEmpty()

            val combined = "$text $desc"
            val isForbidden = forbidden.any { combined.contains(it) }
            if (!isForbidden) {
                val matched = targets.any { label ->
                    text == label || desc == label || text.startsWith(label)
                }
                if (matched) {
                    val clickable = findClickableAncestor(node) ?: node
                    if (allowDisabled || clickable.isEnabled) {
                        return node
                    }
                }
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.addLast(it) }
            }
        }
        return null
    }

    private fun findClickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        var depth = 0
        while (current != null && depth < 6) {
            if (current.isClickable) return current
            current = current.parent
            depth++
        }
        return null
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.isScrollable) return node
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.addLast(it) }
            }
        }
        return null
    }

    private fun isSystemSettingsPackage(pkg: String): Boolean {
        val lower = pkg.lowercase()
        return lower == "com.android.settings" ||
            lower.contains("settings") ||
            lower.contains("securitycenter") ||
            lower.contains("systemmanager")
    }

    override fun onInterrupt() {
        cancelWatchdog()
        handler.removeCallbacksAndMessages(null)
        backSequenceScheduled = false
        AutoCacheCoordinator.stopAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelWatchdog()
        handler.removeCallbacksAndMessages(null)
        backSequenceScheduled = false
    }
}
