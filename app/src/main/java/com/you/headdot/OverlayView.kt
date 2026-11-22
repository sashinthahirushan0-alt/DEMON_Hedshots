package com.you.headdot

import android.content.Context
import android.graphics.*
import android.view.View

class OverlayView(ctx: Context) : View(ctx) {
    private var target: PointF? = null
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 5f
        color = Color.parseColor("#00FF66")
    }
    private val cross = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 3f
        color = Color.parseColor("#FF3355")
    }

    fun update(p: PointF?) { target = p; postInvalidate() }

    override fun onDraw(c: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        c.drawCircle(cx, cy, 26f, cross)
        target?.let { c.drawCircle(it.x, it.y, 22f, ring) }
    }
}
