package com.jarvis.voiceassistant

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference

class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        private var instanceRef: WeakReference<JarvisAccessibilityService>? = null

        fun isServiceRunning(): Boolean = instanceRef?.get() != null

        fun openNotifications(): Boolean {
            return instanceRef?.get()?.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS) ?: false
        }

        fun openQuickSettings(): Boolean {
            return instanceRef?.get()?.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS) ?: false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef = WeakReference(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        instanceRef = null
        return super.onUnbind(intent)
    }
}
