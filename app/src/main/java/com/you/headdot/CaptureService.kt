package com.you.headdot

import android.app.*
import android.content.Intent
import android.graphics.*
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.view.WindowManager

class CaptureService : Service() {

    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var overlay: OverlayView? = null
    private lateinit var handler: Handler

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        handler = Handler(HandlerThread("cap").also { it.start() }.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") { stopSelf(); return START_NOT_STICKY }

        startForeground(1, notif())

        val code = intent?.getIntExtra("code", 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>("data") ?: return START_NOT_STICKY

        val m = resources.displayMetrics
        val w = m.widthPixels
        val h = m.heightPixels

        reader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)
        projection = (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
            .getMediaProjection(code, data)
        projection?.createVirtualDisplay(
            "cap", w, h, m.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader?.surface, null, handler
        )

        reader?.setOnImageAvailableListener({ r ->
            val img = r.acquireLatestImage() ?: return@setOnImageAvailableListener
            try {
                val bmp = cropCenter(img, w, h, 320)
                val head = Detector.findHead(bmp)
                val cx = w / 2f
                val cy = h / 2f
                if (head != null) {
                    overlay?.update(PointF(cx + head.x, cy + head.y))
                    AimService.instance?.steer(head.x.toFloat(), head.y.toFloat())
                } else {
                    overlay?.update(null)
                }
                bmp.recycle()
            } catch (e: Exception) {
            } finally {
                img.close()
            }
        }, handler)

        showOverlay()
        return START_STICKY
    }

    private fun cropCenter(img: Image, w: Int, h: Int, size: Int): Bitmap {
        val plane = img.planes[0]
        val buf = plane.buffer
        val rs = plane.rowStride
        val ps = plane.pixelStride
        val half = size / 2
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val row = ByteArray(ps * size)
        for (y in 0 until size) {
            val sy = (h / 2 - half + y).coerceIn(0, h - 1)
            buf.position(sy * rs + (w / 2 - half) * ps)
            buf.get(row, 0, minOf(row.size, buf.remaining()))
            for (x in 0 until size) {
                val p = x * ps
                if (p + 2 < row.size) {
                    bmp.setPixel(x, y, Color.rgb(
                        row[p].toInt() and 0xFF,
                        row[p + 1].toInt() and 0xFF,
                        row[p + 2].toInt() and 0xFF
                    ))
                }
            }
        }
        return bmp
    }

    private fun showOverlay() {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val v = OverlayView(this)
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        v.alpha = 0.75f
        wm.addView(v, lp)
        overlay = v
    }

    private fun notif(): Notification {
        val ch = NotificationChannel("cap", "Capture", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        return Notification.Builder(this, "cap")
            .setContentTitle("HeadDot")
            .setContentText("Screen කියවනවා...")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
    }

    override fun onDestroy() {
        overlay?.let { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) }
        reader?.close()
        projection?.stop()
        super.onDestroy()
    }
}
