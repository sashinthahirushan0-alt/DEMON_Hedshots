package com.you.headdot

import android.graphics.Bitmap
import android.graphics.Point

object Detector {
    fun findHead(bmp: Bitmap): Point? {
        var sumX = 0
        var sumY = 0
        var n = 0
        for (y in 0 until bmp.height step 2) {
            for (x in 0 until bmp.width step 2) {
                val p = bmp.getPixel(x, y)
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                if (r > 150 && g < 90 && b < 90) {
                    sumX += x; sumY += y; n++
                }
            }
        }
        if (n < 40) return null
        return Point(sumX / n - bmp.width / 2, sumY / n - bmp.height / 2 + 20)
    }
}
