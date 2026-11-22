package com.you.headdot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class AimService : AccessibilityService() {

    companion object {
        var instance: AimService? = null
        var enabled = false
        var strength = 0.55f
        private const val DEAD = 14f
        private const val MAX_DRAG = 220f
        private const val GAP_MS = 60L
    }

    private var last = 0L

    override fun onServiceConnected() { instance = this }

    fun steer(dx: Float, dy: Float) {
        if (!enabled) return
        if (Math.abs(dx) < DEAD && Math.abs(dy) < DEAD) return

        val now = System.currentTimeMillis()
        if (now - last < GAP_MS) return
        last = now

        var tx = dx * strength
        var ty = dy * strength
        val len = Math.hypot(tx.toDouble(), ty.toDouble()).toFloat()
        if (len > MAX_DRAG) {
            tx = tx / len * MAX_DRAG
            ty = ty / len * MAX_DRAG
        }

        val cx = resources.displayMetrics.widthPixels / 2f
        val cy = resources.displayMetrics.heightPixels / 2f

        val path = Path().apply {
            moveTo(cx, cy)
            lineTo(cx + tx, cy + ty)
        }
        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 20))
            .build()
        dispatchGesture(g, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { instance = null; super.onDestroy() }
}
